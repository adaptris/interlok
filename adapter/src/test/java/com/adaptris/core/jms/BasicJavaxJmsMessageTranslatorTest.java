package com.adaptris.core.jms;

import javax.jms.BytesMessage;
import javax.jms.MapMessage;
import javax.jms.Message;
import javax.jms.ObjectMessage;
import javax.jms.Session;
import javax.jms.StreamMessage;
import javax.jms.TextMessage;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.core.jms.activemq.EmbeddedActiveMq;

public class BasicJavaxJmsMessageTranslatorTest extends MessageTypeTranslatorCase {
  
  public BasicJavaxJmsMessageTranslatorTest(String name) {
    super(name);
  }

  public void testMessageToAdaptrisMessage() throws Exception {
    EmbeddedActiveMq broker = new EmbeddedActiveMq();
    MessageTypeTranslatorImp trans = this.createTranslator();
    try {
      broker.start();
      Session session = broker.createConnection().createSession(false, Session.CLIENT_ACKNOWLEDGE);
      Message jmsMsg = createMessage(session);
      addProperties(jmsMsg);
      start(trans, session);
      AdaptrisMessage msg = trans.translate(jmsMsg);
      assertMetadata(msg);
      assertEquals(0, msg.getStringPayload().length());
    }
    finally {
      stop(trans);
      broker.destroy();
    }
  }

  public void testAdaptrisMessageToMessage() throws Exception {
    EmbeddedActiveMq broker = new EmbeddedActiveMq();
    MessageTypeTranslatorImp trans = this.createTranslator();
    try {
      broker.start();
      Session session = broker.createConnection().createSession(false, Session.CLIENT_ACKNOWLEDGE);
      start(trans, session);

      AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage();
      addMetadata(msg);
      Message jmsMsg = trans.translate(msg);
      assertFalse("jmsMsg instanceof TextMessage", jmsMsg instanceof TextMessage);
      assertFalse("jmsMsg instanceof BytesMessage", jmsMsg instanceof BytesMessage);
      assertFalse("jmsMsg instanceof MapMessage", jmsMsg instanceof MapMessage);
      assertFalse("jmsMsg instanceof ObjectMessage", jmsMsg instanceof ObjectMessage);
      assertFalse("jmsMsg instanceof StreamMessage", jmsMsg instanceof StreamMessage);
      
      assertTrue("jmsMsg instanceof Message", jmsMsg instanceof Message);
      assertJmsProperties(jmsMsg);
    }
    finally {
      stop(trans);
      broker.destroy();
    }
  }
  
  public void testAdaptrisMessageWithPayloadToMessage() throws Exception {
    EmbeddedActiveMq broker = new EmbeddedActiveMq();
    MessageTypeTranslatorImp trans = this.createTranslator();
    try {
      broker.start();
      Session session = broker.createConnection().createSession(false, Session.CLIENT_ACKNOWLEDGE);
      start(trans, session);

      AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(TEXT);
      addMetadata(msg);
      Message jmsMsg = trans.translate(msg);
      assertFalse("jmsMsg instanceof TextMessage", jmsMsg instanceof TextMessage);
      assertFalse("jmsMsg instanceof BytesMessage", jmsMsg instanceof BytesMessage);
      assertFalse("jmsMsg instanceof MapMessage", jmsMsg instanceof MapMessage);
      assertFalse("jmsMsg instanceof ObjectMessage", jmsMsg instanceof ObjectMessage);
      assertFalse("jmsMsg instanceof StreamMessage", jmsMsg instanceof StreamMessage);
      
      assertTrue("jmsMsg instanceof Message", jmsMsg instanceof Message);
      assertJmsProperties(jmsMsg);
    }
    finally {
      stop(trans);
      broker.destroy();
    }
  }
  
  @Override
  protected MessageTypeTranslatorImp createTranslator() throws Exception {
    return new BasicJavaxJmsMessageTranslator();
  }

  @Override
  protected Message createMessage(Session session) throws Exception {
    return session.createMessage();
  }

}
