(ns functional-programming-itmo-2021.lab-1.task-10
  "Task 10 from Project Euler:
    - The sum of the primes below 10 is 2 + 3 + 5 + 7 = 17
    - Find the sum of all the primes below two million.
  "
  (:require [functional-programming-itmo-2021.core.sequences :refer [primes]])
  )

(def task-threshold (int 2E+6))                             ;; Task answer: 142913828922

(defn sum-of-primes-below-n-reducing
  "Finds the sum of all the primes bellow N
   Using the reduce operation
  "
  [n]
  (reduce + 0N (take-while #(< % n) primes)))

(defn sum-of-primes-below-n-loopy
  "Finds the sum of all the primes bellow N
   Using the loop/recur operators
  "
  [n]
  (loop [accumulator 0N
         next-prime (first primes)
         others (rest primes)]
    (if
      (< next-prime n)
      (recur (+ accumulator next-prime) (first others) (rest others))
      accumulator
      )
    )
  )

(defn sum-of-primes-below-n-recursive
  "Finds the sum of all the primes bellow N
   Using the standard- and tail- recursion
  "
  ([n] (sum-of-primes-below-n-recursive n 0N (first primes) (rest primes)))
  ([n acc next-prime others]
   (if
     (< next-prime n)
     (recur n (+ acc next-prime) (first others) (rest others))
     acc
     )
   )
  )

(defn sum-of-primes-below-n-non-modular [n]
  (let [
        enqueue (fn [sieve candidate step]
                  (let [m (+ candidate step)]
                    (if (sieve m)
                      (recur sieve m step)
                      (assoc sieve m step)))
                  )
        next-sieve (fn [sieve candidate]
                     (if-let [step (sieve candidate)]
                       (-> (dissoc sieve candidate)                            ; Macro magic O.o
                           (enqueue candidate step))
                       (enqueue sieve candidate (* 2 candidate))
                       )
                     )
        next-primes (fn n-p [sieve candidate]
                      (if (sieve candidate)
                        (recur (next-sieve sieve candidate) (+ 2 candidate))
                        [candidate [(next-sieve sieve candidate) (+ 2 candidate)]]
                        )
                      )]
    (loop [accumulator 0N
           next-prime 2
           params [{} 3]]
      (if
        (< next-prime n)
        (let [[n-p params] (apply next-primes params)]
            (recur (+ accumulator next-prime) n-p params)
          )
        accumulator
        )
      )
    )
  )

(defn task-10-report []
  (do
    (println "Task 10 solutions:
              * sum-of-primes-below-n-reducing
              * sum-of-primes-below-n-loopy
              * sum-of-primes-below-n-recursive
              * sum-of-primes-below-n-non-modular")
    (println "Results are equal:"
             (apply = [
              (time (sum-of-primes-below-n-reducing task-threshold))
              (time (sum-of-primes-below-n-loopy task-threshold))
              (time (sum-of-primes-below-n-recursive task-threshold))
              (time (sum-of-primes-below-n-non-modular task-threshold))
              ])
             )
    (println "Solution:" (sum-of-primes-below-n-reducing task-threshold))
    )
  )