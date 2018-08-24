package com.example.spark.spring;

import lombok.var;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.streaming.Duration;
import org.apache.spark.streaming.api.java.JavaInputDStream;
import org.apache.spark.streaming.api.java.JavaStreamingContext;
import org.apache.spark.streaming.kafka010.ConsumerStrategies;
import org.apache.spark.streaming.kafka010.KafkaUtils;
import org.apache.spark.streaming.kafka010.LocationStrategies;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Collections;
import java.util.HashMap;

@Configuration
public class IOCConfiguration {

    @Bean
    public SparkConf sparkConf() {
        return new SparkConf(true)
                .set("spark.submit.deployMode", "client");
    }

    @Bean
    public JavaSparkContext sc(SparkConf sparkConf) {
        return new JavaSparkContext("local[4]", "spark-kafka-exapmle", sparkConf);
    }

    @Bean
    public SparkSession session(JavaSparkContext sc) {
        return new SparkSession(sc.sc());
    }

    @Bean
    public JavaStreamingContext scc(JavaSparkContext sc) {
        return new JavaStreamingContext(sc, new Duration(2000));
    }

    @Bean
    public JavaInputDStream<ConsumerRecord<String, String>> stream(JavaStreamingContext ssc) {
        var kafkaParams = new HashMap<String, Object>();
        kafkaParams.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "node-3:9092");
        kafkaParams.put(ConsumerConfig.GROUP_ID_CONFIG, "spark.test.group");
        kafkaParams.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        kafkaParams.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);

        return KafkaUtils.createDirectStream(ssc, LocationStrategies.PreferConsistent(),
                ConsumerStrategies.Subscribe(Collections.singletonList("spark.test"), kafkaParams));
    }
}
