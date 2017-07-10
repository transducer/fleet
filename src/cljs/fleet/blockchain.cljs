(ns fleet.blockchain
  (:require-macros [cljs.core.async.macros :refer [go go-loop]])
  (:require [ajax.core :as ajax]
            [cljs-web3.core :as web3]
            [cljs-web3.eth :as web3-eth]
            [cljs-web3.personal :as web3-personal]
            [cljs-web3.utils :as web3-utils]
            [cljs.core.async :as async]
            [cljsjs.web3]
            [fleet.blockchain.constants :as constants]
            [fleet.blockchain.files :as files]
            [fleet.blockchain.utils :as utils]
            [fleet.queries :as queries]))

(enable-console-print!)

(def web3-instance js/web3
  #_(web3/create-web3 "http://localhost:8545/"))

(def network-type
  (case (web3/version-network web3-instance)
    "1" :local-development ; FIXME: same as main-net
    "2" :morden-network    ; deprecated
    "3" :ropsten-network
    :unknown-network))

(defn unlock-own-account []
  (let [account      (first (web3-eth/accounts web3-instance))
        indefinitely 0]
    (web3-personal/unlock-account web3-instance
                                  account
                                  "password"
                                  indefinitely
                                  identity)))

(defn set-active-address []
  (let [account (first (web3-eth/accounts web3-instance))]
    (queries/set-active-account account)))

(defn add-compiled-contract
  "Retrieve :abi and :bin of smart contract with contract-key and store in db"
  [contract-key]
  (go (let [result-chans      (map
                               (partial files/fetch-contract-code
                                        contract-key)
                               [:abi :bin])
            {:keys [abi bin]} (async/<!
                               (go-loop [acc {}
                                         chans result-chans]
                                 (let [c (first chans)]
                                   (if c
                                     (recur (merge acc
                                                   (async/<! c))
                                            (next chans))
                                     acc))))]
        (queries/upsert-contract contract-key
                                 (utils/format-abi abi)
                                 (utils/format-bin bin)))))

(defn deploy-contract [key]
  (let [{:keys [:contract/abi :contract/bin]}
        (queries/fetch-contract key)

        data {:gas  constants/max-gas-limit
              :data bin
              :from (queries/fetch-active-account)}

        handler (fn [err contract]
                  (if-not err
                    (let [address (aget contract "address")]
                      ;; Two calls: transaction received, and
                      ;; contract deployed
                      ;; Check address on the second call
                      (when (web3/address? address)
                        (queries/add-instance key contract)
                        (queries/add-address key address)))
                    (println "error deploying contract" err)))]
    (web3-eth/contract-new web3-instance
                           abi
                           data
                           handler)))

(defn add-ropsten-contract [contract-key address]
  (go (let [result-chan   (files/fetch-contract-code contract-key
                                                     :abi)
            {:keys [abi]} (async/<! result-chan)
            contract      (web3-eth/contract-at web3-instance
                                                abi
                                                address)]
        (queries/add-instance contract-key contract)
        (queries/add-address contract-key address))))

(defn wait-till-contracts-ready [deploy-fn]
  ;; Wait till db ready for three seconds...
  (js/setTimeout deploy-fn 3000))

(defn init []

  ;; Setup db
  (add-compiled-contract :simplesmartassetmanager)
  (set-active-address)
  (unlock-own-account)

  (wait-till-contracts-ready
   (fn []
     (case network-type
       :local-development
       (deploy-contract :simplesmartassetmanager)

       :ropsten-network
       (let [address "0x882a20d4e89eb83202af8ee4ea98d2719bd5e774"]
         (add-ropsten-contract :simplesmartassetmanager address))

       :default
       (throw (ex-info "unknown network" {:type network-type}))))))
