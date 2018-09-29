import config._
import org.apache.spark.ml.recommendation.ALS
import org.apache.spark.sql.functions._
import org.apache.spark.sql.types.IntegerType

import scala.util.Random

val artist = { spark.read.option("delimiter", "\t").csv("/home/josep/machine-learning/audioscrobbler-data/artist_data.txt")
    .select($"_c0" cast IntegerType as "id", $"_c1" as "name").where("id is not null").cache() }
val artistAlias = { spark.read.option("delimiter", "\t").csv("/home/josep/machine-learning/audioscrobbler-data/artist_alias.txt")
  .select($"_c0" cast IntegerType as "badid", $"_c1" cast IntegerType as "goodid").where("badid is not null and goodid is not null").cache() }
val userArtist = { spark.read.option("delimiter", " ").csv("/home/josep/machine-learning/audioscrobbler-data/user_artist_data.txt")
  .select($"_c0" cast IntegerType as "user", $"_c1" cast IntegerType as "artist", $"_c2" cast IntegerType as "playcount").cache() }

val trainData = { userArtist.join(artistAlias, $"artist" === $"badid", "left_outer")
  .select($"user", coalesce($"goodid", $"artist") as "artist", $"playcount").groupBy($"user", $"artist")
  .agg(sum("playcount") as "count").cache }

val model = new ALS().setSeed(Random.nextLong()).
  setImplicitPrefs(true).setRank(10).setRegParam(0.01).setAlpha(1.0).setMaxIter(5).
  setUserCol("user").setItemCol("artist").setRatingCol("count").setPredictionCol("prediction").
  fit(trainData)
