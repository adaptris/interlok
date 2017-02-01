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

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.adaptris.core.AdaptrisMarshaller;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.core.BaseCase;
import com.adaptris.core.ConfiguredProduceDestination;
import com.adaptris.core.DefaultMarshaller;
import com.adaptris.core.DefaultMessageFactory;
import com.adaptris.core.MetadataElement;
import com.adaptris.core.StandaloneProducer;
import com.adaptris.core.jms.activemq.EmbeddedActiveMq;
import com.adaptris.core.metadata.NoOpMetadataFilter;
import com.adaptris.core.metadata.RegexMetadataFilter;
import com.adaptris.core.metadata.RemoveAllMetadataFilter;
import com.adaptris.core.util.LifecycleHelper;

/**
 */
@SuppressWarnings("deprecation")
public abstract class MessageTypeTranslatorCase extends BaseCase {


  public static final String INTEGER_VALUE = "-1";
  public static final String BOOLEAN_VALUE = "true";
  public static final String STRING_VALUE = "value";
  public static final String INTEGER_METADATA = "IntegerMetadataKey";
  public static final String BOOLEAN_METADATA = "BooleanMetadataKey";
  public static final String STRING_METADATA = "StringMetadataKey";
  public static final String TEXT = "The quick brown fox";
  public static final String TEXT2 = "jumps over the lazy dog";

  protected transient Log log = LogFactory.getLog(this.getClass());

  public MessageTypeTranslatorCase(String name) {
    super(name);
  }

  @Override
  protected void setUp() throws Exception {
  }

  @Override
  protected void tearDown() throws Exception {
  }

  protected abstract MessageTypeTranslatorImp createTranslator() throws Exception;

  protected abstract Message createMessage(Session session) throws Exception;

  private StandaloneProducer createProducer(MessageTypeTranslator mt) throws Exception {
    PasProducer producer = new PasProducer(new ConfiguredProduceDestination(getName()));
    producer.setMessageTranslator(mt);
    return new StandaloneProducer(new JmsConnection(), producer);
  }

  public void testRoundTrip() throws Exception {
    MessageTypeTranslatorImp translator = createTranslator();
    translator.setMoveJmsHeaders(true);
    translator.setMetadataFilter(new NoOpMetadataFilter());
    translator.setReportAllErrors(true);
    StandaloneProducer p1 = createProducer(translator);
    StandaloneProducer p2 = roundTrip(p1);
    assertRoundtripEquality(p1, p2);
  }

  protected StandaloneProducer roundTrip(StandaloneProducer src) throws Exception {
    AdaptrisMarshaller m = DefaultMarshaller.getDefaultMarshaller();
    String xml = m.marshal(src);
    return (StandaloneProducer) m.unmarshal(xml);
  }

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

  public void testMoveMetadataJmsMessageToAdaptrisMessage() throws Exception {
    EmbeddedActiveMq broker = new EmbeddedActiveMq();
    MessageTypeTranslatorImp trans = createTranslator();
    try {
      broker.start();
      Session session = broker.createConnection().createSession(false, Session.CLIENT_ACKNOWLEDGE);
      Message jmsMsg = createMessage(session);
      addProperties(jmsMsg);
      start(trans, session);
      AdaptrisMessage msg = trans.translate(jmsMsg);
      assertMetadata(msg);
    }
    finally {
      stop(trans);
      broker.destroy();
    }

  }

  public void testMoveMetadataJmsMessageToAdaptrisMessage_RemoveAllFilter() throws Exception {
    EmbeddedActiveMq broker = new EmbeddedActiveMq();
    MessageTypeTranslatorImp trans = createTranslator();
    trans.setMetadataFilter(new RemoveAllMetadataFilter());
    try {
      broker.start();
      Session session = broker.createConnection().createSession(false, Session.CLIENT_ACKNOWLEDGE);
      Message jmsMsg = createMessage(session);
      addProperties(jmsMsg);
      start(trans, session);
      AdaptrisMessage msg = trans.translate(jmsMsg);
      assertFalse(msg.containsKey(INTEGER_METADATA));
      assertFalse(msg.containsKey(STRING_METADATA));
      assertFalse(msg.containsKey(BOOLEAN_METADATA));
    }
    finally {
      stop(trans);
      broker.destroy();
    }
  }

  public void testMoveMetadata_JmsMessageToAdaptrisMessage_WithFilter() throws Exception {
    EmbeddedActiveMq broker = new EmbeddedActiveMq();
    MessageTypeTranslatorImp trans = createTranslator();
    RegexMetadataFilter regexp = new RegexMetadataFilter();
    regexp.addExcludePattern("IntegerMetadataKey");
    trans.setMetadataFilter(regexp);
    try {
      broker.start();
      Session session = broker.createConnection().createSession(false, Session.CLIENT_ACKNOWLEDGE);
      Message jmsMsg = createMessage(session);
      addProperties(jmsMsg);
      start(trans, session);
      AdaptrisMessage msg = trans.translate(jmsMsg);
      assertMetadata(msg, new MetadataElement(STRING_METADATA, STRING_VALUE));
      assertMetadata(msg, new MetadataElement(BOOLEAN_METADATA, BOOLEAN_VALUE));
      assertFalse(msg.containsKey(INTEGER_METADATA));
    }
    finally {
      stop(trans);
      broker.destroy();
    }

  }

