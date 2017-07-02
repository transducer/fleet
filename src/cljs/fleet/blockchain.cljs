(ns fleet.blockchain
  (:require [ajax.core :as ajax]
            [cljs-web3.core :as web3]
            [cljs-web3.eth :as web3-eth]
            [cljs-web3.personal :as web3-personal]
            [cljsjs.web3]
            [fleet.blockchain :as blockchain]
            [fleet.blockchain.constants :as constants]
            [fleet.queries :as q]
            [goog.string :as string]
            [goog.string.format]))

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

(defn add-compiled-contract
  "Retrieve :abi or :bin of smart contract with contract-key and store in db"
  [contract-key code-type]
  (let [handler (fn [[ok data]]
                  (println 'fetch ok data)
                  (if ok
                    (q/add-contract code-type contract-key data)
                    (println "error fetching" contract-key)))
        request {:method          :get
                 :uri             (string/format "./contracts/build/%s.%s"
                                                 (name contract-key)
                                                 (name code-type))
                 :timeout         6000
                 :response-format (if (= code-type :abi)
                                    (ajax/json-response-format)
                                    (ajax/text-response-format))
                 :handler         handler}]
    (ajax/ajax-request request)))

(defn deploy-compiled-code [abi bin]
  (web3-eth/contract-new
   (clj->js abi)
   {:gas  constants/max-gas-limit
    :data bin
    :from (q/active-account)}))

(defn deploy-contract [key]
  (let [{:keys [abi bin]} (fetch-contract key)]
    (println abi bin)
    #_(deploy-compiled-code abi bin)))

(deploy-contract :greeter)

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
