(ns functional-programming-itmo-2021.lab-2.hash-map
  (:import (clojure.lang IPersistentMap Associative Util ILookup IMapEntry Seqable IPersistentCollection IMeta MapEquivalence IPersistentVector)
           (java.util Map)))

(defn ??? [] (throw (Exception. "Not Implemented")))

(defrecord OpenMapEntry [key tombstone val]
  IMapEntry
  (getKey [_] key)
  (getValue [_] val))

(defn exact-entry? [key candidate]
  (and (not (:tombstone candidate)) (.equals key (:key candidate))))

(defn find-entry [arr key]
  (loop [computed (-> key hash int Math/abs)
         times-left (count arr)]
    (let [pos (rem computed (count arr))]
      (if-let [entry (nth arr pos)]
        (if (exact-entry? key entry)
          [entry pos]
          (if (> times-left 0)
            (recur (inc computed) (dec times-left)) [nil nil]))
        [nil pos]))
  ))

(defn insert-entries
  [amount arr new-entries]
    (if (or (empty? new-entries) (<= amount 0))
      [arr new-entries]
      (if-let [pos (last (find-entry arr (:key (first new-entries))))]
        (recur (dec amount) (assoc arr pos (first new-entries)) (rest new-entries))
        (println "Reached nil: " amount arr new-entries)
        ))
   )

(defn inc-load-by [load arr amount] (+ load (/ (double amount) (count arr))))

(defn non-empty-cells [arr] (filter #(not (or (nil? %) (:tombstone %))) arr))

(defn rebalance
  ([arr] (rebalance arr 2))
  ([arr coef]
   (let [current-size (count arr)
         filtered (non-empty-cells arr)]
         (->> filtered
              (insert-entries (count filtered) (vec (repeat (* coef (max current-size 1)) nil)))
              first
              )
         )))

(defn insert [load arr new]
  (let [next-load (inc-load-by load arr 1)]
    (if (>= next-load 0.8)
      (let [rebalanced (rebalance arr)
            balanced-load (/ (-> rebalanced non-empty-cells count double) (count rebalanced))]
        (insert balanced-load rebalanced new))
      [(first (insert-entries 1 arr [new])) next-load]
      )))

(defn delete [arr key]
  (if-let [pos (last (find-entry arr key))]
    (assoc arr pos (->OpenMapEntry key true nil))
    arr
    ))

(def compute-meta
  (memoize (fn [contents] {:size (count (non-empty-cells contents))} )))

(declare ->OpenAddressesMap)
(deftype OpenAddressesMap [contents load]
  IMeta
  (meta [_] (compute-meta contents))

  ILookup
  (valAt [_ k not-found]
    (if-let [[attempt _] (find-entry contents k)]
      (:val attempt)
      not-found))

  (valAt [m k] (.valAt m k nil))

  Iterable
  (iterator [m] (.iterator (seq m)))

  Seqable
  (seq [_] (non-empty-cells contents))

  IPersistentMap
  (assoc [_ k v] (apply ->OpenAddressesMap (insert load contents (->OpenMapEntry k false v))))
  (assocEx [m k v] (if (.containsKey m k)
                     (.runtimeException Util "Key already present")
                     (.assoc m k v)))
  (without [_ k] (->OpenAddressesMap (delete contents k) load))

  MapEquivalence

  IPersistentCollection
  (count [m] (:size (.meta m)))
  (cons [m new] (cond
                       (and (instance? IPersistentVector new) (>= (count new) 2)) (assoc m (first new) (nth new 2))
                       (instance? IMapEntry new) (assoc m (key new) (val new))
                       (instance? Seqable new) (reduce #(assoc %1 (key %2) (val %2)) m (seq new))
                       ))
  (empty [_] (->OpenAddressesMap [] 1.0))
  (equiv [m o]
    (if (or
          (not (or (instance? Map o) (instance? IPersistentMap o)))
          (and
            (instance? IPersistentMap o)
            (->> o (instance? MapEquivalence) not))
          (not= (count o) (count m)))
        false
      (loop [elems (seq m)]
          (let [cur-elem (first elems)]
            (if-not (empty? elems)
              (if (or
                    (not (contains? o (.getKey cur-elem)))
                    (not (= (.getValue cur-elem) (get o (.getKey cur-elem)))))
                false
                (recur (rest elems)))
              true))))
    )

  Associative
  (containsKey [_ k] (let [[attempt _] (find-entry contents k)]
                       (if-not (or (nil? attempt) (:tombstone attempt))
                         true
                         false
                         )))
  (entryAt [_ k] (first (find-entry contents k)))

  )

(defn open-address-map
  ([]  (->OpenAddressesMap [nil nil nil nil] 0.0))
  ([src-map] (open-address-map src-map 2))
  ([src-map coef] (->OpenAddressesMap (->>
                                   src-map
                                   (map #(->OpenMapEntry (first %) false (last %)))
                                   (#(rebalance % coef))
                                   ) (/ 1.0 coef))))

(def example (open-address-map {1 2 3 4 5 6 7 8}))