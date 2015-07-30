package com.adaptris.core.interceptor;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.adaptris.core.CoreException;
import com.adaptris.util.TimeInterval;

/**
 * Abstract WorkflowInterceptor implementation that exposes metrics via JMX.
 * 
 * <p>
 * In the adapter configuration file this class is aliased as <b>message-metrics-interceptor</b> which is the preferred alternative
 * to the fully qualified classname when building your configuration.
 * </p>
 */
public abstract class MessageMetricsInterceptorImpl extends WorkflowInterceptorImpl {

  protected transient Logger log = LoggerFactory.getLogger(this.getClass().getName());
  private static final TimeInterval DEFAULT_TIMESLICE_DURATION = new TimeInterval(10L, TimeUnit.SECONDS);
  protected static final int DEFAULT_TIMESLICE_HISTORY_COUNT = 100;
  
  private TimeInterval timesliceDuration;
  // * Optional - Defaults to 100
  // * The number of timeslices we should persist for history sake
  private Integer timesliceHistoryCount;
  // * Internal cache array, containing timeslices. Will never have more
  // * time slices than that dictated by time slice history count.
  private transient List<MessageStatistic> cacheArray;

  public MessageMetricsInterceptorImpl() {
    super();
    cacheArray = new ArrayList<MessageStatistic>();
  }

  @Override
  public void init() throws CoreException {
  }

  @Override
  public void start() throws CoreException {
  }

  @Override
  public void stop() {
  }

  @Override
  public void close() {
  }

  protected void updateCurrentTimeSlice(MessageStatistic currentTimeSlice) {
    cacheArray.set(cacheArray.size() - 1, currentTimeSlice);
  }

  protected MessageStatistic getCurrentTimeSlice() {
    MessageStatistic timeSlice = null;
    long timeInMillis = Calendar.getInstance().getTimeInMillis();

    if (cacheArray.size() == 0) {
      timeSlice = new MessageStatistic();
      timeSlice.setEndMillis(timeInMillis + timesliceDurationMs());
      addNewTimeSlice(timeSlice);
    }
    else {
      timeSlice = getLatestTimeSlice();
      if (timeSlice.getEndMillis() <= timeInMillis) {
        timeSlice = new MessageStatistic(timeInMillis + timesliceDurationMs());
        addNewTimeSlice(timeSlice);
      }
    }
    return timeSlice;
  }

  protected void addNewTimeSlice(MessageStatistic timeSlice) {
    if (cacheArray.size() == timesliceHistoryCount() && cacheArray.size() > 0) {
      cacheArray.remove(0);
    }
    cacheArray.add(timeSlice);
  }

  protected MessageStatistic getLatestTimeSlice() {
    if (cacheArray.size() == 0) {
      return null;
    }
    else {
      return cacheArray.get(cacheArray.size() - 1);
    }
  }

  int timesliceHistoryCount() {
    return getTimesliceHistoryCount() != null ? getTimesliceHistoryCount().intValue() : DEFAULT_TIMESLICE_HISTORY_COUNT;
  }

  public Integer getTimesliceHistoryCount() {
    return timesliceHistoryCount;
  }

  /**
   * Set the number of timeslices to keep.
   * 
   * @param timesliceHistoryCount the number of timeslices to keep (default 100)
   */
  public void setTimesliceHistoryCount(Integer timesliceHistoryCount) {
    this.timesliceHistoryCount = timesliceHistoryCount;
  }

  protected List<MessageStatistic> getCacheArray() {
    return cacheArray;
  }

  protected void setCacheArray(ArrayList<MessageStatistic> cacheArray) {
    this.cacheArray = cacheArray;
  }

  public TimeInterval getTimesliceDuration() {
    return timesliceDuration;
  }

  /**
   * Set the duration of each timeslice for metrics gathering.
   * 
   * @param timesliceDuration the timeslice duration, default is 10 seconds if not explicitly specified.
   */
  public void setTimesliceDuration(TimeInterval timesliceDuration) {
    this.timesliceDuration = timesliceDuration;
  }

  long timesliceDurationMs() {
    return getTimesliceDuration() != null ? getTimesliceDuration().toMilliseconds() : DEFAULT_TIMESLICE_DURATION.toMilliseconds();
  }

}
