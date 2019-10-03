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

package com.adaptris.core.jms.activemq;

import static com.adaptris.core.jms.MessageTypeTranslatorCase.addMetadata;
import static com.adaptris.core.jms.MessageTypeTranslatorCase.addProperties;
import static com.adaptris.core.jms.MessageTypeTranslatorCase.assertJmsProperties;
import static com.adaptris.core.jms.MessageTypeTranslatorCase.assertMetadata;
import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.List;
import javax.jms.Message;
import javax.jms.Session;
import org.apache.activemq.ActiveMQSession;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.core.ConfiguredProduceDestination;
import com.adaptris.core.DefaultMessageFactory;
import com.adaptris.core.StandaloneProducer;
import com.adaptris.core.jms.JmsConfig;
import com.adaptris.core.jms.JmsConnection;
import com.adaptris.core.jms.JmsConstants;
import com.adaptris.core.jms.JmsProducer;
import com.adaptris.core.jms.JmsProducerExample;
import com.adaptris.core.jms.JmsProducerImpl;
import com.adaptris.core.jms.MessageTypeTranslatorImp;


public class BlobMessageTranslatorTest extends JmsProducerExample {

  private transient Log log = LogFactory.getLog(this.getClass());

  private static final String INPUT = "Quick zephyrs blow, vexing daft Jim";

  public BlobMessageTranslatorTest(String name) {
    super(name);
  }

  @Override
  protected void setUp() throws Exception {
    super.setUp();
  }

  @Override
  protected void tearDown() throws Exception {
    super.tearDown();
  }

  private Message createMessage(Session session) throws Exception {
    return ((ActiveMQSession) session).createBlobMessage(new ByteArrayInputStream(INPUT.getBytes()));
  }

  public void testMoveMetadataJmsMessageToAdaptrisMessage() throws Exception {
    // This would be best, but we can't mix Junit3 with Junit4 assumptions.
    // Assume.assumeTrue(JmsConfig.jmsTestsEnabled());
    if (!JmsConfig.jmsTestsEnabled()) {
      return;
    }
    MessageTypeTranslatorImp trans = new BlobMessageTranslator();
    EmbeddedActiveMq activeMqBroker = new EmbeddedActiveMq();
    JmsConnection conn = null;
    try {
      activeMqBroker.start();
      conn = activeMqBroker.getJmsConnection(new BasicActiveMqImplementation());
      start(conn);
      Session session = conn.createSession(false, Session.CLIENT_ACKNOWLEDGE);
      Message jmsMsg = createMessage(session);

      addProperties(jmsMsg);
      trans.registerSession(session);
      trans.registerMessageFactory(new DefaultMessageFactory());
      start(trans);
      AdaptrisMessage msg = trans.translate(jmsMsg);

      assertMetadata(msg);
    }
    finally {
      stop(trans);
      stop(conn);
      activeMqBroker.destroy();
    }
  }

  public void testMoveJmsHeadersJmsMessageToAdaptrisMessage() throws Exception {
    // This would be best, but we can't mix Junit3 with Junit4 assumptions.
    // Assume.assumeTrue(JmsConfig.jmsTestsEnabled());
    if (!JmsConfig.jmsTestsEnabled()) {
      return;
    }
    MessageTypeTranslatorImp trans = new BlobMessageTranslator();
    EmbeddedActiveMq activeMqBroker = new EmbeddedActiveMq();
    JmsConnection conn = null;
    try {
      activeMqBroker.start();
      conn = activeMqBroker.getJmsConnection(new BasicActiveMqImplementation());
      start(conn);

      Session session = conn.createSession(false, Session.CLIENT_ACKNOWLEDGE);
      Message jmsMsg = createMessage(session);
      jmsMsg.setJMSCorrelationID("ABC");
      jmsMsg.setJMSDeliveryMode(1);
      jmsMsg.setJMSPriority(4);
      addProperties(jmsMsg);
      long timestamp = System.currentTimeMillis();
      jmsMsg.setJMSTimestamp(timestamp);

      trans.registerSession(session);
      trans.registerMessageFactory(new DefaultMessageFactory());
      trans.setMoveJmsHeaders(true);
      start(trans);

      AdaptrisMessage msg = trans.translate(jmsMsg);
      assertMetadata(msg);
      assertEquals("ABC", msg.getMetadataValue(JmsConstants.JMS_CORRELATION_ID));
      assertEquals("1", msg.getMetadataValue(JmsConstants.JMS_DELIVERY_MODE));
      assertEquals("4", msg.getMetadataValue(JmsConstants.JMS_PRIORITY));
      assertEquals(String.valueOf(timestamp), msg.getMetadataValue(JmsConstants.JMS_TIMESTAMP));
    }
    finally {
      stop(trans);
      stop(conn);
      activeMqBroker.destroy();
    }

  }

