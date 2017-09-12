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

import javax.jms.MapMessage;
import javax.jms.Message;
import javax.jms.Session;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.core.jms.activemq.EmbeddedActiveMq;

/**
 */
public class MapMessageTranslatorTest extends GenericMessageTypeTranslatorCase {
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
      assertTrue(msg.getContent().equals(TEXT));
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
      msg.setContent(TEXT, msg.getContentEncoding());

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
      msg.setContent(TEXT, msg.getContentEncoding());

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
