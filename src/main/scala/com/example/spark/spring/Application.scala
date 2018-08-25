package com.example.spark.spring

import java.util.Properties

import lombok.extern.slf4j.Slf4j
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.spark.sql.{SaveMode, SparkSession}
import org.apache.spark.streaming.StreamingContext
import org.apache.spark.streaming.dstream.InputDStream
import org.springframework.boot.{ApplicationArguments, ApplicationRunner, SpringApplication}
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.context.annotation.Bean

@SpringBootApplication
class Application {
  @Bean
  def runner(spark: SparkSession, scc: StreamingContext,
             stream: InputDStream[ConsumerRecord[String, String]]): ApplicationRunner = new ApplicationRunner {
    override def run(args: ApplicationArguments): Unit = {
      stream.foreachRDD((rdd, time) => {
        val connectionProps = new Properties()
        connectionProps.setProperty("user", "root")
        connectionProps.setProperty("password", "password")
        val dataFrame = spark.createDataFrame(
          rdd.map(record => Entity(record.value())), classOf[Entity])
        dataFrame.write.mode(SaveMode.Append).jdbc("jdbc:mysql://node-3/spark", "test", connectionProps)
      })
      scc.start()
      scc.awaitTermination()
    }
  }
}

object Application {
  def main(args: Array[String]): Unit = SpringApplication.run(classOf[Application])
}
