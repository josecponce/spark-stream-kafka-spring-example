package com.example.spark.spring;

import org.apache.hadoop.io.serializer.Deserializer;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties("kafka")
public class KafkaProperties {
  private String bootstrapServers;
  private String consumerGroup;
  private String[] topics;
  private Class<Deserializer> keyDeserializer;
  private Class<Deserializer> valueDeserializer;
  private Boolean enabled;

  public String getBootstrapServers() {
    return bootstrapServers;
  }

  public void setBootstrapServers(String bootstrapServers) {
    this.bootstrapServers = bootstrapServers;
  }

  public String getConsumerGroup() {
    return consumerGroup;
  }

  public void setConsumerGroup(String consumerGroup) {
    this.consumerGroup = consumerGroup;
  }

  public String[] getTopics() {
    return topics;
  }

  public void setTopics(String[] topics) {
    this.topics = topics;
  }

  public Class<Deserializer> getKeyDeserializer() {
    return keyDeserializer;
  }

  public void setKeyDeserializer(Class<Deserializer> keyDeserializer) {
    this.keyDeserializer = keyDeserializer;
  }

  public Class<Deserializer> getValueDeserializer() {
    return valueDeserializer;
  }

  public void setValueDeserializer(Class<Deserializer> valueDeserializer) {
    this.valueDeserializer = valueDeserializer;
  }

  public Boolean getEnabled() {
    return enabled;
  }

  public void setEnabled(Boolean enabled) {
    this.enabled = enabled;
  }
}
