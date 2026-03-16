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

import com.adaptris.core.*;
import com.adaptris.core.jms.activemq.BasicActiveMqImplementation;
import com.adaptris.core.jms.activemq.EmbeddedActiveMq;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import com.adaptris.core.stubs.MockMessageListener;

public class PasProducerTest extends BasicJmsProducerCase {

  /**
   * @see com.adaptris.core.ExampleConfigCase#retrieveObjectForSampleConfig()
   */
  @Override
  protected Object retrieveObjectForSampleConfig() {
    return retrieveSampleConfig();
  }

  @Override
  protected String createBaseFileName(Object object) {
    ((StandaloneProducer) object).getProducer();
    return super.createBaseFileName(object);
  }

  private StandaloneProducer retrieveSampleConfig() {

    PasProducer p = new PasProducer().withTopic("topicName");
    JmsConnection c = new JmsConnection(new BasicActiveMqImplementation("tcp://localhost:61616"));
    c.setConnectionErrorHandler(new JmsConnectionErrorHandler());
    NullCorrelationIdSource mcs = new NullCorrelationIdSource();
    p.setCorrelationIdSource(mcs);

    StandaloneProducer result = new StandaloneProducer();

    result.setConnection(c);
    result.setProducer(p);

    return result;
  }


  @Override
  protected DefinedJmsProducer createProducer(String dest) {
    return new PasProducer().withTopic(dest);
  }

  @Override
  protected JmsConsumerImpl createConsumer(String dest) {
    return new PasConsumer().withTopic(dest);
  }

  @Override
  protected TopicLoopback createLoopback(EmbeddedActiveMq mq, String dest) {
    return new TopicLoopback(mq, dest);
  }

  @Test
  public void testDoProduce() throws Exception {
    String topicName = "testDoProduceTopic";
    EmbeddedActiveMq broker = new EmbeddedActiveMq();
    broker.start();
    try {
      PasProducer producer = new PasProducer().withTopic(topicName);
      StandaloneProducer standaloneProducer = new StandaloneProducer(broker.getJmsConnection(), producer);
      PasConsumer consumer = new PasConsumer().withTopic(topicName);
      StandaloneConsumer standaloneConsumer = new StandaloneConsumer(broker.getJmsConnection(), consumer);
      MockMessageListener listener = new MockMessageListener();
      standaloneConsumer.registerAdaptrisMessageListener(listener);
      start(standaloneConsumer);
      start(standaloneProducer);
      AdaptrisMessage msg = DefaultMessageFactory.getDefaultInstance().newMessage("Hello JMS Topic");
      producer.doProduce(msg, topicName);
      assertEquals(1, listener.getMessages().size(), "Message should be received on topic");
      assertEquals("Hello JMS Topic", listener.getMessages().get(0).getContent(), "Message content should match");
      stop(standaloneProducer);
      stop(standaloneConsumer);
    } finally {
      broker.destroy();
    }
  }

  @Test
  public void testDefinedJmsProducer_RetryLogic() {
      RetryOnceDefinedJmsProducer producer = new RetryOnceDefinedJmsProducer();
      producer.refreshSessionIfProduceException = true;
      assertDoesNotThrow(() -> producer.doProduce(new com.adaptris.core.DefaultMessageFactory().newMessage(), (javax.jms.Destination) null, (javax.jms.Destination) null));
  }

  @Test
  public void testDefinedJmsProducer_RetryLogic_BothAttemptsFail() {
      AlwaysFailingDefinedJmsProducer producer = new AlwaysFailingDefinedJmsProducer();
      producer.refreshSessionIfProduceException = true;
      assertThrows(javax.jms.JMSException.class, () ->
            producer.doProduce(new com.adaptris.core.DefaultMessageFactory().newMessage(), (javax.jms.Destination) null, (javax.jms.Destination) null)
      );
  }

  // Test double that simulates retry logic by overriding sendMessage only
  static class RetryOnceDefinedJmsProducer extends DefinedJmsProducer {
    private boolean first = true;
    protected javax.jms.Message sendMessage(AdaptrisMessage msg, javax.jms.Destination destination, javax.jms.Destination replyTo) throws javax.jms.JMSException {
      if (first) {
        first = false;
        throw new javax.jms.JMSException("Simulated failure");
      }
      // Always return a dummy message, never call super
      return new org.apache.activemq.command.ActiveMQTextMessage();
    }
    @Override protected void captureOutgoingMessageDetails(javax.jms.Message jmsMsg, AdaptrisMessage msg) { return; }
    @Override protected void logLinkedException(String prefix, Exception e) { throw new UnsupportedOperationException(); }
    @Override public void rollback() { throw new UnsupportedOperationException(); }
    @Override public ProducerSession setupSession(AdaptrisMessage msg) { return null; }
    @Override public ProducerSession setupSession(AdaptrisMessage msg, boolean forceRecreate) { return null; }
    protected void log(String s, Object... args) { throw new UnsupportedOperationException(); }
    @Override protected javax.jms.Destination createDestination(String dest) { return null; }
    @Override protected javax.jms.Destination createTemporaryDestination() { return null; }
    @Override public String endpoint(AdaptrisMessage msg) { return null; }
  }

  // Test double that always fails sendMessage to cover retry catch block
  static class AlwaysFailingDefinedJmsProducer extends DefinedJmsProducer {
    protected javax.jms.Message sendMessage(AdaptrisMessage msg, javax.jms.Destination destination, javax.jms.Destination replyTo) throws javax.jms.JMSException {
      throw new javax.jms.JMSException("Simulated failure");
    }
    @Override protected void captureOutgoingMessageDetails(javax.jms.Message jmsMsg, AdaptrisMessage msg) { return; }
    @Override protected void logLinkedException(String prefix, Exception e) { throw new UnsupportedOperationException(); }
    @Override public void rollback() { throw new UnsupportedOperationException(); }
    @Override public ProducerSession setupSession(AdaptrisMessage msg) { return null; }
    @Override public ProducerSession setupSession(AdaptrisMessage msg, boolean forceRecreate) { return null; }
    protected void log(String s, Object... args) { throw new UnsupportedOperationException(); }
    @Override protected javax.jms.Destination createDestination(String dest) { return null; }
    @Override protected javax.jms.Destination createTemporaryDestination() { return null; }
    @Override public String endpoint(AdaptrisMessage msg) { return null; }
  }
}
