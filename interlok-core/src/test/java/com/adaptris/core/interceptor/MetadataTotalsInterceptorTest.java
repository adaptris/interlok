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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import com.adaptris.core.AdaptrisMarshaller;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.core.DefaultMessageFactory;
import com.adaptris.core.MetadataElement;
import com.adaptris.core.StandaloneProducer;
import com.adaptris.core.util.LifecycleHelper;
import com.adaptris.util.TimeInterval;

public class MetadataTotalsInterceptorTest {

  private static final String COUNTER_1 = "counter1";
  private static final String COUNTER_2 = "counter2";
  private static final TimeInterval TIME_INTERVAL = new TimeInterval(1500L, TimeUnit.MILLISECONDS);

  private MetadataTotalsInterceptor metricsInterceptor;
  
  @Mock private StandaloneProducer mockStandaloneProducer;
  @Mock private AdaptrisMarshaller mockMarshaller;

  @Before
  public void setUp() throws Exception {
    MockitoAnnotations.initMocks(this);
    
    metricsInterceptor = new MetadataTotalsInterceptor(new ArrayList<String>(Arrays.asList(new String[]
    {
        COUNTER_1, COUNTER_2, this.getClass().getSimpleName()
    })));
    metricsInterceptor.setTimesliceDuration(TIME_INTERVAL);
    metricsInterceptor.setTimesliceHistoryCount(2);
  }

  @After
  public void tearDown() throws Exception {
    LifecycleHelper.stop(metricsInterceptor);
    LifecycleHelper.close(metricsInterceptor);
  }

  @Test
  public void testInterceptor() throws Exception {
    LifecycleHelper.init(metricsInterceptor);
    LifecycleHelper.start(metricsInterceptor);
    AdaptrisMessage message = createMessage();

    assertEquals(0, metricsInterceptor.getStats().size());
    submitMessage(message);
    // Make sure there is 1 message in the cache
    assertEquals(1, metricsInterceptor.getStats().size());
    assertEquals(10, ((MetadataStatistic) metricsInterceptor.getStats().get(0)).getValue(COUNTER_1));
    assertEquals(10, ((MetadataStatistic) metricsInterceptor.getStats().get(0)).getValue(COUNTER_2));
    assertEquals(0, ((MetadataStatistic) metricsInterceptor.getStats().get(0)).getValue(this.getClass().getSimpleName()));
    assertEquals(0, ((MetadataStatistic) metricsInterceptor.getStats().get(0)).getValue("blah"));
  }

  @Test
  public void testNoProduceBeforeNewTimeSlice() throws Exception {
    ProducingStatisticManager producingStatisticManager = new ProducingStatisticManager();
    producingStatisticManager.setMarshaller(mockMarshaller);
    producingStatisticManager.setProducer(mockStandaloneProducer);
    
    metricsInterceptor.setStatisticManager(producingStatisticManager);
    
    LifecycleHelper.init(metricsInterceptor);
    LifecycleHelper.start(metricsInterceptor);

    AdaptrisMessage message = DefaultMessageFactory.getDefaultInstance().newMessage();

    // A minus time will expire the time slice immediately after the first message
    metricsInterceptor.setTimesliceDuration(new TimeInterval(-1L, TimeUnit.SECONDS));
    
    assertEquals(0, metricsInterceptor.getStats().size());
    submitMessage(message);

    assertEquals(1, metricsInterceptor.getStats().size());

    verify(mockMarshaller, times(0)).marshal(any());
    verify(mockStandaloneProducer, times(0)).produce(any());
  }

  @Test
  public void testProduceAfterNewTimeSlice() throws Exception {
    ProducingStatisticManager producingStatisticManager = new ProducingStatisticManager();
    producingStatisticManager.setMarshaller(mockMarshaller);
    producingStatisticManager.setProducer(mockStandaloneProducer);
    
    metricsInterceptor.setStatisticManager(producingStatisticManager);
    
    LifecycleHelper.init(metricsInterceptor);
    LifecycleHelper.start(metricsInterceptor);

    AdaptrisMessage message = DefaultMessageFactory.getDefaultInstance().newMessage();

    // A minus time will expire the time slice immediately after the first message
    metricsInterceptor.setTimesliceDuration(new TimeInterval(-1L, TimeUnit.SECONDS));
    
    assertEquals(0, metricsInterceptor.getStats().size());
    submitMessage(message);

    assertEquals(1, metricsInterceptor.getStats().size());
    submitMessage(message);

    verify(mockMarshaller).marshal(any());
    verify(mockStandaloneProducer).produce(any());
  }

