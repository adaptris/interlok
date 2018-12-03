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

public abstract class MetadataMetricsInterceptorImpl extends MetricsInterceptorImpl<MetadataStatistic> {
  
  private transient Object chubb = new Object();
  
  private StatisticManager statisticManager;

  protected MetadataMetricsInterceptorImpl() {
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

  protected void update(StatisticsDelta<MetadataStatistic> d) {
    synchronized (chubb) {
      InterceptorStatistic stat = getCurrentStat();
      updateCurrent(d.apply((MetadataStatistic) stat));
    }
  }

  protected void updateCurrent(MetadataStatistic currentTimeSlice) {
    this.statisticManager().updateCurrent(currentTimeSlice);
  }

  protected List<InterceptorStatistic> getStats() {
    return this.statisticManager().getStats();
  }

  private InterceptorStatistic getCurrentStat() {
    InterceptorStatistic timeSlice = null;
    long timeInMillis = Calendar.getInstance().getTimeInMillis();

    if (this.statisticManager().getStats().size() == 0) {
      timeSlice = new MetadataStatistic();
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
        
        timeSlice = new MetadataStatistic(timeInMillis + timesliceDurationMs());
        this.statisticManager().getStats().add(timeSlice);
      }
    }
    return timeSlice;
  }

  protected StatisticManager statisticManager() {
    if(this.getStatisticManager() != null)
      return this.getStatisticManager();
    else {
      this.setStatisticManager(new StandardStatisticManager(this.timesliceHistoryCount()));
    }
    return this.getStatisticManager();
  }
  
  public StatisticManager getStatisticManager() {
    return statisticManager;
  }

  public void setStatisticManager(StatisticManager statisticManager) {
    this.statisticManager = statisticManager;
  }

}
