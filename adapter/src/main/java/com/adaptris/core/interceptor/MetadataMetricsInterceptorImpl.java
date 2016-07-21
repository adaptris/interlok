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

public abstract class MetadataMetricsInterceptorImpl extends MetricsInterceptorImpl<MetadataStatistic> {
  private transient List<MetadataStatistic> statistics;
  private transient Object chubb = new Object();

  protected MetadataMetricsInterceptorImpl() {
    statistics = new MaxCapacityList<MetadataStatistic>();
  }

  protected void clearStatistics() {
    synchronized (chubb) {
      statistics.clear();
    }
  }

  protected void update(StatisticsDelta<MetadataStatistic> d) {
    synchronized (chubb) {
      MetadataStatistic stat = getCurrentStat();
      updateCurrent(d.apply(stat));
    }
  }

  protected void updateCurrent(MetadataStatistic currentTimeSlice) {
    statistics.set(statistics.size() - 1, currentTimeSlice);
  }

  protected List<MetadataStatistic> getStats() {
    return statistics;
  }

  private MetadataStatistic getCurrentStat() {
    MetadataStatistic timeSlice = null;
    long timeInMillis = Calendar.getInstance().getTimeInMillis();

    if (statistics.size() == 0) {
      timeSlice = new MetadataStatistic();
      timeSlice.setEndMillis(timeInMillis + timesliceDurationMs());
      statistics.add(timeSlice);
    }
    else {
      timeSlice = getLatestStat();
      if (timeSlice.getEndMillis() <= timeInMillis) {
        timeSlice = new MetadataStatistic(timeInMillis + timesliceDurationMs());
        statistics.add(timeSlice);
      }
    }
    return timeSlice;
  }


  private MetadataStatistic getLatestStat() {
    if (statistics.size() == 0) {
      return null;
    }
    else {
      return statistics.get(statistics.size() - 1);
    }
  }

}