  @Test
  public void testCreatesNewTimeSliceAfterTimeDelay() throws Exception {
    LifecycleHelper.init(metricsInterceptor);
    LifecycleHelper.start(metricsInterceptor);
    
    // A negative number will expire the timeslice immediately after the first message
    metricsInterceptor.setTimesliceDuration(new TimeInterval(-1L, TimeUnit.SECONDS));

    AdaptrisMessage message = createMessage();

    assertEquals(0, metricsInterceptor.getStats().size());
    submitMessage(message);
    
    assertEquals(1, metricsInterceptor.getStats().size());
    assertEquals(10, ((MetadataStatistic) metricsInterceptor.getStats().get(0)).getValue(COUNTER_1));
    assertEquals(10, ((MetadataStatistic) metricsInterceptor.getStats().get(0)).getValue(COUNTER_2));
    
    metricsInterceptor.setTimesliceDuration(new TimeInterval(1L, TimeUnit.SECONDS));
    
    submitMessage(message);
    submitMessage(message);

    assertEquals(2, metricsInterceptor.getStats().size());
    assertEquals(10, ((MetadataStatistic) metricsInterceptor.getStats().get(0)).getValue(COUNTER_1));
    assertEquals(10, ((MetadataStatistic) metricsInterceptor.getStats().get(0)).getValue(COUNTER_2));
    assertEquals(20, ((MetadataStatistic) metricsInterceptor.getStats().get(1)).getValue(COUNTER_1));
    assertEquals(20, ((MetadataStatistic) metricsInterceptor.getStats().get(1)).getValue(COUNTER_2));
  }

  @Test
  public void testDoesNotCreateMoreHistoryThanSpecified() throws Exception {
    metricsInterceptor.setStatisticManager(new StandardStatisticManager());
    
    LifecycleHelper.init(metricsInterceptor);
    LifecycleHelper.start(metricsInterceptor);
    
    // A negative number will expire the timeslice immediately after the first message
    metricsInterceptor.setTimesliceDuration(new TimeInterval(-1L, TimeUnit.SECONDS));

    AdaptrisMessage message = createMessage();

    assertEquals(0, metricsInterceptor.getStats().size());
    submitMessage(message);

    assertEquals(1, metricsInterceptor.getStats().size());
    submitMessage(message);

    assertEquals(2, metricsInterceptor.getStats().size());
    submitMessage(message);
    // Should still only be 2 time slices
    assertEquals(2, metricsInterceptor.getStats().size());
  }

  @Test
  public void testMultiThreadedSingleMetricsInterceptorInstance() throws Exception {
    LifecycleHelper.init(metricsInterceptor);
    LifecycleHelper.start(metricsInterceptor);

    // 130 messages in total
    // Each adding a counter of 10... So, we should get 1k for the counters.
    new MetricsInserterThread(20).run();
    new MetricsInserterThread(20).run();
    new MetricsInserterThread(20).run();
    new MetricsInserterThread(20).run();
    new MetricsInserterThread(20).run();

    Thread.sleep(200); // Lets allow the threads to finish
    assertEquals(1000, ((MetadataStatistic) metricsInterceptor.getStats().get(0)).getValue(COUNTER_1));
    assertEquals(1000, ((MetadataStatistic) metricsInterceptor.getStats().get(0)).getValue(COUNTER_2));
  }

  private AdaptrisMessage createMessage() {
    AdaptrisMessage message = AdaptrisMessageFactory.getDefaultInstance().newMessage();
    message.addMetadata(new MetadataElement(COUNTER_1, "10"));
    message.addMetadata(new MetadataElement(COUNTER_2, "10"));
    return message;
  }

  private void submitMessage(AdaptrisMessage msg) {
    metricsInterceptor.workflowStart(msg);
    metricsInterceptor.workflowEnd(msg, msg);
  }

  class MetricsInserterThread extends Thread {
    int numMessages;

    MetricsInserterThread(int numMessages) {
      this.numMessages = numMessages;
    }

    @Override
    public void run() {
      for(int counter = 1; counter <= numMessages; counter ++) {
        submitMessage(createMessage());
      }
    }
  }
}
