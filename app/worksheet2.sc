
val list = List.range(0, 10)

val stream = list.toStream.map(i => {println(i); i * 10})

stream(0)

stream(1)

stream(2)