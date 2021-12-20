(ns functional-programming-itmo-2021.lab-3.main
  (:require [functional-programming-itmo-2021.lab-3.io-streaming :as s]
            [functional-programming-itmo-2021.lab-3.interpolation :as i]
            [clojure.core.async :as a]
            [clojure.string :as str]))

(def train-points (ref []))
(def data-points (ref []))

(def control-rules {:train   #(str/starts-with? % "t")
                    :data    #(str/starts-with? % "d")
                    :predict #(str/starts-with? % "predict")})

(defn add-train-point [line]
  (let [split (vec (.split line ","))
        [_ x_s y_s & _] split
        x (Double/parseDouble x_s)
        y (Double/parseDouble y_s)]
    (println "Adding new train point: " x "->" y)
    (-> train-points
        (alter conj [x y])
        (dosync)
        )))

(defn add-data-point [line]
  (let [split (vec (.split line ","))
        [_ x_s & _] split
        x (Double/parseDouble x_s)]
    (println "Adding new data point: " x)
    (-> data-points
        (alter conj x)
        (dosync)
        )))

(defn predict-from-data [_]
  (let [sorted-train (sort-by first @train-points)
        interpolator (i/spline-interpolator sorted-train)
        data @data-points
        predicted (mapv interpolator data)]
    (print "Predicted: ")
    (doseq [i (range (count data))]
      (print " " (get data i) "->" (get predicted i) "|"))
    (println)
    ))

(defn error-line-handler [err-line]
  (println "Received unsupported line:" err-line))

(def stream (a/chan))

(defn shutdown-guard [effect]
  (let [shutdown-p (promise)
        hook-f (fn [] (do
                        (effect)
                        (println "Shutting down!!!")
                        (deliver shutdown-p 0)))]
    (.addShutdownHook (Runtime/getRuntime)
                      (Thread. ^Runnable hook-f))
    (System/exit @shutdown-p)))

(defn -main []
  (-> stream
      (s/input-producer)
      (s/routing-channels control-rules)
      (update :train #(s/middleware % add-train-point))
      (update :data #(s/middleware % add-data-point))
      (update :predict #(s/middleware % predict-from-data))
      (update :else #(s/middleware % error-line-handler))
      (vals)
      (s/output-consumer)
      )
  (shutdown-guard #(a/close! stream))
  )