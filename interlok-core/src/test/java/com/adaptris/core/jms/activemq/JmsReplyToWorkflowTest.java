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
import static org.junit.Assert.fail;
import java.util.concurrent.TimeUnit;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.core.Channel;
import com.adaptris.core.NullConnectionErrorHandler;
import com.adaptris.core.ProduceException;
import com.adaptris.core.ServiceList;
import com.adaptris.core.StandaloneRequestor;
import com.adaptris.core.jms.JmsReplyToDestination;
import com.adaptris.core.jms.JmsReplyToWorkflow;
import com.adaptris.core.jms.PasConsumer;
import com.adaptris.core.jms.PasProducer;
import com.adaptris.core.jms.PtpConsumer;
import com.adaptris.core.jms.PtpProducer;
import com.adaptris.core.services.metadata.PayloadFromTemplateService;
import com.adaptris.core.stubs.MockChannel;
import com.adaptris.core.stubs.MockSkipProducerService;
import com.adaptris.core.stubs.MockWorkflowInterceptor;
import com.adaptris.core.util.LifecycleHelper;
import com.adaptris.util.TimeInterval;

/**
 * <p>
 * Tests for JmsReplyToWorkflow.
 * </p>
 */
@SuppressWarnings("deprecation")
public class JmsReplyToWorkflowTest {

  private static final String REQUEST_TEXT = "Hello World";
  private static final String REPLY_TEXT = "Goodbye Cruel World";

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

  @Test
  public void testInitWithNullProducerConsumer() throws Exception {
    JmsReplyToWorkflow workflow = new JmsReplyToWorkflow();
    Channel channel = createChannel(activeMqBroker);
    channel.getWorkflowList().add(workflow);
    try {
      LifecycleHelper.prepare(channel);
      LifecycleHelper.init(channel);
      fail("shouldn't init without JMS Producer & Consumer");
    }
    catch (Exception e) {
      // do nothing
    }
    finally {
      LifecycleHelper.stopAndClose(channel);
    }

  }

  @Test
  public void testInitWithNullProducer() throws Exception {
    JmsReplyToWorkflow workflow = new JmsReplyToWorkflow();
    Channel channel = createChannel(activeMqBroker);
    workflow
        .setConsumer(new PtpConsumer().withQueue(testName.getMethodName()));
    channel.getWorkflowList().add(workflow);
    try {
      LifecycleHelper.prepare(channel);
      LifecycleHelper.init(channel);

      fail("shouldn't init without JMS Producer");
    }
    catch (Exception e) {
      // do nothing
    }
    finally {
      LifecycleHelper.stopAndClose(channel);
    }

  }

  @Test
  public void testInitWithNullConsumer() throws Exception {
    JmsReplyToWorkflow workflow = new JmsReplyToWorkflow();
    Channel channel = createChannel(activeMqBroker);
    workflow.setProducer(new PtpProducer());
    channel.getWorkflowList().add(workflow);
    try {
      LifecycleHelper.prepare(channel);
      LifecycleHelper.init(channel);

      fail("shouldn't init without JMS Producer");
    }
    catch (Exception e) {
      // do nothing
    }
    finally {
      LifecycleHelper.stopAndClose(channel);
    }

  }

  @Test
  public void testInitWithMisMatchedProducer() throws Exception {
    JmsReplyToWorkflow workflow = new JmsReplyToWorkflow();
    Channel channel = createChannel(activeMqBroker);
    channel.setProduceConnection(activeMqBroker.getJmsConnection());
    workflow.setProducer(new PasProducer().withTopic(testName.getMethodName()));
    channel.getWorkflowList().add(workflow);
    try {
      LifecycleHelper.prepare(channel);
      LifecycleHelper.init(channel);

      fail("shouldn't init with a Pas Producer / Ptp Consumer");
    }
    catch (Exception e) {
      // do nothing
    }
    finally {
      LifecycleHelper.stopAndClose(channel);
    }

  }

  @Test
  public void testInit() throws Exception {
    JmsReplyToWorkflow workflow = new JmsReplyToWorkflow();
    Channel channel = createChannel(activeMqBroker);
    workflow.setProducer(new PtpProducer().withQueue("TODO"));
    workflow.setConsumer(new PtpConsumer().withQueue(testName.getMethodName()));
    channel.getWorkflowList().add(workflow);
    try {
      LifecycleHelper.initAndStart(channel);
    }
    finally {
      LifecycleHelper.stopAndClose(channel);
    }
  }

  @Test
  public void testPtpWorkflow() throws Exception {
    JmsReplyToWorkflow workflow = new JmsReplyToWorkflow();
    Channel channel = createChannel(activeMqBroker);
    workflow.setProducer(new PtpProducer().withQueue("TODO"));
    workflow.setConsumer(new PtpConsumer().withQueue(testName.getMethodName()));
    workflow.setServiceCollection(createServiceList());
    channel.getWorkflowList().add(workflow);
    StandaloneRequestor sender = new StandaloneRequestor(activeMqBroker.getJmsConnection(),
        new PtpProducer().withQueue(testName.getMethodName()));
    try {
      sender.setReplyTimeout(new TimeInterval(10L, TimeUnit.SECONDS));
      start(channel, sender);
      AdaptrisMessage m = AdaptrisMessageFactory.getDefaultInstance().newMessage(REQUEST_TEXT);
      sender.doService(m);
      assertEquals(REPLY_TEXT, m.getContent());
    }
    finally {
      stop(channel, sender);
    }
  }