  public void testMoveJmsHeadersJmsMessageToAdaptrisMessage() throws Exception {
    EmbeddedActiveMq broker = new EmbeddedActiveMq();
    MessageTypeTranslatorImp trans = createTranslator();
    try {
      Session session = broker.createConnection().createSession(false, Session.CLIENT_ACKNOWLEDGE);
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
      broker.destroy();
    }

  }

  public void testMoveJmsHeadersAdaptrisMessageToJmsMessage() throws Exception {
    EmbeddedActiveMq broker = new EmbeddedActiveMq();
    MessageTypeTranslatorImp trans = createTranslator();
    try {
      broker.start();
      Session session = broker.createConnection().createSession(false, Session.CLIENT_ACKNOWLEDGE);

      AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage();
      addMetadata(msg);
      addRedundantJmsHeaders(msg);

      trans.setMoveJmsHeaders(true);
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
      broker.destroy();
    }

  }

  public void testMoveMetadataAdaptrisMessageToJmsMessage() throws Exception {
    EmbeddedActiveMq broker = new EmbeddedActiveMq();
    MessageTypeTranslatorImp trans = createTranslator();
    try {
      broker.start();
      AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage();

      addMetadata(msg);
      Session session = broker.createConnection().createSession(false, Session.CLIENT_ACKNOWLEDGE);
      start(trans, session);

      Message jmsMsg = trans.translate(msg);
      assertJmsProperties(jmsMsg);
    }
    finally {
      stop(trans);
      broker.destroy();

    }
  }

  public void testMoveMetadata_AdaptrisMessageToJmsMessage_RemoveAll() throws Exception {
    EmbeddedActiveMq broker = new EmbeddedActiveMq();
    MessageTypeTranslatorImp trans = createTranslator();
    trans.setMetadataFilter(new RemoveAllMetadataFilter());
    try {
      broker.start();
      AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage();

      addMetadata(msg);
      Session session = broker.createConnection().createSession(false, Session.CLIENT_ACKNOWLEDGE);
      start(trans, session);

      Message jmsMsg = trans.translate(msg);
      assertNull(jmsMsg.getStringProperty(STRING_METADATA));
      assertNull(jmsMsg.getStringProperty(BOOLEAN_METADATA));
      assertNull(jmsMsg.getStringProperty(INTEGER_METADATA));
    }
    finally {
      stop(trans);
      broker.destroy();

    }
  }

  public void testMoveMetadata_AdaptrisMessageToJmsMessage_WithFilter() throws Exception {

    EmbeddedActiveMq broker = new EmbeddedActiveMq();
    MessageTypeTranslatorImp trans = createTranslator();
    RegexMetadataFilter regexp = new RegexMetadataFilter();
    regexp.addExcludePattern("IntegerMetadataKey");
    trans.setMetadataFilter(regexp);
    try {
      broker.start();
      AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage();
      addMetadata(msg);
      Session session = broker.createConnection().createSession(false, Session.CLIENT_ACKNOWLEDGE);
      start(trans, session);
      Message jmsMsg = trans.translate(msg);
      assertEquals(STRING_VALUE, jmsMsg.getStringProperty(STRING_METADATA));
      assertEquals(BOOLEAN_VALUE, jmsMsg.getStringProperty(BOOLEAN_METADATA));
      assertEquals(Boolean.valueOf(BOOLEAN_VALUE).booleanValue(), jmsMsg.getBooleanProperty(BOOLEAN_METADATA));
      assertNull(jmsMsg.getStringProperty(INTEGER_METADATA));
    }
    finally {
      stop(trans);
      broker.destroy();
    }
  }

  public void testBug895() throws Exception {
    EmbeddedActiveMq broker = new EmbeddedActiveMq();
    MessageTypeTranslatorImp trans = createTranslator();
    try {
      broker.start();
      AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage();

      msg.addMetadata(JmsConstants.JMS_PRIORITY, "9");
      msg.addMetadata(JmsConstants.JMS_TYPE, "idaho");
      Session session = broker.createConnection().createSession(false, Session.CLIENT_ACKNOWLEDGE);
      trans.setMoveJmsHeaders(true);
      start(trans, session);

      Message jmsMsg = trans.translate(msg);

      assertNotSame("JMS Priorities should be different", jmsMsg.getJMSPriority(), 9);
      assertEquals("JMSType should be equal", "idaho", jmsMsg.getJMSType());
    }
    finally {
      stop(trans);
      broker.destroy();

    }
  }

  public static void assertMetadata(AdaptrisMessage msg) {
    assertMetadata(msg, new MetadataElement(STRING_METADATA, STRING_VALUE));
    assertMetadata(msg, new MetadataElement(BOOLEAN_METADATA, BOOLEAN_VALUE));
    assertMetadata(msg, new MetadataElement(INTEGER_METADATA, INTEGER_VALUE));
  }

  protected static void assertMetadata(AdaptrisMessage msg, MetadataElement e) {
    assertTrue(msg.containsKey(e.getKey()));
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
