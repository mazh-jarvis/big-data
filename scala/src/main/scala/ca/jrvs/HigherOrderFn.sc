// Higher order function
val double = (i:Int) => i*2
def higherOrder(x:Int, y:Int=>Int) = y(x)

higherOrder(6, double)


// Scala closure: a function that accesses variable(s) outside of its scope
val y = 5
val multiplier = (x:Int) => x*y
multiplier(2)

