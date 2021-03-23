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

package com.adaptris.interlok.junit.scaffolding.jms;

import static com.adaptris.interlok.junit.scaffolding.jms.JmsConfig.DEFAULT_PAYLOAD;
import static com.adaptris.interlok.junit.scaffolding.jms.JmsConfig.DEFAULT_TTL;
import static com.adaptris.interlok.junit.scaffolding.jms.JmsConfig.HIGHEST_PRIORITY;
import static com.adaptris.core.jms.JmsConstants.JMS_DELIVERY_MODE;
import static com.adaptris.core.jms.JmsConstants.JMS_EXPIRATION;
import static com.adaptris.core.jms.JmsConstants.JMS_PRIORITY;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import javax.jms.DeliveryMode;
import javax.jms.JMSException;
import org.junit.Test;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.core.jms.DefinedJmsProducer;
import com.adaptris.core.jms.JmsConnection;
import com.adaptris.core.jms.JmsConnectionErrorHandler;
import com.adaptris.core.jms.MetadataCorrelationIdSource;
import com.adaptris.core.stubs.MockMessageListener;

public abstract class JmsProducerCase extends JmsProducerExample {

  public static AdaptrisMessage createMessage() {
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(DEFAULT_PAYLOAD);
    msg.addMetadata(JMS_PRIORITY, String.valueOf(HIGHEST_PRIORITY));
    msg.addMetadata(JMS_DELIVERY_MODE, String.valueOf(DeliveryMode.NON_PERSISTENT));
    msg.addMetadata(JMS_EXPIRATION, String.valueOf(DEFAULT_TTL));
    return msg;
  }

  public static void assertMessages(MockMessageListener jms) {
    assertMessages(jms, 1);
  }

  public static void assertMessages(MockMessageListener jms, int size) {
    assertMessages(jms, size, true);
  }

  public static void assertMessages(MockMessageListener jms, int size, boolean assertPayloads) {
    List<AdaptrisMessage> msgs = jms.getMessages();
    assertEquals("Number of Messages", size, msgs.size());
    if (assertPayloads) {
      for (int i = 0; i < size; i++) {
        assertEquals("MessageText", DEFAULT_PAYLOAD, msgs.get(i).getContent());
      }
    }
  }

  private MockProducer createDummyProducer() {
    MockProducer p = new MockProducer();
    p.setDeliveryMode(com.adaptris.core.jms.DeliveryMode.Mode.PERSISTENT.toString());
    p.setPriority(1);
    p.setTtl(0L);
    return p;
  }

  @Test
  public void testSetPriority() throws Exception {
    DefinedJmsProducer p = createDummyProducer();
    assertEquals(Integer.valueOf(1), p.getPriority());
    assertEquals(1, p.messagePriority());
    p.setPriority(null);
    assertEquals(null, p.getPriority());
    assertEquals(4, p.messagePriority());
  }

  @Test
  public void testOverrideDeliveryMode() throws Exception {
    MockProducer p = createDummyProducer();
    AdaptrisMessage msg = createMessage();
    assertEquals(DeliveryMode.NON_PERSISTENT, p.calculateDeliveryMode(msg, p.getDeliveryMode()));
    msg = AdaptrisMessageFactory.getDefaultInstance().newMessage();
    assertEquals(DeliveryMode.PERSISTENT, p.calculateDeliveryMode(msg, p.getDeliveryMode()));
  }

  @Test
  public void testOverridePriority() throws Exception {
    MockProducer p = createDummyProducer();
    AdaptrisMessage msg = createMessage();
    assertEquals(HIGHEST_PRIORITY, p.calculatePriority(msg, p.getPriority()));
    msg = AdaptrisMessageFactory.getDefaultInstance().newMessage();
    assertEquals(1, p.calculatePriority(msg, p.getPriority()));
  }

  @Test
  public void testOverrideTimeToLive() throws Exception {
    MockProducer p = createDummyProducer();
    AdaptrisMessage msg = createMessage();
    msg.addMetadata(JMS_EXPIRATION, String.valueOf(System.currentTimeMillis() + 9999));
    assertTrue("Time to live > 0", p.calculateTimeToLive(msg, p.getTtl()) > 0);
    msg = AdaptrisMessageFactory.getDefaultInstance().newMessage();
    assertEquals(0, p.calculateTimeToLive(msg, p.getTtl()));
  }

  @Test
  public void testOverrideTimeToLiveTimestamp() throws Exception {
    MockProducer p = createDummyProducer();
    AdaptrisMessage msg = createMessage();
    msg.addMetadata(JMS_EXPIRATION, createTimestamp(9999));
    assertTrue("Time to live > 0", p.calculateTimeToLive(msg, p.getTtl()) > 0);
    msg = AdaptrisMessageFactory.getDefaultInstance().newMessage();
    assertEquals(0, p.calculateTimeToLive(msg, p.getTtl()));
    msg = createMessage();
    msg.addMetadata(JMS_EXPIRATION, createTimestamp(-9999));
    assertEquals(0, p.calculateTimeToLive(msg, p.getTtl()));
  }

  @Test
  public void testLogLinkedException() throws Exception {
    MockProducer p = createDummyProducer();
    p.logLinkedException("", new Exception());
    JMSException e1 = new JMSException("exception reason", "exception code");
    p.logLinkedException("hello", e1);
    p.logLinkedException("", e1);
    e1.setLinkedException(new Exception("linked-exception"));
    p.logLinkedException("", e1);
  }

  @Test
  public void testSetCaptureOutgoingMessageDetails() throws Exception {
    MockProducer p = createDummyProducer();
    assertNull(p.getCaptureOutgoingMessageDetails());
    assertFalse(p.captureOutgoingMessageDetails());
    p.setCaptureOutgoingMessageDetails(Boolean.FALSE);
    assertNotNull(p.getCaptureOutgoingMessageDetails());
    assertFalse(p.getCaptureOutgoingMessageDetails());
    assertFalse(p.captureOutgoingMessageDetails());
  }


  private String createTimestamp(long howFarInTheFuture) {
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
    return sdf.format(new Date(System.currentTimeMillis() + howFarInTheFuture));
  }

  protected static JmsConnection configureForExamples(JmsConnection c) {
    c.setPassword("password");
    c.setClientId("client-id");
    c.setConnectionErrorHandler(new JmsConnectionErrorHandler());
    return c;
  }

  protected static DefinedJmsProducer configureForExamples(DefinedJmsProducer p) {
    MetadataCorrelationIdSource mcs = new MetadataCorrelationIdSource();
    mcs.setMetadataKey("MetadataKey_ForCorrelation");
    p.setCorrelationIdSource(mcs);
    return p;
  }
}
