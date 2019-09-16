// abstract classes
abstract class Mammal {
  val warmBlooded: Boolean = true
}

class Pig extends Mammal
var missPiggy = new Pig
println(missPiggy.warmBlooded)

// another abstract class
// wrong way of doing things
/*
abstract class Plane {
  val fun: String = "You bet"
}
var f16 = new Plane
println(f16.fun)
*/

// overloading
class Whatever {
  def ex(s: String): String = s"$s I like words"
  def ex(i: Int) = i*3
}
var w = new Whatever
println(w.ex("Hi"))
println(w.ex(3))

// "apply" method is special
//   it is called by default with unspecified method name
class Star {
  def apply() = {
    "shining"
  }
}
var s = new Star
println(s())