

import lens.impl.HTraversal
import scala.language.higherKinds
import scalaz.std.option._
import scalaz.{Monoid, Applicative}

case class Location(latitude: Double, longitude: Double)
case class Address(city: String, postcode: String, location: Location)
case class Person(age: Int, address: Address)

object Example extends App {
  import lens.Macro._

  val AddressLens  = mkLens[Person, Address]("address")
  val CityLens     = mkLens[Address, String]("city")
  val LocationLens = mkLens[Address, Location]("location")

  object LatLongTraversal extends HTraversal[Location, Double] {
    protected def traversalFunction[F[_] : Applicative](lift: Double => F[Double], from: Location): F[Location] = {
      import scalaz.syntax.applicative._
      (lift(from.latitude) |@| lift(from.longitude))((newLatitude, newLongitude) =>
        from.copy(latitude = newLatitude, longitude = newLongitude)
      )
    }
  }

  val address = Address("London", "EC1...", Location(4.0, 6.0))
  val person = Person(25, address)

  //basic scala
  println(person.copy(address = person.address.copy(city = "Paris")))

  val Person2CityLens= AddressLens >- CityLens

  println(Person2CityLens.set(person, "Paris") )
  println(Person2CityLens.get(person))
  println(Person2CityLens.modify(person, _ + "!!!"))
  println(Person2CityLens.lift(person, city => Option(city)))

  val location = Location(2, 6)

  implicit object Addition extends Monoid[Double] {
    def append(f1: Double, f2: => Double): Double = f1 + f2
    def zero: Double = 0L
  }

  println(LatLongTraversal.get(location))
  println(LatLongTraversal.set(location, 1.0))
  println(LatLongTraversal.fold(location))
  println(LatLongTraversal.modify(location, _ + 2))

  import scalaz.std.list._
  println(LatLongTraversal.lift(location, l => List(l+1, l, l-1)))

  // composition of lens and traversal
  println((AddressLens >- LocationLens >- LatLongTraversal).get(person))

}




