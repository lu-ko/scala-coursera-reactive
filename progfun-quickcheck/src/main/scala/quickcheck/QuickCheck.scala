package quickcheck

import common._

import org.scalacheck._
import Arbitrary._
import Gen._
import Prop._

abstract class QuickCheckHeap extends Properties("Heap") with IntHeap {

  property("min1") = forAll { a: Int =>
    val h = insert(a, empty)
    findMin(h) == a
  }

  property("min2") = forAll { (a: Int, b: Int) =>
    val h = insert(a, insert(b, empty))
    findMin(h) == Math.min(a, b)
  }

  property("delete last min") = forAll { a: Int =>
    isEmpty(deleteMin(insert(a, empty)))
  }

  property("sorted list after findMin") = forAll { heap: H =>
    def getList1(ts: H): List[A] = {
      if (isEmpty(ts)) Nil
      else findMin(ts) :: getList1(deleteMin(ts))
    }
    def validate(list: List[Int]): Boolean = list match {
      case Nil => true
      case x :: Nil => true
      case x :: y :: rest => if (x <= y) validate(y :: rest) else false
    }
    validate(getList1(heap))
  }

  property("insert list and compare sorted") = forAll { list: List[Int] =>
    def insertList(zoznam: List[Int], acc: H): H = zoznam match {
      case Nil => acc
      case a :: rest => insertList(rest, insert(a, acc))
    }
    def getList2(ts: H): List[A] = {
      if (isEmpty(ts)) Nil
      else findMin(ts) :: getList2(deleteMin(ts))
    }
    list.sortWith((x: Int, y: Int) => (x <= y)) == (getList2(insertList(list, empty)))
  }

  property("min of melded") = forAll { (heap1: H, heap2: H) =>
    val melded = meld(heap1, heap2)
    findMin(melded) == Math.min(findMin(heap1), findMin(heap2))
  }

  lazy val genHeap: Gen[H] = for {
    element <- arbitrary[A]
    newHeap <- frequency((1, empty), (5, genHeap))
  } yield insert(element, newHeap)

  implicit lazy val arbHeap: Arbitrary[H] = Arbitrary(genHeap)

  // help/examples:

  lazy val genMap: Gen[Map[Int, Int]] = for {
    k <- arbitrary[Int]
    v <- arbitrary[Int]
    m <- oneOf(value(Map.empty[Int, Int]), genMap)
  } yield m.updated(k, v)

  trait Generator[+T] {
    def generate: T
  }

  lazy val booleans = new Generator[Boolean] {
    def generate = integers.generate > 0
  }

  val integers = new Generator[Int] {
    val rand = new java.util.Random
    def generate = rand.nextInt()
  }
}
