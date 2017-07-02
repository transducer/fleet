(ns fleet.blockchain)

(def max-gas-limit 4000000)

;; (def web3-instance
;;   (web3/create-web3 "http://localhost:8545/"))

;; (web3/from-wei balance :ether)

;; [web3-eth/contract-new
;;  abi
;;  (when-not (= key :ethlance-sponsor-wallet)
;;    (:address (get-contract db :ethlance-db)))
;;  {:gas u/max-gas-limit
;;   :data bin
;;   :from (if address-index
;;           (nth (:my-addresses db) address-index)
;;           (:active-address db))}
;;  [:contract/deployed key contract-keys address-index]
;;  [:log-error :contracts/deploy key]]

;; (def abi nil)
;; (web3-eth/contract abi)

(defn web3 []
  (web3/sha3 "1"))
