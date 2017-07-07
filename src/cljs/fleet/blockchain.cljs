(ns fleet.blockchain
  (:require [ajax.core :as ajax]
            [cljs-web3.core :as web3]
            [cljs-web3.eth :as web3-eth]
            [cljs-web3.personal :as web3-personal]
            [cljsjs.web3]
            [fleet.blockchain.constants :as constants]
            [fleet.blockchain.contracts :as contracts]
            [fleet.blockchain.utils :as utils]
            [fleet.queries :as queries]
            [goog.string :as string]
            [goog.string.format]))

(enable-console-print!)

(defn sha3 []
  (web3/sha3 "2"))

(def web3-instance
  #_(js/web3) ;; FIXME
  (web3/create-web3 "http://localhost:8545/"))

(defn unlock-own-account []
  (let [account (first (web3-eth/accounts web3-instance))]
    (web3-personal/unlock-account web3-instance account "password")))

(defn set-active-address []
  (let [account (first (web3-eth/accounts web3-instance))]
    (queries/set-active-account account)))

(defn deploy-contract [key]
  (let [{:keys [:blockchain/abi :blockchain/bin]} (queries/fetch-contract key)

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

(defn init []
  (contracts/add-compiled-contract :simplesmartassetmanager)
  (unlock-own-account)
  (set-active-address)
  (js/setTimeout (fn [] (deploy-contract :simplesmartassetmanager))
                 3000))

;; (init)
;; (deploy-contract :simplesmartassetmanager)
;; (web3-eth/contract-call (queries/fetch-instance :simplesmartassetmanager) :set-greeting "Hello from the Greeter smart contract" {:from (queries/fetch-active-account)})

#_(cljs-web3.eth/contract-call web3-instance (fleet.queries/get-contract :greeter) :greet)

;; TODO:
#_(add-compiled-contract :mortal)
#_(deploy-contract :mortal)

#_(queries/fetch-active-account)

#_(unlock-own-account)

#_(deploy-compiled-code (clj->js [{:constant false, :inputs [], :name "kill", :outputs [], :payable false, :type :function} {:inputs [], :payable false, :type :constructor}]) (str "0x" "6060604052341561000c57fe5b5b60008054600160a060020a03191633600160a060020a03161790555b5b609c806100386000396000f300606060405263ffffffff60e060020a60003504166341c0e1b581146020575bfe5b3415602757fe5b602d602f565b005b6000543373ffffffffffffffffffffffffffffffffffffffff90811691161415606d5760005473ffffffffffffffffffffffffffffffffffffffff16ff5b5b5600a165627a7a723058209fc9d0ffd32ede3c45bfe55e768fc83bebcd433328c2b4572ce06b53bd483cbb0029"))

;; When successful:
;; {:keys [db]} [contract-key code-type code]
#_(let [code (if (= code-type :abi) (clj->js code) (str "0x" code))
          contract (get-contract db contract-key)
          contract-address (:address contract)]
      (let [new-db (cond-> db
                     true
                     (assoc-in [:eth/contracts contract-key code-type] code)

                     (= code-type :abi)
                     (update-in [:eth/contracts contract-key] merge
                                (when contract-address
                                  {:instance (web3-eth/contract-at (:web3 db) code contract-address)})))]
        (merge
          {:db new-db
           :dispatch-n (remove nil?
                               [(when (all-contracts-loaded? new-db)
                                  [:eth-contracts-loaded])
                                (when (and (= code-type :abi)
                                           (= contract-key :ethlance-config))
                                  [:contract.config/setup-listeners])
                                (when (and (= code-type :abi) (:setter? contract) contract-address)
                                  [:contract/load-and-listen-setter-status contract-key])])})))
