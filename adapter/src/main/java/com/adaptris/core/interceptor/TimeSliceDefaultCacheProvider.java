package com.adaptris.core.interceptor;

import java.util.Calendar;

import com.adaptris.core.CoreException;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * The default implementation that handles the current time slice persistence.
 * <p>
 * This implementation uses a singleton (TimeSlicePersistence) which maintains a list of cache names and for each cache name a cache
 * of time slices. This provider exposes methods that will call into the persistence for common usage; get and put.
 * </p>
 * 
 * @config time-slice-default-cache-provider
 * @author amcgrath
 */
@XStreamAlias("time-slice-default-cache-provider")
public class TimeSliceDefaultCacheProvider extends TimeSliceAbstractCacheProvider {

  private TimeSlicePersistence persistence;

  @Override
  public void start() throws CoreException {
      persistence = TimeSlicePersistence.getInstance();
  }

  @Override
  public void init() throws CoreException {
  }

  @Override
  public void stop() {
  }

  @Override
  public TimeSlice get(String cacheName) {
      Calendar cal = Calendar.getInstance();
      TimeSlice lastTimeSlice = persistence.getCurrentTimeSlice(cacheName);

      if(lastTimeSlice == null) {
          lastTimeSlice = new TimeSlice();
			lastTimeSlice.setEndMillis(cal.getTimeInMillis() + timeSliceDurationMilliseconds());

          persistence.updateCurrentTimeSlice(cacheName, lastTimeSlice);
      }
      if(lastTimeSlice.getEndMillis() < cal.getTimeInMillis()) {
          lastTimeSlice = new TimeSlice();
			lastTimeSlice.setEndMillis(cal.getTimeInMillis() + timeSliceDurationMilliseconds());

          persistence.updateCurrentTimeSlice(cacheName, lastTimeSlice);
      }

      return lastTimeSlice;
  }

  @Override
  public void update(String cacheName, TimeSlice timeslice) {
      persistence.updateCurrentTimeSlice(cacheName, timeslice);
  }

  public TimeSlicePersistence getPersistence() {
      return persistence;
  }
}
