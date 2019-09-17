object Loops{
  // for loop
  for (i <- 1 to 10 by 2)
    print(i + ":")

  // list comprehension
  val list = for (num <- 1 to 10) yield num+1
  list.size

  val str = "Hello"
  for (s <- str) // until str.length
    print(s.toUpper)
}