  @Test
  public void testPasWorkflow() throws Exception {
    JmsReplyToWorkflow workflow = new JmsReplyToWorkflow();
    Channel channel = createChannel(activeMqBroker);
    workflow.setProducer(new PasProducer().withTopic("TODO"));
    workflow.setConsumer(new PasConsumer().withTopic(testName.getMethodName()));
    workflow.setServiceCollection(createServiceList());
    channel.getWorkflowList().add(workflow);
    StandaloneRequestor sender = new StandaloneRequestor(activeMqBroker.getJmsConnection(),
        new PasProducer().withTopic(testName.getMethodName()));
    try {
      sender.setReplyTimeout(new TimeInterval(10L, TimeUnit.SECONDS));
      start(channel, sender);
      AdaptrisMessage m = AdaptrisMessageFactory.getDefaultInstance().newMessage(REQUEST_TEXT);
      sender.doService(m);
      assertEquals(REPLY_TEXT, m.getContent());
    }
    finally {
      stop(channel, sender);
    }
  }

  @Test
  public void testWorkflow_SkipProducer_HasNoEffect() throws Exception {
    JmsReplyToWorkflow workflow = new JmsReplyToWorkflow();
    Channel channel = createChannel(activeMqBroker);
    workflow.setProducer(new PtpProducer().withQueue("TODO"));
    workflow.setConsumer(new PtpConsumer().withQueue(testName.getMethodName()));

    workflow.getServiceCollection().add(new PayloadFromTemplateService().withTemplate(REPLY_TEXT));
    workflow.getServiceCollection().add(new MockSkipProducerService());
    channel.getWorkflowList().add(workflow);
    channel.prepare();
    StandaloneRequestor sender = new StandaloneRequestor(activeMqBroker.getJmsConnection(),
        new PtpProducer().withQueue(testName.getMethodName()));
    try {
      sender.setReplyTimeout(new TimeInterval(10L, TimeUnit.SECONDS));
      start(channel, sender);
      AdaptrisMessage m = AdaptrisMessageFactory.getDefaultInstance().newMessage(REQUEST_TEXT);
      sender.doService(m);
      assertEquals(REPLY_TEXT, m.getContent());
    }
    finally {
      stop(channel, sender);
    }
  }

  @Test
  public void testWorkflowWithInterceptor() throws Exception {
    MockWorkflowInterceptor interceptor = new MockWorkflowInterceptor();
    JmsReplyToWorkflow workflow = new JmsReplyToWorkflow();
    workflow.addInterceptor(interceptor);
    Channel channel = createChannel(activeMqBroker);
    workflow.setProducer(new PasProducer().withTopic("TODO"));
    workflow.setConsumer(new PasConsumer().withTopic(testName.getMethodName()));
    workflow.setServiceCollection(createServiceList());
    channel.getWorkflowList().add(workflow);
    StandaloneRequestor sender = new StandaloneRequestor(activeMqBroker.getJmsConnection(),
        new PasProducer().withTopic(testName.getMethodName()));
    try {
      sender.setReplyTimeout(new TimeInterval(10L, TimeUnit.SECONDS));
      start(channel, sender);
      AdaptrisMessage m = AdaptrisMessageFactory.getDefaultInstance().newMessage(REQUEST_TEXT);
      sender.doService(m);
      assertEquals(REPLY_TEXT, m.getContent());
      assertEquals(1, interceptor.messageCount());
    }
    finally {
      stop(channel, sender);
    }
  }

  @Test
  public void testDoProduce_NoObjectMetadata() throws Exception {
    JmsReplyToWorkflow workflow = new JmsReplyToWorkflow();
    Channel channel = createChannel(activeMqBroker);
    workflow.setProducer(new PtpProducer().withQueue("TODO"));
    workflow.setConsumer(new PtpConsumer().withQueue(testName.getMethodName()));
    channel.getWorkflowList().add(workflow);
    try {
      start(channel);
      AdaptrisMessage m = AdaptrisMessageFactory.getDefaultInstance().newMessage(REQUEST_TEXT);
      // No Object metadata, so should throw an exception.
      workflow.doProduce(m);
      fail();
    } catch (ProduceException expected) {

    } finally {
      stop(channel);
    }
  }


  private Channel createChannel(EmbeddedActiveMq broker) throws Exception {
    Channel channel = new MockChannel();
    channel.setConsumeConnection(broker.getJmsConnection());
    channel.setProduceConnection(broker.getJmsConnection());
    channel.getProduceConnection().setConnectionErrorHandler(new NullConnectionErrorHandler());
    return channel;
  }

  private ServiceList createServiceList() {
    ServiceList result = new ServiceList();
    PayloadFromTemplateService pm = new PayloadFromTemplateService().withTemplate(REPLY_TEXT);
    result.addService(pm);
    return result;
  }
}
