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

import static com.adaptris.core.jms.JmsConstants.JMS_CORRELATION_ID;
import static com.adaptris.core.jms.JmsConstants.JMS_DELIVERY_MODE;
import static com.adaptris.core.jms.JmsConstants.JMS_DESTINATION;
import static com.adaptris.core.jms.JmsConstants.JMS_EXPIRATION;
import static com.adaptris.core.jms.JmsConstants.JMS_MESSAGE_ID;
import static com.adaptris.core.jms.JmsConstants.JMS_PRIORITY;
import static com.adaptris.core.jms.JmsConstants.JMS_REDELIVERED;
import static com.adaptris.core.jms.JmsConstants.JMS_REPLY_TO;
import static com.adaptris.core.jms.JmsConstants.JMS_TIMESTAMP;
import static com.adaptris.core.jms.JmsConstants.JMS_TYPE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;

import com.adaptris.core.AdaptrisMarshaller;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.core.DefaultMarshaller;
import com.adaptris.core.DefaultMessageFactory;
import com.adaptris.core.MetadataElement;
import com.adaptris.core.StandaloneProducer;
import com.adaptris.core.jms.AutoConvertMessageTranslator;
import com.adaptris.core.jms.JmsConnection;
import com.adaptris.core.jms.JmsConstants;
import com.adaptris.core.jms.MessageTypeTranslator;
import com.adaptris.core.jms.MessageTypeTranslatorImp;
import com.adaptris.core.jms.PasProducer;
import com.adaptris.core.jms.activemq.EmbeddedActiveMq;
import com.adaptris.core.metadata.NoOpMetadataFilter;
import com.adaptris.core.metadata.RegexMetadataFilter;
import com.adaptris.core.metadata.RemoveAllMetadataFilter;
import com.adaptris.core.util.LifecycleHelper;
import com.adaptris.interlok.junit.scaffolding.BaseCase;

/**
 */
public abstract class MessageTypeTranslatorCase {


  public static final String INTEGER_VALUE = "-1";
  public static final String BOOLEAN_VALUE = "true";
  public static final String STRING_VALUE = "value";
  public static final String INTEGER_METADATA = "IntegerMetadataKey";
  public static final String BOOLEAN_METADATA = "BooleanMetadataKey";
  public static final String STRING_METADATA = "StringMetadataKey";
  public static final String TEXT = "The quick brown fox";
  public static final String TEXT2 = "jumps over the lazy dog";

  protected static EmbeddedActiveMq activeMqBroker;

  @BeforeAll
  public static void setUpAll() throws Exception {
    activeMqBroker = new EmbeddedActiveMq();
    activeMqBroker.start();
  }

  @AfterAll
  public static void tearDownAll() throws Exception {
    if(activeMqBroker != null) {
      activeMqBroker.destroy();
    }
  }

  protected abstract MessageTypeTranslatorImp createTranslator() throws Exception;

  protected abstract Message createMessage(Session session) throws Exception;

  private StandaloneProducer createProducer(MessageTypeTranslator mt, TestInfo info) throws Exception {
    PasProducer producer = new PasProducer();
    producer.withTopic(info.getDisplayName());
    producer.setMessageTranslator(mt);
    return new StandaloneProducer(new JmsConnection(), producer);
  }

  @Test
  public void testRoundTrip(TestInfo info) throws Exception {
    MessageTypeTranslatorImp translator =
        createTranslator().withMoveJmsHeaders(true).withMetadataFilter(new NoOpMetadataFilter())
        .withReportAllErrors(true);
    StandaloneProducer p1 = createProducer(translator, info);
    StandaloneProducer p2 = roundTrip(p1);
    BaseCase.assertRoundtripEquality(p1, p2);
  }

  protected StandaloneProducer roundTrip(StandaloneProducer src) throws Exception {
    AdaptrisMarshaller m = DefaultMarshaller.getDefaultMarshaller();
    String xml = m.marshal(src);
    return (StandaloneProducer) m.unmarshal(xml);
  }

  @Test
  public void testSetMetadataFilter() throws Exception {
    MessageTypeTranslatorImp translator = createTranslator();
    assertNull(translator.getMetadataFilter());
    assertNotNull(translator.metadataFilter());
    assertEquals(NoOpMetadataFilter.class, translator.metadataFilter().getClass());
    RegexMetadataFilter filter = new RegexMetadataFilter();
    translator.setMetadataFilter(filter);
    assertEquals(filter, translator.getMetadataFilter());
    assertEquals(filter, translator.metadataFilter());
    translator.setMetadataFilter(null);
    assertNull(translator.getMetadataFilter());
    assertNotNull(translator.metadataFilter());
    assertEquals(NoOpMetadataFilter.class, translator.metadataFilter().getClass());
  }

