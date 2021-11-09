(ns functional-programming-itmo-2021.lab-3.io-streaming
  (:require [clojure.core.async :as a]
            [clojure.string :as str]))

(defn input-producer [inp-c]
    (a/go-loop [counter 0]
      (if-let [_ (a/>! inp-c (read-line))]
        (recur (inc counter))
        (println "Input channel was closed"))
      )
    inp-c)

(defn routing-channels [inp-c routing]
    (let [cs (into (hash-map) (mapv (fn [[key _]] [key (a/chan)]) routing))
          vec_rt (vec routing)]
      (a/go-loop []
        (if-let [next-val (a/<! inp-c)]
          (do
            (println "Next val:" next-val)
            (let [channels (->> vec_rt
                            (filter #(apply (last %) [next-val]))
                            (mapv first)
                            (mapv #(get cs %))
                                )]
              (if channels
                (do
                  (println "Adding" next-val "to" channels)
                  (doseq [c channels]
                    (a/>! c next-val)))))
            (recur)
            )
          (doseq [[_ channel] cs]
            (println "Closing channels")
            (a/close! channel)))
        )
      cs
      )
  )


(def example-rules {:train #(str/starts-with? % "T")
                    :predict #(str/starts-with? % "P")})

(defn output-consumer [inp-cs]
  (a/go-loop [counter 0]
    (if-let [result (a/alts! inp-cs)]
      (do
        (println counter "Resulted in" result)
        (recur (inc counter)))
      (println "Output channel was closed"))
    )
  )

(defn middleware [inp-c func]
  (let [opt-c (a/chan)]
    (a/go-loop []
      (if-let [next-val (a/<! inp-c)]
        (do
          (func next-val)
          (a/>! opt-c next-val)
          (recur))
        (a/close! opt-c)
        ))
    ))


; Example
(defn outp [c]
  ;(output-consumer (vals (routing-channels c example-rules)))
  (-> c
      ;(input-producer)
      (routing-channels example-rules)
      (update :train #(middleware % (fn [_] (println "TRAIN"))))
      (update :predict #(middleware % (fn [_] (println "PREDICT"))))
      (vals)
      (output-consumer)
      )
  )

(def foo (a/chan))
(def boo (outp foo))