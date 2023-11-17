/*
 * Copyright 2018 Adaptris Ltd.
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
import static org.junit.jupiter.api.Assertions.assertNotSame;

import javax.jms.Session;
import javax.jms.TextMessage;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.core.jms.activemq.EmbeddedActiveMq;
import com.adaptris.core.util.LifecycleHelper;

public class MessageIdCorrelationIdSourceTest {
  
  
  
  private static EmbeddedActiveMq activeMqBroker;

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

  @BeforeEach
  public void setUp() throws Exception {
  }

  @AfterEach
  public void tearDown() throws Exception {
  }

  @Test
  public void testCorrelationIdAdaptrisMessage_ToMessage() throws Exception {
    JmsConnection conn = activeMqBroker.getJmsConnection();
    try {
      LifecycleHelper.initAndStart(conn, false);
      Session session = conn.createSession(false, Session.CLIENT_ACKNOWLEDGE);
      AdaptrisMessage adpMsg = AdaptrisMessageFactory.getDefaultInstance().newMessage("hello");
      TextMessage jmsMsg = session.createTextMessage();
      MessageIdCorrelationIdSource cs = new MessageIdCorrelationIdSource();
      cs.processCorrelationId(adpMsg, jmsMsg);
      assertEquals(adpMsg.getUniqueId(), jmsMsg.getJMSCorrelationID());
      session.close();
    } finally {
      LifecycleHelper.stopAndClose(conn, false);
    }
  }

  @Test
  public void testCorrelationIdMessage_AdaptrisMessage(TestInfo info) throws Exception {
    JmsConnection conn = activeMqBroker.getJmsConnection();
    try {
      LifecycleHelper.initAndStart(conn, false);
      Session session = conn.createSession(false, Session.CLIENT_ACKNOWLEDGE);
      AdaptrisMessage adpMsg = AdaptrisMessageFactory.getDefaultInstance().newMessage("hello");
      String originalId = adpMsg.getUniqueId();
      TextMessage jmsMsg = session.createTextMessage();
      jmsMsg.setJMSCorrelationID(info.getDisplayName());
      MessageIdCorrelationIdSource cs = new MessageIdCorrelationIdSource();
      cs.processCorrelationId(jmsMsg, adpMsg);
      assertNotSame(originalId, adpMsg.getUniqueId());
      assertEquals(adpMsg.getUniqueId(), jmsMsg.getJMSCorrelationID());
      assertEquals(info.getDisplayName(), adpMsg.getUniqueId());
      session.close();
    } finally {
      LifecycleHelper.stopAndClose(conn, false);
    }
  }

  @Test
  public void testCorrelationIdMessage_AdaptrisMessage_NoCorrelationId() throws Exception {
    JmsConnection conn = activeMqBroker.getJmsConnection();
    try {
      LifecycleHelper.initAndStart(conn, false);
      Session session = conn.createSession(false, Session.CLIENT_ACKNOWLEDGE);
      AdaptrisMessage adpMsg = AdaptrisMessageFactory.getDefaultInstance().newMessage("hello");
      String originalId = adpMsg.getUniqueId();
      TextMessage jmsMsg = session.createTextMessage();
      MessageIdCorrelationIdSource cs = new MessageIdCorrelationIdSource();
      cs.processCorrelationId(jmsMsg, adpMsg);
      assertEquals(originalId, adpMsg.getUniqueId());
      assertNotSame(adpMsg.getUniqueId(), jmsMsg.getJMSCorrelationID());
      session.close();
    } finally {
      LifecycleHelper.stopAndClose(conn, false);
    }
  }
}