  @Test
  public void testSetMoveJmsHeaders() throws Exception {
    MessageTypeTranslatorImp translator = createTranslator();
    assertNull(translator.getMoveJmsHeaders());
    assertFalse(translator.moveJmsHeaders());
    translator.setMoveJmsHeaders(Boolean.TRUE);
    assertEquals(Boolean.TRUE, translator.getMoveJmsHeaders());
    assertTrue(translator.moveJmsHeaders());
    translator.setMoveJmsHeaders(null);
    assertNull(translator.getMoveJmsHeaders());
    assertFalse(translator.moveJmsHeaders());
  }

  @Test
  public void testSetReportAllErrors() throws Exception {
    MessageTypeTranslatorImp translator = createTranslator();
    assertNull(translator.getReportAllErrors());
    assertFalse(translator.reportAllErrors());
    translator.setReportAllErrors(Boolean.TRUE);
    assertEquals(Boolean.TRUE, translator.getReportAllErrors());
    assertTrue(translator.reportAllErrors());
    translator.setReportAllErrors(null);
    assertNull(translator.getReportAllErrors());
    assertFalse(translator.reportAllErrors());
  }

  @Test
  public void testMoveMetadataJmsMessageToAdaptrisMessage() throws Exception {
    MessageTypeTranslatorImp trans = createTranslator();
    try {
      Session session = activeMqBroker.createConnection().createSession(false, Session.CLIENT_ACKNOWLEDGE);
      Message jmsMsg = createMessage(session);
      addProperties(jmsMsg);
      start(trans, session);
      AdaptrisMessage msg = trans.translate(jmsMsg);
      assertMetadata(msg);
    }
    finally {
      stop(trans);
    }

  }

  @Test
  public void testMoveMetadataJmsMessageToAdaptrisMessage_RemoveAllFilter() throws Exception {
    MessageTypeTranslatorImp trans = createTranslator();
    trans.setMetadataFilter(new RemoveAllMetadataFilter());
    try {
      Session session = activeMqBroker.createConnection().createSession(false, Session.CLIENT_ACKNOWLEDGE);
      Message jmsMsg = createMessage(session);
      addProperties(jmsMsg);
      start(trans, session);
      AdaptrisMessage msg = trans.translate(jmsMsg);
      assertFalse(msg.headersContainsKey(INTEGER_METADATA));
      assertFalse(msg.headersContainsKey(STRING_METADATA));
      assertFalse(msg.headersContainsKey(BOOLEAN_METADATA));
    }
    finally {
      stop(trans);
    }
  }

  @Test
  public void testTranslatorNullMessage() throws Exception {
    Message nullMessage = null;
    assertNull(MessageTypeTranslatorImp.translate(new AutoConvertMessageTranslator(), nullMessage));
  }

  @Test
  public void testMoveMetadata_JmsMessageToAdaptrisMessage_WithFilter() throws Exception {
    MessageTypeTranslatorImp trans = createTranslator();
    RegexMetadataFilter regexp = new RegexMetadataFilter();
    regexp.addExcludePattern("IntegerMetadataKey");
    trans.setMetadataFilter(regexp);
    try {
      Session session = activeMqBroker.createConnection().createSession(false, Session.CLIENT_ACKNOWLEDGE);
      Message jmsMsg = createMessage(session);
      addProperties(jmsMsg);
      start(trans, session);
      AdaptrisMessage msg = trans.translate(jmsMsg);
      assertMetadata(msg, new MetadataElement(STRING_METADATA, STRING_VALUE));
      assertMetadata(msg, new MetadataElement(BOOLEAN_METADATA, BOOLEAN_VALUE));
      assertFalse(msg.headersContainsKey(INTEGER_METADATA));
    }
    finally {
      stop(trans);
    }

  }

