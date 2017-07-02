(ns fleet.views
  (:require [fleet.blockchain :as blockchain]
            [fleet.queries :as q]
            [fleet.util :refer [debug-panel]]
            [reagent.core :as r]
            [reagent.ratom :refer-macros [reaction]]))

(defn logo []
  (fn []
    [:p#logo
     [:a
      {:href "/" :title "FlΞΞt"}
      [:span.fa.fa-handshake-o]
      [:span.text "Fleet"]]]))

(defn menu-panel []
  (fn []
    [:div#menucont.bodycontainer.clearfix
     [:div.menutitle [:p [:span.fa.fa-reorder] [:strong "Menu"]]]
     [:ul.menu
      [:li.active [:a.active {:href "/" :title "Contracts"} "Contracts"]]]]))

(defn social-media []
  (fn []
    [:div#socialmedia.clearfix
     [:ul
      [:li
       [:a
        {:rel   "external"
         :href  "https://github.com/erooijak/fleet"
         :title "Github"}
        [:span.fa.fa-github]]]]]))

(defn footer []
  (fn []
    [:div#footercont.clearfix
     [:hr]
     [:p
      "Copyright © Fleet | Powered by "
      [:a {:rel "external" :href "https://ethereum.org"} "Ethereum"]]]))

(defn page-header []
  (fn []
    [:div#page-header
     [:h1 "Smart Asset Management"]]))

(defn explanation []
  (fn []
    [:div "Add the parties involved in the contract below. Use the slider to set
 the percentage of the usage price the party received on usage of the asset.
 Then deploy the contract to the Ethereum blockchain. When the asset is used,
 the price for usage will be deducted from the consumer, and the added parties
 involved will be paid the relative weight."]))

(defn slider [value min max]
  (fn []
    [:input {:type "range" :value @value :min min :max max
             :on-change (fn [e]
                          (reset! value (.-target.value e)))}]))

(defn add-address []
  (let [address (r/atom "")
        weight  (r/atom 0)]
    (fn []
      [:div
       [:h2 "Create new contract"]
       "address: "
       [:span
        [:input {:type      "text"
                 :value     @address
                 :on-change #(reset! address (-> % .-target .-value))}]
        "weight: "
        [slider weight 0 100]]
       [:br]
       [:button.button {:on-click
                        #(do (q/add-party @address @weight)
                             (reset! address "")
                             (reset! weight 0))}
        "Add address"]])))

(defn party-table
  [parties]
  [:table
   [:thead
    [:tr
     [:th "Name"]
     [:th "Weight"]
     [:th ""]]]
   [:tbody
    (for [{party-name :contract/name weight :contract/weight :as party} parties]
      ^{:key party}
      [:tr
       [:td party-name]
       [:td weight]
       [:td [:button.button {:on-click #(q/remove-party party-name)} "Remove"]]])]])

(defn addresses []
  (let [parties (reaction (q/get-parties))]
    (fn []
      [:div
       [:strong "Parties involved in the asset with their weight"]
       (if-some [parties @parties]
         [party-table parties]
         [:p "Add addresses of parties involved in the asset above"])])))

(defn add-contract []
  (let [parties (reaction (q/get-parties))]
    (fn []
      [:button.button {:on-click
                       ;; TODO, PUBLISH to BLOCKCHAIN
                       #(do (println @parties)
                            (println (blockchain/unlock-own-account)))}
       "Publish contract on the blockchain"])))

(defn main-panel []
  (fn []
    [:div
     [:div#left
      [logo]
      [menu-panel]
      [social-media]]
     [:div#right.clearfix
      [:div#custom-page
       [page-header]
       [explanation]
       [add-address]
       [:br]
       [addresses]
       [:br]
       [add-contract]
       [footer]]]]))
