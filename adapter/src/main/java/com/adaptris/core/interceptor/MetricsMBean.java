package com.adaptris.core.interceptor;

import com.adaptris.core.runtime.ChildRuntimeInfoComponentMBean;

/**
 * Common behaviour for all metrics MBeans.
 * 
 * @author lchan
 * 
 */
public interface MetricsMBean extends ChildRuntimeInfoComponentMBean {

  /**
   * Get the current number of timeslices stored.
   * 
   * @return the number of timeslices.
   */
  int getNumberOfTimeSlices();

  /**
   * Get the duration of each timeslice.
   * 
   * @return the duration of each timeslice
   */
  int getTimeSliceDurationSeconds();

  /**
   * Get the end time in millisecond of a given timeslice.
   * 
   * @param index the index of the timeslice
   * @return the end time in millisecond.
   * @deprecated since 3.0.3
   */
  @Deprecated
  long getEndMillisForTimeSliceIndex(int index);
}
