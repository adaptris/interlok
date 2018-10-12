/*
 * Copyright 2015 Adaptris Ltd.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/

package com.adaptris.core.interceptor;

import java.util.Calendar;
import java.util.List;

import com.adaptris.core.CoreException;
import com.adaptris.core.ProduceException;
import com.adaptris.core.util.LifecycleHelper;

/**
 * Abstract WorkflowInterceptor implementation that exposes metrics via JMX.
 * 
 */
public abstract class MessageMetricsInterceptorImpl extends MetricsInterceptorImpl<MessageStatistic> {
  
  private transient Object chubb = new Object();
  
  private StatisticManager<MessageStatistic> statisticManager;

  public MessageMetricsInterceptorImpl() {
    super();
  }
  
  @Override
  public void init() throws CoreException {
    this.statisticManager().setMaxHistoryCount(this.timesliceHistoryCount());
    LifecycleHelper.init(this.statisticManager());
  }
  
  @Override
  public void start() throws CoreException {
    LifecycleHelper.start(this.statisticManager());
  }

  @Override
  public void stop() {
    LifecycleHelper.stop(this.statisticManager());
  }

  @Override
  public void close() {
    LifecycleHelper.close(this.statisticManager());
  }

  protected void clearStatistics() {
    synchronized (chubb) {
      statisticManager().clear();
    }
  }


  protected void update(StatisticsDelta<MessageStatistic> d) {
    synchronized (chubb) {
      MessageStatistic stat = getCurrentTimeSlice();
      updateCurrentTimeSlice(d.apply(stat));
    }
  }

  private void updateCurrentTimeSlice(MessageStatistic currentTimeSlice) {
    this.statisticManager().updateCurrent(currentTimeSlice);
  }

  private MessageStatistic getCurrentTimeSlice() {
    MessageStatistic timeSlice = null;
    long timeInMillis = Calendar.getInstance().getTimeInMillis();

    if (this.statisticManager().getStats().size() == 0) {
      timeSlice = new MessageStatistic();
      timeSlice.setEndMillis(timeInMillis + timesliceDurationMs());
      this.statisticManager().getStats().add(timeSlice);
    }
    else {
      timeSlice = this.statisticManager().getLatestStat();
      if (timeSlice.getEndMillis() <= timeInMillis) {
        try {
          this.statisticManager().produce(timeSlice);
        } catch (ProduceException e) {
          log.error("Failed to produce timeslice.", e);
        }
        
        timeSlice = new MessageStatistic(timeInMillis + timesliceDurationMs());
        this.statisticManager().getStats().add(timeSlice);
      }
    }
    return timeSlice;
  }
  
  protected StatisticManager<MessageStatistic> statisticManager() {
    if(this.getStatisticManager() != null)
      return this.getStatisticManager();
    else {
      this.setStatisticManager(new StandardStatisticManager<MessageStatistic>(this.timesliceHistoryCount()));
    }
    return this.getStatisticManager();
  }
  
  public StatisticManager<MessageStatistic> getStatisticManager() {
    return statisticManager;
  }

  public void setStatisticManager(StatisticManager<MessageStatistic> statisticManager) {
    this.statisticManager = statisticManager;
  }

  protected List<MessageStatistic> getStats() {
    return this.statisticManager().getStats();
  }

}
