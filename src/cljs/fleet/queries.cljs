(ns fleet.queries
  (:require [datascript.core :as d]
            [fleet.db :refer [conn contract-db]]))

;; Frontend

(defn add-party [party-name weight]
  (d/transact! conn [{:db/id -1}
                     {:contract/party  -1
                      :contract/name   party-name
                      :contract/weight weight}]))

(defn get-by-name [party-name]
  (d/q '[:find ?e .
         :in $ ?party-name
         :where [?e :contract/name ?party-name]]
       @contract-db party-name))

(defn remove-party [party-name]
  (d/transact! conn [[:db.fn/retractEntity (get-by-name party-name)]]))

(defn get-parties []
  (-> (d/q '[:find [(pull ?e [*]) ...]
             :where [?e :contract/party _]]
           @contract-db)
      seq))

;; Contract code

(defn fetch-contract [contract-key]
  (let [query-result (d/q '[:find (pull ?e [*]) .
                            :in $ ?key
                            :where [?e :blockchain/key ?key]]
                          @contract-db contract-key)
        {:keys [blockchain/abi blockchain/bin]} query-result]
    {:abi abi :bin bin}))

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
  (d/transact conn [{:blockchain/active-account address}]))

(defn fetch-active-account []
  (d/q '[:find ?name .
         :in $
         :where [_ :blockchain/active-account ?name]]
       @contract-db))

;; Blockchained contracts

(defn add-instance
  "Adds contract instance"
  [contract-key contract-instance]
  (log/error "ADD INSTANCE" contract-key contract-instance)
  (let [{:keys [db/id]} (fetch-contract contract-key)]
    (log/error "ID" id)
    (d/transact conn [[:db/add id :blockchain/instance contract-instance]])))

(defn add-address
  "Adds contract address"
  [contract-key address]
  (let [{:keys [db/id]} (fetch-contract contract-key)]
    (d/transact conn [[:db/add id :blockchain/address address]])))

(defn get-instance [contract-key]
  (d/q '[:find ?instance .
         :in $ ?key
         :where [?e :blockchain/key ?key]
                [?e :blockchain/instance ?instance]]
       @contract-db contract-key))
