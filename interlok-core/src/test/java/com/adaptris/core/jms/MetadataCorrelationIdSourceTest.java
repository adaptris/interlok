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

import static com.adaptris.interlok.junit.scaffolding.BaseCase.start;
import static com.adaptris.interlok.junit.scaffolding.BaseCase.stop;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import javax.jms.Session;
import javax.jms.TextMessage;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.core.jms.activemq.EmbeddedActiveMq;

public class MetadataCorrelationIdSourceTest {

  private static final String CORRELATIONID_KEY = "correlationid_key";
  private static final String TEXT = "The quick brown fox";
  private static final String TEXT2 = "jumps over the lazy dog";

  private static EmbeddedActiveMq activeMqBroker;

  @BeforeAll
  public static void setUpAll() throws Exception {
    activeMqBroker = new EmbeddedActiveMq();
    activeMqBroker.start();
  }

  @AfterAll
  public static void tearDownAll() throws Exception {
    if(activeMqBroker != null) {
      activeMqBroker.destroy();
    }
  }

  @Test
  public void testAdaptrisMessageMetadataToJmsCorrelationId() throws Exception {
    JmsConnection conn = activeMqBroker.getJmsConnection();
    try {
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
    }
  }

  @Test
  public void testAdaptrisMessageMetadataToJmsCorrelationId_NoMetadataKey() throws Exception {
    JmsConnection conn = activeMqBroker.getJmsConnection();
    try {
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
    }
  }

  @Test
  public void testAdaptrisMessageMetadataToJmsCorrelationId_EmptyValue() throws Exception {
    JmsConnection conn = activeMqBroker.getJmsConnection();
    try {
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
    }
  }

  @Test
  public void testJmsCorrelationIdToAdaptrisMessageMetadata() throws Exception {
    JmsConnection conn = activeMqBroker.getJmsConnection();
    try {
      start(conn);
      Session session = conn.createSession(false, Session.CLIENT_ACKNOWLEDGE);
      AdaptrisMessage adpMsg = AdaptrisMessageFactory.getDefaultInstance().newMessage(TEXT);
      TextMessage jmsMsg = session.createTextMessage();
      jmsMsg.setJMSCorrelationID(TEXT2);
      MetadataCorrelationIdSource mcs = new MetadataCorrelationIdSource(CORRELATIONID_KEY);
      mcs.processCorrelationId(jmsMsg, adpMsg);
      assertEquals(jmsMsg.getJMSCorrelationID(), adpMsg.getMetadataValue(CORRELATIONID_KEY));
      session.close();
    }
    finally {
      stop(conn);
    }
  }

  @Test
  public void testJmsCorrelationIdToAdaptrisMessageMetadata_NoMetadataKey() throws Exception {
    JmsConnection conn = activeMqBroker.getJmsConnection();
    try {
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
    }
  }

  @Test
  public void testJmsCorrelationIdToAdaptrisMessageMetadata_NoValue() throws Exception {
    JmsConnection conn = activeMqBroker.getJmsConnection();
    try {
      start(conn);
      Session session = conn.createSession(false, Session.CLIENT_ACKNOWLEDGE);
      AdaptrisMessage adpMsg = AdaptrisMessageFactory.getDefaultInstance().newMessage(TEXT);
      TextMessage jmsMsg = session.createTextMessage();
      MetadataCorrelationIdSource mcs = new MetadataCorrelationIdSource(CORRELATIONID_KEY);
      mcs.processCorrelationId(jmsMsg, adpMsg);
      assertFalse(adpMsg.headersContainsKey(CORRELATIONID_KEY));
      session.close();
    }
    finally {
      stop(conn);
    }
  }

  @Test
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
