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
