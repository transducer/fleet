(ns fleet.blockchain.files
  "Interaction with local smart contract files."
  (:require-macros [cljs.core.async.macros :refer [go go-loop]])
  (:require [ajax.core :as ajax]
            [cljs.core.async :as async]
            [fleet.blockchain.utils :as utils]
            [fleet.queries :as queries]
            [goog.string :as string]
            [goog.string.format]))

;; Review:
;; "I wondered why you were using diminutive suffixes until I realized you were
;; not using Keigo"
(defn- fetch-contract-code
  "Retrieves code of :bin or :abi."
  [contract-key code-type]
  (let [result-chan (async/chan)
        handler     (fn [[ok data]]
                      (if ok
                        (go (async/>! result-chan {code-type data})
                            (async/close! result-chan))
                        (println "error fetching" contract-key)))
        request     {:method          :get
                     :uri             (string/format "./contracts/build/%s.%s"
                                                     (name contract-key)
                                                     (name code-type))
                     :timeout         6000
                     :response-format (if (= code-type :abi)
                                        (ajax/json-response-format)
                                        (ajax/text-response-format))
                     :handler         handler}]
    (ajax/ajax-request request)
    result-chan))

(defn add-compiled-contract
  "Retrieve :abi and :bin of smart contract with contract-key and store in db"
  [contract-key]
  (go (let [result-chans      (map (partial fetch-contract-code contract-key) [:abi :bin])
            {:keys [abi bin]} (async/<!
                               (go-loop [acc {}, chans result-chans]
                                 (let [c (first chans)]
                                   (if c
                                     (recur (merge acc (async/<! c))
                                            (next chans))
                                     acc))))]
        (queries/upsert-contract contract-key
                                 (utils/format-abi abi)
                                 (utils/format-bin bin)))))
