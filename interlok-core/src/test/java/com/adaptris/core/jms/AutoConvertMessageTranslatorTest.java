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

import static com.adaptris.core.jms.ObjectMessageTranslatorTest.assertException;
import static com.adaptris.core.jms.ObjectMessageTranslatorTest.readException;
import static com.adaptris.core.jms.ObjectMessageTranslatorTest.write;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;

import javax.jms.BytesMessage;
import javax.jms.JMSException;
import javax.jms.MapMessage;
import javax.jms.Message;
import javax.jms.MessageEOFException;
import javax.jms.ObjectMessage;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.core.MetadataElement;
import com.adaptris.core.jms.activemq.EmbeddedActiveMq;

public class AutoConvertMessageTranslatorTest extends GenericMessageTypeTranslatorCase {
    
  private static final String ORIGINAL_MESSGAE_TYPE_KEY = "adpmessagetype";

  private static final String MAP_MSG_PREFIX = "mapMsg.";
  private static final String[] KEYS =
  {
      MAP_MSG_PREFIX + INTEGER_METADATA, MAP_MSG_PREFIX + BOOLEAN_METADATA, MAP_MSG_PREFIX + STRING_METADATA
  };

  private static final String[] VALUES =
  {
      INTEGER_VALUE, BOOLEAN_VALUE, STRING_VALUE
  };

  @BeforeClass
  public static void setUpAll() throws Exception {
    activeMqBroker = new EmbeddedActiveMq();
    activeMqBroker.start();
  }
  
  @AfterClass
  public static void tearDownAll() throws Exception {
    if(activeMqBroker != null)
      activeMqBroker.destroy();
  }

  @Test
  public void testConvertFromConsumeTypeBytes() throws Exception {
    AutoConvertMessageTranslator trans = new AutoConvertMessageTranslator();
    trans.setConvertBackToConsumedType(true);
    trans.setRemoveOriginalMessageTypeKey(false);
    try {
      Session session = activeMqBroker.createConnection().createSession(false, Session.CLIENT_ACKNOWLEDGE);
      start(trans, session);

      BytesMessage jmsMsg = session.createBytesMessage();
      addProperties(jmsMsg);
      jmsMsg.writeBytes(TEXT.getBytes());
      jmsMsg.reset();

      AdaptrisMessage msg = trans.translate(jmsMsg);
      Message producedMessage = trans.translate(msg);
      
      assertEquals("Bytes", msg.getMetadataValue(ORIGINAL_MESSGAE_TYPE_KEY));
      assertTrue(producedMessage instanceof BytesMessage);
    }
    finally {
      stop(trans);
    }

  }

  @Test
  public void testConvertFromConsumeTypeText() throws Exception {
    AutoConvertMessageTranslator trans = new AutoConvertMessageTranslator();
    trans.setConvertBackToConsumedType(true);
    trans.setRemoveOriginalMessageTypeKey(false);
    try {
      Session session = activeMqBroker.createConnection().createSession(false, Session.CLIENT_ACKNOWLEDGE);
      start(trans, session);

      TextMessage jmsMsg = session.createTextMessage();
      addProperties(jmsMsg);
      jmsMsg.setText(TEXT);

      AdaptrisMessage msg = trans.translate(jmsMsg);
      Message producedMessage = trans.translate(msg);
      
      assertTrue(producedMessage.propertyExists(ORIGINAL_MESSGAE_TYPE_KEY));
      assertEquals("Text", msg.getMetadataValue(ORIGINAL_MESSGAE_TYPE_KEY));
      assertTrue(producedMessage instanceof TextMessage);
    }
    finally {
      stop(trans);
    }

  }
  
  @Test
  public void testConvertFromConsumeTypeTextRemoveKeyAfter() throws Exception {
    AutoConvertMessageTranslator trans = new AutoConvertMessageTranslator();
    trans.setConvertBackToConsumedType(true);
    trans.setRemoveOriginalMessageTypeKey(true);
    
    assertTrue(trans.getRemoveOriginalMessageTypeKey());
    
    try {
      Session session = activeMqBroker.createConnection().createSession(false, Session.CLIENT_ACKNOWLEDGE);
      start(trans, session);

      TextMessage jmsMsg = session.createTextMessage();
      addProperties(jmsMsg);
      jmsMsg.setText(TEXT);

      AdaptrisMessage msg = trans.translate(jmsMsg);
      Message producedMessage = trans.translate(msg);
      
      assertFalse(producedMessage.propertyExists(ORIGINAL_MESSGAE_TYPE_KEY));
      
      assertFalse(msg.headersContainsKey(ORIGINAL_MESSGAE_TYPE_KEY));
      assertTrue(producedMessage instanceof TextMessage);
    }
    finally {
      stop(trans);
    }

  }

