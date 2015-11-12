package simulations

import math.random

class EpidemySimulator extends Simulator {

  def randomBelow(i: Int) = (random * i).toInt

  protected[simulations] object SimConfig {
    val population: Int = 300
    val roomRows: Int = 8
    val roomColumns: Int = 8

    // to complete: additional parameters of simulation

    val prevalenceRate = 0.01
    val transRate = 0.40
    val dieRate = 0.25
    val airMove = 0.01

    val becomeSick: Int = 6 // 6 days
    val becomeDead: Int = 14 // 14 days
    val becomeImmune: Int = 16 // 16 days
    val becomeHealthy: Int = 18 // 18 days

  }

  import SimConfig._

  // to complete: construct list of persons
  val persons: List[Person] = {
    def generate(id: Int, acc: List[Person]): List[Person] = {
      if (id <= population) {
        val person = new Person(id)
        person.infected = (id <= population * prevalenceRate)
        person.move
        person.infectFromSomeone
        person :: generate(id + 1, acc)
      } else {
        acc
      }
    }
    generate(1, Nil)
  }

  def containsRoom(newPos: (Int, Int), f: Person => Boolean): Boolean = {
    !(persons.find { p => p.row == newPos._1 && p.col == newPos._2 && f(p) }).isEmpty
  }

  class Person(val id: Int) {
    var incubatorStarted = false
    var infected = false
    var sick = false
    var immune = false
    var dead = false

    // demonstrates random number generation
    var row: Int = randomBelow(roomRows)
    var col: Int = randomBelow(roomColumns)

    //
    // to complete with simulation logic
    //
    def setInfected() {
      infected = true;
    }

    def setSick() {
      sick = true;
    }

    def setDead() {
      if (randomBelow(100) <= dieRate * 100) {
        dead = true
      }
    }

    def setImmune() {
      if (!dead) {
        sick = false
        immune = true
      }
    }

    def setHealthy() {
      if (!dead) {
        incubatorStarted = false
        infected = false
        sick = false
        immune = false
        dead = false
      }
    }

    def infectFromSomeone() {
      // infect from someone if he is in my room
      if (!dead && !infected && !immune && (persons != null)) {
        if (containsRoom((row, col), p => p.infected)) {
          if (randomBelow(100) <= transRate * 100) {
            setInfected()
          }
        }
      }

      // if i'm infected and i was not infected in the past -> add to agenda
      if (infected && !incubatorStarted) {
        incubatorStarted = true
        afterDelay(becomeSick)(setSick)
        afterDelay(becomeDead)(setDead)
        afterDelay(becomeImmune)(setImmune)
        afterDelay(becomeHealthy)(setHealthy)
      }

      // check infect people in my room after each day
      afterDelay(1)(infectFromSomeone)
    }

    def move() {
      val moveDelay = randomBelow(5) + 1
      afterDelay(moveDelay)(goAvay)
    }

    def goAvay() {
      if (!dead) {
        // before submit we have to turn off air-traffic extension!!!
        if (randomBelow(100) <= airMove * 100) {
          // go to another random room by airplane
          setNewPosition(getNewPositionAir)
        } else {
          // go to another room in neighborhood by foot if you can
          val newPos = getNewPositionsFoot.filter(isRoomHealthy)
          if (!newPos.isEmpty) {
            setNewPosition(newPos(randomBelow(newPos.length)))
          }
        }

        // next move
        move()
      }
    }

    def setNewPosition(newPos: (Int, Int)) = {
      row = newPos._1
      col = newPos._2

      // infect from someone if he is in my room
      //infectFromSomeone()
    }

    def isRoomHealthy(newPos: (Int, Int)): Boolean = {
      !containsRoom(newPos, p => (p.sick || p.dead))
    }

    def getNewPositionsFoot(): List[(Int, Int)] = {
      def checkOver(newInt: Int, max: Int): Int = {
        if (newInt < -1 || (max + 1) < newInt) {
          throw new IllegalArgumentException("Wrong number of row or col: " + newInt)
        }
        (newInt + max) % max
      }

      // all available moves (4 rooms)
      List(
        (checkOver(row + 1, roomRows), col),
        (checkOver(row - 1, roomRows), col),
        (row, checkOver(col + 1, roomColumns)),
        (row, checkOver(col - 1, roomColumns)))
    }

    def getNewPositionAir(): (Int, Int) = {
      def randomExcept(max: Int, except: Int): Int = {
        var choosen = randomBelow(max)
        if (choosen != except) {
          choosen
        } else {
          randomExcept(max, except)
        }
      }

      // random room except current room
      (randomExcept(roomRows, row), randomExcept(roomColumns, col))
    }

  }
}
