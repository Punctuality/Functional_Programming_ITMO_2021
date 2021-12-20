(ns functional-programming-itmo-2021.lab-4.nats.message)

(defprotocol NatsMessageLike
  (to-dto [this]))

(extend-protocol NatsMessageLike
  String
  (to-dto [str] (.getBytes str "UTF-8")))