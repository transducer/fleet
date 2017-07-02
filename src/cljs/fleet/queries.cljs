(ns fleet.queries
  (:require [datascript.core :as d]
            [fleet.db :refer [conn contract-db]]))

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

(defn fetch-contract [contract-key]
  (d/q '[:find (pull ?e [*]) .
         :in $ ?key
         :where [?e :blockchain/key ?key]]
       @contract-db contract-key))

(defn add-contract [contract-key code-type data]
  (let [{:keys [db/id]} (fetch-contract :greeter)
        db-id           (or id -1)]
    (d/transact! conn [{:db/id db-id}
                       (if (= code-type :abi)
                         {:blockchain/contract -1
                          :blockchain/key      contract-key
                          :blockchain/abi      data}
                         {:blockchain/contract -1
                          :blockchain/key      contract-key
                          :blockchain/bin      data})])))

(defn set-active-account [address]
  ;; TODO: update previous
  (d/transact conn [{:blockchain/active-account address}]))

(defn active-account []
  (d/q '[:find ?name .
         :in $
         :where [_ :blockchain/active-account ?name]]
       @contract-db))
