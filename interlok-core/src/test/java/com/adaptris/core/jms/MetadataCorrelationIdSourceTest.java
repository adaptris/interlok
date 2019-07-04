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

import javax.jms.Session;
import javax.jms.TextMessage;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.core.BaseCase;
import com.adaptris.core.jms.activemq.EmbeddedActiveMq;

@SuppressWarnings("deprecation")
public class MetadataCorrelationIdSourceTest extends BaseCase {

  private static final String CORRELATIONID_KEY = "correlationid_key";
  private static final String TEXT = "The quick brown fox";
  private static final String TEXT2 = "jumps over the lazy dog";

  protected transient Log log = LogFactory.getLog(this.getClass());

  /**
   * <p>
   * Creates a new instance.
   * </p>
   */
  public MetadataCorrelationIdSourceTest(String arg0) throws Exception {
    super(arg0);
  }

  public void testAdaptrisMessageMetadataToJmsCorrelationId() throws Exception {

    EmbeddedActiveMq broker = new EmbeddedActiveMq();
    JmsConnection conn = broker.getJmsConnection();
    try {
      broker.start();
      start(conn);
      Session session = conn.createSession(false, Session.CLIENT_ACKNOWLEDGE);
      AdaptrisMessage adpMsg = AdaptrisMessageFactory.getDefaultInstance().newMessage(TEXT);
      adpMsg.addMetadata(CORRELATIONID_KEY, TEXT2);
      TextMessage jmsMsg = session.createTextMessage();
      MetadataCorrelationIdSource mcs = new MetadataCorrelationIdSource(CORRELATIONID_KEY);
      mcs.processCorrelationId(adpMsg, jmsMsg);
      assertEquals(adpMsg.getMetadataValue(CORRELATIONID_KEY), jmsMsg.getJMSCorrelationID());
      session.close();
    }
    finally {
      stop(conn);
      broker.destroy();
    }
  }

  public void testAdaptrisMessageMetadataToJmsCorrelationId_NoMetadataKey() throws Exception {
    EmbeddedActiveMq broker = new EmbeddedActiveMq();
    JmsConnection conn = broker.getJmsConnection();
    try {
      broker.start();
      start(conn);
      Session session = conn.createSession(false, Session.CLIENT_ACKNOWLEDGE);
      AdaptrisMessage adpMsg = AdaptrisMessageFactory.getDefaultInstance().newMessage(TEXT);
      adpMsg.addMetadata(CORRELATIONID_KEY, TEXT2);
      TextMessage jmsMsg = session.createTextMessage();
      MetadataCorrelationIdSource mcs = new MetadataCorrelationIdSource();
      mcs.processCorrelationId(adpMsg, jmsMsg);
      assertNotSame(adpMsg.getMetadataValue(CORRELATIONID_KEY), jmsMsg.getJMSCorrelationID());
      session.close();
    }
    finally {
      stop(conn);
      broker.destroy();
    }
  }

  public void testAdaptrisMessageMetadataToJmsCorrelationId_EmptyValue() throws Exception {
    EmbeddedActiveMq broker = new EmbeddedActiveMq();
    JmsConnection conn = broker.getJmsConnection();
    try {
      broker.start();
      start(conn);
      Session session = conn.createSession(false, Session.CLIENT_ACKNOWLEDGE);
      AdaptrisMessage adpMsg = AdaptrisMessageFactory.getDefaultInstance().newMessage(TEXT);
      adpMsg.addMetadata(CORRELATIONID_KEY, "");
      TextMessage jmsMsg = session.createTextMessage();
      MetadataCorrelationIdSource mcs = new MetadataCorrelationIdSource(CORRELATIONID_KEY);
      mcs.processCorrelationId(adpMsg, jmsMsg);
      assertTrue(StringUtils.isEmpty(jmsMsg.getJMSCorrelationID()));
      session.close();
    }
    finally {
      stop(conn);
      broker.destroy();
    }
  }

  public void testJmsCorrelationIdToAdaptrisMessageMetadata() throws Exception {
    EmbeddedActiveMq broker = new EmbeddedActiveMq();
    JmsConnection conn = broker.getJmsConnection();
    try {
      broker.start();
      start(conn);
      Session session = conn.createSession(false, Session.CLIENT_ACKNOWLEDGE);
      AdaptrisMessage adpMsg = AdaptrisMessageFactory.getDefaultInstance().newMessage(TEXT);
      TextMessage jmsMsg = session.createTextMessage();
      jmsMsg.setJMSCorrelationID(TEXT2);
      MetadataCorrelationIdSource mcs = new MetadataCorrelationIdSource(CORRELATIONID_KEY);
      mcs.processCorrelationId(jmsMsg, adpMsg);
      assertEquals("Check Correlation Id Keys", jmsMsg.getJMSCorrelationID(), adpMsg.getMetadataValue(CORRELATIONID_KEY));
      session.close();
    }
    finally {
      stop(conn);
      broker.destroy();
    }
  }

  public void testJmsCorrelationIdToAdaptrisMessageMetadata_NoMetadataKey() throws Exception {
    EmbeddedActiveMq broker = new EmbeddedActiveMq();
    JmsConnection conn = broker.getJmsConnection();
    try {
      broker.start();
      start(conn);
      Session session = conn.createSession(false, Session.CLIENT_ACKNOWLEDGE);
      AdaptrisMessage adpMsg = AdaptrisMessageFactory.getDefaultInstance().newMessage(TEXT);
      TextMessage jmsMsg = session.createTextMessage();
      jmsMsg.setJMSCorrelationID(TEXT2);
      MetadataCorrelationIdSource mcs = new MetadataCorrelationIdSource();
      mcs.processCorrelationId(jmsMsg, adpMsg);
      assertNotSame(jmsMsg.getJMSCorrelationID(), adpMsg.getMetadataValue(CORRELATIONID_KEY));
      session.close();
    }
    finally {
      stop(conn);
      broker.destroy();
    }
  }

  public void testJmsCorrelationIdToAdaptrisMessageMetadata_NoValue() throws Exception {
    EmbeddedActiveMq broker = new EmbeddedActiveMq();
    JmsConnection conn = broker.getJmsConnection();
    try {
      broker.start();
      start(conn);
      Session session = conn.createSession(false, Session.CLIENT_ACKNOWLEDGE);
      AdaptrisMessage adpMsg = AdaptrisMessageFactory.getDefaultInstance().newMessage(TEXT);
      TextMessage jmsMsg = session.createTextMessage();
      MetadataCorrelationIdSource mcs = new MetadataCorrelationIdSource(CORRELATIONID_KEY);
      mcs.processCorrelationId(jmsMsg, adpMsg);
      assertFalse(adpMsg.containsKey(CORRELATIONID_KEY));
      session.close();
    }
    finally {
      stop(conn);
      broker.destroy();
    }
  }

  public void testSetMetadataKey() {
    MetadataCorrelationIdSource mcs = new MetadataCorrelationIdSource();
    assertEquals(null, mcs.getMetadataKey());
    try {
      mcs.setMetadataKey("");
      fail();
    }
    catch (IllegalArgumentException e) {

    }
    mcs.setMetadataKey(CORRELATIONID_KEY);
    assertEquals(CORRELATIONID_KEY, mcs.getMetadataKey());
  }
}
