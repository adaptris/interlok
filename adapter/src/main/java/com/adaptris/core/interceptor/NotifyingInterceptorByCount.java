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

import java.util.Calendar;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import javax.validation.Valid;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.util.TimeInterval;

/**
 * 
 * @since 3.0.4
 */
public abstract class NotifyingInterceptorByCount extends NotifyingInterceptor {

  private static final TimeInterval DEFAULT_TIMESLICE_DURATION = new TimeInterval(1L,
      TimeUnit.MINUTES);

  /**
   * Key within the properties containing the total message count
   */
  public static final String KEY_MESSAGE_COUNT = "totalMessageCount";
  /**
   * Key within the properties containing the total number of errors.
   */
  public static final String KEY_MESSAGE_ERROR = "totalErrors";
  /**
   * Key within the properties containing the total size of errors.
   */
  public static final String KEY_MESSAGE_SIZE = "totalSize";
  /**
   * Key within the properties containing the end time for the timeslice.
   */
  public static final String KEY_TIMESLICE_END = "timesliceEndMs";

  /**
   * Key within the properties containing the end time for the timeslice.
   */
  public static final String KEY_TIMESLICE_START = "timesliceStartMs";

  @Valid
  private TimeInterval timesliceDuration;
  private transient MessageStatistic currentTimeSlice;


  protected MessageStatistic getCurrentTimeSlice() {
    long timeInMillis = Calendar.getInstance().getTimeInMillis();
    if (currentTimeSlice == null) {
      currentTimeSlice = new MessageStatistic();
      currentTimeSlice.setEndMillis(timeInMillis + timesliceDurationMs());
    } else {
      if (currentTimeSlice.getEndMillis() <= timeInMillis) {
        currentTimeSlice = new MessageStatistic(timeInMillis + timesliceDurationMs());
      }
    }
    return currentTimeSlice;
  }


  protected static Properties asProperties(MessageStatistic stat) {
    Properties p = new Properties();
    p.setProperty(KEY_MESSAGE_COUNT, String.valueOf(stat.getTotalMessageCount()));
    p.setProperty(KEY_MESSAGE_ERROR, String.valueOf(stat.getTotalMessageErrorCount()));
    p.setProperty(KEY_MESSAGE_SIZE, String.valueOf(stat.getTotalMessageSize()));
    p.setProperty(KEY_TIMESLICE_END, String.valueOf(stat.getEndMillis()));
    p.setProperty(KEY_TIMESLICE_START, String.valueOf(stat.getStartMillis()));
    return p;
  }

  protected MessageStatistic getAndIncrementStatistic(AdaptrisMessage inputMsg, AdaptrisMessage outputMsg) {
    MessageStatistic currentTimeSlice = getCurrentTimeSlice();
    currentTimeSlice.setTotalMessageCount(currentTimeSlice.getTotalMessageCount() + 1);
    currentTimeSlice.setTotalMessageSize(currentTimeSlice.getTotalMessageSize() + inputMsg.getSize());
    if (!wasSuccessful(inputMsg, outputMsg)) {
      currentTimeSlice.setTotalMessageErrorCount(currentTimeSlice.getTotalMessageErrorCount() + 1);
    }
    return currentTimeSlice;
  }

  public TimeInterval getTimesliceDuration() {
    return timesliceDuration;
  }

  /**
   * Set the duration of each timeslice for gathering.
   * 
   * @param timesliceDuration the timeslice duration, default is 1 minute if not explicitly
   *        specified.
   */
  public void setTimesliceDuration(TimeInterval timesliceDuration) {
    this.timesliceDuration = timesliceDuration;
  }

  long timesliceDurationMs() {
    return getTimesliceDuration() != null ? getTimesliceDuration().toMilliseconds()
        : DEFAULT_TIMESLICE_DURATION.toMilliseconds();
  }

}
