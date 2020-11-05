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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;
import javax.jms.Session;
import javax.jms.TextMessage;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.core.jms.activemq.EmbeddedActiveMq;
import com.adaptris.core.util.LifecycleHelper;

public class MessageIdCorrelationIdSourceTest {
  @Rule
  public TestName testName = new TestName();
  
  private static EmbeddedActiveMq activeMqBroker;

  @BeforeClass
  public static void setUpAll() throws Exception {
    activeMqBroker = new EmbeddedActiveMq();
    activeMqBroker.start();
  }
  
  @AfterClass
  public static void tearDownAll() throws Exception {
    if(activeMqBroker != null)
      activeMqBroker.destroy();
  }

  @Before
  public void setUp() throws Exception {
  }

  @After
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
  public void testCorrelationIdMessage_AdaptrisMessage() throws Exception {
    JmsConnection conn = activeMqBroker.getJmsConnection();
    try {
      LifecycleHelper.initAndStart(conn, false);
      Session session = conn.createSession(false, Session.CLIENT_ACKNOWLEDGE);
      AdaptrisMessage adpMsg = AdaptrisMessageFactory.getDefaultInstance().newMessage("hello");
      String originalId = adpMsg.getUniqueId();
      TextMessage jmsMsg = session.createTextMessage();
      jmsMsg.setJMSCorrelationID(testName.getMethodName());
      MessageIdCorrelationIdSource cs = new MessageIdCorrelationIdSource();
      cs.processCorrelationId(jmsMsg, adpMsg);
      assertNotSame(originalId, adpMsg.getUniqueId());
      assertEquals(adpMsg.getUniqueId(), jmsMsg.getJMSCorrelationID());
      assertEquals(testName.getMethodName(), adpMsg.getUniqueId());
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
