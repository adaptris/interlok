package com.adaptris.core.interceptor;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public abstract class MetadataMetricsInterceptorImpl extends MetricsInterceptorImpl {
  private transient List<MetadataStatistic> statistics;

  protected MetadataMetricsInterceptorImpl() {
    statistics = new ArrayList<MetadataStatistic>();
  }

  protected void updateCurrent(MetadataStatistic currentTimeSlice) {
    statistics.set(statistics.size() - 1, currentTimeSlice);
  }

  protected List<MetadataStatistic> getStats() {
    return statistics;
  }

  protected MetadataStatistic getCurrentStat() {
    MetadataStatistic timeSlice = null;
    long timeInMillis = Calendar.getInstance().getTimeInMillis();

    if (statistics.size() == 0) {
      timeSlice = new MetadataStatistic();
      timeSlice.setEndMillis(timeInMillis + timesliceDurationMs());
      addNewStat(timeSlice);
    }
    else {
      timeSlice = getLatestStat();
      if (timeSlice.getEndMillis() <= timeInMillis) {
        timeSlice = new MetadataStatistic(timeInMillis + timesliceDurationMs());
        addNewStat(timeSlice);
      }
    }
    return timeSlice;
  }

  protected void addNewStat(MetadataStatistic timeSlice) {
    if (statistics.size() == getTimesliceHistoryCount() && statistics.size() > 0) {
      statistics.remove(0);
    }
    statistics.add(timeSlice);
  }

  protected MetadataStatistic getLatestStat() {
    if (statistics.size() == 0) {
      return null;
    }
    else {
      return statistics.get(statistics.size() - 1);
    }
  }

}
