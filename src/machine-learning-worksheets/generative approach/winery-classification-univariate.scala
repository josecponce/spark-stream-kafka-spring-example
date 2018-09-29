import java.io.File

import breeze.linalg._
import breeze.numerics.abs
import breeze.plot
import breeze.plot.Figure
import breeze.stats._
import breeze.stats.distributions._

val data = csvread(new File("/home/josep/scala-spark-python/python-module/fundamentals_of_machine_learning_DSE220x/winery-univariate/wine.data.txt"))
val featureNames = Array("Alcohol", "Malic acid", "Ash", "Alcalinity of ash","Magnesium", "Total phenols","Flavanoids", "Nonflavanoid phenols", "Proanthocyanins", "Color intensity", "Hue","OD280/OD315 of diluted wines", "Proline")
val indicesFromPython = IndexedSeq(54,151,63,55,123,121,7,160,106,90,141,146,5,98,168,80,33,18,
61,51,66,37,4,104,60,111,126,86,112,164,26,56,129,45,8,44,
161,92,94,174,24,30,93,101,113,19,135,74,144,16,131,138,40,158,
22,108,175,145,71,162,156,27,83,134,97,118,163,62,2,59,95,96,
43,10,109,73,171,159,110,125,116,166,170,50,155,89,64,122,124,69,
49,48,85,13,136,23,165,20,15,78,52,100,76,3,176,107,6,68,
75,84,130,12,137,152,14,0,91,153,46,11,119,102,35,57,41,173,
65,1,120,143,42,105,132,154,17,38,133,53,139,128,34,28,114,31,
149,127,157,32,142,150,147,29,99,82,79,115,148,177,72,77,25,81,
167,169,39,58,140,88,70,87,36,21,9,103,67,117,47,172)

//val permutationIndices = Rand.permutation(data.rows).draw()//random permutation
val pdata = data(indicesFromPython, ::)
val trainX = pdata(0 to 129, 1 to 13)
val trainY = pdata(0 to 129, 0)
val testX = pdata(130 to 177, 1 to 13)
val testY = pdata(130 to 177, 0)

bincount(trainY.map(x => x.toInt)).mapPairs((i, x) => (i, x))//distribution

val fig = Figure()//plot histogram of a feature
val subplot = fig.subplot(0)
subplot += plot.hist(trainX(::, 0).toDenseVector)
fig.saveas("nada.png")

Range(0, 12).map(i => (i, stddev(trainX(::, i))))//standard deviation for all features

//plot distribution per feature
def plotFeatureDist(featureIndex: Int): Unit = {
  val f = Figure()
  val p2 = f.subplot(0)

  val colors = Array("green", "red", "blue")
  Range(1,4).foreach(i => {
    val indexes = trainY.toDenseVector.findAll{ y => y.toInt == i }
    val feature = trainX(indexes, featureIndex)
    val g = Gaussian(mean(feature), stddev(feature))
    val gValues = g.samplesVector(1000)
    val ordered = gValues(argsort(gValues))
    val distribution = ordered.map(g(_))

    p2 += plot.plot(ordered, distribution, colorcode = colors(i - 1))
  })
  p2.title = s"$featureIndex:${featureNames(featureIndex)}"
  f.saveas("subplots.png")
}
//featureNames.indices.foreach(plotFeatureDist)

//models
def generativeModel(featureIndex: Int, y: Int): (Double, Gaussian) = {
  val indices = trainY.findAll(i => i.toInt == y)
  val feature = trainX(indices, featureIndex)
  val g = Gaussian(mean(feature), stddev.population(feature))
  val ratio = indices.length.toDouble / trainY.length
  (ratio, g)
}

//error indices
val testErrors = DenseVector.range(0, featureNames.length).map(i => {
  val models = DenseVector.range(1, 4).map(generativeModel(i, _))
  val feature = testX(::, i)
  val predictions = feature.map(x => argmax(models.map(m => Math.log(m._1) + m._2.logPdf(x))) + 1)
  1 - (bincount(abs(predictions - testY.map(_.toInt))).valueAt(0) / feature.length.toDouble)
})

val min3Erorrs = argtopk(abs(testErrors - 1.0),3)

val trainErrors = DenseVector.range(0, featureNames.length).map(i => {
  val models = DenseVector.range(1, 4).map(generativeModel(i, _))
  val feature = trainX(::, i)
  val predictions = feature.map(x => argmax(models.map(m => Math.log(m._1) + m._2.logPdf(x))) + 1)
  1 - (bincount(abs(predictions - trainY.map(_.toInt))).valueAt(0) / feature.length.toDouble)
})

val minTrainErorrs = argtopk(abs(trainErrors - 1.0),3)

