(ns functional-programming-itmo-2021.lab-2.map-unit-testing
  (:require [clojure.test :refer :all]
            [functional-programming-itmo-2021.lab-2.hash-map :refer :all]))

(def full-map (open-address-map (reduce #(assoc %1 %2 %2) {} (range 9))))
(def mixed-map (open-address-map (reduce #(assoc %1 %2 %2) {} (range 5))))
(def empty-map (open-address-map {}))

(deftest full-get-test
    (is (= (range 9) (map #(get full-map %) (range 9)))))

(deftest mixed-get-test
  (is (= (concat (range 5) (repeat 4 nil)) (map #(get mixed-map %) (range 9)))))

(deftest empty-get-test
  (is (= (repeat 9 nil) (map #(get empty-map %) (range 9)))))

(deftest full-insert-test
  (is (= (concat (range 13) [nil]) (map #(get (merge full-map {9 9 10 10 11 11 12 12}) %) (range 14)))))

(deftest mixed-insert-test
  (is (= (concat (range 5) (repeat 4 nil) (range 9 13) [nil]) (map #(get (merge mixed-map {9 9 10 10 11 11 12 12}) %) (range 14)))))

(deftest empty-insert-test
  (is (= (concat (repeat 9 nil) (range 9 13) [nil]) (map #(get (merge empty-map {9 9 10 10 11 11 12 12}) %) (range 14)))))

(deftest full-delete-test
  (is (= (concat (repeat 4 nil) (range 4 9) (repeat 5 nil)) (map #(get (dissoc full-map 0 1 2 3) %) (range 14)))))

(deftest mixed-delete-test
  (is (= (concat (repeat 4 nil) [4] (repeat 9 nil)) (map #(get (dissoc mixed-map 0 1 2 3) %) (range 14)))))

(deftest full-delete-test
  (is (= (repeat 14 nil) (map #(get (dissoc empty-map 0 1 2 3) %) (range 14)))))

(deftest full-count-test
  (is (= 9 (count full-map))))

(deftest mixed-count-test
  (is (= 5 (count mixed-map))))

(deftest empty-count-test
  (is (= 0 (count empty-map))))

(deftest full-map-equiv-test
  (is (.equiv full-map (reduce #(assoc %1 %2 %2) {} (range 9)))))

(deftest mixed-map-equiv-test
  (is (.equiv mixed-map (reduce #(assoc %1 %2 %2) {} (range 5)))))

(deftest empty-map-equiv-test
  (is (.equiv empty-map {})))
