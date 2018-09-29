import config._
import org.apache.spark.sql.DataFrame

val linkage = spark.read.option("header", "true").option("nullValue", "?").option("inferSchema", "true").csv("/home/josep/linkage-machine-learning/data/blocks").cache

val matches = linkage.where("is_match = true")
val matchSummary = matches.describe()

val misses = linkage.filter($"is_match" === false)
val missSummary = misses.describe()

def pivotSummary(summary: DataFrame): DataFrame = {
  summary.flatMap(row => (1 until row.size)
    .map(i => (row.getString(0), matchSummary.schema(i).name, row.getString(i).toDouble)))
    .toDF("metric", "field", "value").groupBy("field").pivot("metric").sum("value").cache()
}

val matchLongForm = pivotSummary(matchSummary)
val missLongForm = pivotSummary(missSummary)
matchLongForm.createOrReplaceTempView("match_desc")
missLongForm.createOrReplaceTempView("miss_desc")
val features = spark.sql(
  """select a.field, a.count + b.count total, a.mean - b.mean delta
    from match_desc a
     join miss_desc b on a.field = b.field
      where a.field not in ("id_1", "id_2")
      order by delta desc, total desc
  """)

//starting here we deal with case classess instead of plain dataframse

case class MatchData (
                       id_1: Int,
                       id_2: Int,
                       cmp_fname_c1: Option[Double],
                       cmp_fname_c2: Option[Double],
                       cmp_lname_c1: Option[Double],
                       cmp_lname_c2: Option[Double],
                       cmp_sex: Option[Int],
                       cmp_bd: Option[Int],
                       cmp_bm: Option[Int],
                       cmp_by: Option[Int],
                       cmp_plz: Option[Int],
                       is_match: Boolean
                     )
case class Score(value: Double) {
  def +(oi: Option[Any]) = Score(value + (oi.getOrElse(0.0) match {
    case i: Int => i.toDouble
    case i: Double => i
  }))
  def +(i: Double) = Score(value + i)
}
def crossTabs(scored: DataFrame, t: Double): DataFrame =
  scored.selectExpr(s"score >= $t as above", "is_match").groupBy("above").pivot("is_match").count()
def findBestFit(scored: DataFrame, start: Long, end: Long) = {
  var bestFit = 0.0
  var bestFitFalsePositives = Long.MaxValue
  var done = false
  (start until end).foreach(i => {
    if (!done) {
      val threshold = i / 10.0
      println(s"Attempting threshold = $threshold")
      val result = crossTabs(scored, threshold).cache()
      val falsePositives = result.where("above = true").select($"false").take(1)
      val falsePositivesCount = if (!falsePositives.isEmpty) falsePositives(0)(0).asInstanceOf[Long] else 0
      val falseNegatives = result.where("above = false").select($"true").take(1)
      val falseNegativesCount = if (!falseNegatives.isEmpty) falseNegatives(0)(0).asInstanceOf[Long] else 0
      if (falseNegativesCount <= 100 && falsePositivesCount < bestFitFalsePositives) {
        bestFit = threshold
        bestFitFalsePositives = falsePositivesCount
        println(s"bestFit: $bestFit, falsePositives: $falsePositivesCount")
      } else {
        done = true
      }
    }
  })
  bestFit
}
val matchData = linkage.as[MatchData]

val scored = matchData.map(md =>
  ((Score(md.cmp_lname_c1.getOrElse(0.0)) + md.cmp_plz + md.cmp_by + md.cmp_bd + md.cmp_bm).value, md.is_match)).toDF("score", "is_match").cache
val bestFit = findBestFit(scored, 38, 50)
println("Printing crosstab for best fit threshold producing less than 100 false negatives using Score(md.cmp_lname_c1.getOrElse(0.0)) + md.cmp_plz + md.cmp_by + md.cmp_bd + md.cmp_bm)")
crossTabs(scored, bestFit).show
/*
+-----+-------+-----+
|above|  false| true|
+-----+-------+-----+
| true|    637|20875|
|false|5727564|   56|
+-----+-------+-----+
 */

val scoredWithSexPenalty = matchData.map(md =>
  ((Score(md.cmp_lname_c1.getOrElse(0.0)) + md.cmp_plz + md.cmp_by + md.cmp_bd + md.cmp_bm + (if (md.cmp_sex.getOrElse(1) == 0) -1 else 0)).value, md.is_match)).toDF("score", "is_match").cache
val bestFitWithSexPenalty = findBestFit(scoredWithSexPenalty, 38, 60)
println("Printing crosstab for best fit threshold producing less than 100 false negatives using Score(md.cmp_lname_c1.getOrElse(0.0)) + md.cmp_plz + md.cmp_by + md.cmp_bd + md.cmp_bm + (if (md.cmp_sex.getOrElse(1) == 0) -1 else 0)")
crossTabs(scoredWithSexPenalty, bestFitWithSexPenalty).show
/*
+-----+-------+-----+
|above|  false| true|
+-----+-------+-----+
| true|    328|20833|
|false|5727873|   98|
+-----+-------+-----+
 */

val scoredWithFirstNameIfLastName = matchData.map(md =>
  ((Score(md.cmp_lname_c1.getOrElse(0.0)) + md.cmp_plz + md.cmp_by + md.cmp_bd + md.cmp_bm  + (if (md.cmp_lname_c1.getOrElse(0.0) > 0.8) md.cmp_fname_c1 else Option(0.0))).value, md.is_match)).toDF("score", "is_match").cache
val bestFitWithFirstNameIfLastName = findBestFit(scoredWithFirstNameIfLastName, 47, 60)
println("Printing crosstab for best fit threshold producing less than 100 false negatives using Score(md.cmp_lname_c1.getOrElse(0.0)) + md.cmp_plz + md.cmp_by + md.cmp_bd + md.cmp_bm  + (if (md.cmp_lname_c1.getOrElse(0.0) > 0.8) md.cmp_fname_c1 else Option(0.0))")
crossTabs(scoredWithFirstNameIfLastName, bestFitWithFirstNameIfLastName).show
/* 47 threshold
+-----+-------+-----+
|above|  false| true|
+-----+-------+-----+
| true|     13|20837|
|false|5728188|   94|
+-----+-------+-----+
 */