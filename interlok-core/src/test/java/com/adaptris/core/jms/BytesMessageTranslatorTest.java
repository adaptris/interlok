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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.anyByte;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import java.io.ByteArrayOutputStream;
import java.util.Arrays;
import javax.jms.BytesMessage;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageEOFException;
import javax.jms.Session;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.core.MetadataElement;
import com.adaptris.core.jms.activemq.EmbeddedActiveMq;
import com.adaptris.core.metadata.RegexMetadataFilter;
import com.adaptris.core.metadata.RemoveAllMetadataFilter;
import com.adaptris.core.stubs.DefectiveMessageFactory;

@SuppressWarnings("deprecation")
public class BytesMessageTranslatorTest extends GenericMessageTypeTranslatorCase {

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
  
  private static byte[] BYTES = new byte[256]; {
    for(int i=0; i<BYTES.length; i++) {
      BYTES[i] = (byte)i;
    }
  }
  
  private static byte[] BYTES_ALT = new byte[256]; {
    for (int i = 0, j = -10; i < BYTES_ALT.length; i++, j++) {
      BYTES_ALT[i] = (byte) j;
    }
  }

  // We aren't actually producing the message, so we have to
  // switch to read-only mode.
  @Override
  @Test
  public void testMoveMetadataJmsMessageToAdaptrisMessage() throws Exception {
    MessageTypeTranslatorImp trans = createTranslator();
    try {
      Session session = activeMqBroker.createConnection().createSession(false, Session.CLIENT_ACKNOWLEDGE);
      BytesMessage jmsMsg = createMessage(session);

      addProperties(jmsMsg);
      start(trans, session);
      // We aren't actually producing the message, so we have to
      // switch to read-only mode.
      jmsMsg.reset();
      AdaptrisMessage msg = trans.translate(jmsMsg);
      assertMetadata(msg);
    }
    finally {
      stop(trans);
    }
  }

