(ns functional-programming-itmo-2021.lab-3.main
  (:require [functional-programming-itmo-2021.lab-3.io-streaming :as s]
            [functional-programming-itmo-2021.lab-3.interpolation :as i]
            [clojure.core.async :as a]))

(defn router [x] (if (= x "a") :a :not-a))

;(defn -main []
;  (let [inp (s/input-channel)
;        out (s/output-channel inp)])
;  (Thread/sleep 1E+6)
;  )