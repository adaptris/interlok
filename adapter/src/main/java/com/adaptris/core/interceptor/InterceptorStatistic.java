package com.adaptris.core.interceptor;



public abstract class InterceptorStatistic {

  private long endMillis;
  private long startMillis;

  public InterceptorStatistic() {
    setStartMillis(System.currentTimeMillis());
  }


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

}
