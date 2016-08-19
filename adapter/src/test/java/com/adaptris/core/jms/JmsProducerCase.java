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

package com.adaptris.core.jms;

import static com.adaptris.core.jms.JmsConfig.DEFAULT_PAYLOAD;
import static com.adaptris.core.jms.JmsConfig.DEFAULT_TTL;
import static com.adaptris.core.jms.JmsConfig.HIGHEST_PRIORITY;
import static com.adaptris.core.jms.JmsConstants.JMS_DELIVERY_MODE;
import static com.adaptris.core.jms.JmsConstants.JMS_EXPIRATION;
import static com.adaptris.core.jms.JmsConstants.JMS_PRIORITY;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import javax.jms.DeliveryMode;
import javax.jms.Destination;
import javax.jms.JMSException;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.core.ConfiguredProduceDestination;
import com.adaptris.core.CoreException;
import com.adaptris.core.ProduceDestination;
import com.adaptris.core.ProduceException;
import com.adaptris.core.stubs.MockMessageListener;

@SuppressWarnings("deprecation")
public abstract class JmsProducerCase extends JmsProducerExample {

  public JmsProducerCase(String name) {
    super(name);
  }

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

  private DefinedJmsProducer createDummyProducer() {
    DefinedJmsProducer p = new DummyProducer();
    p.setDeliveryMode(com.adaptris.core.jms.DeliveryMode.Mode.PERSISTENT.toString());
    p.setPriority(1);
    p.setTtl(0L);
    return p;
  }

  public void testSetPriority() throws Exception {
    DefinedJmsProducer p = createDummyProducer();
    assertEquals(1, p.getPriority());
    try {
      p.setPriority(-1);
      fail();
    }
    catch (IllegalArgumentException e) {

    }

    try {
      p.setPriority(999);
      fail();
    }
    catch (IllegalArgumentException e) {

    }
  }

  public void testOverrideDeliveryMode() throws Exception {
    DefinedJmsProducer p = createDummyProducer();
    AdaptrisMessage msg = createMessage();
    assertEquals(DeliveryMode.NON_PERSISTENT, p.calculateDeliveryMode(msg));
    msg = AdaptrisMessageFactory.getDefaultInstance().newMessage();
    assertEquals(DeliveryMode.PERSISTENT, p.calculateDeliveryMode(msg));
  }

  public void testOverridePriority() throws Exception {
    DefinedJmsProducer p = createDummyProducer();
    AdaptrisMessage msg = createMessage();
    assertEquals(HIGHEST_PRIORITY, p.calculatePriority(msg));
    msg = AdaptrisMessageFactory.getDefaultInstance().newMessage();
    assertEquals(1, p.calculatePriority(msg));
  }

  public void testOverrideTimeToLive() throws Exception {
    DefinedJmsProducer p = createDummyProducer();
    AdaptrisMessage msg = createMessage();
    msg.addMetadata(JMS_EXPIRATION, String.valueOf(System.currentTimeMillis() + 9999));
    assertTrue("Time to live > 0", p.calculateTimeToLive(msg) > 0);
    msg = AdaptrisMessageFactory.getDefaultInstance().newMessage();
    assertEquals(0, p.calculateTimeToLive(msg));
  }

  public void testOverrideTimeToLiveTimestamp() throws Exception {
    DefinedJmsProducer p = createDummyProducer();
    AdaptrisMessage msg = createMessage();
    msg.addMetadata(JMS_EXPIRATION, createTimestamp(9999));
    assertTrue("Time to live > 0", p.calculateTimeToLive(msg) > 0);
    msg = AdaptrisMessageFactory.getDefaultInstance().newMessage();
    assertEquals(0, p.calculateTimeToLive(msg));
    msg = createMessage();
    msg.addMetadata(JMS_EXPIRATION, createTimestamp(-9999));
    assertEquals(0, p.calculateTimeToLive(msg));
  }

  public void testSetCaptureOutgoingMessageDetails() throws Exception {
    DefinedJmsProducer p = createDummyProducer();
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
    p.setDestination(new ConfiguredProduceDestination("SampleQ1"));
    MetadataCorrelationIdSource mcs = new MetadataCorrelationIdSource();
    mcs.setMetadataKey("MetadataKey_ForCorrelation");
    p.setCorrelationIdSource(mcs);
    return p;
  }

  private class DummyProducer extends DefinedJmsProducer {

    @Override
    protected Destination createDestination(String name) throws JMSException {
      throw new JMSException("NO!");
    }

    @Override
    protected void produce(AdaptrisMessage msg, Destination dest, Destination replyTo) throws JMSException, CoreException {
      throw new ProduceException();
    }

    @Override
    protected AdaptrisMessage doRequest(AdaptrisMessage msg, ProduceDestination dest, long timeout) throws ProduceException {
      throw new ProduceException();
    }

    @Override
    protected Destination createTemporaryDestination() throws JMSException {
      throw new JMSException("NO!");
    }

  }
}
