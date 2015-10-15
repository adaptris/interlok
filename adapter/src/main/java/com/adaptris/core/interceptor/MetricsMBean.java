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
