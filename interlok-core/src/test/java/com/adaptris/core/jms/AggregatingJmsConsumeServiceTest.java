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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import java.util.concurrent.TimeUnit;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.DefaultMessageFactory;
import com.adaptris.core.ServiceException;
import com.adaptris.core.StandaloneProducer;
import com.adaptris.core.jms.activemq.BasicActiveMqImplementation;
import com.adaptris.core.jms.activemq.EmbeddedActiveMq;
import com.adaptris.core.services.aggregator.AggregatingServiceExample;
import com.adaptris.core.services.aggregator.ConsumeDestinationFromMetadata;
import com.adaptris.core.services.aggregator.IgnoreOriginalMimeAggregator;
import com.adaptris.core.services.aggregator.ReplaceWithFirstMessage;
import com.adaptris.core.util.MimeHelper;
import com.adaptris.util.TimeInterval;
import com.adaptris.util.text.mime.BodyPartIterator;

public class AggregatingJmsConsumeServiceTest extends AggregatingServiceExample {

  private static final String DATA_QUEUE = "SampleQueue";
  protected static final String DATA_PAYLOAD = "Pack my box with five dozen liquor jugs";
  protected static final String PAYLOAD = "Glib jocks quiz nymph to vex dwarf";
  private static final String CORRELATION_ID_KEY = "correlationId";

  private static final String DEFAULT_FILTER_KEY = "metadataFilterKey";
  
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
  
  @Override
  protected boolean doStateTests() {
    return false;
  }

  @Test
  public void testNoOpMethods() throws Exception {
    
    AggregatingJmsConsumeService service = createService(activeMqBroker);
    try {
      start(service);
      assertNull(service.configuredCorrelationIdSource());
      assertNull(service.configuredMessageListener());
      assertNull(service.configuredMessageTranslator());
      assertEquals(AcknowledgeMode.Mode.AUTO_ACKNOWLEDGE.acknowledgeMode(), service.configuredAcknowledgeMode());
      assertEquals(0, service.rollbackTimeout());
      assertEquals(false, service.isManagedTransaction());
    }
    finally {
      stop(service);
    }
  }

  @Test
  public void testService() throws Exception {
    AggregatingJmsConsumeService service = createService(activeMqBroker);
    try {
      sendDataMessage(activeMqBroker);
      start(service);
      AdaptrisMessage msg = new DefaultMessageFactory().newMessage(PAYLOAD);
      msg.addMetadata(DEFAULT_FILTER_KEY, "JMSCorrelationID = '0001'");
      service.doService(msg);
      assertNotSame(PAYLOAD, msg.getContent());
      assertEquals(DATA_PAYLOAD, msg.getContent());
    }
    finally {
      stop(service);
    }
  }

  @Test
  public void testServiceWithTimeout() throws Exception {
    AggregatingJmsConsumeService service = createService(activeMqBroker);
    try {
      sendDataMessage(activeMqBroker);
      start(service);
      AdaptrisMessage msg = new DefaultMessageFactory().newMessage(PAYLOAD);
      msg.addMetadata(DEFAULT_FILTER_KEY, "JMSCorrelationID = '0002'");
      // Will timeout after 5 seconds...
      service.doService(msg);
      fail();
    }
    catch (ServiceException expected) {

    }
    finally {
      stop(service);
    }
  }

  @Test
  public void testService_MultipleMessages() throws Exception {
    AggregatingQueueConsumer consumer = new AggregatingQueueConsumer();
    consumer.setMessageAggregator(new IgnoreOriginalMimeAggregator());
    AggregatingJmsConsumeService service = createService(activeMqBroker, consumer, getName());
    try {
      sendDataMessage(activeMqBroker, getName());
      sendDataMessage(activeMqBroker, getName());
      start(service);
      AdaptrisMessage msg = new DefaultMessageFactory().newMessage(PAYLOAD);
      msg.addMetadata(DEFAULT_FILTER_KEY, "JMSCorrelationID = '0001'");
      service.doService(msg);
      BodyPartIterator input = MimeHelper.createBodyPartIterator(msg);
      assertEquals(2, input.size());
    }
    finally {
      stop(service);
    }
  }