  @Test
  public void testMoveJmsHeadersJmsMessageToAdaptrisMessage() throws Exception {
    MessageTypeTranslatorImp trans = createTranslator();
    try {
      Session session = activeMqBroker.createConnection().createSession(false, Session.CLIENT_ACKNOWLEDGE);
      Message jmsMsg = createMessage(session);
      jmsMsg.setJMSCorrelationID("ABC");
      jmsMsg.setJMSDeliveryMode(1);
      jmsMsg.setJMSPriority(4);
      addProperties(jmsMsg);
      long timestamp = System.currentTimeMillis();
      jmsMsg.setJMSTimestamp(timestamp);

      trans.setMoveJmsHeaders(true);
      start(trans, session);

      AdaptrisMessage msg = trans.translate(jmsMsg);
      assertMetadata(msg);
      assertEquals("ABC", msg.getMetadataValue(JmsConstants.JMS_CORRELATION_ID));
      assertEquals("1", msg.getMetadataValue(JmsConstants.JMS_DELIVERY_MODE));
      assertEquals("4", msg.getMetadataValue(JmsConstants.JMS_PRIORITY));
      assertEquals(String.valueOf(timestamp), msg.getMetadataValue(JmsConstants.JMS_TIMESTAMP));
    }
    finally {
      stop(trans);
    }

  }

  @Test
  public void testMoveJmsHeadersAdaptrisMessageToJmsMessage() throws Exception {
    MessageTypeTranslatorImp trans = createTranslator().withMoveJmsHeaders(true);
    try {
      Session session = activeMqBroker.createConnection().createSession(false, Session.CLIENT_ACKNOWLEDGE);

      AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage();
      addMetadata(msg);
      addRedundantJmsHeaders(msg);

      start(trans, session);

      Message jmsMsg = trans.translate(msg);
      assertEquals(msg.getMetadataValue(JMS_TYPE), jmsMsg.getJMSType());
      assertNotSame(msg.getMetadataValue(JMS_CORRELATION_ID), jmsMsg.getJMSCorrelationID());
      assertNotSame(msg.getMetadataValue(JMS_TIMESTAMP), jmsMsg.getJMSTimestamp());
      assertNotSame(msg.getMetadataValue(JMS_REDELIVERED), jmsMsg.getJMSPriority());
      assertNotSame(msg.getMetadataValue(JMS_MESSAGE_ID), jmsMsg.getJMSMessageID());
      assertNotSame(msg.getMetadataValue(JMS_EXPIRATION), jmsMsg.getJMSExpiration());
      assertNotSame(msg.getMetadataValue(JMS_DELIVERY_MODE), jmsMsg.getJMSDeliveryMode());
      assertJmsProperties(jmsMsg);
    }
    finally {
      stop(trans);
    }

  }

  @Test
  public void testMoveMetadataAdaptrisMessageToJmsMessage() throws Exception {
    MessageTypeTranslatorImp trans = createTranslator();
    try {
      AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage();

      addMetadata(msg);
      Session session = activeMqBroker.createConnection().createSession(false, Session.CLIENT_ACKNOWLEDGE);
      start(trans, session);

      Message jmsMsg = trans.translate(msg);
      assertJmsProperties(jmsMsg);
    }
    finally {
      stop(trans);

    }
  }

  @Test
  public void testMoveMetadata_AdaptrisMessageToJmsMessage_RemoveAll() throws Exception {
    MessageTypeTranslatorImp trans =
        createTranslator().withMetadataFilter(new RemoveAllMetadataFilter());
    try {
      AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage();

      addMetadata(msg);
      Session session = activeMqBroker.createConnection().createSession(false, Session.CLIENT_ACKNOWLEDGE);
      start(trans, session);

      Message jmsMsg = trans.translate(msg);
      assertNull(jmsMsg.getStringProperty(STRING_METADATA));
      assertNull(jmsMsg.getStringProperty(BOOLEAN_METADATA));
      assertNull(jmsMsg.getStringProperty(INTEGER_METADATA));
    }
    finally {
      stop(trans);

    }
  }

  @Test
  public void testMoveMetadata_AdaptrisMessageToJmsMessage_WithFilter() throws Exception {
    MessageTypeTranslatorImp trans = createTranslator();
    RegexMetadataFilter regexp = new RegexMetadataFilter();
    regexp.addExcludePattern("IntegerMetadataKey");
    trans.setMetadataFilter(regexp);
    try {
      AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage();
      addMetadata(msg);
      Session session = activeMqBroker.createConnection().createSession(false, Session.CLIENT_ACKNOWLEDGE);
      start(trans, session);
      Message jmsMsg = trans.translate(msg);
      assertEquals(STRING_VALUE, jmsMsg.getStringProperty(STRING_METADATA));
      assertEquals(BOOLEAN_VALUE, jmsMsg.getStringProperty(BOOLEAN_METADATA));
      assertEquals(Boolean.valueOf(BOOLEAN_VALUE).booleanValue(), jmsMsg.getBooleanProperty(BOOLEAN_METADATA));
      assertNull(jmsMsg.getStringProperty(INTEGER_METADATA));
    }
    finally {
      stop(trans);
    }
  }

