val x = 3.0 + 1

var number = {val x = 2 * 2; x + 40}

val source = 3
var badSong = {
  s"$source bling"
}
println(badSong)

var band = {
  val name = "sublime"
  name
}
println(band)

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
