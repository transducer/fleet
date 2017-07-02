(defproject fleet "0.1.0-SNAPSHOT"
  :dependencies [

                 ;; Our beloved Clojure
                 [org.clojure/clojure "1.9.0-alpha10"]
                 [org.clojure/clojurescript "1.9.473"]

                 ;; Blockchain
                 [cljs-web3 "0.19.0-0-2"]

                 ;; HTTP
                 [cljs-ajax "0.5.8"]

                 ;; Frontend
                 [datascript "0.16.1"]
                 [reagent "0.6.0"]]

  :plugins [[lein-auto "0.1.2"]
            [lein-cljsbuild "1.1.4"]
            [lein-shell "0.5.0"]]

  :min-lein-version "2.5.3"

  :source-paths ["src/cljs"]

  :clean-targets ^{:protect false} ["resources/public/js/compiled" "target"]

  :figwheel {:css-dirs ["resources/public/css"]}

  :auto {"compile-solidity" {:file-pattern #"\.(sol)$"
                             :paths        ["resources/public/contracts/src"]}}

  :aliases {"compile-solidity" ["shell" "./compile-solidity.sh"]
            "start-devnet"     ["shell" "./start-devnet.sh"]}

  :profiles
  {:dev
   {:dependencies [[binaryage/devtools "0.8.3"]
                   [com.cemerick/piggieback "0.2.1"]
                   [figwheel-sidecar "0.5.8"]
                   [org.clojure/tools.nrepl "0.2.11"]]

    :plugins [[lein-figwheel "0.5.9"]]}}

  :cljsbuild
  {:builds
   [{:id           "dev"
     :source-paths ["src/cljs"]
     :figwheel     {:on-jsload "fleet.core/reload"}
     :compiler     {:main                 fleet.core
                    :output-to            "resources/public/js/compiled/app.js"
                    :output-dir           "resources/public/js/compiled/out"
                    :asset-path           "js/compiled/out"
                    :source-map-timestamp true
                    :preloads             [devtools.preload]
                    :external-config      {:devtools/config {:features-to-install :all}}}}

    {:id           "min"
     :source-paths ["src/cljs"]
     :compiler     {:main            fleet.core
                    :output-to       "resources/public/js/compiled/app.js"
                    :optimizations   :advanced
                    :closure-defines {goog.DEBUG false}
                    :pretty-print    false}}]})
