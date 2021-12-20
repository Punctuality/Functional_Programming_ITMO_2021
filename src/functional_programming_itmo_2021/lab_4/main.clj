(ns functional-programming-itmo-2021.lab-4.main
  (:require [functional-programming-itmo-2021.lab-4.nats.operations :refer :all]
            [functional-programming-itmo-2021.lab-4.nats.connection :refer :all]
            [manifold.stream :as s])
  (:import (io.nats.client Message)))


(def topic "some-topic")
(def address "localhost:4222")

(def connection_pub
  (connection
    (with :verbose)
    (with :server address)
    (with :connection-name "clojure-test-publish")))

(def connection_sub
  (connection
    (with :verbose)
    (with :server address)
    (with :connection-name "clojure-test-subscribe")))

(def subscription
  (subscribe
    (to connection_sub)
    (subject topic)
    async-sub
    (stream-sub topic)
    ))

(defn do-pub []
  (repeatedly 10 #(publish
    (to connection_pub)
    (subject topic)
    (message "Hello world!")
    (headers {"authors" ["Sergey" "Eugene"]})
    sync-pub)))

(defn -main []
  (println "Started")
  (doall (do-pub))
  (println "Published 10 messages")
  @(s/consume (fn [^Message msg]
                (let [data (.getData msg)
                      text (String. data "UTF-8")]
                  (println "Received a message: " text)))
              subscription))
