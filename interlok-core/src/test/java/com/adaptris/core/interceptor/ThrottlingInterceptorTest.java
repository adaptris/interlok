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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import java.util.Calendar;
import java.util.concurrent.TimeUnit;
import org.junit.Test;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.DefaultMessageFactory;
import com.adaptris.core.util.LifecycleHelper;
import com.adaptris.util.TimeInterval;

public class ThrottlingInterceptorTest {

  public ThrottlingInterceptor createAndStart(String cacheName) throws Exception {
    ThrottlingInterceptor i = new ThrottlingInterceptor();
    i.setCacheName(cacheName);
    i.setTimeSliceInterval(new TimeInterval(500L, TimeUnit.MILLISECONDS));
    LifecycleHelper.initAndStart(i);
    return i;
  }

  public void destroy(ThrottlingInterceptor i) throws Exception {
    ((TimeSliceDefaultCacheProvider) i.getCacheProvider()).getPersistence().clear();
    LifecycleHelper.stopAndClose(i);
  }

  @Test
  public void testThrottleNoDelay() throws Exception {
    ThrottlingInterceptor throttlingInterceptor = createAndStart("default");
    throttlingInterceptor.setMaximumMessages(6);

    long startTimeLong = Calendar.getInstance().getTimeInMillis();

    AdaptrisMessage newMessage = DefaultMessageFactory.getDefaultInstance().newMessage();
    throttlingInterceptor.workflowStart(newMessage);
    throttlingInterceptor.workflowStart(newMessage);
    throttlingInterceptor.workflowStart(newMessage);
    throttlingInterceptor.workflowStart(newMessage);
    throttlingInterceptor.workflowStart(newMessage);

    long endTimeLong = Calendar.getInstance().getTimeInMillis();
    // Make sure we WERE NOT delayed
    assertTrue(startTimeLong + 5000 > endTimeLong);
    destroy(throttlingInterceptor);
  }

  @Test
  public void testThrottleDelay() throws Exception {
    ThrottlingInterceptor throttlingInterceptor = createAndStart("default");
    throttlingInterceptor.setMaximumMessages(2);

    long startTimeLong = Calendar.getInstance().getTimeInMillis();

    AdaptrisMessage newMessage = DefaultMessageFactory.getDefaultInstance().newMessage();
    throttlingInterceptor.workflowStart(newMessage);
    throttlingInterceptor.workflowStart(newMessage);
    throttlingInterceptor.workflowStart(newMessage);

    long endTimeLong = Calendar.getInstance().getTimeInMillis();
    // Make sure we WERE delayed
    assertFalse(startTimeLong + 500 > endTimeLong);
    destroy(throttlingInterceptor);
  }

  @Test
  public void testMultiThreadedWithSingleInterceptor() throws Exception {
    ThrottlingInterceptor throttle = createAndStart("mp");
    throttle.setMaximumMessages(Integer.MAX_VALUE);
    
    // Total messages = 50
    new MetricsInserterThread(10, throttle).run();
    new MetricsInserterThread(10, throttle).run();
    new MetricsInserterThread(10, throttle).run();
    new MetricsInserterThread(10, throttle).run();
    new MetricsInserterThread(10, throttle).run();
    
    Thread.sleep(200); // allow the interceptors to finish processing

    int totalMessageCount = throttle.getCacheProvider().get("mp").getTotalMessageCount();
    assertEquals(50, totalMessageCount);
  }

  @Test
  public void testMultiThreadedWithSingleInterceptorMultiTimeSlice() throws Exception {
    ThrottlingInterceptor throttle = createAndStart("NewCache");
    throttle.setMaximumMessages(40);
    
    // Total messages = 50
    new MetricsInserterThread(10, throttle).run();
    new MetricsInserterThread(10, throttle).run();
    new MetricsInserterThread(10, throttle).run();
    new MetricsInserterThread(10, throttle).run();
    new MetricsInserterThread(10, throttle).run();
    
    // the first 40 messages will be consumed into the first time slice.
    // a second timeslice will be created, which should have the remaining 10 messages (remember we set the max to 40 above...).
    int totalMessageCount = throttle.getCacheProvider().get("NewCache").getTotalMessageCount();
    assertEquals(10, totalMessageCount);
  }
  
  /**
   * Test class that simply whacks messages into the interceptor
   * @author Aaron
   */
  class MetricsInserterThread extends Thread {
    int numMessages;
    ThrottlingInterceptor throttle;

    MetricsInserterThread(int numMessages, ThrottlingInterceptor i) {
      this.numMessages = numMessages;
      this.throttle = i;
    }
    
    @Override
    public void run() {
      for(int counter = 1; counter <= numMessages; counter ++) {
        AdaptrisMessage message = DefaultMessageFactory.getDefaultInstance().newMessage();
        throttle.workflowStart(message);
      }
    }
  }

}
