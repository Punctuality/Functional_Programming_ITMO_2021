(ns functional-programming-itmo-2021.lab-4.nats.connection
  (:require [functional-programming-itmo-2021.lab-4.util :as u]
            [clojure.core.match :refer [match]])
  (:import (io.nats.client Options$Builder ConnectionListener ReconnectDelayHandler Nats JetStreamOptions$Builder)
           (javax.net.ssl SSLContext)))


(defn with [^Options$Builder builder & arguments]
  (match (vec arguments)
    [:no-echo]          (.noEcho builder)
    [:no-headers]       (.noHeaders builder)
    [:no-no-responders] (.noNoResponders builder)
    [:no-reconnect]     (.noReconnect builder)

    [:auth-handler creds-file] (.authHandler builder (Nats/credentials creds-file))
    [:auth-handler jwt nkey]   (.authHandler builder (Nats/credentials jwt nkey))

    [:server server]            (.server builder server)
    [:servers & servers]        (.servers builder (into-array String servers))
    [:ping-interval dur]        (.pingInterval builder (u/duration dur))
    [:ssl-context ssl-type]     (.sslContext builder (SSLContext/getInstance ssl-type))
    [:token token]              (.token builder token)
    [:data-port-type port-type] (.dataPortType builder port-type)

    [:connection-name name]     (.connectionName builder name)
    [:connection-listener func] (.connectionListener builder (proxy [ConnectionListener] []
                                                                   (connectionEvent [conn type] (func conn type))))
    [:connection-timeout dur]   (.connectionTimeout builder (u/duration dur))

    [:reconnect-buffer-size size]   (.reconnectBufferSize builder size)
    [:reconnect-delay-handler func] (.reconnectDelayHandler builder (proxy [ReconnectDelayHandler] []
                                                                      (getWaitTime [totalTries] (func totalTries))))
    [:reconnect-jitter dur]         (.reconnectJitter builder (u/duration dur))
    [:reconnect-jitter-tls dur]     (.reconnectJitterTls builder (u/duration dur))
    [:reconnect-wait dur]           (.reconnectWait builder (u/duration dur))

    [:max-control-line limit]               (.maxControlLine builder limit)
    [:max-messages-in-outgoing-queue limit] (.maxMessagesInOutgoingQueue builder limit)
    [:max-pings-out limit]                  (.maxPingsOut builder limit)
    [:max-reconnects limit]                 (.maxReconnects builder limit)

    [:request-cleanup-interval dur] (.requestCleanupInterval builder (u/duration dur))
    [:buffer-size size]             (.bufferSize builder size)
    [:user-info user-name pass]     (.userInfo builder user-name pass)
    [:inbox-prefix prefix]          (.inboxPrefix builder prefix)

    [:verbose]                        (.verbose builder)
    [:discard-message-when-full]      (.discardMessagesWhenOutgoingQueueFull builder)
    [:old-request-style]              (.oldRequestStyle builder)
    [:pedantic]                       (.pedantic builder)
    [:open-tls]                       (.opentls builder)
    [:secure]                         (.secure builder)
    [:trace-connection]               (.traceConnection builder)

    [:executor service] (.executor builder service)
    [:error-listener error-listener] (.errorListener builder error-listener)
    ))

(defmacro connection
  "Creates a connection to NATS mesh-server.
  Configuration is achieved through eDSL grammar:

      (connection
        (with :no-headers)
        (with :no-echo)
        (with :buffer-size 100)
        (with :max-reconnects 5)
        ...
        (with :verbose))

  Available keyword properties:
    :no-echo :no-headers :no-no-responders :no-reconnect
    :auth-handler
    :server :servers :ping-interval :ssl-context :token :data-port-type
    :connection-name :connection-listener :connection-timeout
    :reconnect-buffer-size :reconnect-delay-handler :reconnect-jitter :reconnect-jitter-tls :reconnect-wait
    :max-control-line :max-messages-in-outgoing-queue :max-pings-out :max-reconnects
    :request-cleanup-interval :buffer-size :user-info :inbox-prefix
    :verbose :discard-message-when-full :old-request-style :pedantic n:open-tls :secure :trace-connection
    :executor :error-listener
  "
  [& params]
  `(-> (Options$Builder.) ~@params .build Nats/connect))


; EXAMPLE
;(defn connection
;  "Creates connection to Nats.io instance"
;  [& addresses]
;  (let [bootstrap-servers (u/flat-map #(str/split % #"\,") addresses)
;        ]))

; (defn create-nats
;  "creates a Nats connection, returning a Nats object
;   - urls : nats server urls, either a seq or comma separated"
;  [& urls]
;  (let [servers (flatten (map #(str/split % #",") urls))
;        j-servers (into-array String servers)
;        cf (ConnectionFactory. j-servers)]
;    (.createConnection cf)))
;