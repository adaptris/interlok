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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.adaptris.core.CoreException;
import com.adaptris.util.TimeInterval;

/**
 * Abstract WorkflowInterceptor implementation that captures historical data.
 * 
 */
public abstract class MetricsInterceptorImpl extends WorkflowInterceptorImpl {

  protected transient Logger log = LoggerFactory.getLogger(this.getClass().getName());
  private static final TimeInterval DEFAULT_TIMESLICE_DURATION = new TimeInterval(10L, TimeUnit.SECONDS);
  private TimeInterval timesliceDuration;
  // * Optional - Defaults to 100
  // * The number of timeslices we should persist for history sake
  private int timesliceHistoryCount;

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

  public int getTimesliceHistoryCount() {
    return timesliceHistoryCount;
  }

  /**
   * Set the number of timeslices to keep.
   * 
   * @param timesliceHistoryCount the number of timeslices to keep (default 100)
   */
  public void setTimesliceHistoryCount(int timesliceHistoryCount) {
    this.timesliceHistoryCount = timesliceHistoryCount;
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
