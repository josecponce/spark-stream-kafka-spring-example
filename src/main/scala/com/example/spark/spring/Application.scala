package com.example.spark.spring

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.{ApplicationArguments, ApplicationRunner, SpringApplication}
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Bean

@SpringBootApplication
@EnableConfigurationProperties
class Application {
  @Bean
  def runner(applicationContext: ApplicationContext): ApplicationRunner = new ApplicationRunner {
    override def run(args: ApplicationArguments): Unit = {
      Application.context = applicationContext
//      stream.map(_.value()).foreachRDD((rdd, time) => {
//        val connectionProps = new Properties()
//        connectionProps.setProperty("user", "root")
//        connectionProps.setProperty("password", "password")
//        spark.read.json(rdd).write.mode(SaveMode.Append)
//          .jdbc("jdbc:mysql://node-3/spark", "test", connectionProps)
//      })
//      scc.start()
//      scc.awaitTermination()
    }
  }
}

object Application {
  def main(args: Array[String]): Unit = SpringApplication.run(classOf[Application], args :_ *)
  var context: ApplicationContext = _
}
