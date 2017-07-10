(ns fleet.core
  (:require [fleet.blockchain :as blockchain]
            [fleet.config :as config]
            [fleet.views :as views]
            [reagent.core :as r]))

(defn render [view]
  (let [node (.getElementById js/document "app")]
    (r/render-component view node)))

(defn rerender []
  (let [node (.getElementById js/document "app")]
    (r/unmount-component-at-node node)
    (render [views/main-panel])))

(defn ^:export mount []
  (if blockchain/provides-web3?
    (do
      (blockchain/init)
      (render [views/main-panel]))
    (render [views/no-web3-panel])))

(defn ^:export reload []
  (when blockchain/provides-web3?
    (rerender)))
