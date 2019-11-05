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

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import com.adaptris.core.stubs.MockNonStandardRequestReplyProducer;
import com.adaptris.core.stubs.MockRequestReplyProducer;
import com.adaptris.util.TimeInterval;
import org.mockito.Mock;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@SuppressWarnings("deprecation")
public class StandaloneRequestorTest extends GeneralServiceExample {

  public StandaloneRequestorTest(String name) {
    super(name);
  }

  @Override
  protected void setUp() throws Exception {
  }

  public void testSetTimeoutOverride() throws Exception {
    MockRequestReplyProducer m = new MockRequestReplyProducer();
    StandaloneRequestor service = new StandaloneRequestor(m);
    assertNull(service.getReplyTimeout());
    assertEquals(-1, service.timeoutOverrideMs());

    TimeInterval interval = new TimeInterval(10L, TimeUnit.SECONDS);
    service.setReplyTimeout(interval);
    assertEquals(interval, service.getReplyTimeout());
    assertEquals(interval.toMilliseconds(), service.timeoutOverrideMs());

    service.setReplyTimeout(null);
    assertNull(service.getReplyTimeout());
    assertEquals(-1, service.timeoutOverrideMs());

  }

  public void testStandardDoService() throws Exception {
    MockRequestReplyProducer m = new MockRequestReplyProducer();
    StandaloneRequestor service = new StandaloneRequestor(m);
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage("XYZ");
    execute(service, msg);
    assertTrue(msg.containsKey(MockRequestReplyProducer.REPLY_METADATA_KEY));
    assertEquals(MockRequestReplyProducer.REPLY_METADATA_VALUE, msg.getMetadataValue(MockRequestReplyProducer.REPLY_METADATA_KEY));
    assertEquals(1, m.getProducedMessages().size());
  }

  public void testNonStandardDoService() throws Exception {
    MockNonStandardRequestReplyProducer m = new MockNonStandardRequestReplyProducer();
    StandaloneRequestor service = new StandaloneRequestor(m);
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage("XYZ");
    execute(service, msg);
    assertTrue(msg.containsKey(MockNonStandardRequestReplyProducer.REPLY_METADATA_KEY));
    assertEquals(MockNonStandardRequestReplyProducer.REPLY_METADATA_VALUE, msg
        .getMetadataValue(MockNonStandardRequestReplyProducer.REPLY_METADATA_KEY));
    assertEquals(1, m.getProducedMessages().size());
  }

  public void testDoServiceNoTimeout() throws Exception {
    MockRequestReplyProducer m = new MockRequestReplyProducer();
    StandaloneRequestor service = new StandaloneRequestor(m);
    service.setReplyTimeout(null);
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage("XYZ");
    execute(service, msg);
    assertTrue(msg.containsKey(MockRequestReplyProducer.REPLY_METADATA_KEY));
    assertEquals(MockRequestReplyProducer.REPLY_METADATA_VALUE, msg.getMetadataValue(MockRequestReplyProducer.REPLY_METADATA_KEY));
    assertEquals(1, m.getProducedMessages().size());
  }

  public void testCreateName() throws Exception {
    MockRequestReplyProducer m = new MockRequestReplyProducer();
    StandaloneRequestor service = new StandaloneRequestor(m);
    assertEquals(MockRequestReplyProducer.class.getName(), service.createName());
    assertEquals(service.getProducer().createName(), service.createName());
  }

  public void testCreateQualifier() throws Exception {
    MockRequestReplyProducer mp = new MockRequestReplyProducer();
    StandaloneRequestor service = new StandaloneRequestor(mp);
    mp.setUniqueId("abc");
    assertEquals("abc", service.createQualifier());
    assertEquals(service.getProducer().createQualifier(), service.createQualifier());
  }

  public void testNullProducer() throws Exception {
    StandaloneRequestor service = new StandaloneRequestor();
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage("XYZ");
    execute(service, msg);
    // all we care here is that a NPE isn't thrown (see INTERLOK-2829)
  }

  public void testNullMessage() throws Exception {
    StandaloneRequestor service = new StandaloneRequestor();
    service.doService(null);
  }

  public void testConsumerProducer() throws Exception {
    AdaptrisMessageProducer mp = mock(AdaptrisMessageProducer.class);
    StandaloneRequestor service = new StandaloneRequestor(mp);
    service.setReplyTimeout(new TimeInterval(-1L, TimeUnit.MILLISECONDS));
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage("XYZ");
    doReturn(msg).when(mp).request(msg);
    execute(service, msg);
  }
  
  public void testConsumerProducerCopyFails() throws Exception {
    AdaptrisMessageProducer mp = mock(AdaptrisMessageProducer.class);
    StandaloneRequestor service = new StandaloneRequestor(mp);
    service.setReplyTimeout(new TimeInterval(-1L, TimeUnit.MILLISECONDS));
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage("XYZ");
    AdaptrisMessage reply = mock(AdaptrisMessage.class);
    doReturn(reply).when(mp).request(msg);
    doThrow(new IOException("expected")).when(reply).getInputStream();
    try {
      service.doService(msg);
      fail("Output stream on the message should thrpow an error and be caught.");
    } catch (CoreException ex) {
      // expected
    }
  }
  
  public void testConsumerProducerNullMessageWithReply() throws Exception {
    AdaptrisMessageProducer mp = mock(AdaptrisMessageProducer.class);
    StandaloneRequestor service = new StandaloneRequestor(mp);
    service.setReplyTimeout(new TimeInterval(-1L, TimeUnit.MILLISECONDS));
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage("XYZ");
    doReturn(msg).when(mp).request(null);
    service.doService(null);
  }

  @Override
  protected Object retrieveObjectForSampleConfig() {
    return new StandaloneRequestor();
  }
}
