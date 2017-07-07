(ns fleet.queries
  (:require [datascript.core :as d]
            [fleet.db :refer [conn contract-db]]))

;; Frontend

(defn add-beneficiary [address weight]
  (d/transact! conn [{:db/id -1}
                     {:beneficiary/beneficiary -1
                      :beneficiary/address     address
                      :beneficiary/weight      weight}]))

(defn get-by-address [address]
  (d/q '[:find ?e .
         :in $ ?address
         :where [?e :beneficiary/address ?address]]
       @contract-db address))

(defn remove-beneficiary [address]
  (d/transact! conn [[:db.fn/retractEntity (get-by-address address)]]))

(defn get-beneficiaries []
  (-> (d/q '[:find [(pull ?e [*]) ...]
             :where [?e :beneficiary/beneficiary _]]
           @contract-db)
      seq))

;; Contract code

(defn fetch-contract [contract-key]
  (d/q '[:find (pull ?e [*]) .
         :in $ ?key
         :where [?e :contract/key ?key]]
       @contract-db contract-key))

(defn upsert-contract
  "Either updates existing contract with abi or bin, or adds new entry."
  [contract-key abi bin]
  (let [{:keys [db/id]} (fetch-contract :greeter)]
    (if (some? id)
      (d/transact! conn [[:db/add id :contract/abi abi]
                         [:db/add id :contract/bin bin]])
      (d/transact! conn [{:db/id -1}
                         {:contract/contract -1
                          :contract/key      contract-key
                          :contract/abi      abi
                          :contract/bin      bin}]))))

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
    (d/transact! conn [[:db/add id :contract/instance contract-instance]])))

(defn add-address
  "Adds contract address"
  [contract-key address]
  (println "Adding contract address for" contract-key)
  (let [{:keys [db/id]} (fetch-contract contract-key)]
    (d/transact! conn [[:db/add id :contract/address address]])))

(defn fetch-instance [contract-key]
  (println "getting instance for" contract-key)
  (let [instance (or (d/q '[:find ?instance .
                            :in $ ?key
                            :where [?e :contract/key ?key]
                                   [?e :contract/instance ?instance]]
                          @contract-db contract-key))]
    (or instance
        (throw (ex-info "No instance of smart contract in database"
                        {:contract-key contract-key})))))
