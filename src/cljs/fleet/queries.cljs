(ns fleet.queries
  (:require [datascript.core :as d]
            [fleet.db :refer [conn contract-db]]))

;; Frontend

(defn add-party [address weight]
  (d/transact! conn [{:db/id -1}
                     {:contract/party   -1
                      :contract/address address
                      :contract/weight  weight}]))

(defn get-by-address [address]
  (d/q '[:find ?e .
         :in $ ?address
         :where [?e :contract/address ?address]]
       @contract-db address))

(defn remove-party [address]
  (d/transact! conn [[:db.fn/retractEntity (get-by-address address)]]))

(defn get-parties []
  (-> (d/q '[:find [(pull ?e [*]) ...]
             :where [?e :contract/party _]]
           @contract-db)
      seq))

;; Contract code

(defn fetch-contract [contract-key]
  (d/q '[:find (pull ?e [*]) .
         :in $ ?key
         :where [?e :blockchain/key ?key]]
       @contract-db contract-key))

(defn upsert-contract
  "Either updates existing contract with abi or bin, or adds new entry."
  [contract-key abi bin]
  (let [{:keys [db/id]} (fetch-contract :greeter)]
    (if (some? id)
      (d/transact! conn [[:db/add id :blockchain/abi abi]
                         [:db/add id :blockchain/bin bin]])
      (d/transact! conn [{:db/id -1}
                         {:blockchain/contract -1
                          :blockchain/key      contract-key
                          :blockchain/abi      abi
                          :blockchain/bin      bin}]))))

(defn set-active-account [address]
  ;; TODO: update previous
  (d/transact! conn [{:blockchain/active-account address}]))

(defn fetch-active-account []
  (d/q '[:find ?name .
         :in $
         :where [_ :blockchain/active-account ?name]]
       @contract-db))

;; Blockchained contracts

(defn add-instance
  "Adds contract instance"
  [contract-key contract-instance]
  (println "Adding contract instance for" contract-key)
  (let [{:keys [db/id]} (fetch-contract contract-key)]
    (d/transact! conn [[:db/add id :blockchain/instance contract-instance]])))

(defn add-address
  "Adds contract address"
  [contract-key address]
  (println "Adding contract address for" contract-key)
  (let [{:keys [db/id]} (fetch-contract contract-key)]
    (d/transact! conn [[:db/add id :blockchain/address address]])))

(defn fetch-instance [contract-key]
  (println "getting instance for" contract-key)
  (let [instance (or (d/q '[:find ?instance .
                            :in $ ?key
                            :where [?e :blockchain/key ?key]
                            [?e :blockchain/instance ?instance]]
                          @contract-db contract-key))]
    (or instance
        (throw (ex-info "No instance of smart contract in database"
                        {:contract-key contract-key})))))
