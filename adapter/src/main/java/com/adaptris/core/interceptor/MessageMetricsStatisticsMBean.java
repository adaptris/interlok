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

import java.util.List;

import com.adaptris.core.CoreException;


/**
 * Management bean interface for message statistics.
 * 
 * @author amcgrath
 */
public interface MessageMetricsStatisticsMBean extends MetricsMBean {

  /**
   * Get the number of messages for a given timeslice.
   * 
   * @param index the index of the timeslice.
   * @return the number of messages.
   * @deprecated since 3.0.3 use {@link #getStatistics()} instead for efficiency when dealing with remote MBeans
   */
  @Deprecated
	int getNumberOfMessagesForTimeSliceIndex(int index);

  /**
   * Get the total size of messages processed in a given timeslice
   * 
   * @param index the index of the timeslice
   * @return the total size of messages.
   * @deprecated since 3.0.3 use {@link #getStatistics()} instead for efficiency when dealing with remote MBeans.
   */
  @Deprecated
	long getTotalSizeOfMessagesForTimeSliceIndex(int index);

  /**
   * Get the total number of messages that had an error in a given timeslice.
   * 
   * @param index the index of the timeslice
   * @return the total of messages that errored.
   * @deprecated since 3.0.3 use {@link #getStatistics()} instead for efficiency when dealing with remote MBeans.
   */
  @Deprecated
	int getNumberOfErrorMessagesForTimeSliceIndex(int index);


  /**
   * Get a simple string representation of stats.
   *
   * @return a simple string
   */
	String getTotalStringStats();

  /**
   * Get all the statistics hosted by this management bean.
   * 
   * @return a copy of all the statistics.
   * @since 3.0.3
   */
  List<MessageStatistic> getStatistics() throws CoreException;

  /**
   * Returns a view of the portion of this list between the specified {@code fromIndex}, inclusive,
   * and {@code toIndex}, exclusive.
   * <p>
   * Although similar to {@link List#subList(int, int)}; it is designed to return you a copy of the
   * list in question; any changes to the returned list will not be reflected in the underlying
   * list.
   * </p>
   * 
   * @param fromIndex
   * @param toIndex
   * @return a new list containing the statistics.
   * @since 3.0.3
   */
  List<MessageStatistic> getStatistics(int fromIndex, int toIndex) throws CoreException;

}
