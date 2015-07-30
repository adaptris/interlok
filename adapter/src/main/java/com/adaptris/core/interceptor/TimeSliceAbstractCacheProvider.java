package com.adaptris.core.interceptor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract base for implementation of {@link TimeSliceCacheProvider}
 * 
 * @author amcgrath
 */
public abstract class TimeSliceAbstractCacheProvider implements TimeSliceCacheProvider {

  private transient Logger log = LoggerFactory.getLogger(this.getClass().getName());

  protected static final String CURRENT_TIME_SLICE_END_KEY = "ts_end";
  protected static final String CURRENT_TIME_SLICE_MESSAGE_COUNT_KEY = "ts_count";

  private transient long timeSliceDurationMilliseconds;

  public TimeSliceAbstractCacheProvider() {
  }

  protected long timeSliceDurationMilliseconds() {
		return timeSliceDurationMilliseconds;
  }

  protected void setTimeSliceDurationMilliseconds(long timeSliceDurationMilliseconds) {
		this.timeSliceDurationMilliseconds = timeSliceDurationMilliseconds;
  }
}
