(ns fleet.db
  (:require [datascript.core :as d]
            [reagent.core :as r]))

(defonce schema
  {;; contracts made on the front-end
   :contract/party  {:db/type :db.type/ref}
   :contract/weight {:db/cardinality :db.cardinality/one}
   :contract/name   {:db/cardinality :db.cardinality/one}

   ;; refers to smart contracts
   :blockchain/contract {:db/type :db.type/ref}
   :blockchain/key      {:db/cardinality :db.cardinality/one}
   :blockchain/abi      {:db/cardinality :db.cardinality/one}})

(defonce conn
  (d/create-conn schema))

(defonce contract-db
  (let [ratom (r/atom (d/db conn))]
    (add-watch conn :watch-conn (fn [_ _ _ new-db] (reset! ratom new-db)))
    ratom))
