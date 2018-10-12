package com.adaptris.core.interceptor;

import java.util.List;

import com.adaptris.core.ComponentLifecycle;
import com.adaptris.core.ProduceException;

public interface StatisticManager extends ComponentLifecycle {

  public void produce(InterceptorStatistic interceptorStatistic) throws ProduceException;
  
  public void clear();
  
  public void updateCurrent(InterceptorStatistic currentTimeSlice);
    
  public InterceptorStatistic getLatestStat();
  
  public List<InterceptorStatistic> getStats();
  
  public void setMaxHistoryCount(int maxHistoryCount);
  
}
