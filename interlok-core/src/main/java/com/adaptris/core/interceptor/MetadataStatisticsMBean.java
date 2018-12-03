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
 * Management bean interface for metadata totals statistics.
 * 
 */
public interface MetadataStatisticsMBean extends MetricsMBean {

  /**
   * Get all the statistics hosted by this management bean.
   * @return a copy of all the statistics.
   */
  List<MetadataStatistic> getStatistics() throws CoreException;

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
  List<MetadataStatistic> getStatistics(int fromIndex, int toIndex) throws CoreException;

}
