package com.example.spark.spring;

import lombok.extern.slf4j.Slf4j;
import lombok.var;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.logging.log4j.util.PropertiesUtil;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SaveMode;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.streaming.api.java.JavaInputDStream;
import org.apache.spark.streaming.api.java.JavaStreamingContext;
import org.springframework.beans.propertyeditors.PropertiesEditor;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.util.Properties;

@SpringBootApplication
@Slf4j
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @Bean
    public ApplicationRunner runner(SparkSession spark, JavaStreamingContext scc,
                                    JavaInputDStream<ConsumerRecord<String, String>> stream) {
        return args -> {
            stream.foreachRDD((rdd, time) -> {
                var connectionProps = new Properties();
                connectionProps.setProperty("user", "root");
                connectionProps.setProperty("password", "password");
                Dataset<Row> dataFrame = spark.createDataFrame(
                        rdd.map(record -> new Entity(record.value())), Entity.class);
                dataFrame.write().mode(SaveMode.Append).jdbc("jdbc:mysql://node-3/spark", "test", connectionProps);
            });
            scc.start();
            scc.awaitTermination();
        };
    }
}
