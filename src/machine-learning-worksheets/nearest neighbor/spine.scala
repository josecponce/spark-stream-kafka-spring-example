import java.io.File

import breeze.linalg._
import breeze.numerics._
import breeze.stats.distributions.{Gaussian, MultivariateGaussian}

val labels = Array("NO", "DH", "SL")
val data = csvread(new File("/home/josep/scala-spark-python/python-module/fundamentals_of_machine_learning_DSE220x/NN_spine/column_3C.csv"), separator = ' ')

val trainSet = DenseMatrix.vertcat(data(0 to 19, ::), data(40 to 187, ::), data(230 to 309, ::))
val trainData = trainSet(::, 0 to 5)
val trainLabels = trainSet(::, 6)

val testSet = DenseMatrix.vertcat(data(20 to 39, ::), data(188 to 230, ::))
val testData = testSet(::, 0 to 5)
val testLabels = testSet(::, 6)

//L2 (Euclidean distance)
val predictions = testData(*, ::).map(row => trainLabels.valueAt(argmin(sqrt(squaredDistance(trainData(*, ::), row)))))
val labelPrediction = DenseMatrix.horzcat(testLabels.asDenseMatrix.t, (predictions :== testLabels).toDenseVector.asDenseMatrix.t.map(b => if (b) 1.0 else 0.0))
val labelPredictionDistribution = labelPrediction(*, ::).map(row => (row.valueAt(0), row.valueAt(1))).data.groupBy(k => (k._1, k._2)).map(k => ((labels(k._1._1.toInt), k._1._2.toInt == 1), k._2.length))

val predictions1= testData(*, ::).map(row => trainLabels.valueAt(argmin(sum(abs(trainData(*, ::) - row).apply(*, ::)))))
val labelPrediction1 = DenseMatrix.horzcat(testLabels.asDenseMatrix.t, (predictions1 :== testLabels).toDenseVector.asDenseMatrix.t.map(b => if (b) 1.0 else 0.0))
val labelPredictionDistribution1 = labelPrediction1(*, ::).map(row => (row.valueAt(0), row.valueAt(1))).data.groupBy(k => (k._1, k._2)).map(k => ((labels(k._1._1.toInt), k._1._2.toInt == 1), k._2.length))

val conflictingPredictionCount = (predictions - predictions1).toArray.count(_ != 0.0)

MultivariateGaussian(predictions, labelPrediction).apply()
Gaussian(2.0, 1.0).apply()
