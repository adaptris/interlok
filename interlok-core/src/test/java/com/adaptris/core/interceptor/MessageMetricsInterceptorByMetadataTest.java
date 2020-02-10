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
import java.util.concurrent.TimeUnit;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.CoreConstants;
import com.adaptris.core.DefaultMessageFactory;
import com.adaptris.core.MetadataElement;
import com.adaptris.core.util.LifecycleHelper;
import com.adaptris.util.KeyValuePair;
import com.adaptris.util.TimeInterval;

public class MessageMetricsInterceptorByMetadataTest {

  private MessageMetricsInterceptorByMetadata interceptor;

  @Before
  public void setUp() throws Exception {
    interceptor = new MessageMetricsInterceptorByMetadata();
    interceptor.setTimesliceDuration(new TimeInterval(1L, TimeUnit.SECONDS));
    interceptor.setTimesliceHistoryCount(2);
    interceptor.setMetadataElement(new KeyValuePair("messageType", "ORDER"));
  }

  @After
  public void tearDown() throws Exception {
    LifecycleHelper.stop(interceptor);
    LifecycleHelper.close(interceptor);
  }

  private AdaptrisMessage createMessage(boolean addMetadata) {
    AdaptrisMessage message = DefaultMessageFactory.getDefaultInstance().newMessage();
    if (addMetadata) {
      message.addMetadata(new MetadataElement("messageType", "ORDER"));
    }
    return message;
  }

  @Test
  public void testInterceptor() throws Exception {
    LifecycleHelper.init(interceptor);
    LifecycleHelper.start(interceptor);
    AdaptrisMessage message = createMessage(true);

    assertEquals(0, interceptor.getStats().size());
    submitMessage(message);
    assertEquals(1, interceptor.getStats().size());
    assertEquals(1, ((MessageStatistic) interceptor.getStats().get(0)).getTotalMessageCount());
  }

  @Test
  public void testInterceptor_NoMatch() throws Exception {
    LifecycleHelper.init(interceptor);
    LifecycleHelper.start(interceptor);
    AdaptrisMessage message = createMessage(false);

    assertEquals(0, interceptor.getStats().size());
    submitMessage(message);
    assertEquals(0, interceptor.getStats().size());
  }

  @Test
  public void testInterceptor_MatchByRegexp() throws Exception {
    interceptor.setMetadataElement(new KeyValuePair("messageType", "ORD.*"));
    LifecycleHelper.init(interceptor);
    LifecycleHelper.start(interceptor);
    AdaptrisMessage message = createMessage(true);

    assertEquals(0, interceptor.getStats().size());
    submitMessage(message);
    assertEquals(1, interceptor.getStats().size());
    assertEquals(1, ((MessageStatistic) interceptor.getStats().get(0)).getTotalMessageCount());
  }

  @Test
  public void testInterceptor_WithException() throws Exception {
    LifecycleHelper.init(interceptor);
    LifecycleHelper.start(interceptor);
    AdaptrisMessage message = createMessage(true);
    message.getObjectHeaders().put(CoreConstants.OBJ_METADATA_EXCEPTION, new Exception());
    assertEquals(0, interceptor.getStats().size());
    submitMessage(message);
    assertEquals(1, interceptor.getStats().size());
    assertEquals(1, ((MessageStatistic) interceptor.getStats().get(0)).getTotalMessageCount());
    assertEquals(1, ((MessageStatistic) interceptor.getStats().get(0)).getTotalMessageErrorCount());
  }

  @Test
  public void testCreatesNewTimeSliceAfterTimeDelay() throws Exception {
    LifecycleHelper.init(interceptor);
    LifecycleHelper.start(interceptor);

    AdaptrisMessage message = createMessage(true);

    interceptor.setTimesliceDuration(new TimeInterval(-1L, TimeUnit.SECONDS));
    
    assertEquals(0, interceptor.getStats().size());
    submitMessage(message);

    interceptor.setTimesliceDuration(new TimeInterval(1L, TimeUnit.SECONDS));
    
    assertEquals(1, interceptor.getStats().size());
    submitMessage(message);
    submitMessage(message);

    assertEquals(2, interceptor.getStats().size());
    assertEquals(1, ((MessageStatistic) interceptor.getStats().get(0)).getTotalMessageCount());
    assertEquals(2, ((MessageStatistic) interceptor.getStats().get(1)).getTotalMessageCount());
  }

  @Test
  public void testDoesNotCreateMoreHistoryThanSpecified() throws Exception {
    LifecycleHelper.init(interceptor);
    LifecycleHelper.start(interceptor);

    AdaptrisMessage message = createMessage(true);

    interceptor.setTimesliceDuration(new TimeInterval(-1L, TimeUnit.SECONDS));
    
    assertEquals(0, interceptor.getStats().size());
    submitMessage(message);
    
    assertEquals(1, interceptor.getStats().size());
    submitMessage(message);

    assertEquals(2, interceptor.getStats().size());
    submitMessage(message);
    // Should still only be 2 time slices
    assertEquals(2, interceptor.getStats().size());
  }

  @Test
  public void testMultiThreadedSingleMetricsInterceptorInstance() throws Exception {
    LifecycleHelper.init(interceptor);
    LifecycleHelper.start(interceptor);
    
    interceptor.setTimesliceDuration(new TimeInterval(2L, TimeUnit.SECONDS));

    // 130 messages in total
    new MetricsInserterThread(20).run();
    new MetricsInserterThread(10).run();
    new MetricsInserterThread(30).run();
    new MetricsInserterThread(50).run();
    new MetricsInserterThread(20).run();

    Thread.sleep(300); // Lets allow the threads to finish
    assertEquals(130, ((MessageStatistic) interceptor.getStats().get(0)).getTotalMessageCount());
  }

  private void submitMessage(AdaptrisMessage msg) {
    interceptor.workflowStart(msg);
    interceptor.workflowEnd(msg, msg);
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
        AdaptrisMessage message = createMessage(true);
        submitMessage(message);
      }
    }
  }
}
