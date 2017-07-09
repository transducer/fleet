(ns fleet.views
  (:require [fleet.blockchain :as blockchain]
            [fleet.blockchain.smart-asset-manager :as asset-manager]
            [fleet.queries :as queries]
            [fleet.util :refer [debug-panel]]
            [reagent.core :as r]
            [reagent.ratom :refer-macros [reaction]]))

(defn logo []
  [:p#logo
   [:a
    {:href "/" :title "FlΞΞt"}
    [:span.fa.fa-handshake-o]
    [:span.text "Fleet"]]])

(def use-asset-handler
  (fn [e]
    (.preventDefault e)
    (let [asset-name (js/prompt "Please enter asset name" "dryer")]
      (when (and asset-name (not= asset-name ""))
        (asset-manager/use-asset asset-name)))))

(defn menu-panel []
  [:div#menucont.bodycontainer.clearfix
   [:div.menutitle [:p
                    [:span.fa.fa-reorder]
                    [:strong "Menu"]]]
   [:ul.menu
    [:li.active [:a.active {:href "/" :title "Smart assets"}
                 "Smart assets"]]
    [:li [:a {:href "/"
              :title "Use asset"
              :onClick use-asset-handler}
          "Use asset"]]]])

(defn social-media []
  [:div#socialmedia.clearfix
   [:ul
    [:li
     [:a
      {:rel   "external"
       :href  "https://github.com/erooijak/fleet"
       :title "Github"}
      [:span.fa.fa-github]]]]])

(defn footer []
  [:div#footercont.clearfix
   [:hr]
   [:p
    "Copyright © Fleet | Powered by "
    [:a {:rel "external" :href "https://ethereum.org"} "Ethereum"]]])

(defn page-header []
  [:div#page-header
   [:h1 "Smart Asset Management"]])

(defn explanation []
  [:div "Add the addresses of the beneficiaries involved in the smart asset
 below. Use the slider to set the relative weight of the usage price the
 beneficiary receives on usage of the asset. Then deploy the smart asset
 contract to the Ethereum blockchain. When the asset is used, the price for
 usage will be deducted from the consumer, and the added beneficiaries involved
 will be paid the relative weight (their weight as percentage of total
 weight)."])

(defn slider [value min max]
  [:input {:type "range" :value @value :min min :max max
           :on-change (fn [e]
                        (reset! value (.-target.value e)))}])

(defn add-beneficiary []
  (let [address (r/atom "")
        weight  (r/atom 0)]
    (fn []
      [:div
       [:h2 "Create new smart asset"]
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
           weight  :beneficiary/weight
           :as     beneficiary} beneficiaries]
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
         [:p "First add addresses of beneficiaries involved"])])))

(defn add-smart-asset []
  (let [asset-name    (r/atom "dryer")
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
       [:span "Usage price of asset (in ETH): "
        [:input {:type      "number"
                 :value     @usage-price
                 :on-change #(reset! usage-price
                                     (js/parseInt
                                      (-> % .-target .-value)))}]]
       [:button.button {:on-click
                        #(asset-manager/create
                          @asset-name
                          @usage-price
                          @beneficiaries)}
        "Publish on the blockchain"]])))

(defn main-panel []
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
     [footer]]]])
