package com.adaptris.core.interceptor;

import java.util.Properties;

import com.thoughtworks.xstream.annotations.XStreamAlias;

public @XStreamAlias("interceptor-statistic")
class SerializableStatistic {
  
  private long endMillis;
  private long startMillis;
  private int totalMessageCount;
  private long totalMessageSize;
  private int totalMessageErrorCount;
  private Properties metadataStatistics;
  public long getEndMillis() {
    return endMillis;
  }
  public void setEndMillis(long endMillis) {
    this.endMillis = endMillis;
  }
  public long getStartMillis() {
    return startMillis;
  }
  public void setStartMillis(long startMillis) {
    this.startMillis = startMillis;
  }
  public int getTotalMessageCount() {
    return totalMessageCount;
  }
  public void setTotalMessageCount(int totalMessageCount) {
    this.totalMessageCount = totalMessageCount;
  }
  public long getTotalMessageSize() {
    return totalMessageSize;
  }
  public void setTotalMessageSize(long totalMessageSize) {
    this.totalMessageSize = totalMessageSize;
  }
  public int getTotalMessageErrorCount() {
    return totalMessageErrorCount;
  }
  public void setTotalMessageErrorCount(int totalMessageErrorCount) {
    this.totalMessageErrorCount = totalMessageErrorCount;
  }
  public Properties getMetadataStatistics() {
    return metadataStatistics;
  }
  public void setMetadataStatistics(Properties metadataStatistics) {
    this.metadataStatistics = metadataStatistics;
  }
  
}