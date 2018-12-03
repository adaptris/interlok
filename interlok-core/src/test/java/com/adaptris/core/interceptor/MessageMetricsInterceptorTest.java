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

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.concurrent.TimeUnit;

import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.adaptris.core.AdaptrisMarshaller;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.CoreConstants;
import com.adaptris.core.DefaultMessageFactory;
import com.adaptris.core.ProduceException;
import com.adaptris.core.StandaloneProducer;
import com.adaptris.core.util.LifecycleHelper;
import com.adaptris.util.TimeInterval;

import junit.framework.TestCase;

public class MessageMetricsInterceptorTest extends TestCase {

  private MessageMetricsInterceptor metricsInterceptor;
  
  @Mock private StandaloneProducer mockStandaloneProducer;
  @Mock private AdaptrisMarshaller mockMarshaller;

  @Override
  public void setUp() throws Exception {
    MockitoAnnotations.initMocks(this);
    
    metricsInterceptor = new MessageMetricsInterceptor();
    metricsInterceptor.setTimesliceDuration(new TimeInterval(5L, TimeUnit.SECONDS));
    metricsInterceptor.setTimesliceHistoryCount(2);
  }

  @Override
  public void tearDown() throws Exception {
    LifecycleHelper.stop(metricsInterceptor);
    LifecycleHelper.close(metricsInterceptor);
  }

  public void testInterceptor() throws Exception {
    LifecycleHelper.init(metricsInterceptor);
    LifecycleHelper.start(metricsInterceptor);
    AdaptrisMessage message = DefaultMessageFactory.getDefaultInstance().newMessage();

    assertEquals(0, metricsInterceptor.getStats().size());
    submitMessage(message);
    // Make sure there is 1 message in the cache
    assertEquals(1, metricsInterceptor.getStats().size());
    assertEquals(1, ((MessageStatistic) metricsInterceptor.getStats().get(0)).getTotalMessageCount());
  }

  public void testInterceptor_WithException() throws Exception {
    LifecycleHelper.init(metricsInterceptor);
    LifecycleHelper.start(metricsInterceptor);
    AdaptrisMessage message = DefaultMessageFactory.getDefaultInstance().newMessage();
    message.getObjectHeaders().put(CoreConstants.OBJ_METADATA_EXCEPTION, new Exception());
    assertEquals(0, metricsInterceptor.getStats().size());
    submitMessage(message);
    assertEquals(1, metricsInterceptor.getStats().size());
    assertEquals(1, ((MessageStatistic) metricsInterceptor.getStats().get(0)).getTotalMessageCount());
    assertEquals(1, ((MessageStatistic) metricsInterceptor.getStats().get(0)).getTotalMessageErrorCount());
  }

  public void testCreatesNewTimeSliceAfterTimeDelay() throws Exception {
    LifecycleHelper.init(metricsInterceptor);
    LifecycleHelper.start(metricsInterceptor);

    AdaptrisMessage message = DefaultMessageFactory.getDefaultInstance().newMessage();

    // A minus time will expire the time slice immediately after the first message
    metricsInterceptor.setTimesliceDuration(new TimeInterval(-1L, TimeUnit.SECONDS));
    
    assertEquals(0, metricsInterceptor.getStats().size());
    submitMessage(message);

    assertEquals(1, metricsInterceptor.getStats().size());
    
    metricsInterceptor.setTimesliceDuration(new TimeInterval(500L, TimeUnit.MILLISECONDS));
    
    submitMessage(message);
    submitMessage(message);

    assertEquals(2, metricsInterceptor.getStats().size());
    assertEquals(1, ((MessageStatistic) metricsInterceptor.getStats().get(0)).getTotalMessageCount());
    assertEquals(2, ((MessageStatistic) metricsInterceptor.getStats().get(1)).getTotalMessageCount());
  }
  
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
  
  public void testRestartProducerAfterProduceFailure() throws Exception {
    doThrow(new ProduceException("Expected."))
        .when(mockStandaloneProducer).produce(any());
        
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
    
    //test the restart.
    verify(mockStandaloneProducer).requestStop();
  }

  public void testDoesNotCreateMoreHistoryThanSpecified() throws Exception {
    LifecycleHelper.init(metricsInterceptor);
    LifecycleHelper.start(metricsInterceptor);

    AdaptrisMessage message = DefaultMessageFactory.getDefaultInstance().newMessage();

    // A minus time will expire the time slice immediately after the first message
    metricsInterceptor.setTimesliceDuration(new TimeInterval(-1L, TimeUnit.SECONDS));
    
    assertEquals(0, metricsInterceptor.getStats().size());
    submitMessage(message);

    assertEquals(1, metricsInterceptor.getStats().size());
    
    metricsInterceptor.setTimesliceDuration(new TimeInterval(500L, TimeUnit.MILLISECONDS));
    
    submitMessage(message);
    submitMessage(message);

    assertEquals(2, metricsInterceptor.getStats().size());

    waitFor(1);

    submitMessage(message);
    submitMessage(message);
    submitMessage(message);
    // Should still only be 2 time slices
    assertEquals(2, metricsInterceptor.getStats().size());
    assertEquals(2, ((MessageStatistic) metricsInterceptor.getStats().get(0)).getTotalMessageCount());
    assertEquals(3, ((MessageStatistic) metricsInterceptor.getStats().get(1)).getTotalMessageCount());
  }


  public void testMultiThreadedSingleMetricsInterceptorInstance() throws Exception {
    LifecycleHelper.init(metricsInterceptor);
    LifecycleHelper.start(metricsInterceptor);

    // 130 messages in total
    new MetricsInserterThread(20).run();
    new MetricsInserterThread(10).run();
    new MetricsInserterThread(30).run();
    new MetricsInserterThread(50).run();
    new MetricsInserterThread(20).run();

    Thread.sleep(1000); // Lets allow the threads to finish
    assertEquals(130, ((MessageStatistic) metricsInterceptor.getStats().get(0)).getTotalMessageCount());
  }

  private void waitFor(int seconds) throws Exception {
    Thread.sleep(seconds * 1000);
  }

  private void submitMessage(AdaptrisMessage msg) {
    metricsInterceptor.workflowStart(msg);
    metricsInterceptor.workflowEnd(msg, msg);
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
        submitMessage(message);
      }
    }
  }
}
