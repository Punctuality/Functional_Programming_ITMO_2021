(ns functional-programming-itmo-2021.lab-4.nats.operations
  (:require [functional-programming-itmo-2021.lab-4.nats.message :as m]
            [clojure.core.async :as a])
  (:import (io.nats.client Connection Subscription Dispatcher)
           (clojure.lang IPersistentMap)
           (io.nats.client.impl NatsMessage Headers)))

(defmacro ^:private edit-aggregate
  ([keyword key-name] `(edit-aggregate ~keyword ~key-name identity))
  ([keyword key-name opt-f]
   `(fn [aggregate# elem#]
     (if-not (nil? (~keyword aggregate#))
       (-> (str "Already defined " ~key-name) IllegalArgumentException. throw)
       (do
          (println "Agg" aggregate#)
         (assoc aggregate# ~keyword (~opt-f elem#)))
       ))))

(defn- check-aggregate [aggregate]
  (let [assert-not-nil #(-> aggregate %2 nil? not (assert (str %1 " is nil")))]
    (assert-not-nil "connection" :conn)
    (assert-not-nil "subject" :subject)
    ))


(defrecord ^:private PublishAggregate [^Connection conn
                                       ^String subject
                                       ^ String reply-to
                                       #^bytes message
                                       ^IPersistentMap headers])
(def ^:private empty-pub (->PublishAggregate nil nil nil nil nil))

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
  "
  [& body]
  `(-> empty-pub ~@body))

; Subscribe DSL

(defrecord ^:private SubscribeAggregate [^Connection conn ^String subject])
(def ^:private empty-sub (->SubscribeAggregate nil nil))

(defn ^Subscription sync-sub [^SubscribeAggregate aggregate]
  (check-aggregate aggregate)
  (.subscribe (:conn aggregate) (:subject aggregate)))

(defn ^Dispatcher async-sub [^SubscribeAggregate aggregate]
  (check-aggregate aggregate)
  (.createDispatcher (:conn aggregate)))


(defmacro subscribe
  "Subscribe to a subject at NATS.
  Configuration is derived from eDSL grammar:

      (subsribe
        (to nats-connection)
        (subject \"some fonny subject\"))
        (sync-pub)
        (TODO Process sync)
        ; or (async-pub)
             (TODO Process async)

  "
  [& body]
  `(-> empty-sub ~@body))

;(defprotocol INatsMessage
;  (msg-body [_]))
;
;(defrecord NatsMessage [nats-message]
;  INatsMessage
;  (msg-body [_]
;    (edn/read-string
;     (String. (.getData nats-message)
;              "UTF-8"))))
;
;(defn ^:private create-nats-subscription
;  [nats subject {:keys [queue] :as opts} stream]
;  (.subscribeAsync
;   nats
;   subject
;   queue
;   (reify
;     MessageHandler
;     (onMessage [_ m]
;       (s/put! stream (map->NatsMessage {:nats-message m}))))))
;
;(defn subscribe
;  "returns a a Manifold source-only stream of INatsMessages from a NATS subject.
;   close the stream to dispose of the subscription"
;  ([nats subject] (subscribe nats subject {}))
;  ([nats subject opts]
;   (let [stream (s/stream)
;         source (s/source-only stream)
;         nats-subscription (create-nats-subscription nats subject opts stream)]
;     (s/on-closed stream
;                  (fn []
;                    (log/info "closing NATS subscription: " subject)
;                    (.close nats-subscription)))
;     source)))
;
;(defn publish
;  "publish a message
;  - subject-or-fn : either a string specifying a fixed subject or a
;                     (fn [item] ...) which extracts a subject from an item"
;  ([nats subject-or-fn] (publish nats subject-or-fn "" {}))
;  ([nats subject-or-fn body] (publish nats subject-or-fn body {}))
;  ([nats subject-or-fn body {:keys [reply] :as opts}]
;   (let [is-subject-fn? (or (var? subject-or-fn) (fn? subject-or-fn))
;         subject (if is-subject-fn? (subject-or-fn body) subject-or-fn)]
;     (if subject
;       (.publish nats subject reply (.getBytes (pr-str body) "UTF-8"))
;       (log/warn (ex-info
;                  (str "no subject "
;                       (if is-subject-fn? "extracted" "given"))
;                  {:body body}))))))
;
;(defn publisher
;  "returns a Manifold sink-only stream which publishes items put on the stream
;   to NATS"
;  ([nats subject-or-fn]
;   (let [stream (s/stream)]
;     (s/consume (fn [body]
;                  (publish nats subject-or-fn body))
;                stream)
;     (s/sink-only stream))))
;
;(defn pubsub
;  "returns a Manifold source+sink stream for a single NATS subject.
;   the source returns INatsMessages, while the sink accepts
;   strings"
;  ([nats subject] (pubsub nats subject {}))
;  ([nats subject opts]
;   (let [pub-stream (s/stream)
;         sub-stream (s/stream)
;
;         nats-subscription (create-nats-subscription nats subject opts sub-stream)]
;
;     (s/consume (fn [body] (publish nats subject body)) pub-stream)
;
;     (s/on-closed sub-stream (fn [] (.close nats-subscription)))
;
;     (s/splice pub-stream sub-stream))))