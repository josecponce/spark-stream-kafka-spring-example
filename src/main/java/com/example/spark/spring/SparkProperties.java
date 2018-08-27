package com.example.spark.spring;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
@ConfigurationProperties("spark")
public class SparkProperties {
  private String deployMode;
  private String master;
  private String appName;
  private BigDecimal offHeapMemoryGb = BigDecimal.ZERO;

    public String getDeployMode() {
        return deployMode;
    }

    public void setDeployMode(String deployMode) {
        this.deployMode = deployMode;
    }

    public String getMaster() {
        return master;
    }

    public void setMaster(String master) {
        this.master = master;
    }

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public BigDecimal getOffHeapMemoryGb() {
        return offHeapMemoryGb;
    }

    public void setOffHeapMemoryGb(BigDecimal offHeapMemoryGb) {
        this.offHeapMemoryGb = offHeapMemoryGb;
    }
}
