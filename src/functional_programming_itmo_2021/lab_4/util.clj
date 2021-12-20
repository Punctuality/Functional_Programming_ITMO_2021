(ns functional-programming-itmo-2021.lab-4.util
  (:import (java.time Duration)
           (clojure.lang ISeq)))

(defmacro flat-map [f & arguments]
  `(flatten (map ~f ~@arguments)))

(defmacro expand [method obj params]
  `(apply (fn [x#] (~method ~obj x#)) ~params))

(defprotocol DurationLike
  (duration ^Duration [this]))

(extend-protocol DurationLike
  Duration
  (duration [this] this)

  Long
  (duration [this] (Duration/ofSeconds this))
  )

; DSL helpers

(defn split-config-specs [separator ^ISeq args]
  (loop [acc [] specs args]
    (let [[found remaining] (split-with (partial not= separator) specs)]
      (if (empty? remaining)
        (conj acc (vec found))
        (if-not (empty? found)
          (recur (conj acc (vec found)) remaining)
          (recur acc (rest remaining))
          ))
      ))
  )

(defn find-param [keyword ^ISeq args]
  (if-not (empty? args)
    (let [[head & others] args]
      (if-not (= keyword head)
        (recur keyword (rest args))
        {keyword (first others)}
        ))
    {}
    )
  )