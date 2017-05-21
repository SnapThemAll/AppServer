
val list = List.range(0, 20)

private def percentage(percent: Int): Float = percent / 100f

val splitIndex = Math.round(list.size * percentage(65) )
val (a, b) = list.splitAt(splitIndex)

a

b
