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
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import java.util.concurrent.TimeUnit;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import com.adaptris.core.AdaptrisMarshaller;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.core.CoreException;
import com.adaptris.core.DefaultMessageFactory;
import com.adaptris.core.MetadataElement;
import com.adaptris.core.StandaloneProducer;
import com.adaptris.core.util.LifecycleHelper;
import com.adaptris.interlok.util.Closer;
import com.adaptris.util.TimeInterval;

public class MetadataCountInterceptorTest {

  private static final TimeInterval TIME_INTERVAL = new TimeInterval(1500L, TimeUnit.MILLISECONDS);

  private static final String COUNTER_1 = "counter1";
  private static final String COUNTER_2 = "counter2";
  private static final String METADATA_KEY = "key";

  private MetadataCountInterceptor metricsInterceptor;

  @Mock private StandaloneProducer mockStandaloneProducer;
  @Mock private AdaptrisMarshaller mockMarshaller;
  private AutoCloseable openMocks;

  @Before
  public void setUp() throws Exception {
    openMocks = MockitoAnnotations.openMocks(this);

    metricsInterceptor = new MetadataCountInterceptor(METADATA_KEY);
    metricsInterceptor.setTimesliceDuration(TIME_INTERVAL);
    metricsInterceptor.setTimesliceHistoryCount(2);
  }

  @After
  public void tearDown() throws Exception {
    LifecycleHelper.stop(metricsInterceptor);
    LifecycleHelper.close(metricsInterceptor);
    Closer.closeQuietly(openMocks);
  }

  @Test
  public void testInit_NoMetadataKey() throws Exception {
    metricsInterceptor = new MetadataCountInterceptor();
    try {
      LifecycleHelper.init(metricsInterceptor);
      fail();
    }
    catch (CoreException expected) {
    }

  }

  @Test
  public void testInterceptor() throws Exception {
    LifecycleHelper.init(metricsInterceptor);
    LifecycleHelper.start(metricsInterceptor);
    AdaptrisMessage message = createMessage(COUNTER_1);

    assertEquals(0, metricsInterceptor.getStats().size());
    submitMessage(message);
    // Make sure there is 1 message in the cache
    assertEquals(1, metricsInterceptor.getStats().size());
    assertEquals(1, ((MetadataStatistic) metricsInterceptor.getStats().get(0)).getValue(COUNTER_1));
    assertEquals(0, ((MetadataStatistic) metricsInterceptor.getStats().get(0)).getValue(COUNTER_2));
  }

  @Test
  public void testInterceptor_NoMetadataValue() throws Exception {
    LifecycleHelper.init(metricsInterceptor);
    LifecycleHelper.start(metricsInterceptor);
    assertEquals(0, metricsInterceptor.getStats().size());
    submitMessage(AdaptrisMessageFactory.getDefaultInstance().newMessage());
    // Make sure there is 1 message in the cache
    assertEquals(1, metricsInterceptor.getStats().size());
    assertEquals(0, ((MetadataStatistic) metricsInterceptor.getStats().get(0)).getValue(COUNTER_1));
    assertEquals(0, ((MetadataStatistic) metricsInterceptor.getStats().get(0)).getValue(COUNTER_2));
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
    // a negative number will expire the timeslice immediately after the first message
    metricsInterceptor.setTimesliceDuration(new TimeInterval(-1L, TimeUnit.SECONDS));

    AdaptrisMessage message = createMessage(COUNTER_1);

    assertEquals(0, metricsInterceptor.getStats().size());
    submitMessage(message);
    assertEquals(1, metricsInterceptor.getStats().size());
    assertEquals(1, ((MetadataStatistic) metricsInterceptor.getStats().get(0)).getValue(COUNTER_1));
    assertEquals(0, ((MetadataStatistic) metricsInterceptor.getStats().get(0)).getValue(COUNTER_2));

    metricsInterceptor.setTimesliceDuration(new TimeInterval(1L, TimeUnit.SECONDS));

    submitMessage(message);
    submitMessage(message);

    assertEquals(2, metricsInterceptor.getStats().size());
    assertEquals(1, ((MetadataStatistic) metricsInterceptor.getStats().get(0)).getValue(COUNTER_1));
    assertEquals(0, ((MetadataStatistic) metricsInterceptor.getStats().get(0)).getValue(COUNTER_2));
    assertEquals(2, ((MetadataStatistic) metricsInterceptor.getStats().get(1)).getValue(COUNTER_1));
    assertEquals(0, ((MetadataStatistic) metricsInterceptor.getStats().get(1)).getValue(COUNTER_2));
  }

  @Test
  public void testDoesNotCreateMoreHistoryThanSpecified() throws Exception {
    LifecycleHelper.init(metricsInterceptor);
    LifecycleHelper.start(metricsInterceptor);

    metricsInterceptor.setTimesliceDuration(new TimeInterval(-1L, TimeUnit.SECONDS));

    AdaptrisMessage message = createMessage(COUNTER_1);

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

    new MetricsInserterThread(20, COUNTER_1).run();
    new MetricsInserterThread(20, COUNTER_2).run();
    new MetricsInserterThread(20, COUNTER_1).run();
    new MetricsInserterThread(20, COUNTER_2).run();
    new MetricsInserterThread(20, COUNTER_1).run();

    Thread.sleep(200); // Lets allow the threads to finish
    assertEquals(60, ((MetadataStatistic) metricsInterceptor.getStats().get(0)).getValue(COUNTER_1));
    assertEquals(40, ((MetadataStatistic) metricsInterceptor.getStats().get(0)).getValue(COUNTER_2));
  }

  private void waitFor(int seconds) {
    try {
      Thread.sleep(seconds * 1000);
    }
    catch (InterruptedException e) {
    }
  }

  private AdaptrisMessage createMessage(String metadataValue) {
    AdaptrisMessage message = AdaptrisMessageFactory.getDefaultInstance().newMessage();
    message.addMetadata(new MetadataElement(METADATA_KEY, metadataValue));
    return message;
  }

  private void submitMessage(AdaptrisMessage msg) {
    metricsInterceptor.workflowStart(msg);
    metricsInterceptor.workflowEnd(msg, msg);
  }

  class MetricsInserterThread extends Thread {
    int numMessages;
    String value;
    MetricsInserterThread(int numMessages, String value) {
      this.numMessages = numMessages;
      this.value = value;
    }

    @Override
    public void run() {
      for(int counter = 1; counter <= numMessages; counter ++) {
        submitMessage(createMessage(value));
      }
    }
  }
}
