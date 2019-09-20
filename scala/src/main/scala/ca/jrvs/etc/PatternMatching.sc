def toGreek(letter: Char) = {
  letter match {
    case 'a' | 'A' => "Alpha"
    case 'b' | 'B' => "Beta"
    case _ => "Invalid input"
  }
}

toGreek('A')

def max(x: Int, y: Int) =
  x > y match {
    case true => x
    case false => y
}

println(max(5, 8))
