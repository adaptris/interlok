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

/**
 * Abstract base for implementation of {@link TimeSliceCacheProvider}
 * 
 * @author amcgrath
 */
public abstract class TimeSliceAbstractCacheProvider implements TimeSliceCacheProvider {

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
