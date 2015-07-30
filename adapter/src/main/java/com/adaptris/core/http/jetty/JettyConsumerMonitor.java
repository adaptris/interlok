package com.adaptris.core.http.jetty;

public class JettyConsumerMonitor {

  private boolean messageComplete = false;
  
  private long startTime;
  
  private long endTime;

  public long getStartTime() {
    return startTime;
  }

  public void setStartTime(long startTime) {
    this.startTime = startTime;
  }

  public long getEndTime() {
    return endTime;
  }

  public void setEndTime(long endTime) {
    this.endTime = endTime;
  }

  public boolean isMessageComplete() {
    return messageComplete;
  }

  public void setMessageComplete(boolean messageComplete) {
    this.messageComplete = messageComplete;
  }
  
}
