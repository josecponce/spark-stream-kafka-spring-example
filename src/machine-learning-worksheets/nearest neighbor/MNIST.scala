import java.awt.Color
import java.io.File
import java.util.UUID

import breeze.linalg._
import breeze.numerics._
import breeze.plot
import breeze.plot.{Figure, GradientPaintScale}

val testData = csvread(new File("/home/josep/scala-spark-python/python-module/fundamentals_of_machine_learning_DSE220x/NN_MNIST/MNIST/test_data.csv"), separator = ',')
val testLabels = csvread(new File("/home/josep/scala-spark-python/python-module/fundamentals_of_machine_learning_DSE220x/NN_MNIST/MNIST/test_labels.csv"), separator = ',').toDenseVector
val trainData = csvread(new File("/home/josep/scala-spark-python/python-module/fundamentals_of_machine_learning_DSE220x/NN_MNIST/MNIST/train_data.csv"), separator = ',')
val trainLabels = csvread(new File("/home/josep/scala-spark-python/python-module/fundamentals_of_machine_learning_DSE220x/NN_MNIST/MNIST/train_labels.csv"), separator = ',').toDenseVector

val trainLabelsDist = trainLabels.data.groupBy(k => k).map(tuple => (tuple._1.toInt, tuple._2.length))
val testLabelsDist = testLabels.data.groupBy(k => k).map(tuple => (tuple._1.toInt, tuple._2.length))

def plotImage(row: Transpose[DenseVector[Double]], name: String): Unit = {
  val m = row.inner.toDenseMatrix.reshape(28, 28).t//transpose because vectors are turned into matrices "column wise" instead of "row wise"
  val m2 = m.t.toDenseMatrix.apply(::, 27 to 0 by -1).t//can't have negative steps on rows so need to transpose and copy, then reverse columns and then transpose again to get reversed cols as our original rows reversed
  val fig: Figure = Figure()
  val subplot = fig.subplot(0)
  subplot.title = name
  subplot += plot.image(m2,
    new GradientPaintScale[Double](0 ,255, Range(0, 255).map(i => new Color(i, i, i)).toArray))
  fig.saveas(s"/tmp/${UUID.randomUUID()}.png")
}

//plotImage(testData(0, ::), "testData(0)")
//plotImage(trainData(0, ::), "trainData(0)")

print("Distance from 7 to 1: ", squaredDistance(trainData(4, ::).inner,trainData(5, ::).inner))
print("Distance from 7 to 2: ", squaredDistance(trainData(4, ::).inner,trainData(1, ::).inner))
print("Distance from 7 to 7: ", squaredDistance(trainData(4, ::).inner,trainData(7, ::).inner))

def NN_classifier(v: Transpose[DenseVector[Double]]): (Double, Double) = {
  val nn = argmin(squaredDistance(trainData(*, ::), v.inner))
  (nn, trainLabels.valueAt(nn))
}

println("A success case:")
val nnS = NN_classifier(testData(0, ::))
println(s"NN classification: ${nnS._2}")
println(s"True label: ${testLabels.data(0)}")
//println(s"The test image: ")
//plotImage(testData(0, ::), "testData(0)")
//println("The corresponding nearest neighbor image:")
//plotImage(trainData(nn._1, ::), "NN for testData(0)")

println("A failure case:")
val nnF = NN_classifier(testData(39, ::))
println(s"NN classification: ${nnF._2}")
println(s"True label: ${testLabels.data(39)}")
//println(s"The test image:")
//plotImage(testData(39, ::), "testData(39)")
//println("The corresponding nearest neighbor image:")
//plotImage(trainData(nnF._1, ::), "NN for testData(39)")

println("Test data point 100")
val nn100 = NN_classifier(testData(100, ::))
println(s"NN classification: ${nn100._2}")
println(s"True label: ${testLabels.data(100)}")
//println(s"The test image:")
//plotImage(testData(100, ::), "testData(100)")
//println("The corresponding nearest neighbor image:")
//plotImage(trainData(nn100._1, ::), "NN for testData(100)")

def time[T](action: => T): T = {
  val time = System.currentTimeMillis()
  val result = action
  println(s"Time(s): ${(System.currentTimeMillis() - time) / 1000}")
  result
}

val predictions = time {testData(*, ::).map(v => NN_classifier(Transpose(v))._2)}

val resultDist = (predictions :== testLabels).toArray.groupBy(k => k).map(t => (t._1, t._2.length))
val error = resultDist(false).toDouble / testLabels.length
println(s"Error of nearest neighbor classifier: $error")

NN_classifier(testData(100, ::))
