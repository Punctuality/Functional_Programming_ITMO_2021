(ns functional-programming-itmo-2021.core.sequences)

(defn ^:private enqueue [sieve candidate step]
   (let [m (+ candidate step)]
    (if (sieve m)
      (recur sieve m step)
      (assoc sieve m step)))
  )

(defn ^:private next-sieve [sieve candidate]
   (if-let [step (sieve candidate)]
    (-> (dissoc sieve candidate)                            ; Macro magic O.o
        (enqueue candidate step))
    (enqueue sieve candidate (* 2 candidate))
    )
  )

(defn ^:private next-primes [sieve candidate]
   (if (sieve candidate)
    (recur (next-sieve sieve candidate) (+ 2 candidate))
    (cons candidate
          (lazy-seq (next-primes (next-sieve sieve candidate) (+ 2 candidate))))
    )
  )

(def primes (concat [2] (next-primes {} 3)))

; Example of call-tree by executing (take 10 primes)
;  Next-primes:  {} 3
;    Next-sieve: {} 3
;      Enqueue: {} 3 6
;  Next-primes:  {9 6} 5
;    Next-sieve: {9 6} 5
;      Enqueue: {9 6} 5 10
;  Next-primes:  {9 6, 15 10} 7
;    Next-sieve: {9 6, 15 10} 7
;      Enqueue: {9 6, 15 10} 7 14
;  Next-primes:  {9 6, 15 10, 21 14} 9
;    Next-sieve: {9 6, 15 10, 21 14} 9
;      Enqueue: {15 10, 21 14} 9 6
;      Enqueue: {15 10, 21 14} 15 6
;      Enqueue: {15 10, 21 14} 21 6
;  Next-primes:  {15 10, 21 14, 27 6} 11
;    Next-sieve: {15 10, 21 14, 27 6} 11
;      Enqueue: {15 10, 21 14, 27 6} 11 22
;  Next-primes:  {15 10, 21 14, 27 6, 33 22} 13
;    Next-sieve: {15 10, 21 14, 27 6, 33 22} 13
;      Enqueue: {15 10, 21 14, 27 6, 33 22} 13 26
;  Next-primes:  {15 10, 21 14, 27 6, 33 22, 39 26} 15
;    Next-sieve: {15 10, 21 14, 27 6, 33 22, 39 26} 15
;      Enqueue: {21 14, 27 6, 33 22, 39 26} 15 10
;  Next-primes:  {21 14, 27 6, 33 22, 39 26, 25 10} 17
;    Next-sieve: {21 14, 27 6, 33 22, 39 26, 25 10} 17
;      Enqueue: {21 14, 27 6, 33 22, 39 26, 25 10} 17 34
;  Next-primes:  {21 14, 27 6, 33 22, 39 26, 25 10, 51 34} 19
;    Next-sieve: {21 14, 27 6, 33 22, 39 26, 25 10, 51 34} 19
;      Enqueue: {21 14, 27 6, 33 22, 39 26, 25 10, 51 34} 19 38
;  Next-primes:  {21 14, 27 6, 33 22, 39 26, 25 10, 51 34, 57 38} 21
;    Next-sieve: {21 14, 27 6, 33 22, 39 26, 25 10, 51 34, 57 38} 21
;      Enqueue: {27 6, 33 22, 39 26, 25 10, 51 34, 57 38} 21 14
;  Next-primes:  {27 6, 33 22, 39 26, 25 10, 51 34, 57 38, 35 14} 23
;    Next-sieve: {27 6, 33 22, 39 26, 25 10, 51 34, 57 38, 35 14} 23
;      Enqueue: {27 6, 33 22, 39 26, 25 10, 51 34, 57 38, 35 14} 23 46
;  Next-primes:  {27 6, 33 22, 39 26, 25 10, 51 34, 57 38, 35 14, 69 46} 25
;    Next-sieve: {27 6, 33 22, 39 26, 25 10, 51 34, 57 38, 35 14, 69 46} 25
;      Enqueue: {27 6, 33 22, 39 26, 51 34, 57 38, 35 14, 69 46} 25 10
;      Enqueue: {27 6, 33 22, 39 26, 51 34, 57 38, 35 14, 69 46} 35 10
;  Next-primes:  {27 6, 33 22, 39 26, 51 34, 57 38, 35 14, 69 46, 45 10} 27
;    Next-sieve: {27 6, 33 22, 39 26, 51 34, 57 38, 35 14, 69 46, 45 10} 27
;      Enqueue: {33 22, 39 26, 51 34, 57 38, 35 14, 69 46, 45 10} 27 6
;      Enqueue: {33 22, 39 26, 51 34, 57 38, 35 14, 69 46, 45 10} 33 6
;      Enqueue: {33 22, 39 26, 51 34, 57 38, 35 14, 69 46, 45 10} 39 6
;      Enqueue: {33 22, 39 26, 51 34, 57 38, 35 14, 69 46, 45 10} 45 6
;      Enqueue: {33 22, 39 26, 51 34, 57 38, 35 14, 69 46, 45 10} 51 6
;      Enqueue: {33 22, 39 26, 51 34, 57 38, 35 14, 69 46, 45 10} 57 6
;  Next-primes:  {33 22, 39 26, 51 34, 57 38, 35 14, 69 46, 45 10, 63 6} 29