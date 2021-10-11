(ns functional-programming-itmo-2021.lab-1.task-21
  "Task 21 from Project Euler:
    Let d(n) be defined as the sum of proper divisors of n
    (numbers less than n which divide evenly into n).
    If d(a) = b and d(b) = a, where a ≠ b,
    then a and b are an amicable pair and each of a and b
    are called amicable numbers.

    For example, the proper divisors of 220 are 1, 2, 4, 5, 10, 11, 20, 22, 44, 55 and 110
    Therefore d(220) = 284. The proper divisors of 284 are 1, 2, 4, 71 and 142
    So d(284) = 220.

    Evaluate the sum of all the amicable numbers under 10000.
  "
  )

(def task-threshold 1E+4)

(defn ^:private divisors-of-num
  ([num] (divisors-of-num num 0 []))
  ([num last divisors]
   (let [next (inc last)] (if (< next num)
       (if (zero? (rem num next))
         (recur num next (cons next divisors))
         (recur num next divisors))
       divisors
       )))
  )

(defn finding-amicable-numbers-lazy-mapped
  "Finds pairs of amicable numbers, up to threshold
   Using collection operators: map, filter, sort, distinct
  "
  [threshold]
  (let [divisors-sums (fn [coll] (map #(reduce + (divisors-of-num %)) coll))
        numbers (range 1 (inc threshold))
        first-row (divisors-sums numbers)
        second-row (divisors-sums first-row)]
      (distinct (map #(sort [(% 0) (% 1)])
                     (filter #(and (not= (% 0) (% 1)) (== (% 0) (% 2)))
                                  (map vector numbers first-row second-row))))
    )
  )

(defn finding-amicable-numbers-recursive
  "Finds pairs of amicable numbers, up to threshold
   Using standard- and tail- recursion
  "
  ([threshold] (finding-amicable-numbers-recursive #{} threshold 1))
  ([acc threshold current]
    (let [divisors-sum #(reduce + (divisors-of-num %))
          fst-iter (divisors-sum current)
          snd-iter (divisors-sum fst-iter)
          next-acc (if
                     (and (not= current fst-iter) (== current snd-iter))
                     (conj acc (sort [current fst-iter]))
                     acc
                     )]
        (if (>= current threshold) next-acc (recur next-acc threshold (inc current)))
      )
   )
  )

(defn finding-amicable-numbers-loopy
  "Finds pairs of amicable numbers, up to threshold
   Using loop/recur operators
  "
  [threshold]
  (loop [acc #{}
         current 1]
   (let [divisors-sum #(reduce + (divisors-of-num %))
         fst-iter (divisors-sum current)
         snd-iter (divisors-sum fst-iter)
         next-acc (if
                    (and (not= current fst-iter) (== current snd-iter))
                    (conj acc (sort [current fst-iter]))
                    acc
                    )]
     (if (>= current threshold) next-acc (recur next-acc (inc current)))
     )
   )
  )

(defn finding-amicable-numbers-non-modular
  "Finds pairs of amicable numbers, up to threshold
   Bulk implementation
  "
  [threshold]
  (let [divisors (fn [num last divisors]
                   (let [next (inc last)] (if (< next num)
                       (if (zero? (rem num next))
                         (recur num next (cons next divisors))
                         (recur num next divisors))
                       divisors
                       )))
        divisors-sums (fn [coll] (map #(reduce + (divisors % 0 [])) coll))
        numbers (range 1 (inc threshold))
        first-row (divisors-sums numbers)
        second-row (divisors-sums first-row)]
    (distinct (map #(sort [(% 0) (% 1)])
                   (filter #(and (not= (% 0) (% 1)) (== (% 0) (% 2)))
                           (map vector numbers first-row second-row))))
    )
  )

(defn task-21-report []
  (do
    (println "Task 21 solutions:
              * finding-amicable-numbers-lazy-mapped
              * finding-amicable-numbers-recursive
              * finding-amicable-numbers-loopy
              * finding-amicable-numbers-non-modular")
    (println "Results are equal:"
             (apply = [
                       (time (apply hash-set (finding-amicable-numbers-lazy-mapped task-threshold)))
                       (time (apply hash-set (finding-amicable-numbers-recursive task-threshold)))
                       (time (apply hash-set (finding-amicable-numbers-loopy task-threshold)))
                       (time (apply hash-set (finding-amicable-numbers-non-modular task-threshold)))
                       ])
             )
    (println "Solution:" (finding-amicable-numbers-lazy-mapped task-threshold))
    )
  )