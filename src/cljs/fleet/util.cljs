(ns fleet.util
  (:require [cljs.pprint :refer [pprint]]))

(defn debug-panel
  "Debug panel, pretty prints any data you pass in on the screen.
  Formats datascript DB."
  [s]
  [:pre
   (if (= (type s) datascript.db/DB)
     (with-out-str (doseq [d s] (pprint (vec d))))
     (with-out-str (pprint s)))])
