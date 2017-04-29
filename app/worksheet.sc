import breeze.linalg._

// ROWS AND COLUMS
val rowX = DenseVector.zeros[Double](5)
val rowY = DenseVector[Double](1,2,3)
val rowZ = SparseVector[Double](1,2,3)
val colX = rowX.t
rowX(3 to 4) :=  2d
rowX(4) = 19d
rowX(0 to 2) := rowY
rowX
rowX(-1) == rowX(4)

//MATRIX
val matM = DenseMatrix.zeros[Int](5,5)
matM.rows -> matM.cols
matM(::, 1)
matM(4,::) := DenseVector(1,2,3,4,5).t
matM(-2,::) := rowX.map(_.toInt).t
matM(::,1) := rowX.map(_.toInt)
matM
matM(0 to 1, 0 to 1) := DenseMatrix((3,1),(-1,-2))
matM

//Broadcasting
import breeze.stats.mean
val dm = DenseMatrix(
  (1.0,2.0,3.0),
  (4.0,5.0,6.0))
val res = dm(::, *) + DenseVector(3.0, 4.0)
res(::, *) := DenseVector(3.0, 4.0)
res
mean(dm(*, ::))
mean(res(::, *))

//Distributions
import breeze.stats.distributions._
val poi = new Poisson(3.0)
val s = poi.sample(5)
s map { poi.probabilityOf(_) }
val doublePoi = for(x <- poi) yield x.toDouble
breeze.stats.meanAndVariance(doublePoi.samples.take(1000))
poi.mean -> poi.variance

//breeze.optimize.linear
import breeze.optimize.linear._
val lp = new LinearProgram()
import lp._
val x0 = Real()
val x1 = Real()
val x2 = Real()

val lpp =  ( (x0 +  x1 * 2 + x2 * 3 )
  subjectTo ( x0 * -1 + x1 + x2 <= 20)
  subjectTo ( x0 - x1 * 3 + x2 <= 30)
  subjectTo ( x0 <= 40 )
  )

val result = maximize( lpp)

norm(result.result - DenseVector(40.0,17.5,42.5), 2) < 1E-4

//Breeze-Viz
import breeze.linalg._
import breeze.plot._

val f = Figure()
val p = f.subplot(0)
val x = linspace(0.0,1.0)
p += plot(x, x :^ 2.0)
p += plot(x, x :^ 3.0, '.')
p.xlabel = "x axis"
p.ylabel = "y axis"

val p2 = f.subplot(2,1,1)
val g = breeze.stats.distributions.Gaussian(0,1)
p2 += hist(g.sample(100000),100)
p2.title = "A normal distribution"

val f2 = Figure()
f2.subplot(0) += image(DenseMatrix.rand(200,200))