  @Test
  public void testConvertFromConsumeTypeTextDefaultRemoveKeyAfter() throws Exception {
    AutoConvertMessageTranslator trans = new AutoConvertMessageTranslator();
    trans.setConvertBackToConsumedType(true);
    
    assertNull(trans.getRemoveOriginalMessageTypeKey()); // defaults to true if null.
    
    try {
      Session session = activeMqBroker.createConnection().createSession(false, Session.CLIENT_ACKNOWLEDGE);
      start(trans, session);

      TextMessage jmsMsg = session.createTextMessage();
      addProperties(jmsMsg);
      jmsMsg.setText(TEXT);

      AdaptrisMessage msg = trans.translate(jmsMsg);
      Message producedMessage = trans.translate(msg);
      
      assertFalse(producedMessage.propertyExists(ORIGINAL_MESSGAE_TYPE_KEY));
      
      assertFalse(msg.headersContainsKey(ORIGINAL_MESSGAE_TYPE_KEY));
      assertTrue(producedMessage instanceof TextMessage);
    }
    finally {
      stop(trans);
    }

  }
  
  @Test
  public void testConvertFromConsumeTypeMap() throws Exception {
    AutoConvertMessageTranslator trans = new AutoConvertMessageTranslator();
    trans.setConvertBackToConsumedType(true);
    trans.setRemoveOriginalMessageTypeKey(false);
    try {
      Session session = activeMqBroker.createConnection().createSession(false, Session.CLIENT_ACKNOWLEDGE);
      start(trans, session);

      MapMessage jmsMsg = session.createMapMessage();
      addProperties(jmsMsg);
      addToMapMessage(jmsMsg);

      AdaptrisMessage msg = trans.translate(jmsMsg);
      Message producedMessage = trans.translate(msg);
      
      assertEquals("Map", msg.getMetadataValue(ORIGINAL_MESSGAE_TYPE_KEY));
      assertTrue(producedMessage instanceof MapMessage);
    }
    finally {
      stop(trans);
    }

  }
  
  @Test
  public void testConvertFromConsumeTypeObject() throws Exception {
    AutoConvertMessageTranslator trans = new AutoConvertMessageTranslator();
    trans.setConvertBackToConsumedType(true);
    trans.setRemoveOriginalMessageTypeKey(false);
    try {
      Session session = activeMqBroker.createConnection().createSession(false, Session.CLIENT_ACKNOWLEDGE);
      start(trans, session);

      ObjectMessage jmsMsg = session.createObjectMessage();
      Exception e = new Exception("This is an Exception that was serialized");
      e.fillInStackTrace();
      jmsMsg.setObject(e);
      addProperties(jmsMsg);

      AdaptrisMessage msg = trans.translate(jmsMsg);
      Message producedMessage = trans.translate(msg);
      
      assertEquals("Object", msg.getMetadataValue(ORIGINAL_MESSGAE_TYPE_KEY));
      assertTrue(producedMessage instanceof ObjectMessage);
    }
    finally {
      stop(trans);
    }

  }
  
  @Test
  public void testConvertFromConsumeTypeBytesNoMetadataKey() throws Exception {
    AutoConvertMessageTranslator trans = new AutoConvertMessageTranslator();
    trans.setConvertBackToConsumedType(true);
    trans.setRemoveOriginalMessageTypeKey(false);
    try {
      Session session = activeMqBroker.createConnection().createSession(false, Session.CLIENT_ACKNOWLEDGE);
      start(trans, session);

      BytesMessage jmsMsg = session.createBytesMessage();
      addProperties(jmsMsg);
      jmsMsg.writeBytes(TEXT.getBytes());
      jmsMsg.reset();

      AdaptrisMessage msg = trans.translate(jmsMsg);
      msg.removeMessageHeader(ORIGINAL_MESSGAE_TYPE_KEY);
      Message producedMessage = trans.translate(msg);
      
      assertFalse(msg.headersContainsKey(ORIGINAL_MESSGAE_TYPE_KEY));
      assertTrue(producedMessage instanceof TextMessage);
    }
    finally {
      stop(trans);
    }

  }
  
