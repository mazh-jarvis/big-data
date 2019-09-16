// scala singleton
// scala objects cannot be instantiated
object Hello {
  def speak = "sup"
}
println(Hello.speak)

// object behaviour
object Cake {
  var base = "cookies"
  var sauce = "peanut butter"
  def apply() = {
    s"Give me $base and $sauce"
  }
}
println(Cake())

// traits are like java interfaces
//  but are less enforced
trait Cool {
  var speak = "I am groovy"
  var more = "more"
  def ex = { println("Hello World") }
}
object JoeCamel extends Cool
println(JoeCamel.speak)
println(JoeCamel.more)

// multiple composition
trait Speed {
  def run = {
    "really fast"
  }
}

trait Jump {
  def leap = {
    "really high"
  }
}

object Spiderman extends Speed with Jump {
  def describe = {
    s"I can run $run and jump $leap"
  }
}

println(Spiderman.describe)


// Diamond problem: won't compile!
/*
println("** diamond")
trait X {
  var yyy = "hi"
}

trait Y {
  var yyy = "bye"
}

object Moo extends X with Y

println(Moo.yyy)*/
