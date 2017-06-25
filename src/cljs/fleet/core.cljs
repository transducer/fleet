(ns fleet.core
  (:require [fleet.config :as config]
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
  (render [views/main-panel]))

(defn ^:export reload []
  (rerender))
