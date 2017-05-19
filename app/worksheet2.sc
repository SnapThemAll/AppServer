
val a = Set(1, 2, 3)
val b = Set(3, 4, 5)

val c = Set(a, b)

c.reduce(_ ++ _)