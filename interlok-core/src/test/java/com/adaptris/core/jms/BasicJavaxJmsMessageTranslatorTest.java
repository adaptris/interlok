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
import javax.jms.BytesMessage;
import javax.jms.MapMessage;
import javax.jms.Message;
import javax.jms.ObjectMessage;
import javax.jms.Session;
import javax.jms.StreamMessage;
import javax.jms.TextMessage;
import org.junit.Assume;
import org.junit.Test;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.core.jms.activemq.EmbeddedActiveMq;

public class BasicJavaxJmsMessageTranslatorTest extends GenericMessageTypeTranslatorCase {

  @Test
  public void testMessageToAdaptrisMessage() throws Exception {
    Assume.assumeTrue(JmsConfig.jmsTestsEnabled());
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
      assertEquals(0, msg.getContent().length());
    }
    finally {
      stop(trans);
      broker.destroy();
    }
  }

  @Test
  public void testAdaptrisMessageToMessage() throws Exception {
    Assume.assumeTrue(JmsConfig.jmsTestsEnabled());
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
  
  @Test
  public void testAdaptrisMessageWithPayloadToMessage() throws Exception {
    Assume.assumeTrue(JmsConfig.jmsTestsEnabled());
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
