import java.awt.{Color, Paint, Stroke}
import java.awt.geom.{AffineTransform, Ellipse2D}

import breeze.linalg.{argmax, eig}
import breeze.numerics.atan
import breeze.plot
import breeze.plot.{Figure, PaintScale, Series}
import breeze.stats.distributions.ChiSquared
import breeze.stats.{covmat, mean}
import org.jfree.chart.annotations.XYShapeAnnotation
import org.jfree.chart.plot.XYPlot
import org.jfree.chart.renderer.xy.{XYBlockRenderer, XYItemRenderer}
import org.jfree.data.DomainOrder
import org.jfree.data.contour.DefaultContourDataset
import org.jfree.data.general.{DatasetChangeListener, DatasetGroup}
import org.jfree.data.xy.XYDataset

import scala.reflect.ClassTag

val label = 1
val confidence = 0.95
val chiSquared = ChiSquared(2).inverseCdf(confidence)
val raw = trainX(trainY.findAll(_.toInt == label), Seq(0,6)).toDenseMatrix
val covMatrix = covmat(raw)
val eigen = eig(covMatrix)
val largestEigenVector = eigen.eigenvectors(argmax(eigen.eigenvalues), ::)
val alfa = atan(largestEigenVector(0)/largestEigenVector(1))
val means = mean(raw(::, *))
val xAxis = Math.sqrt(chiSquared * eigen.eigenvalues(0))
val yAxis = Math.sqrt(chiSquared * eigen.eigenvalues(1))
val upperLeft = (means(0) - xAxis, means(1) - yAxis)
val ellipse = new Ellipse2D.Double(upperLeft._1, upperLeft._2, 2 * xAxis, 2 * yAxis)
val rotatedEllipse = AffineTransform.getRotateInstance(-alfa, means(0), means(1)).createTransformedShape(ellipse)
val ellipseAnnotation = new XYShapeAnnotation(rotatedEllipse)


val features = (0, 6)
val fig = Figure()
val subplot = fig.subplot(0)
subplot.plot.addAnnotation(ellipseAnnotation)
subplot += plot.scatter(trainX(::, features._1), trainX(::, features._2), (i: Int) => 0.05,
  i => trainY(i) match {case 1 => Color.red case 2 => Color.green case 3 => Color.blue})
fig.saveas("contour.png")
