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
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import javax.jms.Message;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.core.jms.activemq.EmbeddedActiveMq;
import com.adaptris.core.metadata.RegexMetadataFilter;

public class TextMessageTranslatorTest extends GenericMessageTypeTranslatorCase {

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
  public void testTextMessageToAdaptrisMessage() throws Exception {
    TextMessageTranslator trans = new TextMessageTranslator();
    try {
      Session session = activeMqBroker.createConnection().createSession(false, Session.CLIENT_ACKNOWLEDGE);
      Message jmsMsg = createMessage(session);
      addProperties(jmsMsg);
      start(trans, session);
      AdaptrisMessage msg = trans.translate(jmsMsg);
      assertMetadata(msg);
      assertEquals(TEXT, msg.getContent());
    }
    finally {
      stop(trans);
    }
  }

  @Test
  public void testAdaptrisMessageToTextMessage() throws Exception {
    TextMessageTranslator trans = new TextMessageTranslator();
    try {
      Session session = activeMqBroker.createConnection().createSession(false, Session.CLIENT_ACKNOWLEDGE);
      start(trans, session);

      AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(TEXT);
      addMetadata(msg);
      Message jmsMsg = trans.translate(msg);
      assertTrue(jmsMsg instanceof TextMessage);
      assertEquals(TEXT, ((TextMessage) jmsMsg).getText());
      assertJmsProperties(jmsMsg);
    }
    finally {
      stop(trans);
    }
  }

  @Test
  public void testAdaptrisMessageToTextMessageWithMetadataFilter() throws Exception {
    TextMessageTranslator trans = new TextMessageTranslator();

    RegexMetadataFilter filter = new RegexMetadataFilter();
    filter.addExcludePattern(INTEGER_METADATA);
    trans.setMetadataFilter(filter);
    try {
      Session session = activeMqBroker.createConnection().createSession(false, Session.CLIENT_ACKNOWLEDGE);
      start(trans, session);

      AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(TEXT);
      addMetadata(msg);
      Message jmsMsg = trans.translate(msg);
      assertTrue(jmsMsg instanceof TextMessage);
      assertEquals(TEXT, ((TextMessage) jmsMsg).getText());

      assertEquals(STRING_VALUE, jmsMsg.getStringProperty(STRING_METADATA));
      assertEquals(BOOLEAN_VALUE, jmsMsg.getStringProperty(BOOLEAN_METADATA));
      assertEquals(Boolean.valueOf(BOOLEAN_VALUE).booleanValue(),
          jmsMsg.getBooleanProperty(BOOLEAN_METADATA)); // default
      // We should not of copied the integer value according to the filter.
      assertNull(jmsMsg.getStringProperty(INTEGER_METADATA));
    } finally {
      stop(trans);
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
