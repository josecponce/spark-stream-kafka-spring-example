import java.io.File

import breeze.numerics._
import breeze.linalg._
import breeze.plot
import breeze.plot.Figure
import breeze.stats._
import breeze.stats.distributions._

val data = csvread(new File("/home/josep/scala-spark-python/python-module/fundamentals_of_machine_learning_DSE220x/winery-bivariate/wine.data.txt"))
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

def fitGaussian(featuresIndices: Seq[Int], label: Int): MultivariateGaussian = {
  val features = trainX(trainY.findAll(_.toInt == label), featuresIndices).toDenseMatrix
  MultivariateGaussian(mean(features(::, *)).t, covmat(features) * ((features.rows-1.0)/features.rows))//note the * ((features.rows-1.0)/features.rows) to make the covariance calculation biased
}
def fitGenerative(featuresIndices: Seq[Int], labels: Int): DenseVector[(Double, MultivariateGaussian)] = {
  DenseVector.range(1, labels + 1).map(i =>
    (trainY.findAll(_.toInt == i).length.toDouble / trainY.length, fitGaussian(featuresIndices, i)))
}
def plot2Features(features: (Int, Int)): Unit = {
  import java.awt.Color

  val fig = Figure()
  val subplot = fig.subplot(0)
  subplot += plot.scatter(trainX(::, features._1), trainX(::, features._2), (i: Int) => 0.05,
    i => trainY(i) match {case 1 => Color.red case 2 => Color.green case 3 => Color.blue})
  subplot.xlabel = featureNames(features._1)
  subplot.ylabel = featureNames(features._2)
  fig.saveas("contour.png")
}

val errors = featureNames.indices.flatMap(x => {
  featureNames.indices.map(y => {
    val error = if (x != y) {
      val features = Seq(x, y)
      val models = fitGenerative(features, 3)
      val predictions = testX(*, features).map(test => {
        argmax(models.map(model => Math.log(model._1) + model._2.logPdf(test.toDenseVector))) + 1.0
      })
      sum((predictions :!= testY).map(if (_) 1 else 0))// / testY.length.toDouble
    } else testY.length + 1
    (x, y, error)
  })
})
val min = errors.minBy(_._3)//this is the set of features with the smallest test error