  private AggregatingJmsConsumeService createService(EmbeddedActiveMq broker) {
    AggregatingQueueConsumer consumer = new AggregatingQueueConsumer();
    consumer.setMessageAggregator(new ReplaceWithFirstMessage());
    return createService(broker, consumer);
  }

  private AggregatingJmsConsumeService createService(EmbeddedActiveMq broker, AggregatingQueueConsumer consumer) {
    return createService(broker, consumer, DATA_QUEUE);
  }
  
  private AggregatingJmsConsumeService createService(EmbeddedActiveMq broker, AggregatingQueueConsumer consumer, String queue) {
    AggregatingJmsConsumeService result = new AggregatingJmsConsumeService();
    result.setConnection(broker.getJmsConnection(new BasicActiveMqImplementation(), true));
    ConsumeDestinationFromMetadata destination = new ConsumeDestinationFromMetadata();
    destination.setFilterMetadataKey(DEFAULT_FILTER_KEY);
    destination.setDefaultDestination(queue);
    consumer.setDestination(destination);
    consumer.setTimeout(new TimeInterval(5L, TimeUnit.SECONDS));
    result.setJmsConsumer(consumer);
    return result;
  }

  private void sendDataMessage(EmbeddedActiveMq broker) throws Exception {
    sendDataMessage(broker, DATA_QUEUE);
  }
  
  private void sendDataMessage(EmbeddedActiveMq broker, String queue) throws Exception {
    PtpProducer producer = new PtpProducer().withQueue(queue);
    MetadataCorrelationIdSource corr = new MetadataCorrelationIdSource();
    corr.setMetadataKey(CORRELATION_ID_KEY);
    producer.setCorrelationIdSource(corr);
    StandaloneProducer sp = new StandaloneProducer(broker.getJmsConnection(new BasicActiveMqImplementation(), true), producer);
    try {
      start(sp);
      AdaptrisMessage msg = new DefaultMessageFactory().newMessage(DATA_PAYLOAD);
      msg.addMetadata(CORRELATION_ID_KEY, "0001");
      sp.doService(msg);
    }
    finally {
      stop(sp);
    }
  }

  @Override
  protected Object retrieveObjectForSampleConfig() {
    AggregatingJmsConsumeService service = null;

    service = new AggregatingJmsConsumeService();
    ConsumeDestinationFromMetadata mfd = new ConsumeDestinationFromMetadata();
    mfd.setDefaultDestination("SampleQ1");
    mfd.setFilterMetadataKey("filterSelectorKey");
    JmsConnection jmsConnection = new JmsConnection(new BasicActiveMqImplementation("tcp://localhost:61616"));
    jmsConnection.setConnectionAttempts(2);
    jmsConnection.setConnectionRetryInterval(new TimeInterval(3L, "SECONDS"));
    service.setConnection(jmsConnection);
    AggregatingQueueConsumer consumer = new AggregatingQueueConsumer(mfd);
    consumer.setMessageAggregator(new ReplaceWithFirstMessage());
    consumer.setMessageTranslator(new TextMessageTranslator());
    service.setJmsConsumer(consumer);
    return service;
  }


  @Override
  protected String getExampleCommentHeader(Object o) {
    return super.getExampleCommentHeader(o)
        + "\n<!-- \n In the example here, you will have previously generated a metadata value that contains a valid filter"
        + "\nexpression for JMS; e.g. filterSelectorKey contains the value \"JMSCorrelationID= '001'\"."
        + "\nwhich will be used to filter messages on the queue 'SampleQ1' where the JMSCorrelationID matches that value."
        + "\nNote that the queue could also be derived from metadata rather than being statically configured."
        + "\n\nYou can use the other metadata services that are available to create correct metadata value such as "
        + "\n1. XpathMetadataService to extract the correlation ID from your document"
        + "\n2. ReplaceMetadataService with a search-value of \"^(.*)$\" and a replacement-value of \"JMSCorrelationID = '{1}'\""
        + "\n\nThe exact filter expression you require will depend on your JMS provider." + "\n-->\n";
  }

}
