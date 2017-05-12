package computing

object Utils {

  def w(picA: PictureFingerPrint, picB: PictureFingerPrint): Double = picA.distanceWith(picB)

  def fNN(userSet: Set[Category], validationSet: Set[Category]): Double = {
    require(userSet.size == validationSet.size,
      s"userSet size = ${userSet.size}, validationSet size = ${validationSet.size}. " +
        s"This means that they don't contain the same categories")
    userSet.map(_.name).foreach(catName =>
      require(validationSet.map(_.name).contains(catName),
        s"Category named $catName of the userSet is missing in the validationSet")
    )

    validationSet.map { y =>
      y.pictures.map{ i =>
        userSet.find(cat => cat.name == y.name)
          .get
          .pictures.map{ s =>
            w(i, s)
        }.min
      }.sum
    }.sum
  }

}
