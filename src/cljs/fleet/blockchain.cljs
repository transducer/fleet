(ns fleet.blockchain
  (:require-macros [cljs.core.async.macros :refer [go go-loop]])
  (:require [ajax.core :as ajax]
            [cljs-web3.core :as web3]
            [cljs-web3.eth :as web3-eth]
            [cljs-web3.personal :as web3-personal]
            [cljs.core.async :as async]
            [cljsjs.web3]
            [fleet.blockchain.constants :as constants]
            [fleet.queries :as q]
            [goog.string :as string]
            [goog.string.format]))

(enable-console-print!)

(def web3-instance
  (web3/create-web3 "http://localhost:8545/"))

(defn fetch-contract [key]
  (let [{:keys [blockchain/abi blockchain/bin]} (q/fetch-contract key)]
    {:abi abi :bin bin}))

(defn set-active-address []
  (let [account (first (web3-eth/accounts web3-instance))]
    (q/set-active-account account)))

(defn unlock-own-account []
  (let [account (first (web3-eth/accounts web3-instance))]
    (web3-personal/unlock-account web3-instance account "password")))

(defn sha3 []
  (web3/sha3 "1"))

(defn fetch-contract-code [contract-key code-type]
  (let [result-chan (async/chan)
        handler     (fn [[ok data]]
                      (if ok
                        (go (async/>! result-chan {code-type data})
                            (async/close! result-chan))
                        (println "error fetching" contract-key)))
        request     {:method          :get
                     :uri             (string/format "./contracts/build/%s.%s"
                                                     (name contract-key)
                                                     (name code-type))
                     :timeout         6000
                     :response-format (if (= code-type :abi)
                                        (ajax/json-response-format)
                                        (ajax/text-response-format))
                     :handler         handler}]
    (ajax/ajax-request request)
    result-chan))

(defn format-bin [bin]
  (str "0x" bin))

(defn add-compiled-contract
  "Retrieve :abi or :bin of smart contract with contract-key and store in db"
  [contract-key]
  (go (let [result-chans      (map (partial fetch-contract-code contract-key) [:abi :bin])
            {:keys [abi bin]} (async/<!
                               (go-loop [acc {} chans result-chans]
                                 (let [c (first chans)]
                                   (if c
                                     (recur (merge acc (async/<! c))
                                            (next chans))
                                     acc))))]
        (q/upsert-contract contract-key abi (format-bin bin)))))

(add-compiled-contract :greeter)

(defn deploy-compiled-code [abi bin]
  (web3-eth/contract-new
   web3-instance
   (clj->js abi)
   {:gas  constants/max-gas-limit
    :data bin
    :from (q/fetch-active-account)}))

(defn deploy-contract [key]
  (let [{:keys [abi bin]} (fetch-contract key)]
    (println abi bin)
    ))

;; TODO:
#_(add-compiled-contract :mortal)
#_(deploy-contract :mortal)

#_(q/fetch-active-account)

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
