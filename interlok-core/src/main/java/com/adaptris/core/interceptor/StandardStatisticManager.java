package com.adaptris.core.interceptor;

import com.adaptris.annotation.AdapterComponent;
import com.adaptris.annotation.ComponentProfile;
import com.adaptris.core.ProduceException;
import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias("standard-statistic-manager")
@AdapterComponent
@ComponentProfile(summary = "Base statistic manager that handles timeslices.", tag = "interceptor")
public class StandardStatisticManager extends BaseStatisticManager {

  public StandardStatisticManager() {
  }
  
  public StandardStatisticManager(int maxHistoryCount) {
    super(maxHistoryCount);
  }
  
  @Override
  public void produce(InterceptorStatistic interceptorStatistic) throws ProduceException {
    log.trace("Standard statistic manager skipping timeslice producer.");
  }

}