  // We aren't actually producing the message, so we have to
  // switch to read-only mode.
  @Override
  @Test
  public void testMoveJmsHeadersJmsMessageToAdaptrisMessage() throws Exception {
    MessageTypeTranslatorImp trans = createTranslator();
    try {
      Session session = activeMqBroker.createConnection().createSession(false, Session.CLIENT_ACKNOWLEDGE);
      BytesMessage jmsMsg = createMessage(session);
      jmsMsg.setJMSCorrelationID("ABC");
      jmsMsg.setJMSDeliveryMode(1);
      jmsMsg.setJMSPriority(4);
      addProperties(jmsMsg);
      long timestamp = System.currentTimeMillis();
      jmsMsg.setJMSTimestamp(timestamp);

      trans.setMoveJmsHeaders(true);
      start(trans, session);
      // We aren't actually producing the message, so we have to
      // switch to read-only mode.

      jmsMsg.reset();

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

  @Override
  @Test
  public void testMoveMetadataJmsMessageToAdaptrisMessage_RemoveAllFilter() throws Exception {
    MessageTypeTranslatorImp trans = createTranslator();
    trans.setMetadataFilter(new RemoveAllMetadataFilter());
    try {
      Session session = activeMqBroker.createConnection().createSession(false, Session.CLIENT_ACKNOWLEDGE);
      BytesMessage jmsMsg = createMessage(session);
      addProperties(jmsMsg);
      start(trans, session);
      // We aren't actually producing the message, so we have to
      // switch to read-only mode.
      jmsMsg.reset();
      AdaptrisMessage msg = trans.translate(jmsMsg);
      assertFalse(msg.containsKey(INTEGER_METADATA));
      assertFalse(msg.containsKey(STRING_METADATA));
      assertFalse(msg.containsKey(BOOLEAN_METADATA));
    }
    finally {
      stop(trans);
    }
  }

  @Override
  @Test
  public void testMoveMetadata_JmsMessageToAdaptrisMessage_WithFilter() throws Exception {
    MessageTypeTranslatorImp trans = createTranslator();
    RegexMetadataFilter regexp = new RegexMetadataFilter();
    regexp.addExcludePattern("IntegerMetadataKey");
    trans.setMetadataFilter(regexp);
    try {
      Session session = activeMqBroker.createConnection().createSession(false, Session.CLIENT_ACKNOWLEDGE);
      BytesMessage jmsMsg = createMessage(session);
      addProperties(jmsMsg);
      start(trans, session);
      // We aren't actually producing the message, so we have to
      // switch to read-only mode.
      jmsMsg.reset();

      AdaptrisMessage msg = trans.translate(jmsMsg);
      assertMetadata(msg, new MetadataElement(STRING_METADATA, STRING_VALUE));
      assertMetadata(msg, new MetadataElement(BOOLEAN_METADATA, BOOLEAN_VALUE));
      assertFalse(msg.containsKey(INTEGER_METADATA));
    }
    finally {
      stop(trans);
    }

  }

  @Test
  public void testBytesMessageToAdaptrisMessage() throws Exception {
    MessageTypeTranslatorImp trans = createTranslator();
    try {
      Session session = activeMqBroker.createConnection().createSession(false, Session.CLIENT_ACKNOWLEDGE);
      BytesMessage jmsMsg = session.createBytesMessage();
      jmsMsg.writeBytes(BYTES);
      addProperties(jmsMsg);
      start(trans, session);
      jmsMsg.reset();
      AdaptrisMessage msg = trans.translate(jmsMsg);
      assertMetadata(msg);
      assertTrue("Payload is not equal", Arrays.equals(BYTES, msg.getPayload()));
    }
    finally {
      stop(trans);
    }
  }

  @Test
  public void testBytesMessageToAdaptrisMessage_Alt() throws Exception {
    MessageTypeTranslatorImp trans = createTranslator();
    try {
      Session session = activeMqBroker.createConnection().createSession(false, Session.CLIENT_ACKNOWLEDGE);
      BytesMessage jmsMsg = session.createBytesMessage();
      jmsMsg.writeBytes(BYTES_ALT);
      addProperties(jmsMsg);
      start(trans, session);
      jmsMsg.reset();
      AdaptrisMessage msg = trans.translate(jmsMsg);
      assertMetadata(msg);
      assertTrue(Arrays.equals(BYTES_ALT, msg.getPayload()));
    } finally {
      stop(trans);
    }
  }


  @Test
  public void testBytesMessageToAdaptrisMessage_StreamFailure() throws Exception {
    MessageTypeTranslatorImp trans = createTranslator();
    try {
      Session session = activeMqBroker.createConnection().createSession(false, Session.CLIENT_ACKNOWLEDGE);
      BytesMessage jmsMsg = createMessage(session);
      jmsMsg.writeBytes(TEXT.getBytes());
      start(trans, session);
      trans.registerMessageFactory(new DefectiveMessageFactory());
      jmsMsg.reset();
      try {
        trans.translate(jmsMsg);
        fail();
      } catch (JMSException expected) {

      }
    } finally {
      stop(trans);
    }
  }

  @Test
  public void testBytesMessageToAdaptrisMessage_StreamFailure_CheckedJMSException() throws Exception {

    BytesMessage jmsMsg = Mockito.mock(BytesMessage.class);
    Session session = Mockito.mock(Session.class);
    doThrow(new JMSException(testName.getMethodName())).when(jmsMsg).readByte();
    when(session.createBytesMessage()).thenReturn(jmsMsg);
    BytesMessageTranslator trans = new BytesMessageTranslator() {
      @Override
      long streamThreshold() {
        return TEXT.length() - 1;
      }
    };
    try {
      start(trans, session);
      try {
        trans.translate(jmsMsg);
        fail();
      } catch (JMSException expected) {

      }
    } finally {
      stop(trans);
    }
  }

  @Test
  public void testAdaptrisMessageToBytesMessage() throws Exception {
    MessageTypeTranslatorImp trans = createTranslator();
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
  public void testAdaptrisMessageToBytesMessage_StreamFailure() throws Exception {
    BytesMessageTranslator trans = new BytesMessageTranslator() {
      @Override
      long streamThreshold() {
        return TEXT.length() - 1;
      }
    };
    try {
      Session session = activeMqBroker.createConnection().createSession(false, Session.CLIENT_ACKNOWLEDGE);
      start(trans, session);
      AdaptrisMessage msg = new DefectiveMessageFactory().newMessage(TEXT);
      addMetadata(msg);
      try {
        trans.translate(msg);
        fail();
      } catch (JMSException expected) {
      }
    } finally {
      stop(trans);
    }
  }

  @Test
  public void testAdaptrisMessageToBytesMessage_StreamFailure_CheckedJMSException() throws Exception {

    BytesMessage jmsMsg = Mockito.mock(BytesMessage.class);
    Session session = Mockito.mock(Session.class);
    doThrow(new JMSException(testName.getMethodName())).when(jmsMsg).writeByte(anyByte());
    when(session.createBytesMessage()).thenReturn(jmsMsg);
    BytesMessageTranslator trans = new BytesMessageTranslator() {
      @Override
      long streamThreshold() {
        return TEXT.length() - 1;
      }
    };
    try {
      start(trans, session);
      AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(TEXT);
      try {
        trans.translate(msg);
        fail();
      } catch (JMSException expected) {
      }
    } finally {
      stop(trans);
    }
  }



  @Test
  public void testAdaptrisMessageToBytesMessage_ExceedsThreshold() throws Exception {
    BytesMessageTranslator trans = new BytesMessageTranslator() {

      @Override
      long streamThreshold() {
        return TEXT.length() -1;
      }
    };
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
  protected BytesMessage createMessage(Session session) throws Exception {
    return session.createBytesMessage();
  }

  /**
   * @see com.adaptris.core.jms.MessageTypeTranslatorCase#createTranslator()
   */
  @Override
  protected MessageTypeTranslatorImp createTranslator() throws Exception {
    return new BytesMessageTranslator();
  }

}
