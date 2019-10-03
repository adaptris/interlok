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

import static com.adaptris.core.BaseCase.start;
import static com.adaptris.core.BaseCase.stop;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import javax.jms.JMSException;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.junit.Assume;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.core.ConfiguredProduceDestination;
import com.adaptris.core.StandaloneProducer;
import com.adaptris.core.jms.JmsConfig;
import com.adaptris.core.jms.JmsConnection;
import com.adaptris.core.jms.JmsDestination;
import com.adaptris.core.jms.PtpProducer;
import com.adaptris.core.jms.VendorImplementation;

public class BasicActiveMqImplementationTest {

  protected static final String PRIMARY = "tcp://localhost:61616";
  @Rule
  public TestName testName = new TestName();

  @Test
  public void testConnectionFactory() throws Exception {
    VendorImplementation mq = create();
    JmsConnection c = new JmsConnection();
    ActiveMQConnectionFactory f = (ActiveMQConnectionFactory) mq.createConnectionFactory();
    doAssertions(f);

  }

  protected void doAssertions(ActiveMQConnectionFactory f) throws Exception {
    assertEquals(PRIMARY, f.getBrokerURL());
  }

  protected BasicActiveMqImplementation create() {
    return new BasicActiveMqImplementation(PRIMARY);
  }


  @Test
  public void testRfc6167_Basic() throws Exception {
    Assume.assumeTrue(JmsConfig.jmsTestsEnabled());
    EmbeddedActiveMq broker = new EmbeddedActiveMq();
    StandaloneProducer producer = null;

    try {
      broker.start();
      BasicActiveMqImplementation vendorImp = create();
      PtpProducer ptp = new PtpProducer(new ConfiguredProduceDestination(testName.getMethodName()));
      producer = new StandaloneProducer(broker.getJmsConnection(vendorImp), ptp);
      start(producer);
      // Send a message so that the session is correct.
      producer.doService(AdaptrisMessageFactory.getDefaultInstance().newMessage("abcde"));
      JmsDestination jmsDest = vendorImp.createDestination("jms:queue:myQueueName", ptp);
      assertNotNull(jmsDest.getDestination());
      assertTrue(javax.jms.Queue.class.isAssignableFrom(jmsDest.getDestination().getClass()));
      assertNull(jmsDest.deliveryMode());
      assertNull(jmsDest.getReplyToDestination());
      assertNull(jmsDest.priority());
      assertNull(jmsDest.timeToLive());
      assertNull(jmsDest.subscriptionId());
      assertFalse(jmsDest.noLocal());
    } finally {
      stop(producer);
      broker.destroy();
    }
  }

  @Test
  public void testRfc6167_WithParams() throws Exception {
    Assume.assumeTrue(JmsConfig.jmsTestsEnabled());
    EmbeddedActiveMq broker = new EmbeddedActiveMq();
    StandaloneProducer producer = null;

    try {
      broker.start();
      BasicActiveMqImplementation vendorImp = create();
      PtpProducer ptp = new PtpProducer(new ConfiguredProduceDestination(testName.getMethodName()));
      producer = new StandaloneProducer(broker.getJmsConnection(vendorImp), ptp);
      start(producer);
      // Send a message so that the session is correct.
      producer.doService(AdaptrisMessageFactory.getDefaultInstance().newMessage("abcde"));
      JmsDestination jmsDest =
          vendorImp
              .createDestination(
                  "jms:queue:MyQueueName?replyToName=StaticReplyTo&priority=1&deliveryMode=NON_PERSISTENT&timeToLive=1000",
                  ptp);
      assertNotNull(jmsDest.getDestination());
      assertTrue(javax.jms.Queue.class.isAssignableFrom(jmsDest.getDestination().getClass()));
      assertNotNull(jmsDest.getReplyToDestination());
      assertTrue(javax.jms.Queue.class.isAssignableFrom(jmsDest.getReplyToDestination().getClass()));

      assertNotNull(jmsDest.deliveryMode());
      assertEquals("NON_PERSISTENT", jmsDest.deliveryMode());
      assertNotNull(jmsDest.priority());
      assertEquals(Integer.valueOf(1), jmsDest.priority());
      assertNotNull(jmsDest.timeToLive());
      assertEquals(Long.valueOf(1000), jmsDest.timeToLive());
      assertNull(jmsDest.subscriptionId());
      assertFalse(jmsDest.noLocal());
    } finally {
      stop(producer);
      broker.destroy();
    }
  }


  @Test
  public void testRfc6167_Invalid() throws Exception {
    Assume.assumeTrue(JmsConfig.jmsTestsEnabled());
    EmbeddedActiveMq broker = new EmbeddedActiveMq();
    StandaloneProducer producer = null;

    try {
      broker.start();
      BasicActiveMqImplementation vendorImp = create();
      PtpProducer ptp = new PtpProducer(new ConfiguredProduceDestination(testName.getMethodName()));
      producer = new StandaloneProducer(broker.getJmsConnection(vendorImp), ptp);
      start(producer);
      // Send a message so that the session is correct.
      producer.doService(AdaptrisMessageFactory.getDefaultInstance().newMessage("abcde"));
      try {
        JmsDestination jmsDest = vendorImp.createDestination("hello:queue:myQueueName", ptp);
      } catch (JMSException e) {
        assertTrue(e.getMessage().startsWith("failed to parse"));
      }
      try {
        JmsDestination jmsDest = vendorImp.createDestination(null, ptp);
      } catch (JMSException e) {
        assertTrue(e.getMessage().startsWith("failed to parse"));
      }
    } finally {
      stop(producer);
      broker.destroy();
    }
  }



}
