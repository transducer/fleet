(ns fleet.blockchain.utils)

(defn- ascii-char? [c]
  (< (.charCodeAt c) 128))

(defn gas-estimate [string]
  (if string
    (let [ascii-freqs (frequencies (map ascii-char? string))]
      (+ (* (get ascii-freqs true 0) 800)
         (* (get ascii-freqs false 0) 1550)))
    0))

(defn format-bin [bin]
  (str "0x" bin))

(defn format-abi [abi]
  (clj->js abi))
