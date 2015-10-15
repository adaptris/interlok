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

import java.util.Calendar;
import java.util.concurrent.TimeUnit;

import junit.framework.TestCase;

import com.adaptris.core.interceptor.TimeSliceDefaultCacheProvider;
import com.adaptris.core.util.LifecycleHelper;
import com.adaptris.util.GuidGenerator;
import com.adaptris.util.TimeInterval;

public class UnlicenseThrottlingInterceptorTest extends TestCase {

  private UnlicensedThrottlingInterceptor throttlingInterceptor;
  private String cacheName = new GuidGenerator().safeUUID();
  private int unlimitedPeriodSeconds = 5;
  private int timeslicePeriodSeconds = 5;
  private TimeInterval timeSlice = new TimeInterval(5L, TimeUnit.SECONDS.name());
  private int maximumMessages = 1;
  private String timeSliceUnit = "SECONDS";

  @Override
  public void setUp() throws Exception {
    throttlingInterceptor = new UnlicensedThrottlingInterceptor(unlimitedPeriodSeconds, timeSlice, maximumMessages, cacheName);

    LifecycleHelper.init(throttlingInterceptor);
    LifecycleHelper.start(throttlingInterceptor);
  }

  @Override
  public void tearDown() throws Exception {
    ((TimeSliceDefaultCacheProvider)throttlingInterceptor.getLimitedInterceptor().getCacheProvider()).getPersistence().clear();
  }

  public void testUnlimitedTime() {
    long startTimeLong = Calendar.getInstance().getTimeInMillis();
    AdaptrisMessage newMessage = DefaultMessageFactory.getDefaultInstance().newMessage();

    // even though we have specified only to allow 5 messages through,
    // we should be able to get 10 through before the unlimited time of
    // 5 seconds kicks in.
    throttlingInterceptor.workflowStart(newMessage);
    throttlingInterceptor.workflowStart(newMessage);
    throttlingInterceptor.workflowStart(newMessage);
    throttlingInterceptor.workflowStart(newMessage);
    throttlingInterceptor.workflowStart(newMessage);
    throttlingInterceptor.workflowStart(newMessage);
    throttlingInterceptor.workflowStart(newMessage);
    throttlingInterceptor.workflowStart(newMessage);
    throttlingInterceptor.workflowStart(newMessage);
    throttlingInterceptor.workflowStart(newMessage);

    long endTimeLong = Calendar.getInstance().getTimeInMillis();
    // Make sure we WERE NOT delayed
    assertTrue(startTimeLong + 5000 > endTimeLong);
  }

  public void testLimitedTimeKicksIn() throws Exception {
    long startTimeLong = Calendar.getInstance().getTimeInMillis();
    AdaptrisMessage newMessage = DefaultMessageFactory.getDefaultInstance().newMessage();

    // First let through a few messages
    throttlingInterceptor.workflowStart(newMessage);
    throttlingInterceptor.workflowStart(newMessage);
    throttlingInterceptor.workflowStart(newMessage);

    // Then wait for the unlimited time to expire (expires in 5 seconds)
    try {
      Thread.sleep(6000);
    } catch (InterruptedException e) {
      throw new Exception(e);
    }

    // Now put through a couple more, which should be limited to
    // only allow one through every 5 seconds.
    throttlingInterceptor.workflowStart(newMessage);
    throttlingInterceptor.workflowStart(newMessage);

    long endTimeLong = Calendar.getInstance().getTimeInMillis();
    // Make sure we WERE delayed
    assertFalse(startTimeLong + 10000 > endTimeLong);
  }
}
