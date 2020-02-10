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
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.DefaultMessageFactory;
import com.adaptris.core.util.LifecycleHelper;
import com.adaptris.util.TimeInterval;

public class ThrottlingInterceptorTest {

  private ThrottlingInterceptor throttlingInterceptor;
  private String cacheName = "default";

  @Before
  public void setUp() throws Exception {
    throttlingInterceptor = new ThrottlingInterceptor();
    throttlingInterceptor.setCacheName(cacheName);
    throttlingInterceptor.setTimeSliceInterval(new TimeInterval(500L, TimeUnit.MILLISECONDS));

    LifecycleHelper.init(throttlingInterceptor);
    LifecycleHelper.start(throttlingInterceptor);
  }

  @After
  public void tearDown() throws Exception{
    ((TimeSliceDefaultCacheProvider)throttlingInterceptor.getCacheProvider()).getPersistence().clear();
  }

  @Test
  public void testThrottleNoDelay() throws Exception {
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
  }

  @Test
  public void testThrottleDelay() throws Exception {
    throttlingInterceptor.setMaximumMessages(2);

    long startTimeLong = Calendar.getInstance().getTimeInMillis();

    AdaptrisMessage newMessage = DefaultMessageFactory.getDefaultInstance().newMessage();
    throttlingInterceptor.workflowStart(newMessage);
    throttlingInterceptor.workflowStart(newMessage);
    throttlingInterceptor.workflowStart(newMessage);

    long endTimeLong = Calendar.getInstance().getTimeInMillis();
    // Make sure we WERE delayed
    assertFalse(startTimeLong + 500 > endTimeLong);
  }

  @Test
  public void testMultiThreadedWithSingleInterceptor() throws Exception {
    throttlingInterceptor.setMaximumMessages(Integer.MAX_VALUE);
    
    // Total messages = 50
    new MetricsInserterThread(10).run();
    new MetricsInserterThread(10).run();
    new MetricsInserterThread(10).run();
    new MetricsInserterThread(10).run();
    new MetricsInserterThread(10).run();
    
    Thread.sleep(200); // allow the interceptors to finish processing
    
    int totalMessageCount = throttlingInterceptor.getCacheProvider().get(cacheName).getTotalMessageCount();
    assertEquals(50, totalMessageCount);
  }

  @Test
  public void testMultiThreadedWithSingleInterceptorMultiTimeSlice() throws Exception {
    throttlingInterceptor.setMaximumMessages(40);
    throttlingInterceptor.setCacheName("NewCache");
    
    // Total messages = 50
    new MetricsInserterThread(10).run();
    new MetricsInserterThread(10).run();
    new MetricsInserterThread(10).run();
    new MetricsInserterThread(10).run();
    new MetricsInserterThread(10).run();
    
    // the first 40 messages will be consumed into the first time slice.
    // a second timeslice will be created, which should have the remaining 10 messages (remember we set the max to 40 above...).
    int totalMessageCount = throttlingInterceptor.getCacheProvider().get("NewCache").getTotalMessageCount();
    assertEquals(10, totalMessageCount);
  }
  
  /**
   * Test class that simply whacks messages into the interceptor
   * @author Aaron
   */
  class MetricsInserterThread extends Thread {
    int numMessages;
    
    MetricsInserterThread(int numMessages) {
      this.numMessages = numMessages;
    }
    
    @Override
    public void run() {
      for(int counter = 1; counter <= numMessages; counter ++) {
        AdaptrisMessage message = DefaultMessageFactory.getDefaultInstance().newMessage();
        throttlingInterceptor.workflowStart(message);
      }
    }
  }

}
