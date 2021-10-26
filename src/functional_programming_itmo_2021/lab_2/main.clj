(ns functional-programming-itmo-2021.lab-2.main
  (:require [functional-programming-itmo-2021.lab-2.hash-map :refer :all])
  )

(defn -main []
  (let [hashmap (open-address-map {9 9 10 10})
        merged-with-example (merge hashmap example)
        dissoced (dissoc merged-with-example 1 2 9)
        updated (assoc dissoced 10 "OTHER VALUE")
        retrieved-value (get updated 10)]
      (do
        (println "Example of working with Open-Addressing Hash Map")
        (println "Example hash map: " example)
        (println "Other hash map: " hashmap)
        (println "Merged hash map: " merged-with-example)
        (println "Dissoced hash map: " dissoced)
        (println "Updated hash map: " updated)
        (println "Retrieved value by key 10: " retrieved-value)
        )))