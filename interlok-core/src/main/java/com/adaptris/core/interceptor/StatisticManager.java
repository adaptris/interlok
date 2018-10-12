package com.adaptris.core.interceptor;

import java.util.List;

import com.adaptris.core.ComponentLifecycle;
import com.adaptris.core.ProduceException;

public interface StatisticManager<T extends InterceptorStatistic> extends ComponentLifecycle {

  public void produce(T interceptorStatistic) throws ProduceException;
  
  public void clear();
  
  public void updateCurrent(T currentTimeSlice);
    
  public T getLatestStat();
  
  public List<T> getStats();
  
  public void setMaxHistoryCount(int maxHistoryCount);
  
}
