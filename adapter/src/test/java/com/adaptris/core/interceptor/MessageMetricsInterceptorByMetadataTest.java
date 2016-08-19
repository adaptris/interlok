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

import java.util.concurrent.TimeUnit;

import junit.framework.TestCase;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.CoreConstants;
import com.adaptris.core.DefaultMessageFactory;
import com.adaptris.core.MetadataElement;
import com.adaptris.core.util.LifecycleHelper;
import com.adaptris.util.TimeInterval;

public class MessageMetricsInterceptorByMetadataTest extends TestCase {

  private MessageMetricsInterceptorByMetadata interceptor;

  @Override
  public void setUp() throws Exception {
    interceptor = new MessageMetricsInterceptorByMetadata();
    interceptor.setTimesliceDuration(new TimeInterval(5L, TimeUnit.SECONDS));
    interceptor.setTimesliceHistoryCount(2);
    interceptor.setMetadataElement(new MetadataElement("messageType", "ORDER"));
  }

  @Override
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

  public void testInterceptor() throws Exception {
    LifecycleHelper.init(interceptor);
    LifecycleHelper.start(interceptor);
    AdaptrisMessage message = createMessage(true);

    assertEquals(0, interceptor.getStats().size());
    submitMessage(message);
    assertEquals(1, interceptor.getStats().size());
    assertEquals(1, interceptor.getStats().get(0).getTotalMessageCount());
  }

  public void testInterceptor_NoMatch() throws Exception {
    LifecycleHelper.init(interceptor);
    LifecycleHelper.start(interceptor);
    AdaptrisMessage message = createMessage(false);

    assertEquals(0, interceptor.getStats().size());
    submitMessage(message);
    assertEquals(0, interceptor.getStats().size());
  }

  public void testInterceptor_MatchByRegexp() throws Exception {
    interceptor.setMetadataElement(new MetadataElement("messageType", "ORD.*"));
    LifecycleHelper.init(interceptor);
    LifecycleHelper.start(interceptor);
    AdaptrisMessage message = createMessage(true);

    assertEquals(0, interceptor.getStats().size());
    submitMessage(message);
    assertEquals(1, interceptor.getStats().size());
    assertEquals(1, interceptor.getStats().get(0).getTotalMessageCount());
  }

  public void testInterceptor_WithException() throws Exception {
    LifecycleHelper.init(interceptor);
    LifecycleHelper.start(interceptor);
    AdaptrisMessage message = createMessage(true);
    message.getObjectHeaders().put(CoreConstants.OBJ_METADATA_EXCEPTION, new Exception());
    assertEquals(0, interceptor.getStats().size());
    submitMessage(message);
    assertEquals(1, interceptor.getStats().size());
    assertEquals(1, interceptor.getStats().get(0).getTotalMessageCount());
    assertEquals(1, interceptor.getStats().get(0).getTotalMessageErrorCount());
  }

  public void testCreatesNewTimeSliceAfterTimeDelay() throws Exception {
    LifecycleHelper.init(interceptor);
    LifecycleHelper.start(interceptor);

    AdaptrisMessage message = createMessage(true);

    assertEquals(0, interceptor.getStats().size());
    submitMessage(message);

    waitFor(6);

    assertEquals(1, interceptor.getStats().size());
    submitMessage(message);
    submitMessage(message);

    assertEquals(2, interceptor.getStats().size());
    assertEquals(1, interceptor.getStats().get(0).getTotalMessageCount());
    assertEquals(2, interceptor.getStats().get(1).getTotalMessageCount());
  }

  public void testDoesNotCreateMoreHistoryThanSpecified() throws Exception {
    LifecycleHelper.init(interceptor);
    LifecycleHelper.start(interceptor);

    AdaptrisMessage message = createMessage(true);

    assertEquals(0, interceptor.getStats().size());
    submitMessage(message);

    waitFor(6);

    assertEquals(1, interceptor.getStats().size());
    submitMessage(message);
    submitMessage(message);

    assertEquals(2, interceptor.getStats().size());

    waitFor(6);

    submitMessage(message);
    submitMessage(message);
    submitMessage(message);
    // Should still only be 2 time slices
    assertEquals(2, interceptor.getStats().size());
    assertEquals(2, interceptor.getStats().get(0).getTotalMessageCount());
    assertEquals(3, interceptor.getStats().get(1).getTotalMessageCount());
  }


  public void testMultiThreadedSingleMetricsInterceptorInstance() throws Exception {
    LifecycleHelper.init(interceptor);
    LifecycleHelper.start(interceptor);

    // 130 messages in total
    new MetricsInserterThread(20).run();
    new MetricsInserterThread(10).run();
    new MetricsInserterThread(30).run();
    new MetricsInserterThread(50).run();
    new MetricsInserterThread(20).run();

    Thread.sleep(5000); // Lets allow the threads to finish
    assertEquals(130, interceptor.getStats().get(0).getTotalMessageCount());
  }

  private void waitFor(int seconds) throws Exception {
    Thread.sleep(seconds * 1000);
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
