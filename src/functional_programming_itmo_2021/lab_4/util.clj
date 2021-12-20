(ns functional-programming-itmo-2021.lab-4.util
  (:import (java.time Duration)))

(defmacro flat-map [f & arguments]
  `(flatten (map ~f ~@arguments)))

(defprotocol DurationLike
  (duration ^Duration [this]))

(extend-protocol DurationLike
  Duration
  (duration [this] this)

  Long
  (duration [this] (Duration/ofSeconds this))
  )