  public void testMoveMetadataAdaptrisMessageToJmsMessage() throws Exception {
    // This would be best, but we can't mix Junit3 with Junit4 assumptions.
    // Assume.assumeTrue(JmsConfig.jmsTestsEnabled());
    if (!JmsConfig.jmsTestsEnabled()) {
      return;
    }
    MessageTypeTranslatorImp trans = new BlobMessageTranslator();
    EmbeddedActiveMq activeMqBroker = new EmbeddedActiveMq();
    JmsConnection conn = null;
    try {
      activeMqBroker.start();
      conn = activeMqBroker.getJmsConnection(new BasicActiveMqImplementation());
      start(conn);

      AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage();

      addMetadata(msg);
      Session session = conn.createSession(false, Session.CLIENT_ACKNOWLEDGE);
      trans.registerSession(session);
      trans.registerMessageFactory(new DefaultMessageFactory());
      start(trans);

      Message jmsMsg = trans.translate(msg);
      assertJmsProperties(jmsMsg);
    }
    finally {
      stop(trans);
      stop(conn);
      activeMqBroker.destroy();
    }

  }

  public void testBug895() throws Exception {
    // This would be best, but we can't mix Junit3 with Junit4 assumptions.
    // Assume.assumeTrue(JmsConfig.jmsTestsEnabled());
    if (!JmsConfig.jmsTestsEnabled()) {
      return;
    }
    MessageTypeTranslatorImp trans = new BlobMessageTranslator();
    EmbeddedActiveMq activeMqBroker = new EmbeddedActiveMq();
    JmsConnection conn = null;
    try {
      activeMqBroker.start();
      conn = activeMqBroker.getJmsConnection(new BasicActiveMqImplementation());
      start(conn);

      AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage();

      msg.addMetadata(JmsConstants.JMS_PRIORITY, "9");
      msg.addMetadata(JmsConstants.JMS_TYPE, "idaho");
      Session session = conn.createSession(false, Session.CLIENT_ACKNOWLEDGE);
      trans.setMoveJmsHeaders(true);
      trans.registerSession(session);
      trans.registerMessageFactory(new DefaultMessageFactory());
      start(trans);

      Message jmsMsg = trans.translate(msg);

      assertNotSame("JMS Priorities should be different", jmsMsg.getJMSPriority(), 9);
      assertEquals("JMSType should be equal", "idaho", jmsMsg.getJMSType());
    }
    finally {
      stop(trans);
      stop(conn);
      activeMqBroker.destroy();
    }

  }


  private static StandaloneProducer buildStandaloneProducer(JmsConnection c, JmsProducer p) {

    p.setDestination(new ConfiguredProduceDestination("jms:queue:MyQueueName?priority=4"));
    p.setMessageTranslator(new BlobMessageTranslator("metadataKeyContainingTheUrlReference"));
    c.setUserName("BrokerUsername");
    c.setPassword("BrokerPassword");
    c.setVendorImplementation(new BasicActiveMqImplementation(BasicActiveMqImplementationTest.PRIMARY));
    return new StandaloneProducer(c, p);
  }

  @Override
  protected List retrieveObjectsForSampleConfig() {
    ArrayList result = new ArrayList();
    result.add(buildStandaloneProducer(new JmsConnection(), new JmsProducer()));
    return result;
  }

  /**
   * @see com.adaptris.core.ExampleConfigCase#retrieveObjectForSampleConfig()
   */
  @Override
  protected Object retrieveObjectForSampleConfig() {
    return null;
  }

  @Override
  protected String createBaseFileName(Object object) {
    JmsProducerImpl p = (JmsProducerImpl) ((StandaloneProducer) object).getProducer();
    return super.createBaseFileName(object) + "-" + p.getMessageTranslator().getClass().getSimpleName();
  }

}
