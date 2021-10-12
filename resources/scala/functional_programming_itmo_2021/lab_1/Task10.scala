package scala.functional_programming_itmo_2021.lab_1

import Numeric.Implicits.given
import Ordering.Implicits.given

object Task10:
  def taskThreshold[N: Numeric] = Numeric[N].fromInt(2_000_000)

  def primes[N: Numeric]: LazyList[N] = {
    // Implementation of Sieve of Eratosthenes

    def enqueue(sieve: Map[N, N], candidate: N, step: N): Map[N, N] =
      if (sieve contains (candidate + step))
        enqueue(sieve, candidate + step, step)
      else
        sieve + (candidate + step -> step)

    def nextSieve(sieve: Map[N, N], candidate: N): Map[N, N] =
      sieve get candidate match {
        case Some(step) => enqueue(sieve - candidate, candidate, step)
        case None       => enqueue(sieve, candidate, candidate * Numeric[N].fromInt(2))
      }

    def nextPrimes(sieve: Map[N, N], candidate: N): LazyList[N] =
      if (sieve contains candidate)
        nextPrimes(nextSieve(sieve, candidate), candidate + Numeric[N].fromInt(2))
      else
        LazyList(candidate) lazyAppendedAll
          nextPrimes(nextSieve(sieve, candidate), candidate + Numeric[N].fromInt(2))

    Numeric[N].fromInt(2) #:: nextPrimes(Map.empty, Numeric[N].fromInt(3))
  }

  def sumOfPrimesBelowN[N: Numeric: Ordering](n: N): N =
    primes[N].takeWhile(_ < n).foldLeft(Numeric[N].fromInt(0))(_ + _)

  def task10Report[N: Numeric: Ordering]: String = {
    val start = System.currentTimeMillis()
    val result = sumOfPrimesBelowN[N](taskThreshold)
    val stop = System.currentTimeMillis()
    s"""
      |Task 10 solution:
      | * sumOfPrimesBelowN
      |Finished in: ${stop - start} ms
      |Solution: $result
      |""".stripMargin
  }