  @Test
  public void testBug895() throws Exception {
    MessageTypeTranslatorImp trans = createTranslator();
    try {
      AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage();

      msg.addMetadata(JmsConstants.JMS_PRIORITY, "9");
      msg.addMetadata(JmsConstants.JMS_TYPE, "idaho");
      Session session = activeMqBroker.createConnection().createSession(false, Session.CLIENT_ACKNOWLEDGE);
      trans.setMoveJmsHeaders(true);
      start(trans, session);

      Message jmsMsg = trans.translate(msg);

      assertNotSame(jmsMsg.getJMSPriority(), 9);
      assertEquals("idaho", jmsMsg.getJMSType());
    }
    finally {
      stop(trans);
    }
  }

  public static void assertMetadata(AdaptrisMessage msg) {
    assertMetadata(msg, new MetadataElement(STRING_METADATA, STRING_VALUE));
    assertMetadata(msg, new MetadataElement(BOOLEAN_METADATA, BOOLEAN_VALUE));
    assertMetadata(msg, new MetadataElement(INTEGER_METADATA, INTEGER_VALUE));
  }

  protected static void assertMetadata(AdaptrisMessage msg, MetadataElement e) {
    assertTrue(msg.headersContainsKey(e.getKey()));
    assertEquals(e.getValue(), msg.getMetadataValue(e.getKey()));
  }

  public static void assertJmsProperties(Message jmsMsg) throws JMSException {
    assertEquals(STRING_VALUE, jmsMsg.getStringProperty(STRING_METADATA));
    assertEquals(BOOLEAN_VALUE, jmsMsg.getStringProperty(BOOLEAN_METADATA));
    assertEquals(Boolean.valueOf(BOOLEAN_VALUE).booleanValue(), jmsMsg.getBooleanProperty(BOOLEAN_METADATA)); // default
    assertEquals(INTEGER_VALUE, jmsMsg.getStringProperty(INTEGER_METADATA));
    assertEquals(Integer.valueOf(INTEGER_VALUE).intValue(), jmsMsg.getIntProperty(INTEGER_METADATA));
  }

  public static void addMetadata(AdaptrisMessage msg) {
    msg.addMetadata(STRING_METADATA, STRING_VALUE);
    msg.addMetadata(BOOLEAN_METADATA, BOOLEAN_VALUE);
    msg.addMetadata(INTEGER_METADATA, INTEGER_VALUE);
  }

  public static void addRedundantJmsHeaders(AdaptrisMessage msg) {
    String[] RESERVED_JMS =
      {
          JMS_CORRELATION_ID, JMS_TYPE, JMS_TIMESTAMP, JMS_REPLY_TO, JMS_REDELIVERED, JMS_PRIORITY, JMS_MESSAGE_ID, JMS_EXPIRATION,
          JMS_DELIVERY_MODE, JMS_DESTINATION
      };
    for (String key : RESERVED_JMS) {
      msg.addMetadata(key, "XXX");
    }

  }

  public static void addProperties(Message jmsMsg) throws JMSException {
    jmsMsg.setStringProperty(STRING_METADATA, STRING_VALUE);
    jmsMsg.setBooleanProperty(BOOLEAN_METADATA, Boolean.valueOf(BOOLEAN_VALUE).booleanValue());
    jmsMsg.setIntProperty(INTEGER_METADATA, Integer.valueOf(INTEGER_VALUE).intValue());
  }

  public static void start(MessageTypeTranslatorImp trans, Session session) throws Exception {
    trans.registerSession(session);
    trans.registerMessageFactory(new DefaultMessageFactory());
    LifecycleHelper.init(trans);
    LifecycleHelper.start(trans);
  }

  public static void stop(MessageTypeTranslatorImp trans) {
    LifecycleHelper.stop(trans);
    LifecycleHelper.close(trans);
  }
}
