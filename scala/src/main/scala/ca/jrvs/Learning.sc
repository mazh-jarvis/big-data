// constant
val x = 3.0 + 1

// expressions
var number = {val x = 2 * 2; x + 40}

// scala f strings
val source = 3
var badSong = {
  s"$source bling"
}
println(badSong)

// in expr, the last statement is returned
var band = {
  val name = "sublime"
  name
}
println(band)

// some more examples
class Bar {
  def apply: Unit = new Bar
}

object FooMaker {
  def apply: Bar = new Bar
}

def howdy = println("Hello!")
howdy


// 2 ways to define functions
def func1 = (i:Int) => i*2
val func2 = (i:Int) => i*2

// return statement can be implicit
def add(a: Int, b: Int): Int = {
  a + b
}
println(add(3, 6))

// test
def theBest(category: String = "Hi", thing: String): String = {
  s"The best type of $category is $thing"
}
