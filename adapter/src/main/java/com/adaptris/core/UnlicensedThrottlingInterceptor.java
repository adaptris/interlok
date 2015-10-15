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

package com.adaptris.core;

import java.util.Date;
import java.util.GregorianCalendar;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.adaptris.core.interceptor.ThrottlingInterceptor;
import com.adaptris.core.interceptor.WorkflowInterceptorImpl;
import com.adaptris.core.util.LoggingHelper;
import com.adaptris.util.GuidGenerator;
import com.adaptris.util.TimeInterval;
import com.adaptris.util.license.License;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * UnlicensedThrottlingInterceptor.
 * <p>
 * This interceptor will be created automatically by each workflow, if the adapter is running without an unlimited license. In this
 * case, this interceptor creates a period of time that will allow an unlimited amount of messages to be immediately processed by
 * the workflow. After this time has expired all messages will be throttled. The default values are as follows:
 * </p>
 * <ul>
 * <li>The unlimited period of time - 5 minutes.</li>
 * <li>After the unlimited time, each time slice will be 1 hour in length</li>
 * <li>After the unlimited time, each time slice will allow a maximum of 1 message</li>
 * </ul>
 * 
 * @config unlicensed-throttling-interceptor
 * 
 * @author amcgrath
 * @see ThrottlingInterceptor
 * 
 */
@XStreamAlias("unlicensed-throttling-interceptor")
public final class UnlicensedThrottlingInterceptor extends WorkflowInterceptorImpl {

  /**
   * The unit of time we wish to create each timeslice for.
   */
  private static final String TIME_SLICE_UNIT = "HOURS";

  /**
   * This is the period of time, from when the Adapter starts up, that no throttling restrictions will be in place.
   */
  private static final int UNLIMITED_PERIOD_SECONDS = 300; // 5 minutes.

  /**
   * The time period when messages will be recorded and throttled to a maximum threshold.
   */
  private static final int TIME_SLICE_DURATION = 1; // 1 hour

  /**
   * The threshold that determines how many messages may be allowed to be immediately processed per time slice.
   */
  private static final int MAXIMUM_MESSAGES_PER_TIME_SLICE = 1;

  /**
   * The default cache name used by the throttling interceptor.
   */
  private static final String TIME_SLICE_CACHE_NAME = new GuidGenerator().safeUUID();

  private transient int maximumMessagesPerSlice;
  private transient GregorianCalendar unlimitedEndTime;
  private transient ThrottlingInterceptor limitedInterceptor;
  private transient Logger log = LoggerFactory.getLogger(this.getClass());

  private static transient TimeInterval ONE_HOUR = new TimeInterval(1L, TimeUnit.HOURS.name());
  private TimeInterval timesliceInterval;

  /**
   * Default no-arg constructor Will default the following parameters; UNLIMITED_PERIOD_SECONDS = 300 TIME_SLICE_DURATION = 1
   * TIME_SLICE_UNIT = "HOUR" MAXIMUM_MESSAGES_PER_TIME_SLICE = 1 TIME_SLICE_CACHE_NAME = new GuidGenerator().safeUUID()
   */
  public UnlicensedThrottlingInterceptor() {
    this(UNLIMITED_PERIOD_SECONDS, ONE_HOUR, MAXIMUM_MESSAGES_PER_TIME_SLICE, TIME_SLICE_CACHE_NAME);
  }

  UnlicensedThrottlingInterceptor(int unlimitedPeriodSeconds, TimeInterval interval, int maximumMessages, String cacheName) {
    timesliceInterval = interval;
    maximumMessagesPerSlice = maximumMessages;

    limitedInterceptor = new ThrottlingInterceptor();
    limitedInterceptor.setTimeSliceInterval(timesliceInterval);
    limitedInterceptor.setMaximumMessages(maximumMessagesPerSlice);
    limitedInterceptor.setCacheName(cacheName);

    unlimitedEndTime = new GregorianCalendar();
    unlimitedEndTime.setTime(new Date());
    unlimitedEndTime.add(GregorianCalendar.SECOND, unlimitedPeriodSeconds);
  }

  @Override
  public void workflowStart(AdaptrisMessage inputMsg) {
    if (new Date().after(unlimitedEndTime.getTime())) {
      limitedInterceptor.workflowStart(inputMsg);
    }
  }

  @Override
  public void workflowEnd(AdaptrisMessage inputMsg, AdaptrisMessage outputMsg) {
    if (new Date().after(unlimitedEndTime.getTime())) {
      limitedInterceptor.workflowEnd(inputMsg, outputMsg);
    }

  }

  @Override
  public void init() throws CoreException {
    limitedInterceptor.init();
  }

  @Override
  public void start() throws CoreException {
    log.info("Workflow [{}] will be throttled after {}", LoggingHelper.friendlyName(parentWorkflow()), unlimitedEndTime());
    limitedInterceptor.start();
  }

  @Override
  public void stop() {
    limitedInterceptor.stop();
  }

  @Override
  public void close() {
    limitedInterceptor.close();
  }

  ThrottlingInterceptor getLimitedInterceptor() {
    return limitedInterceptor;
  }

  Date unlimitedEndTime() {
    return unlimitedEndTime.getTime();
  }

  public boolean isEnabled(License license) {
    return true;
  }
}
