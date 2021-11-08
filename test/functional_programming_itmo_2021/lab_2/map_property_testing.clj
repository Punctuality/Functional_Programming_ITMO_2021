(ns functional-programming-itmo-2021.lab-2.map-property-testing
  (:require [clojure.test :refer :all]
            [functional-programming-itmo-2021.lab-2.hash-map :refer :all]))

(defn run-test [test-fn times] (reduce #(and %1 %2) (repeatedly times test-fn)))

; Three properties
; 1. If Map was build using keyA, it contains keyA
; 2. If Map was disassociated by keyA, it no longer contains it
; 3. If Map was merged with another one, it contains all subset of keys

(defn generate-vec [size] (repeatedly size #(rand-int 10E+6)))

; First property (map <- ... keyA ...) contains keyA

(defn contains-key-prop []
  (let [limit 1000
        data (generate-vec limit)
        generated (open-address-map (reduce #(assoc %1 %2 %2) {} data))
        rnd-idx (rand-int limit)]
      (contains? generated (nth data rnd-idx))
    ))

(deftest first-property
  (is (run-test contains-key-prop 100)))

; Second property (dissoc map keyA) not contains keyA

(defn dissoc-key-prop []
  (let [limit 1000
        data (generate-vec limit)
        generated (open-address-map (reduce #(assoc %1 %2 %2) {} data))
        rnd-key (nth data (rand-int limit))
        stripped (dissoc generated rnd-key)]
    (not (contains? stripped rnd-key))))

(deftest second-property
  (is (run-test dissoc-key-prop 100)))

; Third property (merge map1 map2) contains all keys

(defn merge-key-prop []
  (let [limit 1000
        data-1 (generate-vec limit)
        data-2 (generate-vec limit)
        generated-1 (open-address-map (reduce #(assoc %1 %2 %2) {} data-1))
        generated-2 (open-address-map (reduce #(assoc %1 %2 %2) {} data-2))
        all-keys (reduce #(conj %1 %2) #{} (concat data-1 data-2))
        all-merged (merge generated-1 generated-2)]
    (reduce #(and %1 %2) (map #(contains? all-merged %) all-keys))
    ))

(deftest third-property
  (is (run-test merge-key-prop 100)))
