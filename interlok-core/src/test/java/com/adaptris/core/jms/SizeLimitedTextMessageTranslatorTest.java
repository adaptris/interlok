package com.adaptris.core.jms;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.DefaultMessageFactory;
import com.adaptris.core.jms.activemq.EmbeddedActiveMq;

public class SizeLimitedTextMessageTranslatorTest {

  public static final String TEXT = "The quick brown fox";

  protected static EmbeddedActiveMq activeMqBroker;
  
  @BeforeAll
  public static void setUpAll() throws Exception {
    activeMqBroker = new EmbeddedActiveMq();
    activeMqBroker.start();
  }
  
  @AfterAll
  public static void tearDownAll() throws Exception {
    if(activeMqBroker != null)
      activeMqBroker.destroy();
  }
  
  @Test
  public void defaultTest() {
    
  }
  
//  @Test
//  public void testMaxSizeExceeded() throws Exception {
//    SizeLimitedTextMessageTranslator trans = new SizeLimitedTextMessageTranslator();
//    trans.setMaxSizeBytes(TEXT.getBytes().length - 1);
//
//    try {
//      AdaptrisMessage aMessage = DefaultMessageFactory.getDefaultInstance().newMessage(TEXT);
//      Session session = activeMqBroker.createConnection().createSession(false, Session.CLIENT_ACKNOWLEDGE);
//      start(trans, session);
//      assertThrows(JMSException.class, () -> trans.translate(aMessage));
//
//    } finally {
//      stop(trans);
//    }
//  }
//  
//  @Test
//  public void testMaxSizeExceededCustomMessage() throws Exception {
//    SizeLimitedTextMessageTranslator trans = new SizeLimitedTextMessageTranslator();
//    trans.setMaxSizeBytes(TEXT.getBytes().length - 1);
//    trans.setLimitExceededExceptionMessage("Testing");
//
//    try {
//      AdaptrisMessage aMessage = DefaultMessageFactory.getDefaultInstance().newMessage(TEXT);
//      Session session = activeMqBroker.createConnection().createSession(false, Session.CLIENT_ACKNOWLEDGE);
//      start(trans, session);
//      
//      assertTrue(assertThrows(JMSException.class, () -> trans.translate(aMessage)).getMessage().startsWith("Testing"));
//
//    } finally {
//      stop(trans);
//    }
//  }

  /**
   * @see com.adaptris.core.jms.MessageTypeTranslatorCase#createMessage(javax.jms.Session)
   */
  
  protected Message createMessage(Session session) throws Exception {
    return session.createTextMessage(TEXT);
  }

  /**
   * @see com.adaptris.core.jms.MessageTypeTranslatorCase#createTranslator()
   */
  
  protected MessageTypeTranslatorImp createTranslator() throws Exception {
    return new SizeLimitedTextMessageTranslator();
  }
}
