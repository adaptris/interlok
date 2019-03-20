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

import java.util.concurrent.TimeUnit;

import com.adaptris.core.CoreException;
import com.adaptris.util.NumberUtils;
import com.adaptris.util.TimeInterval;

/**
 * Abstract WorkflowInterceptor implementation that captures historical data.
 * 
 */
public abstract class MetricsInterceptorImpl<T> extends WorkflowInterceptorImpl {

  private static final TimeInterval DEFAULT_TIMESLICE_DURATION = new TimeInterval(10L, TimeUnit.SECONDS);
  protected static final int DEFAULT_TIMESLICE_HISTORY_COUNT = 100;

  private TimeInterval timesliceDuration;
  // * Optional - Defaults to 100
  // * The number of timeslices we should persist for history sake
  private Integer timesliceHistoryCount;

  public MetricsInterceptorImpl() {
    super();
    timesliceHistoryCount = 100;
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


  protected int timesliceHistoryCount() {
    return NumberUtils.toIntDefaultIfNull(getTimesliceHistoryCount(),
        DEFAULT_TIMESLICE_HISTORY_COUNT);
  }

  public Integer getTimesliceHistoryCount() {
    return timesliceHistoryCount;
  }

  /**
   * Set the number of timeslices to keep.
   * 
   * @param s the number of timeslices to keep (default 100)
   */
  public void setTimesliceHistoryCount(Integer s) {
    this.timesliceHistoryCount = s;
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
    return TimeInterval.toMillisecondsDefaultIfNull(getTimesliceDuration(),
        DEFAULT_TIMESLICE_DURATION);
  }

  public interface StatisticsDelta<E extends InterceptorStatistic> {
    public E apply(E currentStat);
  }
}
