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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import javax.jms.BytesMessage;
import javax.jms.MapMessage;
import javax.jms.Message;
import javax.jms.ObjectMessage;
import javax.jms.Session;
import javax.jms.StreamMessage;
import javax.jms.TextMessage;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.core.jms.activemq.EmbeddedActiveMq;

public class BasicJavaxJmsMessageTranslatorTest extends GenericMessageTypeTranslatorCase {

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
  public void testMessageToAdaptrisMessage() throws Exception {
    MessageTypeTranslatorImp trans = this.createTranslator();
    try {
      Session session = activeMqBroker.createConnection().createSession(false, Session.CLIENT_ACKNOWLEDGE);
      Message jmsMsg = createMessage(session);
      addProperties(jmsMsg);
      start(trans, session);
      AdaptrisMessage msg = trans.translate(jmsMsg);
      assertMetadata(msg);
      assertEquals(0, msg.getContent().length());
    }
    finally {
      stop(trans);
    }
  }

  @Test
  public void testAdaptrisMessageToMessage() throws Exception {
    MessageTypeTranslatorImp trans = this.createTranslator();
    try {
      Session session = activeMqBroker.createConnection().createSession(false, Session.CLIENT_ACKNOWLEDGE);
      start(trans, session);

      AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage();
      addMetadata(msg);
      Message jmsMsg = trans.translate(msg);
      assertFalse(jmsMsg instanceof TextMessage);
      assertFalse(jmsMsg instanceof BytesMessage);
      assertFalse(jmsMsg instanceof MapMessage);
      assertFalse(jmsMsg instanceof ObjectMessage);
      assertFalse(jmsMsg instanceof StreamMessage);
      
      assertTrue(jmsMsg instanceof Message);
      assertJmsProperties(jmsMsg);
    }
    finally {
      stop(trans);
    }
  }
  
  @Test
  public void testAdaptrisMessageWithPayloadToMessage() throws Exception {
    MessageTypeTranslatorImp trans = this.createTranslator();
    try {
      Session session = activeMqBroker.createConnection().createSession(false, Session.CLIENT_ACKNOWLEDGE);
      start(trans, session);

      AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(TEXT);
      addMetadata(msg);
      Message jmsMsg = trans.translate(msg);
      assertFalse(jmsMsg instanceof TextMessage);
      assertFalse(jmsMsg instanceof BytesMessage);
      assertFalse(jmsMsg instanceof MapMessage);
      assertFalse(jmsMsg instanceof ObjectMessage);
      assertFalse(jmsMsg instanceof StreamMessage);
      
      assertTrue(jmsMsg instanceof Message);
      assertJmsProperties(jmsMsg);
    }
    finally {
      stop(trans);
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