  @Test
  public void testConvertFromConsumeTypeBytesIllegalMetadataKey() throws Exception {
    AutoConvertMessageTranslator trans = new AutoConvertMessageTranslator();
    trans.setConvertBackToConsumedType(true);
    try {
      Session session = activeMqBroker.createConnection().createSession(false, Session.CLIENT_ACKNOWLEDGE);
      start(trans, session);

      BytesMessage jmsMsg = session.createBytesMessage();
      addProperties(jmsMsg);
      jmsMsg.writeBytes(TEXT.getBytes());
      jmsMsg.reset();

      AdaptrisMessage msg = trans.translate(jmsMsg);
      msg.addMessageHeader(ORIGINAL_MESSGAE_TYPE_KEY, "xxx.xxx");
      Message producedMessage = trans.translate(msg);
      
      assertEquals("xxx.xxx", msg.getMetadataValue(ORIGINAL_MESSGAE_TYPE_KEY));
      assertTrue(producedMessage instanceof TextMessage);
    }
    finally {
      stop(trans);
    }

  }
  
  @Test
  public void testBytesMessageToAdaptrisMessage() throws Exception {
    AutoConvertMessageTranslator trans = new AutoConvertMessageTranslator();
    try {
      Session session = activeMqBroker.createConnection().createSession(false, Session.CLIENT_ACKNOWLEDGE);
      start(trans, session);

      BytesMessage jmsMsg = session.createBytesMessage();
      addProperties(jmsMsg);
      jmsMsg.writeBytes(TEXT.getBytes());
      jmsMsg.reset();

      AdaptrisMessage msg = trans.translate(jmsMsg);
      assertMetadata(msg);
      assertEquals(TEXT, msg.getContent());
    }
    finally {
      stop(trans);
    }

  }

  @Test
  public void testAdaptrisMessageToBytesMessage() throws Exception {
    AutoConvertMessageTranslator trans = new AutoConvertMessageTranslator();
    trans.setJmsOutputType(AutoConvertMessageTranslator.SupportedMessageType.Bytes.name());
    try {
      Session session = activeMqBroker.createConnection().createSession(false, Session.CLIENT_ACKNOWLEDGE);
      start(trans, session);

      AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(TEXT);
      addMetadata(msg);
      Message jmsMsg = trans.translate(msg);
      assertTrue("jmsMsg instanceof BytesMessage", jmsMsg instanceof BytesMessage);
      ((BytesMessage) jmsMsg).reset();
      assertEquals(TEXT, new String(getBytes((BytesMessage) jmsMsg)));
      assertJmsProperties(jmsMsg);
    }
    finally {
      stop(trans);
    }
  }

  @Test
  public void testAdaptrisMessageToTextMessage() throws Exception {
    AutoConvertMessageTranslator trans = new AutoConvertMessageTranslator();
    trans.setJmsOutputType(AutoConvertMessageTranslator.SupportedMessageType.Text.name());
    try {
      Session session = activeMqBroker.createConnection().createSession(false, Session.CLIENT_ACKNOWLEDGE);
      start(trans, session);
      AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(TEXT);
      addMetadata(msg);
      Message jmsMsg = trans.translate(msg);
      assertTrue("jmsMsg instanceof TextMessage", jmsMsg instanceof TextMessage);
      assertEquals(TEXT, ((TextMessage) jmsMsg).getText());
      assertJmsProperties(jmsMsg);
    }
    finally {
      stop(trans);

    }
  }

  @Test
  public void testMapMessageToAdaptrisMessage() throws Exception {
    AutoConvertMessageTranslator trans = new AutoConvertMessageTranslator();
    try {
      Session session = activeMqBroker.createConnection().createSession(false, Session.CLIENT_ACKNOWLEDGE);
      start(trans, session);

      MapMessage jmsMsg = session.createMapMessage();
      addProperties(jmsMsg);
      addToMapMessage(jmsMsg);

      AdaptrisMessage msg = trans.translate(jmsMsg);
      assertMetadata(msg);
      assertMapData(msg);
    }
    finally {
      stop(trans);
    }
  }

  @Test
  public void testAdaptrisMessageToMapMessage() throws Exception {
    AutoConvertMessageTranslator trans = new AutoConvertMessageTranslator();
    trans.setJmsOutputType(AutoConvertMessageTranslator.SupportedMessageType.Map.name());
    try {
      Session session = activeMqBroker.createConnection().createSession(false, Session.CLIENT_ACKNOWLEDGE);
      start(trans, session);
      AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(TEXT);
      addMetadata(msg);
      Message jmsMsg = trans.translate(msg);
      assertTrue("jmsMsg instanceof TextMessage", jmsMsg instanceof MapMessage);
      assertJmsProperties(jmsMsg);
      assertTrue(((MapMessage) jmsMsg).getString(STRING_METADATA).equals(STRING_VALUE));
      assertTrue(((MapMessage) jmsMsg).getString(BOOLEAN_METADATA).equals(BOOLEAN_VALUE));
      assertTrue(((MapMessage) jmsMsg).getString(INTEGER_METADATA).equals(INTEGER_VALUE));
    }
    finally {
      stop(trans);
    }
  }

