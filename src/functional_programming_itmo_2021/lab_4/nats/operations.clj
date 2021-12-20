(ns functional-programming-itmo-2021.lab-4.nats.operations
  (:require [functional-programming-itmo-2021.lab-4.nats.message :as m]
            [clojure.core.async :as a]
            [manifold.stream :as s]
            [functional-programming-itmo-2021.lab-4.util :as u])
  (:import (io.nats.client Connection Subscription Dispatcher MessageHandler Message)
           (io.nats.client.impl NatsMessage Headers)
           (clojure.lang Fn IPersistentMap)
           (java.time Duration)))

(defmacro ^:private edit-aggregate
  ([keyword key-name] `(edit-aggregate ~keyword ~key-name identity))
  ([keyword key-name opt-f]
   `(fn [aggregate# elem#]
     (if-not (nil? (~keyword aggregate#))
       (-> (str "Already defined " ~key-name) IllegalArgumentException. throw)
       (do
          ;(println "Agg" aggregate#)
         (assoc aggregate# ~keyword (~opt-f elem#)))
       ))))

(defn- check-aggregate [aggregate]
  (let [assert-not-nil #(-> aggregate %2 nil? not (assert (str %1 " is nil")))]
    (assert-not-nil "connection" :conn)
    (assert-not-nil "subject" :subject)
    ))


(defrecord PublishAggregate [^Connection conn
                                       ^String subject
                                       ^ String reply-to
                                       #^bytes message
                                       ^IPersistentMap headers])
(def empty-pub (->PublishAggregate nil nil nil nil nil))

(defn- produce-msg [^PublishAggregate aggregate]
  (let [subject (:subject aggregate)
        reply (:reply-to aggregate)
        data (:message aggregate)
        headers (:headers aggregate)]
    (NatsMessage.
      subject
      reply
      (if (nil? headers) nil (reduce-kv #(.add %1 %2 %3) (Headers.) headers))
      data
      true
      ))
  )

; Publish DSL

(def to (edit-aggregate :conn "connection"))
(def subject (edit-aggregate :subject "subject" #(str %)))
(def reply-to (edit-aggregate :reply-to "reply-to" #(str %)))
(def message (edit-aggregate :message "message" #(m/to-dto %)))
(def headers (edit-aggregate :headers "headers"))

(defn sync-pub [^PublishAggregate aggregate]
  (check-aggregate aggregate)
  (.publish (:conn aggregate) (produce-msg aggregate)))

(defn async-pub [^PublishAggregate aggregate] (a/go (sync-pub aggregate)))

(defn future-pub [^PublishAggregate aggregate]
  (check-aggregate aggregate)
  (let [comp-future (.request (:conn aggregate) (produce-msg aggregate))]
    (future (.get comp-future))))

; Subscribe DSL

(defrecord SubscribeAggregate [^Connection conn ^String subject])
(def empty-sub (->SubscribeAggregate nil nil))

(defn ^Subscription sync-sub [^SubscribeAggregate aggregate]
  (check-aggregate aggregate)
  (.subscribe (:conn aggregate) (:subject aggregate)))

(defn async-sub
  ([^SubscribeAggregate aggregate]
   (check-aggregate aggregate)
   (.createDispatcher (:conn aggregate)))
  (^Subscription [^SubscribeAggregate aggregate ^Fn fn]
   (.subscribe (async-sub aggregate) (proxy [MessageHandler] []
                                       (onMessage [^Message msg] (fn msg))))))

(defn stream-sub
  ([^Dispatcher dispatcher subject]
   (let [stream (s/stream)
         source (s/source-only stream)
         handler (proxy [MessageHandler] []
                   (onMessage [msg] (s/put! stream msg)))
         subscription (.subscribe dispatcher subject handler)]
     (s/on-closed stream #(.close subscription))
     source))
  ([^Subscription subscription]
   (lazy-seq (cons
               (.nextMessage subscription (u/duration 10))
               (stream-sub subscription)))))

(defn stream-pub
  "returns a Manifold sink-only stream which publishes items put on the stream
   to NATS"
  ([^PublishAggregate aggregate]
   (check-aggregate aggregate)
   (let [stream (s/stream)]
     (s/consume (fn [msg hval]
                  (let [adjusted-agg (-> aggregate
                                         (message msg)
                                         (headers hval))]
                    (.publish (:conn aggregate) msg)))
                stream)
     (s/sink-only stream))))

(defmacro publish
  "Publish a message to NATS.
  Configuration is derived from eDSL grammar:

      (publish
        (to nats-connection)
        (subject \"some fonny subject\")
        (reply-to \"somewhere to put answer to\")
        (message \"where important thing to share\")
        (headers {\"regions\" [\"Central Asia\" \"Central Europe\"]}
        ; Position dependent statements
        (sync-pub)
        ; or (async-pub)
        ; or (future-pub)
        ; or (stream-pub) message and headers are defined on consumer
  "
  [& body]
  `(-> empty-pub ~@body))

(defmacro subscribe
  "Subscribe to a subject at NATS.
  Configuration is derived from eDSL grammar:

      (subscribe
        (to nats-connection)
        (subject \"some fonny subject\"))
        (sync-sub)
        [(stream-sub)]
        ; or (async-sub [callback func])
             [(stream-sub)]

  "
  [& body]
  `(-> empty-sub ~@body))