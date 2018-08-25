package com.example.spark.spring

import org.apache.kafka.clients.consumer.{ConsumerConfig, ConsumerRecord}
import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.spark.sql.{SQLContext, SparkSession}
import org.apache.spark.streaming.dstream.InputDStream
import org.apache.spark.streaming.kafka010.{ConsumerStrategies, KafkaUtils, LocationStrategies}
import org.apache.spark.streaming.{Duration, StreamingContext}
import org.apache.spark.{SparkConf, SparkContext}
import org.springframework.context.annotation.{Bean, Configuration}

@Configuration
class IOCConfiguration {

  @Bean
  def sparkConf(): SparkConf = new SparkConf(false).set("spark.submit.deployMode", "client")

  @Bean
  def sc(sparkConf: SparkConf): SparkContext = new SparkContext("local[4]", "spark-kafka-exapmle", sparkConf)

  @Bean
  def sqlContext(sc: SparkContext): SQLContext = new SQLContext(sc)

  @Bean
  def session(sqlContext: SQLContext): SparkSession = sqlContext.sparkSession

  @Bean
  def scc(sc: SparkContext): StreamingContext = new StreamingContext(sc, Duration(2000))

  @Bean
  def stream(ssc: StreamingContext): InputDStream[ConsumerRecord[String, String]] = {
    var kafkaParams = Map[String, Object](
      ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG -> "node-3:9092",
      ConsumerConfig.GROUP_ID_CONFIG -> "spark.test.group",
      ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG -> classOf[StringDeserializer],
      ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG -> classOf[StringDeserializer]
    )

    KafkaUtils.createDirectStream[String, String](ssc, LocationStrategies.PreferConsistent,
      ConsumerStrategies.Subscribe[String, String](Array("spark.test").toSet, kafkaParams))

  }
}
