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

import static org.apache.commons.lang.StringUtils.isEmpty;

import java.util.Calendar;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.NotBlank;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.adaptris.annotation.AutoPopulated;
import com.adaptris.annotation.ComponentProfile;
import com.adaptris.annotation.DisplayOrder;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.CoreException;
import com.adaptris.core.util.ManagedThreadFactory;
import com.adaptris.util.TimeInterval;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * The throttling interceptor will attempt to limit the number of messages that are processed by an adapter over a given time slot.
 * <p>
 * A time slice is measured through the {@link #setTimeSliceInterval(TimeInterval)}. Once the first message is processed by this
 * interceptor, a time slice is created. The start of the time slice is the current time, the end of the time slice is set as the
 * current time + the number of time units in the configuration. Every message processed by the interceptor will start a check to
 * see if the current time slice is out of date. If it is out of date, then a new time slice is created. If the current time slice
 * is not out of date, then we check to make sure the maximum messages have not already passed through. If we have not yet reached
 * the maximum messages for the current time slice, we simply increment the message count on the time slice. If we have reached the
 * maximum messages, then we will delay this processing thread until the current time slice is finished, and finally a new time
 * slice is created.
 * </p>
 * <p>
 * Any given instance of the ThrottlingInterceptor can be set to work with a particular cache name. This allows you to configure a
 * throttling interceptor on one or more workflows to all use the same time slice or use a completely different time slice.
 * </p>
 * <p>
 * For example, you can set workflow A and workflow B to both have a throttling interceptor, where both interceptors use the same
 * cache name. This will have the effect of restricting the sum of messages passing through both workflows against the same time
 * slice statistics.
 * </p>
 * <p>
 * Likewise, you may have a third workflow with an interceptor configured to use a different cache name. This means messages passing
 * through the third workflow will not have the same throttling restrictions as those on workflow A and B.
 * </p>
 * The following properties may be set in configuration;
 * <ul>
 * <li>time-slice-interval - This is the number of time units each time slice will use. The default value is 5.</li>
 * <li>maximum-messages - The maximum amount of messages allowed to pass through the current time slice The default value is
 * {@link Integer#MAX_VALUE}</li>
 * <li>cache-provider - The implementation that handles the time slice memory management. The default is
 * {@link TimeSliceDefaultCacheProvider}</li>
 * <li>cache-name - This allows you to set the cache name that the time slice statistics will be created/used from;<strong>this must
 * be specified</strong></li>
 * </ul>
 * 
 * @config throttling-interceptor
 * 
 * @author amcgrath
 */
@XStreamAlias("throttling-interceptor")
@ComponentProfile(summary = "Throttles message flow based on some message count criteria",
    tag = "interceptor,jmx")
@DisplayOrder(order = {"maximumMessages", "timeSliceInterval"})
public class ThrottlingInterceptor extends WorkflowInterceptorImpl {

  private transient Logger log = LoggerFactory.getLogger(this.getClass().getName());

  private static final int MAXIMUM_MESSAGES_DEFAULT = Integer.MAX_VALUE;
  private static final TimeInterval DEFAULT_TIMESLICE_INTERVAL = new TimeInterval(5L, TimeUnit.SECONDS);

  private TimeInterval timeSliceInterval;
  private int maximumMessages;

  private transient ExecutorService executor;

  @Valid
  @NotNull
  @AutoPopulated
  private TimeSliceCacheProvider cacheProvider;
  /**
   * Required.
   */
  @NotBlank
  private String cacheName;

  public ThrottlingInterceptor() {
    super();
    maximumMessages = MAXIMUM_MESSAGES_DEFAULT;
    cacheProvider = new TimeSliceDefaultCacheProvider();
  }

  @Override
  public void init() throws CoreException {
    if (isEmpty(cacheName)) {
      throw new CoreException("Cache Name not specified.");
    }

    ((TimeSliceAbstractCacheProvider) cacheProvider).setTimeSliceDurationMilliseconds(getMillisecondDuration());
    getCacheProvider().init();
    executor = Executors.newSingleThreadExecutor(new ManagedThreadFactory());
  }

  @Override
  public void start() throws CoreException {
    getCacheProvider().start();
  }

  @Override
  public void stop() {
    getCacheProvider().stop();
  }

  @Override
  public void close() {
    executor.shutdownNow();
  }

  //
  @Override
  public synchronized void workflowStart(AdaptrisMessage inputMsg) {
    // CacheProvider.get() will always return a timeslice - can never be null.
    TimeSlice currentTimeSlice = getCacheProvider().get(getCacheName());

    log.debug("Using timeslice - " + currentTimeSlice.toString());
    if (currentTimeSlice.getTotalMessageCount() >= getMaximumMessages()) {
      long currentTime = Calendar.getInstance().getTimeInMillis();
      long delayUntil = currentTimeSlice.getEndMillis();

      long delayFor = Math.abs(delayUntil - currentTime);
      log.debug("Delaying thread (Throttling) for {} ms", delayFor);
      if (throttle(delayFor)) workflowStart(inputMsg);
    }
    else {
      currentTimeSlice.setTotalMessageCount(currentTimeSlice.getTotalMessageCount() + 1);
    }
  }

  // returns true if the throttle was successfully applied
  private boolean throttle(final long delay) {
    final CyclicBarrier barrier = new CyclicBarrier(2);
    // This is to make sure we don't break the barrier before the real delay is up.
    // 
    long barrierDelay = delay + TimeUnit.SECONDS.toMillis(1L);
    boolean result = true;
    try {
      executor.execute(new Runnable() {

        @Override
        public void run() {
          try {
            Thread.sleep(delay);
            barrier.await(TimeUnit.SECONDS.toMillis(1L), TimeUnit.MILLISECONDS);
          }
          catch (InterruptedException | BrokenBarrierException | TimeoutException e) {
            log.trace("Interrupted during sleep; breaking barrier");
            barrier.reset();
          }
        }
        
      });
      barrier.await(barrierDelay, TimeUnit.MILLISECONDS);
    }
    catch (InterruptedException | BrokenBarrierException | TimeoutException e) {
      result = false;
    }
    return result;
  }

  private long getMillisecondDuration() {
    return getTimeSliceInterval() != null ? getTimeSliceInterval().toMilliseconds() : DEFAULT_TIMESLICE_INTERVAL.toMilliseconds();
  }

  @Override
  public void workflowEnd(AdaptrisMessage inputMsg, AdaptrisMessage outputMsg) {
  }

  public int getMaximumMessages() {
    return maximumMessages;
  }

  /**
   * Set the maximum number of messages that can be processed in each timeslice.
   * 
   * @param maximumMessages the max number of messages, default is {@value java.lang.Integer#MAX_VALUE}
   */
  public void setMaximumMessages(int maximumMessages) {
    this.maximumMessages = maximumMessages;
  }

  public TimeSliceCacheProvider getCacheProvider() {
    return cacheProvider;
  }

  /**
   * Set the caching provider for timeslices.
   * 
   * @param cacheProvider the caching provider.
   */
  public void setCacheProvider(TimeSliceCacheProvider cacheProvider) {
    this.cacheProvider = cacheProvider;
  }

  public String getCacheName() {
    return cacheName;
  }

  /**
   * Set the cache name for shared throttles.
   * 
   * @param cacheName the cache name.
   */
  public void setCacheName(String cacheName) {
    this.cacheName = cacheName;
  }

  /**
   * Set the interval for each timeslice.
   * 
   * @param interval
   */
  public void setTimeSliceInterval(TimeInterval interval) {
    timeSliceInterval = interval;
  }

  public TimeInterval getTimeSliceInterval() {
    return timeSliceInterval;
  }
}
