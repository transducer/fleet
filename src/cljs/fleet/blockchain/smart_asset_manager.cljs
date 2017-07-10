(ns fleet.blockchain.smart-asset-manager
  "Mapping to the Simple Smart Asset Manager contract"
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [ajax.core :as ajax]
            [cljs-web3.core :as web3]
            [cljs-web3.eth :as web3-eth]
            [cljs.core.async :as async]
            [fleet.blockchain.constants :as constants]
            [fleet.queries :as queries]))

(def contract-key :simplesmartassetmanager)

(defn say-hello []
  (let [instance (queries/fetch-instance contract-key)]
    (web3-eth/contract-call instance
                            :greet
                            (fn [err res]
                              (if-not err
                                (println res)
                                (println "Error:" err))))))

(def default-handler
  (fn [err result]
    (if-not err
      (println "submitted to network awaiting inclusion in a block"
               result)
      (println "something went wrong"
               err))))

(defn addresses-and-weights [beneficiaries]
  (let [transpose (partial apply mapv vector)]
    (->> beneficiaries
         (map (juxt :beneficiary/address
                    :beneficiary/weight))
         transpose)))

(defn create [asset-name usage-price beneficiaries]
  (let [account             (queries/fetch-active-account)
        instance            (queries/fetch-instance contract-key)
        data                {:from  account
                             :gas   constants/max-gas-limit}
        [addresses weights] (addresses-and-weights beneficiaries)]
    (web3-eth/contract-call instance
                            :create-smart-asset
                            asset-name
                            (web3/to-wei usage-price :ether)
                            addresses
                            weights
                            data
                            default-handler)))

(defn get-usage-price
  "Returns ETH-value of usage price of asset-name on channel"
  [asset-name]
  (let [to-float-xf (map #(js/parseFloat %))
        result-chan (async/chan 1 to-float-xf)
        instance    (queries/fetch-instance contract-key)]
    (web3-eth/contract-call instance
                            :get-usage-price
                            asset-name
                            (fn [err result]
                              (if-not err
                                (go (async/>! result-chan result))
                                (println "Error:" err))))
    result-chan))

(defn use-asset [asset-name]
  (go (let [account     (queries/fetch-active-account)
            instance    (queries/fetch-instance contract-key)
            usage-price (async/<! (get-usage-price asset-name))
            price-eth   (web3/from-wei usage-price :ether)
            data        {:from  account
                         :gas   constants/max-gas-limit
                         :value usage-price}]
        (println "using" asset-name "with usage price" price-eth
                 "ETH")
        (web3-eth/contract-call instance
                                :use-asset
                                asset-name
                                data
                                default-handler))))

(defn remove-asset [asset-name]
  (let [account  (queries/fetch-active-account)
        instance (queries/fetch-instance contract-key)
        data     {:from account
                  :gas  constants/max-gas-limit}]
    (web3-eth/contract-call instance
                            :remove-asset
                            asset-name
                            data
                            default-handler)))

(defn remove-contract [address]
  (let [account  (queries/fetch-active-account)
        instance (queries/fetch-instance contract-key)
        data     {:from account
                  :gas  constants/max-gas-limit}]
    (web3-eth/contract-call instance
                            :remove
                            address
                            data
                            default-handler)))
