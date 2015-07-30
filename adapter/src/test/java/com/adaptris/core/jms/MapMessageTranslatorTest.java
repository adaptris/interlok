/*
 * $RCSfile: MapMessageTranslatorTest.java,v $
 * $Revision: 1.2 $
 * $Date: 2009/04/17 08:15:54 $
 * $Author: lchan $
 */
package com.adaptris.core.jms;

import javax.jms.MapMessage;
import javax.jms.Message;
import javax.jms.Session;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.core.jms.activemq.EmbeddedActiveMq;

/**
 */
@SuppressWarnings("deprecation")
public class MapMessageTranslatorTest extends MessageTypeTranslatorCase {
  private static final String BODY_KEY1 = "bodykey1";

  public MapMessageTranslatorTest(String name) {
    super(name);
  }


  /**
   * @see com.adaptris.core.jms.MessageTypeTranslatorCase#createMessage(javax.jms.Session)
   */
  @Override
  protected Message createMessage(Session session) throws Exception {
    return session.createMapMessage();
  }

  /**
   * @see com.adaptris.core.jms.MessageTypeTranslatorCase#createTranslator()
   */
  @Override
  protected MessageTypeTranslatorImp createTranslator() throws Exception {
    MapMessageTranslator t = new MapMessageTranslator();
    t.setKeyForPayload(BODY_KEY1);
    return t;
  }

  public void testMapMessageToAdaptrisMessage() throws Exception {
    EmbeddedActiveMq broker = new EmbeddedActiveMq();
    MapMessageTranslator t = new MapMessageTranslator();
    try {
      broker.start();
      Session session = broker.createConnection().createSession(false, Session.CLIENT_ACKNOWLEDGE);
      MapMessage jmsMsg = session.createMapMessage();
      jmsMsg.setString(BODY_KEY1, TEXT);
      addProperties(jmsMsg);
      t.setKeyForPayload(BODY_KEY1);
      start(t, session);
      AdaptrisMessage msg = t.translate(jmsMsg);
      assertMetadata(msg);
      assertTrue(msg.getStringPayload().equals(TEXT));
    }
    finally {
      stop(t);
      broker.destroy();
    }
  }

  public void testAdaptrisMessageToMapMessage() throws Exception {
    EmbeddedActiveMq broker = new EmbeddedActiveMq();
    MapMessageTranslator t = new MapMessageTranslator();
    try {
      broker.start();
      Session session = broker.createConnection().createSession(false, Session.CLIENT_ACKNOWLEDGE);
      AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage();

      addMetadata(msg);
      msg.setStringPayload(TEXT);

      t.setKeyForPayload(BODY_KEY1);
      start(t, session);

      MapMessage jmsMsg = (MapMessage) t.translate(msg);
      assertJmsProperties(jmsMsg);
      assertTrue(jmsMsg.getString(BODY_KEY1).equals(TEXT));
    }
    finally {
      stop(t);
      broker.destroy();
    }
  }

  public void testAdaptrisMessageToMapMessageWithMetadataAsPayload() throws Exception {
    EmbeddedActiveMq broker = new EmbeddedActiveMq();
    MapMessageTranslator t = new MapMessageTranslator();
    try {
      broker.start();
      Session session = broker.createConnection().createSession(false, Session.CLIENT_ACKNOWLEDGE);
      AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage();

      addMetadata(msg);
      msg.setStringPayload(TEXT);

      t.setKeyForPayload(BODY_KEY1);
      t.setTreatMetadataAsPartOfMessage(true);
      start(t, session);
      MapMessage jmsMsg = (MapMessage) t.translate(msg);
      assertJmsProperties(jmsMsg);
      assertTrue(jmsMsg.getString(BODY_KEY1).equals(TEXT));
      assertTrue(jmsMsg.getString(STRING_METADATA).equals(STRING_VALUE));
      assertTrue(jmsMsg.getString(BOOLEAN_METADATA).equals(BOOLEAN_VALUE));
      assertTrue(jmsMsg.getString(INTEGER_METADATA).equals(INTEGER_VALUE));
    }
    finally {
      stop(t);
      broker.destroy();
    }
  }

}
