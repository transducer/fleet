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
        handler             (fn [err result]
                              (if-not err
                                (println "smart asset created"
                                         result)
                                (println "something went wrong"
                                         err)))
        [addresses weights] (addresses-and-weights beneficiaries)]
    (web3-eth/contract-call instance
                            :create-smart-asset
                            asset-name
                            (web3/to-wei usage-price :ether)
                            addresses
                            weights
                            data
                            handler)))

(defn use-asset [asset-name]
  (let [account  (queries/fetch-active-account)
        instance (queries/fetch-instance contract-key)
        data     {:from account
                  :gas  constants/max-gas-limit}
        handler  (fn [err result]
                   (if-not err
                     (println "smart asset used" result)
                     (println "something went wrong" err)))]
    (web3-eth/contract-call instance
                            :use-asset
                            asset-name
                            data
                            handler)))
