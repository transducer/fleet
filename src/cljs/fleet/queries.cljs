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

(defn add-contract
  "Either updates existing contract with abi or bin, or add new entry..."

  ;; TODO: cleaner to retrieve bin and abi in one go and have this function take
  ;; bin and abi as params

  [code-type contract-key data]
  (let [{:keys [db/id]} (fetch-contract :greeter)
        exists?         (boolean id)]
    (if exists?
      ;; FIXME: updating doe not work
      (d/transact! conn [(if (= code-type :abi)
                           {:db/id id :blockchain/abi data}
                           {:db/id id :blockchain/bin data})])
      (d/transact! conn [{:db/id -1}
                         (if (= code-type :abi)
                           {:blockchain/contract -1
                            :blockchain/key      contract-key
                            :blockchain/abi      data}
                           {:blockchain/contract -1
                            :blockchain/key      contract-key
                            :blockchain/bin      data})]))))

(defn set-active-account [address]
  ;; TODO: update previous
  (d/transact conn [{:blockchain/active-account address}]))

(defn active-account []
  (d/q '[:find ?name .
         :in $
         :where [_ :blockchain/active-account ?name]]
       @contract-db))
