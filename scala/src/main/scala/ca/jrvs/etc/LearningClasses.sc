// class constructor with instance var one-liner
class Carrot(val flavor: String)
var c = new Carrot(flavor = "weird")
println(c.flavor)

// class inference
class Singer(var alias: String)
var singers = List(
  new Singer("miley"),
  new Singer("bradley")
)
println {
  singers map(_.alias)
}
/**
  * Under the hood:
  * println {
  singers.map { (s: Singer) =>
    s.alias
  }
}
*/

// inheritance
class Tree {
  def about = "I am made of wood"
}
class Pine extends Tree {
  override def about = super.about + " and sticky!"
}
class Willow extends Tree
var aPine = new Pine
println(aPine.about)

// default parameters
class Kitchen(var color: String, val floorType: String = "tile") {
  def describe =
    s"The kitchen has $color $floorType floors"

}
var myKitchen = new Kitchen("purple")
println(myKitchen.describe)

// example
class Person(val first:String, val last:String) {
  def fullName = s"$first $last"
}
println(new Person("John", "Smith").fullName)