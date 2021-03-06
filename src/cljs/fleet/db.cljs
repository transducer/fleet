(ns fleet.db
  (:require [datascript.core :as d]
            [reagent.core :as r]))

(defonce schema
  {;; info needed for smart asset created on the front-end
   :beneficiary/beneficiary {:db/type :db.type/ref}
   :beneficiary/weight      {:db/cardinality :db.cardinality/one}
   :beneficiary/address     {:db/cardinality :db.cardinality/one}

   ;; smart contract related
   :contract/contract {:db/type :db.type/ref}
   :contract/key      {:db/cardinality :db.cardinality/one}
   :contract/abi      {:db/cardinality :db.cardinality/one}
   :contract/bin      {:db/cardinality :db.cardinality/one}
   :contract/instance {:db/cardinality :db.cardinality/one}

   ;; the from address for transactions
   :blockchain/active-account {:db/cardinality :db.cardinality/one}})

(defonce conn
  (d/create-conn schema))

(defonce contract-db
  (let [ratom (r/atom (d/db conn))]
    (add-watch conn :watch-conn (fn [_ _ _ new-db] (reset! ratom new-db)))
    ratom))
