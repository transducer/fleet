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

(defn addresses-and-weights [parties]
  (let [transpose (partial apply mapv vector)]
    (->> parties
         (map (juxt :contract/address
                    :contract/weight))
         transpose)))

(defn create-smart-asset [name usage-price parties]
  (let [account             (queries/fetch-active-account)
        instance            (queries/fetch-instance contract-key)
        data                {:from  account
                             :gas   constants/max-gas-limit}
        [addresses weights] (addresses-and-weights parties)]
    (println name usage-price addresses weights data)
    (web3-eth/contract-call instance
                            :create-smart-asset
                            name
                            usage-price
                            addresses
                            weights
                            data)))

(defn get-usage-price [asset-name]
  (let [account  (queries/fetch-active-account)
        instance (queries/fetch-instance contract-key)
        data     {:from account
                  :gas  constants/max-gas-limit}]
    (web3-eth/contract-call instance
                            :get-usage-price
                            asset-name
                            data)))

(get-usage-price "foo")

(defn asset-used [name]
  #_(let [account  (queries/fetch-active-account)
        instance (queries/fetch-instance contract-key)]
    (web3-eth/contract-call instance )))

;; :value (web3/to-wei usage-price
;;                     :ether)
