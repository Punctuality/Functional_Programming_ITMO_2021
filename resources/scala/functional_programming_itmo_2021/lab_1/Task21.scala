package scala.functional_programming_itmo_2021.lab_1

import Integral.Implicits.given
import Ordering.Implicits.given

object Task21:
  def taskThreshold[N: Integral] = Numeric[N].fromInt(10_000)

  // Recursion would've been faster, but i chose more idiomatic way
  def divisorsSums[N: Integral](coll: Seq[N]): Seq[N] =
    coll.map( n =>
      Iterator.from(1, 1)
      .map(Numeric[N].fromInt)
      .takeWhile(_ < n)
      .filter(d => (n % d) == Numeric[N].fromInt(0))
      .foldLeft(Numeric[N].fromInt(0))(_ + _))


  def findingAmicableNumbers[N: Integral: Ordering](threshold: N): Set[(N, N)] = {
    val numbers   = Iterator.from(1, 1).map(Numeric[N].fromInt).takeWhile(_ <= threshold).toSeq
    val firstRow  = divisorsSums(numbers)
    val secondRow = divisorsSums(firstRow)

    numbers.lazyZip(firstRow).lazyZip(secondRow).iterator.collect{
      case (num, fst, snd) if num != fst && num == snd =>
        if (num > fst) fst -> num else num -> fst
    }.toSet
  }


  def task21Report[N: Integral: Ordering]: String = {
    val start = System.currentTimeMillis()
    val result = findingAmicableNumbers[N](taskThreshold)
    val stop = System.currentTimeMillis()
    s"""
       |Task 21 solution:
       | * findingAmicableNumbers
       |Finished in: ${stop - start} ms
       |Solution: $result
       |""".stripMargin
  }