  @Test
  public void testObjectMessageToAdaptrisMessage() throws Exception {
    AutoConvertMessageTranslator trans = new AutoConvertMessageTranslator();
    try {
      Session session = activeMqBroker.createConnection().createSession(false, Session.CLIENT_ACKNOWLEDGE);
      start(trans, session);
      ObjectMessage jmsMsg = session.createObjectMessage();
      Exception e = new Exception("This is an Exception that was serialized");
      e.fillInStackTrace();
      jmsMsg.setObject(e);
      addProperties(jmsMsg);

      AdaptrisMessage msg = trans.translate(jmsMsg);
      assertMetadata(msg);
      Exception o = readException(msg);
      assertException(e, o);
    }
    finally {
      stop(trans);
    }
  }

  @Test
  public void testAdaptrisMessageToObjectMessage() throws Exception {
    AutoConvertMessageTranslator trans = new AutoConvertMessageTranslator();
    trans.setJmsOutputType(AutoConvertMessageTranslator.SupportedMessageType.Object.name());
    try {
      Session session = activeMqBroker.createConnection().createSession(false, Session.CLIENT_ACKNOWLEDGE);
      start(trans, session);
      AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage();
      Exception e = new Exception("This is an Exception that was serialized");
      write(e, msg);
      addMetadata(msg);
      Message jmsMsg = trans.translate(msg);
      assertTrue("jmsMsg instanceof ObjectMessage", jmsMsg instanceof ObjectMessage);
      assertJmsProperties(jmsMsg);
      assertException(e, (Exception) ((ObjectMessage) jmsMsg).getObject());
    }
    finally {
      stop(trans);
    }
  }
  
  @Test
  public void testMessageToAdaptrisMessageWithFallback() throws Exception {
    AutoConvertMessageTranslator trans = new AutoConvertMessageTranslator();
    try {
      Session session = activeMqBroker.createConnection().createSession(false, Session.CLIENT_ACKNOWLEDGE);
      start(trans, session);
      Message jmsMsg = session.createMessage();

      addProperties(jmsMsg);
      
      AdaptrisMessage msg = trans.translate(jmsMsg);
      assertMetadata(msg);
    }
    finally {
      stop(trans);
    }
  }
  
  @Test
  public void testAdaptrisMessageToMessageWithFallback() throws Exception {
    AutoConvertMessageTranslator trans = new AutoConvertMessageTranslator();
    trans.setJmsOutputType("xxx");
    try {
      Session session = activeMqBroker.createConnection().createSession(false, Session.CLIENT_ACKNOWLEDGE);
      start(trans, session);
      AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(TEXT);
      
      Message result = trans.translate(msg);
      assertNotNull(result);
    }
    finally {
      stop(trans);
    }
  }

  private static void addToMapMessage(MapMessage msg) throws JMSException {
    for (int i = 0; i < KEYS.length; i++) {
      msg.setString(KEYS[i], VALUES[i]);
    }
  }

  private static void assertMapData(AdaptrisMessage msg) throws JMSException {
    for (int i = 0; i < KEYS.length; i++) {
      assertMetadata(msg, new MetadataElement(KEYS[i], VALUES[i]));
    }
  }

  private static byte[] getBytes(BytesMessage msg) throws JMSException {
    ByteArrayOutputStream payload = new ByteArrayOutputStream();
    while (true) {
      try {
        payload.write(msg.readByte());
      }
      catch (MessageEOFException e) {
        break;
      }
    }
    return payload.toByteArray();
  }

  /**
   * @see com.adaptris.core.jms.MessageTypeTranslatorCase#createMessage(javax.jms.Session)
   */
  @Override
  protected Message createMessage(Session session) throws Exception {
    return session.createTextMessage();
  }

  /**
   * @see com.adaptris.core.jms.MessageTypeTranslatorCase#createTranslator()
   */
  @Override
  protected MessageTypeTranslatorImp createTranslator() throws Exception {
    return new AutoConvertMessageTranslator();
  }

}
