(ns fleet.views
  (:require [fleet.blockchain :as blockchain]
            [fleet.blockchain.smart-asset-manager :as asset-manager]
            [fleet.queries :as queries]
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
    [:div "Add the addresses of the beneficiaries involved in the smart asset
 below. Use the slider to set the relative weight of the usage price the
 beneficiary receives on usage of the asset. Then deploy the smart asset
 contract to the Ethereum blockchain. When the asset is used, the price for
 usage will be deducted from the consumer, and the added beneficiaries involved
 will be paid the relative weight (their weight as percentage of total
 weight)."]))

(defn slider [value min max]
  (fn []
    [:input {:type "range" :value @value :min min :max max
             :on-change (fn [e]
                          (reset! value (.-target.value e)))}]))

(defn add-beneficiary []
  (let [address (r/atom "")
        weight  (r/atom 0)]
    (fn []
      [:div
       [:h2 "Create new contract"]
       "address: "
       [:span
        [:input {:type      "text"
                 :value     @address
                 :on-change #(reset! address
                                     (-> % .-target .-value))}]
        "weight: "
        [slider weight 0 100]]
       [:br]
       [:button.button {:on-click
                        #(do (queries/add-beneficiary @address
                                                      @weight)
                             (reset! address "")
                             (reset! weight 0))}
        "Add beneficiary"]])))

(defn beneficiary-table
  [beneficiaries]
  [:table
   [:thead
    [:tr
     [:th "Address"]
     [:th "Weight"]
     [:th ""]]]
   [:tbody
    (for [{address :beneficiary/address
           weight :beneficiary/weight
           :as beneficiary} beneficiaries]
      ^{:key beneficiary}
      [:tr
       [:td address]
       [:td weight]
       [:td [:button.button
             {:on-click
              #(queries/remove-beneficiary address)}
             "Remove"]]])]])

(defn beneficiaries []
  (let [beneficiaries (reaction (queries/get-beneficiaries))]
    (fn []
      [:div
       [:strong "Beneficiaries involved in the asset with their
                 weight"]
       (if-some [beneficiaries @beneficiaries]
         [beneficiary-table beneficiaries]
         [:p "Add addresses of parties involved in the asset
              above"])])))

(defn add-smart-asset []
  (let [asset-name    (r/atom "")
        usage-price   (r/atom 100)
        beneficiaries (reaction (queries/get-beneficiaries))]
    (fn []
      [:div
       [:span "Name of asset: "
        [:input {:type      "text"
                 :value     @asset-name
                 :on-change #(reset! asset-name
                                     (-> % .-target .-value))}]]
       [:br]
       [:span "Usage price of asset (ETH): "
        [:input {:type      "number"
                 :value     @usage-price
                 :on-change #(reset! usage-price
                                     (js/parseInt
                                      (-> % .-target .-value)))}]]
       [:button.button {:on-click
                        #(println (asset-manager/create-smart-asset
                                   @asset-name
                                   @usage-price
                                   @beneficiaries))}
        "Publish on the blockchain"]])))

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
       [add-beneficiary]
       [:br]
       [beneficiaries]
       [:br]
       [add-smart-asset]
       [footer]]]]))
