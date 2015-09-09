package com.adaptris.core.jms;

import static com.adaptris.core.jms.ObjectMessageTranslatorTest.assertException;
import static com.adaptris.core.jms.ObjectMessageTranslatorTest.readException;
import static com.adaptris.core.jms.ObjectMessageTranslatorTest.write;

import java.io.ByteArrayOutputStream;

import javax.jms.BytesMessage;
import javax.jms.JMSException;
import javax.jms.MapMessage;
import javax.jms.Message;
import javax.jms.MessageEOFException;
import javax.jms.ObjectMessage;
import javax.jms.Session;
import javax.jms.TextMessage;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.core.MetadataElement;
import com.adaptris.core.jms.activemq.EmbeddedActiveMq;

public class AutoConvertMessageTranslatorTest extends MessageTypeTranslatorCase {

  private static final String MAP_MSG_PREFIX = "mapMsg.";
  private static final String[] KEYS =
  {
      MAP_MSG_PREFIX + INTEGER_METADATA, MAP_MSG_PREFIX + BOOLEAN_METADATA, MAP_MSG_PREFIX + STRING_METADATA
  };

  private static final String[] VALUES =
  {
      INTEGER_VALUE, BOOLEAN_VALUE, STRING_VALUE
  };

  public AutoConvertMessageTranslatorTest(String name) {
    super(name);
  }

  public void testBytesMessageToAdaptrisMessage() throws Exception {
    EmbeddedActiveMq broker = new EmbeddedActiveMq();

    AutoConvertMessageTranslator trans = new AutoConvertMessageTranslator();
    try {
      broker.start();
      Session session = broker.createConnection().createSession(false, Session.CLIENT_ACKNOWLEDGE);
      start(trans, session);

      BytesMessage jmsMsg = session.createBytesMessage();
      addProperties(jmsMsg);
      jmsMsg.writeBytes(TEXT.getBytes());
      jmsMsg.reset();

      AdaptrisMessage msg = trans.translate(jmsMsg);
      assertMetadata(msg);
      assertEquals(TEXT, msg.getStringPayload());
    }
    finally {
      stop(trans);
      broker.destroy();
    }

  }

  public void testAdaptrisMessageToBytesMessage() throws Exception {
    EmbeddedActiveMq broker = new EmbeddedActiveMq();
    AutoConvertMessageTranslator trans = new AutoConvertMessageTranslator();
    trans.setJmsOutputType(AutoConvertMessageTranslator.SupportedMessageType.Bytes.name());
    try {
      broker.start();
      Session session = broker.createConnection().createSession(false, Session.CLIENT_ACKNOWLEDGE);
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
      broker.destroy();
    }
  }

  public void testAdaptrisMessageToTextMessage() throws Exception {
    EmbeddedActiveMq broker = new EmbeddedActiveMq();
    AutoConvertMessageTranslator trans = new AutoConvertMessageTranslator();
    trans.setJmsOutputType(AutoConvertMessageTranslator.SupportedMessageType.Text.name());
    try {
      Session session = broker.createConnection().createSession(false, Session.CLIENT_ACKNOWLEDGE);
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
      broker.destroy();

    }
  }

  public void testMapMessageToAdaptrisMessage() throws Exception {
    EmbeddedActiveMq broker = new EmbeddedActiveMq();

    AutoConvertMessageTranslator trans = new AutoConvertMessageTranslator();
    try {
      broker.start();
      Session session = broker.createConnection().createSession(false, Session.CLIENT_ACKNOWLEDGE);
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
      broker.destroy();
    }
  }

  public void testAdaptrisMessageToMapMessage() throws Exception {
    EmbeddedActiveMq broker = new EmbeddedActiveMq();
    AutoConvertMessageTranslator trans = new AutoConvertMessageTranslator();
    trans.setJmsOutputType(AutoConvertMessageTranslator.SupportedMessageType.Map.name());
    try {
      broker.start();
      Session session = broker.createConnection().createSession(false, Session.CLIENT_ACKNOWLEDGE);
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
      broker.destroy();
    }
  }

  public void testObjectMessageToAdaptrisMessage() throws Exception {
    EmbeddedActiveMq broker = new EmbeddedActiveMq();

    AutoConvertMessageTranslator trans = new AutoConvertMessageTranslator();
    try {
      broker.start();
      Session session = broker.createConnection().createSession(false, Session.CLIENT_ACKNOWLEDGE);
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
      broker.destroy();
    }
  }

  public void testAdaptrisMessageToObjectMessage() throws Exception {
    EmbeddedActiveMq broker = new EmbeddedActiveMq();
    AutoConvertMessageTranslator trans = new AutoConvertMessageTranslator();
    trans.setJmsOutputType(AutoConvertMessageTranslator.SupportedMessageType.Object.name());
    try {
      broker.start();
      Session session = broker.createConnection().createSession(false, Session.CLIENT_ACKNOWLEDGE);
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
      broker.destroy();
    }
  }
  
  public void testMessageToAdaptrisMessageWithFallback() throws Exception {
    EmbeddedActiveMq broker = new EmbeddedActiveMq();

    AutoConvertMessageTranslator trans = new AutoConvertMessageTranslator();
    try {
      broker.start();
      Session session = broker.createConnection().createSession(false, Session.CLIENT_ACKNOWLEDGE);
      start(trans, session);
      Message jmsMsg = session.createMessage();

      addProperties(jmsMsg);
      
      AdaptrisMessage msg = trans.translate(jmsMsg);
      assertMetadata(msg);
    }
    finally {
      stop(trans);
      broker.destroy();
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
