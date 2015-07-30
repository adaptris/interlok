/*
 * $RCSfile: TextMessageTranslatorTest.java,v $
 * $Revision: 1.7 $
 * $Date: 2009/04/15 15:22:06 $
 * $Author: lchan $
 */
package com.adaptris.core.jms;

import javax.jms.Message;
import javax.jms.Session;
import javax.jms.TextMessage;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.core.jms.activemq.EmbeddedActiveMq;
import com.adaptris.core.metadata.RegexMetadataFilter;

public class TextMessageTranslatorTest extends MessageTypeTranslatorCase {

  public TextMessageTranslatorTest(String name) {
    super(name);
  }

  public void testTextMessageToAdaptrisMessage() throws Exception {
    EmbeddedActiveMq broker = new EmbeddedActiveMq();
    TextMessageTranslator trans = new TextMessageTranslator();
    try {
      broker.start();
      Session session = broker.createConnection().createSession(false, Session.CLIENT_ACKNOWLEDGE);
      Message jmsMsg = createMessage(session);
      addProperties(jmsMsg);
      start(trans, session);
      AdaptrisMessage msg = trans.translate(jmsMsg);
      assertMetadata(msg);
      assertEquals(TEXT, msg.getStringPayload());
    }
    finally {
      stop(trans);
      broker.destroy();
    }
  }

  public void testAdaptrisMessageToTextMessage() throws Exception {
    EmbeddedActiveMq broker = new EmbeddedActiveMq();
    TextMessageTranslator trans = new TextMessageTranslator();
    try {
      broker.start();
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
  
  public void testAdaptrisMessageToTextMessageWithMetadataFilter() throws Exception {
	    EmbeddedActiveMq broker = new EmbeddedActiveMq();
	    TextMessageTranslator trans = new TextMessageTranslator();
	    
	    RegexMetadataFilter filter = new RegexMetadataFilter();
	    filter.addExcludePattern(INTEGER_METADATA);
	    trans.setMetadataFilter(filter);
	    try {
	      broker.start();
	      Session session = broker.createConnection().createSession(false, Session.CLIENT_ACKNOWLEDGE);
	      start(trans, session);

	      AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(TEXT);
	      addMetadata(msg);
	      Message jmsMsg = trans.translate(msg);
	      assertTrue("jmsMsg instanceof TextMessage", jmsMsg instanceof TextMessage);
	      assertEquals(TEXT, ((TextMessage) jmsMsg).getText());
	      
	      assertEquals(STRING_VALUE, jmsMsg.getStringProperty(STRING_METADATA));
	      assertEquals(BOOLEAN_VALUE, jmsMsg.getStringProperty(BOOLEAN_METADATA));
	      assertEquals(Boolean.valueOf(BOOLEAN_VALUE).booleanValue(), jmsMsg.getBooleanProperty(BOOLEAN_METADATA)); // default
	      // We should not of copied the integer value according to the filter.
	      assertNull(jmsMsg.getStringProperty(INTEGER_METADATA));
	    }
	    finally {
	      stop(trans);
	      broker.destroy();
	    }
	  }

  /**
   * @see com.adaptris.core.jms.MessageTypeTranslatorCase#createMessage(javax.jms.Session)
   */
  @Override
  protected Message createMessage(Session session) throws Exception {
    return session.createTextMessage(TEXT);
  }

  /**
   * @see com.adaptris.core.jms.MessageTypeTranslatorCase#createTranslator()
   */
  @Override
  protected MessageTypeTranslatorImp createTranslator() throws Exception {
    return new TextMessageTranslator();
  }
}
