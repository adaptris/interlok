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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract WorkflowInterceptor implementation that exposes metrics via JMX.
 * 
 */
public abstract class MessageMetricsInterceptorImpl extends MetricsInterceptorImpl<MessageStatistic> {

  protected transient Logger log = LoggerFactory.getLogger(this.getClass().getName());
  
  // * Internal cache array, containing timeslices. Will never have more
  // * time slices than that dictated by time slice history count.
  private transient List<MessageStatistic> statistics;
  private transient Object chubb = new Object();

  public MessageMetricsInterceptorImpl() {
    super();
    statistics = new MaxCapacityList<MessageStatistic>();
  }

  protected void clearStatistics() {
    synchronized (chubb) {
      statistics.clear();
    }
  }


  protected void update(StatisticsDelta<MessageStatistic> d) {
    synchronized (chubb) {
      MessageStatistic stat = getCurrentTimeSlice();
      updateCurrentTimeSlice(d.apply(stat));
    }
  }

  private void updateCurrentTimeSlice(MessageStatistic currentTimeSlice) {
    statistics.set(statistics.size() - 1, currentTimeSlice);
  }

  private MessageStatistic getCurrentTimeSlice() {
    MessageStatistic timeSlice = null;
    long timeInMillis = Calendar.getInstance().getTimeInMillis();

    if (statistics.size() == 0) {
      timeSlice = new MessageStatistic();
      timeSlice.setEndMillis(timeInMillis + timesliceDurationMs());
      statistics.add(timeSlice);
    }
    else {
      timeSlice = getLatestTimeSlice();
      if (timeSlice.getEndMillis() <= timeInMillis) {
        timeSlice = new MessageStatistic(timeInMillis + timesliceDurationMs());
        statistics.add(timeSlice);
      }
    }
    return timeSlice;
  }

  private MessageStatistic getLatestTimeSlice() {
    if (statistics.size() == 0) {
      return null;
    }
    else {
      return statistics.get(statistics.size() - 1);
    }
  }

  protected List<MessageStatistic> getStats() {
    return statistics;
  }

}
