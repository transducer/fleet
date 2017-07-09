(ns fleet.blockchain.smart-asset-manager
  "Mapping to the Simple Smart Asset Manager contract"
  (:require [cljs-web3.core :as web3]
            [cljs-web3.eth :as web3-eth]
            [fleet.blockchain.constants :as constants]
            [fleet.queries :as queries]))

(def contract-key :simplesmartassetmanager)

(defn say-hello []
  (let [instance (queries/fetch-instance contract-key)]
    (web3-eth/contract-call instance :greet)))

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
    (println (web3-eth/contract-call instance
                                     :create-smart-asset
                                     asset-name
                                     usage-price
                                     (rest addresses)
                                     weights
                                     data))))

(defn get-usage-price [asset-name]
  (let [account  (queries/fetch-active-account)
        instance (queries/fetch-instance contract-key)
        data     {:from account
                  :gas  constants/max-gas-limit}]
    (web3-eth/contract-call instance
                            :get-usage-price
                            asset-name
                            data)))

(println (get-usage-price "foo"))

(defn use-asset [asset-name]
  (let [account  (queries/fetch-active-account)
        instance (queries/fetch-instance contract-key)
        data     {:from account
                  :gas  constants/max-gas-limit}]
    (println (web3-eth/contract-call instance
                                     :use-asset
                                     asset-name
                                     data))))

;; :value (web3/to-wei usage-price
;;                     :ether)
