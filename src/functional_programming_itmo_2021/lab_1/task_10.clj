(ns functional-programming-itmo-2021.lab-1.task-10
  "Task 10 from Project Euler:
    - The sum of the primes below 10 is 2 + 3 + 5 + 7 = 17
    - Find the sum of all the primes below two million.
  "
  )

(def task-threshold (int 2E+6))

(defn sum-primes-until-n-1
  "Find the sum of all the primes below N (tailrec)"
  ([N] (sum-primes-until-n-1 2 [] N))
  ([iter acc N] (if (>= iter N)
                  (reduce + (map bigint acc) )
                  (if (let [helper (fn [primes num]
                                     (if (and (not-empty primes) (zero? (rem num (first primes))))
                                     false
                                     (if (empty? primes)
                                       true
                                       (recur (rest primes) num)))
                                     )]
                        (helper acc iter)
                        )
                    (recur (inc iter) (conj acc iter) N)
                    (recur (inc iter) acc N)
                    )))
  )

(defn enqueue [sieve candidate step]
  (let [m (+ candidate step)]
    (if (sieve m)
      (recur sieve m step)
      (assoc sieve m step)))
  )

(defn next-sieve [sieve candidate]
  (if-let [step (sieve candidate)]
    (-> (dissoc sieve candidate)
        (enqueue candidate step))
    (enqueue sieve candidate (+ candidate candidate))
    )
  )

(defn next-primes [sieve candidate]
  (if (sieve candidate)
    (next-primes (next-sieve sieve candidate) (+ 2 candidate))
    (cons candidate
          (lazy-seq (next-primes (next-sieve sieve candidate) (+ 2 candidate))))
    )
  )

(def primes (concat [2] (next-primes {} 3)))