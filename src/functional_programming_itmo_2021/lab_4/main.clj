(ns functional-programming-itmo-2021.lab-4.main
  (:require [functional-programming-itmo-2021.lab-4.util :as u]
            [clojure.string :as str]))

(def a
  (u/flat-map #(str/split % #"\,") ["a,d,s" "a,d,s" "a,d,s"] ))
