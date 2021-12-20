(ns functional-programming-itmo-2021.lab-3.io-streaming
  (:require [clojure.core.async :as a]))

(defn input-producer [inp-c]
  (a/go-loop [counter 0]
    (if-let [next-line (read-line)]
      (if-let [_ (a/>! inp-c next-line)]
        (recur (inc counter))
        (println "Input channel was closed")))
    )
  inp-c)

(defn routing-channels [inp-c routing]
  (let [cs (assoc
             (into (hash-map) (mapv (fn [[key _]] [key (a/chan)]) routing))
             :else (a/chan))
        vec_rt (vec routing)]
    (a/go-loop []
      (if-let [next-val (a/<! inp-c)]
        (do
          (let [channels (->> vec_rt
                              (filter #(apply (last %) [next-val]))
                              (mapv first)
                              (mapv #(get cs %)))]
            (if channels
              (if (not (empty? channels))
                (doseq [c channels]
                  (a/>! c next-val))
                (a/>! (cs :else) next-val)
                )))
          (recur)
          )
        (doseq [[_ channel] cs]
          (println "Closing channels")
          (a/close! channel)))
      )
    cs
    )
  )

(defn output-consumer [inp-cs]
  (a/go-loop []
    (if-let [_ (a/alts! inp-cs)]
      (recur)
      (println "Output channel was closed"))
    ))

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
    opt-c))