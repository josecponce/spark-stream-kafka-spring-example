import java.awt.geom.{AffineTransform, Ellipse2D}

import breeze.linalg._
import breeze.numerics.atan
import breeze.plot
import breeze.plot.Series
import breeze.stats.distributions.ChiSquared
import breeze.stats.{covmat, mean}

import scala.collection.mutable.ArrayBuffer
//http://www.visiondummy.com/2014/04/draw-error-ellipse-representing-covariance-matrix/
def contour(confidence: Double, raw: DenseMatrix[Double], style: Char = '-', colorcode: String = null, name: String = null): Series = new Series {
  val chiSquared = ChiSquared(2).inverseCdf(confidence)
  val covMatrix = covmat(raw)
  val eigen = eig(covMatrix)
  var alfa = atan(eigen.eigenvectors(0,1) / eigen.eigenvectors(0,0))
  if (alfa < 0) alfa += 6.28318530718
  val means = mean(raw(::, *))
  val xAxis = Math.sqrt(chiSquared * eigen.eigenvalues(0))
  val yAxis = Math.sqrt(chiSquared * eigen.eigenvalues(1))
  val upperLeft = (means(0) - xAxis, means(1) - yAxis)
  val ellipse = new Ellipse2D.Double(upperLeft._1, upperLeft._2, 2 * xAxis, 2 * yAxis)
  val rotatedEllipse = AffineTransform.getRotateInstance(-alfa, means(0), means(1)).createTransformedShape(ellipse)
  val path = rotatedEllipse.getPathIterator(null, 0.001)
  val segment = new Array[Double](6)
  val x = ArrayBuffer.empty[Double]
  val y = ArrayBuffer.empty[Double]

  while (!path.isDone) {
    path.currentSegment(segment)
    x += segment(0)
    y += segment(1)
    path.next()
  }

  plot.plot(DenseVector(x.toArray), DenseVector(y.toArray), style = style, colorcode = colorcode, name = name)
}