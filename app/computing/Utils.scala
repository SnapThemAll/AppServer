package computing

object Utils {

  def w(picA: PictureFingerPrint, picB: PictureFingerPrint): Double = ???

  def fNN(userSet: Set[Category], validationSet: Set[Category]): Double = {
    /*
    require(userSet.size == validationSet.size,
      s"userSet size (${userSet.size}) != validationSet size (${validationSet.size})")
    */

    validationSet.map { y =>
      y.pictures.map{ i =>
        userSet.find(cat => cat.name == y.name)
          .get
          .pictures.map{ s =>
            w(i, s)
        }.max
      }.sum
    }.sum
  }

}
