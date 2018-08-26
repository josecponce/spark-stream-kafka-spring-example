package com.example.spark.spring

import org.apache.kafka.clients.consumer.{ConsumerConfig, ConsumerRecord}
import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.spark.sql.{SQLContext, SparkSession}
import org.apache.spark.streaming.dstream.InputDStream
import org.apache.spark.streaming.kafka010.{ConsumerStrategies, KafkaUtils, LocationStrategies}
import org.apache.spark.streaming.{Duration, StreamingContext}
import org.apache.spark.{SparkConf, SparkContext}
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.{Bean, Configuration}

@Configuration
class IOCConfiguration() {
  private val DEPLOY_MODE = "spark.submit.deployMode"
  private val SERIALIZER = "spark.serializer"
  private val KRYO_SERIALIZER = "org.apache.spark.serializer.KryoSerializer"


  @Bean
  def sparkConf(config: SparkProperties): SparkConf = new SparkConf(false)
    .set(DEPLOY_MODE, config.getDeployMode)
    .set(SERIALIZER, KRYO_SERIALIZER)

  @Bean
  def sc(config: SparkProperties, sparkConf: SparkConf): SparkContext = new SparkContext(config.getMaster, config.getAppName, sparkConf)

  @Bean
  def sqlContext(sc: SparkContext): SQLContext = new SQLContext(sc)

  @Bean
  def session(sqlContext: SQLContext): SparkSession = sqlContext.sparkSession

  @Bean
  def scc(sc: SparkContext): StreamingContext = new StreamingContext(sc, Duration(2000))

  @Bean
  @ConditionalOnProperty(Array("kafka.enabled"))
  def stream(config: KafkaProperties, ssc: StreamingContext): InputDStream[ConsumerRecord[String, String]] = {
    var kafkaParams = Map[String, Object](
      ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG -> config.getBootstrapServers,
      ConsumerConfig.GROUP_ID_CONFIG -> config.getConsumerGroup,
      ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG -> classOf[StringDeserializer],
      ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG -> classOf[StringDeserializer]
    )

    KafkaUtils.createDirectStream[String, String](ssc, LocationStrategies.PreferConsistent,
      ConsumerStrategies.Subscribe[String, String](config.getTopics.toSet, kafkaParams))

  }
}