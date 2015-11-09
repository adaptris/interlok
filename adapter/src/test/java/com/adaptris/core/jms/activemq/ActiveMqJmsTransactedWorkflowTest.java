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

import static com.adaptris.core.jms.JmsConfig.DEFAULT_PAYLOAD;
import static com.adaptris.core.jms.activemq.EmbeddedActiveMq.createSafeUniqueId;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.core.BaseCase;
import com.adaptris.core.Channel;
import com.adaptris.core.ConfiguredConsumeDestination;
import com.adaptris.core.ConfiguredProduceDestination;
import com.adaptris.core.CoreException;
import com.adaptris.core.DefaultMessageFactory;
import com.adaptris.core.FixedIntervalPoller;
import com.adaptris.core.ProduceDestination;
import com.adaptris.core.ProduceException;
import com.adaptris.core.Service;
import com.adaptris.core.ServiceException;
import com.adaptris.core.ServiceImp;
import com.adaptris.core.ServiceList;
import com.adaptris.core.StandaloneProducer;
import com.adaptris.core.StandardProcessingExceptionHandler;
import com.adaptris.core.Workflow;
import com.adaptris.core.jms.JmsConnection;
import com.adaptris.core.jms.JmsConsumerImpl;
import com.adaptris.core.jms.JmsPollingConsumerImpl;
import com.adaptris.core.jms.JmsTransactedWorkflow;
import com.adaptris.core.jms.PasConsumer;
import com.adaptris.core.jms.PasProducer;
import com.adaptris.core.jms.PtpConsumer;
import com.adaptris.core.jms.PtpPollingConsumer;
import com.adaptris.core.jms.PtpProducer;
import com.adaptris.core.jms.TextMessageTranslator;
import com.adaptris.core.services.exception.ConfiguredException;
import com.adaptris.core.services.exception.ThrowExceptionService;
import com.adaptris.core.stubs.MockChannel;
import com.adaptris.core.stubs.MockMessageProducer;
import com.adaptris.core.stubs.MockSkipProducerService;
import com.adaptris.core.stubs.MockWorkflowInterceptor;
import com.adaptris.util.TimeInterval;

/**
 * Tests for JmsTransactedWorkflow that don't rely on Sonic.
 */
public class ActiveMqJmsTransactedWorkflowTest extends BaseCase {

  private static Log logR = LogFactory.getLog(ActiveMqJmsTransactedWorkflowTest.class);

  public ActiveMqJmsTransactedWorkflowTest(String arg0) {
    super(arg0);
  }

  @Override
  protected void setUp() throws Exception {
  }

  @Override
  protected void tearDown() throws Exception {
  }

  public void testHandleChannelUnavailableWithException_Bug2343() throws Exception {
    int msgCount = 10;
    EmbeddedActiveMq activeMqBroker = new EmbeddedActiveMq();
    String destination = createSafeUniqueId(new Object());
    final Channel channel = createStartableChannel(activeMqBroker, true, "testHandleChannelUnavailableWithException_Bug2343",
        destination);
    JmsTransactedWorkflow workflow = (JmsTransactedWorkflow) channel.getWorkflowList().get(0);
    workflow.setChannelUnavailableWaitInterval(new TimeInterval(1L, TimeUnit.SECONDS));
    workflow.getServiceCollection().addService(new ThrowExceptionService(new ConfiguredException("Fail")));
    try {
      activeMqBroker.start();
      channel.requestStart();
      channel.toggleAvailability(false);
      Timer t = new Timer();
      t.schedule(new TimerTask() {
        @Override
        public void run() {
          channel.toggleAvailability(true);
        }

      }, 666);
      StandaloneProducer sender = new StandaloneProducer(activeMqBroker.getJmsConnection(), new PtpProducer(
          new ConfiguredProduceDestination(destination)));
      send(sender, msgCount);
    }
    finally {
      channel.requestClose();
    }
    assertEquals(msgCount, activeMqBroker.messagesOnQueue(destination));
    activeMqBroker.destroy();
  }

  public void testHandleChannelUnavailable_Bug2343() throws Exception {
    int msgCount = 10;
    EmbeddedActiveMq activeMqBroker = new EmbeddedActiveMq();
    String destination = createSafeUniqueId(new Object());
    final Channel channel = createStartableChannel(activeMqBroker, true, "testHandleChannelUnavailable_Bug2343", destination);
    JmsTransactedWorkflow workflow = (JmsTransactedWorkflow) channel.getWorkflowList().get(0);
    workflow.setChannelUnavailableWaitInterval(new TimeInterval(1L, TimeUnit.SECONDS));
    try {
      activeMqBroker.start();
      channel.requestStart();
      channel.toggleAvailability(false);
      Timer t = new Timer();
      t.schedule(new TimerTask() {
        @Override
        public void run() {
          channel.toggleAvailability(true);
        }

      }, 666);
      StandaloneProducer sender = new StandaloneProducer(activeMqBroker.getJmsConnection(), new PtpProducer(
          new ConfiguredProduceDestination(destination)));
      send(sender, msgCount);
      waitForMessages((MockMessageProducer) workflow.getProducer(), msgCount);
      assertEquals(msgCount, ((MockMessageProducer) workflow.getProducer()).getMessages().size());
    }
    finally {
      channel.requestClose();
    }
    assertEquals(0, activeMqBroker.messagesOnQueue(destination));
    activeMqBroker.destroy();
  }

  public void testServiceException() throws Exception {
    int msgCount = 10;
    EmbeddedActiveMq activeMqBroker = new EmbeddedActiveMq();
    String destination = createSafeUniqueId(new Object());

    Channel channel = createStartableChannel(activeMqBroker, true, "testServiceException", destination);
    JmsTransactedWorkflow workflow = (JmsTransactedWorkflow) channel.getWorkflowList().get(0);
    workflow.getServiceCollection().addService(new ThrowExceptionService(new ConfiguredException("Fail")));
    try {
      activeMqBroker.start();
      channel.requestStart();
      StandaloneProducer sender = new StandaloneProducer(activeMqBroker.getJmsConnection(), new PtpProducer(
          new ConfiguredProduceDestination(destination)));
      send(sender, msgCount);

    }
    finally {
      channel.requestClose();
    }
    assertEquals(msgCount, activeMqBroker.messagesOnQueue(destination));
    activeMqBroker.destroy();
  }


  public void testProduceException() throws Exception {
    int msgCount = 10;
    EmbeddedActiveMq activeMqBroker = new EmbeddedActiveMq();
    String destination = createSafeUniqueId(new Object());

    Channel channel = createStartableChannel(activeMqBroker, true, "testProduceException", destination);
    JmsTransactedWorkflow workflow = (JmsTransactedWorkflow) channel.getWorkflowList().get(0);
    workflow.setProducer(new MockMessageProducer() {
      @Override
      public void produce(AdaptrisMessage msg) throws ProduceException {
        throw new ProduceException();
      }

      @Override
      public void produce(AdaptrisMessage msg, ProduceDestination destination) throws ProduceException {
        throw new ProduceException();
      }
    });
    try {
      activeMqBroker.start();
      channel.requestStart();
      StandaloneProducer sender = new StandaloneProducer(activeMqBroker.getJmsConnection(), new PtpProducer(
          new ConfiguredProduceDestination(destination)));
      send(sender, msgCount);
    }
    finally {
      channel.requestClose();
    }
    assertEquals(msgCount, activeMqBroker.messagesOnQueue(destination));
    activeMqBroker.destroy();
  }

  public void testRuntimeException() throws Exception {
    int msgCount = 10;
    EmbeddedActiveMq activeMqBroker = new EmbeddedActiveMq();
    String destination = createSafeUniqueId(new Object());

    Channel channel = createStartableChannel(activeMqBroker, true, "testRuntimeException", destination);
    JmsTransactedWorkflow workflow = (JmsTransactedWorkflow) channel.getWorkflowList().get(0);
    workflow.setProducer(new MockMessageProducer() {
      @Override
      public void produce(AdaptrisMessage msg) throws ProduceException {
        throw new RuntimeException();
      }

      @Override
      public void produce(AdaptrisMessage msg, ProduceDestination destination) throws ProduceException {
        throw new RuntimeException();
      }
    });
    try {
      activeMqBroker.start();
      channel.requestStart();
      StandaloneProducer sender = new StandaloneProducer(activeMqBroker.getJmsConnection(), new PtpProducer(
          new ConfiguredProduceDestination(destination)));
      send(sender, msgCount);
    }
    finally {
      channel.requestClose();
    }
    assertEquals(msgCount, activeMqBroker.messagesOnQueue(destination));
    activeMqBroker.destroy();
  }

  // In Non-Strict Mode, if you have configured an error handler, then
  // the transaction is successful.
  public void testServiceExceptionNonStrictWithErrorHandler() throws Exception {
    int msgCount = 10;
    EmbeddedActiveMq activeMqBroker = new EmbeddedActiveMq();
    String destination = createSafeUniqueId(new Object());
    MockMessageProducer meh = new MockMessageProducer();
    Channel channel = createStartableChannel(activeMqBroker, true, "testServiceExceptionNonStrictWithErrorHandler", destination);
    JmsTransactedWorkflow workflow = (JmsTransactedWorkflow) channel.getWorkflowList().get(0);
    workflow.setStrict(Boolean.FALSE);
    workflow.getServiceCollection().addService(new ThrowExceptionService(new ConfiguredException("Fail")));
    channel.setMessageErrorHandler(new StandardProcessingExceptionHandler(
    		new ServiceList(new ArrayList<Service>(Arrays.asList(new Service[]
  	    	      {
  	    	        new StandaloneProducer(meh)
  	    	      })))));
    channel.prepare();
    try {
      activeMqBroker.start();
      channel.requestStart();
      StandaloneProducer sender = new StandaloneProducer(activeMqBroker.getJmsConnection(), new PtpProducer(
          new ConfiguredProduceDestination(destination)));
      send(sender, msgCount);
      waitForMessages(meh, msgCount);
      assertEquals(0, ((MockMessageProducer) workflow.getProducer()).getMessages().size());
    }
    finally {
      channel.requestClose();
    }
    assertEquals(0, activeMqBroker.messagesOnQueue(destination));
    activeMqBroker.destroy();
  }

  // In Strict Mode, Then even if you have configured an error handler, then
  // the transaction is unsucessful if we have an exception, leading to msgs on
  // the queue.
  public void testServiceExceptionStrictWithErrorHandler() throws Exception {
    int msgCount = 10;
    EmbeddedActiveMq activeMqBroker = new EmbeddedActiveMq();
    String destination = createSafeUniqueId(new Object());
    MockMessageProducer meh = new MockMessageProducer();
    Channel channel = createStartableChannel(activeMqBroker, true, "testServiceExceptionStrictWithErrorHandler", destination);
    JmsTransactedWorkflow workflow = (JmsTransactedWorkflow) channel.getWorkflowList().get(0);
    workflow.setStrict(Boolean.TRUE);
    workflow.getServiceCollection().addService(new ThrowExceptionService(new ConfiguredException("Fail")));
    channel.setMessageErrorHandler(new StandardProcessingExceptionHandler(
    		new ServiceList(new ArrayList<Service>(Arrays.asList(new Service[]
  	    	      {
  	    	        new StandaloneProducer(meh)
  	    	      })))));
    try {
      activeMqBroker.start();
      channel.requestStart();
      StandaloneProducer sender = new StandaloneProducer(activeMqBroker.getJmsConnection(), new PtpProducer(
          new ConfiguredProduceDestination(destination)));
      send(sender, msgCount);
    }
    finally {
      channel.requestClose();
    }
    assertEquals(msgCount, activeMqBroker.messagesOnQueue(destination));
    activeMqBroker.destroy();
  }

  public void testMessagesRolledBackUsingQueue() throws Exception {
    int msgCount = 10;
    EmbeddedActiveMq activeMqBroker = new EmbeddedActiveMq();
    String destination = createSafeUniqueId(new Object());

    Channel channel = createStartableChannel(activeMqBroker, true, "testMessagesRolledBackUsingQueue", destination);
    JmsTransactedWorkflow workflow = (JmsTransactedWorkflow) channel.getWorkflowList().get(0);
    workflow.getServiceCollection().addService(new ThrowExceptionService(new ConfiguredException("Fail")));
    try {
      activeMqBroker.start();
      channel.requestStart();
      StandaloneProducer sender = new StandaloneProducer(activeMqBroker.getJmsConnection(), new PtpProducer(
          new ConfiguredProduceDestination(destination)));
      send(sender, msgCount);

    }
    finally {
      channel.requestClose();
    }
    assertEquals(msgCount, activeMqBroker.messagesOnQueue(destination));
    activeMqBroker.destroy();
  }

  public void testMessagesRolledBackUsingTopic() throws Exception {
    int msgCount = 10;
    EmbeddedActiveMq activeMqBroker = new EmbeddedActiveMq();
    String destination = createSafeUniqueId(new Object());

    Channel channel = createStartableChannel(activeMqBroker, false, "testMessagesRolledBackUsingTopic", destination);
    JmsTransactedWorkflow workflow = (JmsTransactedWorkflow) channel.getWorkflowList().get(0);
    workflow.getServiceCollection().addService(new ThrowExceptionService(new ConfiguredException("Fail")));
    try {
      activeMqBroker.start();
      channel.requestStart();
      StandaloneProducer sender = new StandaloneProducer(activeMqBroker.getJmsConnection(), new PasProducer(
          new ConfiguredProduceDestination(destination)));
      send(sender, msgCount);
      assertEquals(0, ((MockMessageProducer) workflow.getProducer()).getMessages().size());
    }
    finally {
      channel.requestClose();
    }
    // can't actually check the count of messsages on a topic, that's a trifle
    // silly; you might check per-subscription...
    // assertEquals(msgCount, activeMqBroker.messageCount(get(TOPIC)));
    activeMqBroker.destroy();
  }

  public void testMessagesCommittedUsingQueue() throws Exception {
    int msgCount = 10;
    EmbeddedActiveMq activeMqBroker = new EmbeddedActiveMq();
    String destination = createSafeUniqueId(new Object());

    Channel channel = createStartableChannel(activeMqBroker, true, "testMessagesCommittedUsingQueue", destination);
    JmsTransactedWorkflow workflow = (JmsTransactedWorkflow) channel.getWorkflowList().get(0);
    try {
      activeMqBroker.start();
      channel.requestStart();
      StandaloneProducer sender = new StandaloneProducer(activeMqBroker.getJmsConnection(), new PtpProducer(
          new ConfiguredProduceDestination(destination)));
      send(sender, msgCount);
      waitForMessages((MockMessageProducer) workflow.getProducer(), msgCount);
      assertEquals(msgCount, ((MockMessageProducer) workflow.getProducer()).getMessages().size());
    }
    finally {
      channel.requestClose();
    }
    assertEquals(0, activeMqBroker.messagesOnQueue(destination));
    activeMqBroker.destroy();
  }

  public void testWorkflow_SkipProducer() throws Exception {

    int msgCount = 10;
    EmbeddedActiveMq activeMqBroker = new EmbeddedActiveMq();
    String destination = createSafeUniqueId(new Object());

    Channel channel = createStartableChannel(activeMqBroker, true, "testWorkflow_SkipProducer", destination);
    JmsTransactedWorkflow workflow = (JmsTransactedWorkflow) channel.getWorkflowList().get(0);
    MockMessageProducer serviceProducer = new MockMessageProducer();
    workflow.getServiceCollection().addAll(Arrays.asList(new Service[] {
        new StandaloneProducer(serviceProducer), new MockSkipProducerService()
    }));
    MockMessageProducer workflowProducer = (MockMessageProducer) workflow.getProducer();

    try {
      activeMqBroker.start();
      channel.requestStart();
      StandaloneProducer sender = new StandaloneProducer(activeMqBroker.getJmsConnection(), new PtpProducer(
          new ConfiguredProduceDestination(destination)));
      send(sender, msgCount);
      waitForMessages(serviceProducer, msgCount);
      assertEquals(msgCount, serviceProducer.messageCount());
      assertEquals(0, workflowProducer.messageCount());
    }
    finally {
      channel.requestClose();
    }
    assertEquals(0, activeMqBroker.messagesOnQueue(destination));
    activeMqBroker.destroy();
  }

  public void testMessagesCommittedUsingTopic() throws Exception {
    int msgCount = 10;
    EmbeddedActiveMq activeMqBroker = new EmbeddedActiveMq();
    String destination = createSafeUniqueId(new Object());
    Channel channel = createStartableChannel(activeMqBroker, false, "testMessagesCommittedUsingTopic", destination);
    JmsTransactedWorkflow workflow = (JmsTransactedWorkflow) channel.getWorkflowList().get(0);
    try {
      activeMqBroker.start();
      channel.requestStart();
      StandaloneProducer sender = new StandaloneProducer(activeMqBroker.getJmsConnection(), new PasProducer(
          new ConfiguredProduceDestination(destination)));
      send(sender, msgCount);
      waitForMessages((MockMessageProducer) workflow.getProducer(), msgCount);
      assertEquals(msgCount, ((MockMessageProducer) workflow.getProducer()).getMessages().size());
    }
    finally {
      channel.requestClose();
    }
    activeMqBroker.destroy();
  }

  public void testWorkflowWithInterceptor() throws Exception {
    int msgCount = 10;
    EmbeddedActiveMq activeMqBroker = new EmbeddedActiveMq();
    String destination = createSafeUniqueId(new Object());
    Channel channel = createStartableChannel(activeMqBroker, false, "testMessagesCommittedUsingTopic", destination);
    JmsTransactedWorkflow workflow = (JmsTransactedWorkflow) channel.getWorkflowList().get(0);
    MockWorkflowInterceptor interceptor = new MockWorkflowInterceptor();
    workflow.addInterceptor(interceptor);
    try {
      activeMqBroker.start();
      channel.requestStart();
      StandaloneProducer sender = new StandaloneProducer(activeMqBroker.getJmsConnection(), new PasProducer(
          new ConfiguredProduceDestination(destination)));
      send(sender, msgCount);
      waitForMessages((MockMessageProducer) workflow.getProducer(), msgCount);
      assertEquals(msgCount, ((MockMessageProducer) workflow.getProducer()).getMessages().size());
      assertEquals(msgCount, interceptor.messageCount());
    }
    finally {
      channel.requestClose();
    }
    activeMqBroker.destroy();
  }

  public void testMessagesOrderedUsingQueue() throws Exception {
    int msgCount = 10;
    EmbeddedActiveMq activeMqBroker = new EmbeddedActiveMq();
    String destination = createSafeUniqueId(new Object());

    Channel channel = createStartableChannel(activeMqBroker, true, "testMessagesOrderedUsingQueue", destination);
    JmsTransactedWorkflow workflow = (JmsTransactedWorkflow) channel.getWorkflowList().get(0);
    workflow.getServiceCollection().addService(new RandomlyFail());
    try {
      activeMqBroker.start();
      channel.requestStart();
      StandaloneProducer sender = new StandaloneProducer(activeMqBroker.getJmsConnection(), new PtpProducer(
          new ConfiguredProduceDestination(destination)));
      start(sender);
      for (int i = 0; i < msgCount; i++) {
        sender.doService(AdaptrisMessageFactory.getDefaultInstance().newMessage("" + i));
      }
      stop(sender);
      waitForMessages((MockMessageProducer) workflow.getProducer(), msgCount);
      List<AdaptrisMessage> receivedList = ((MockMessageProducer) workflow.getProducer()).getMessages();
      assertEquals(msgCount, receivedList.size());

      for (int i = 0; i < msgCount; i++) {
        assertEquals(String.valueOf(i), receivedList.get(i).getStringPayload());
      }
    }
    finally {
      channel.requestClose();
    }
    assertEquals(0, activeMqBroker.messagesOnQueue(destination));
    activeMqBroker.destroy();
  }

  public void testMessagesOrderedUsingTopic() throws Exception {
    int msgCount = 10;
    EmbeddedActiveMq activeMqBroker = new EmbeddedActiveMq();
    String destination = createSafeUniqueId(new Object());
    Channel channel = createStartableChannel(activeMqBroker, false, "testMessagesOrderedUsingTopic", destination);
    JmsTransactedWorkflow workflow = (JmsTransactedWorkflow) channel.getWorkflowList().get(0);
    workflow.getServiceCollection().addService(new RandomlyFail());
    try {
      activeMqBroker.start();
      channel.requestStart();
      StandaloneProducer sender = new StandaloneProducer(activeMqBroker.getJmsConnection(), new PasProducer(
          new ConfiguredProduceDestination(destination)));
      start(sender);
      for (int i = 0; i < msgCount; i++) {
        sender.doService(AdaptrisMessageFactory.getDefaultInstance().newMessage("" + i));
      }
      stop(sender);
      waitForMessages((MockMessageProducer) workflow.getProducer(), msgCount);
      List<AdaptrisMessage> receivedList = ((MockMessageProducer) workflow.getProducer()).getMessages();
      assertEquals(msgCount, receivedList.size());

      for (int i = 0; i < msgCount; i++) {
        assertEquals(String.valueOf(i), receivedList.get(i).getStringPayload());
      }
    }
    finally {
      channel.requestClose();
    }
    activeMqBroker.destroy();
  }

  public void testMessagesOrderedUsingQueuePollingConsumer() throws Exception {
    int msgCount = 10;
    EmbeddedActiveMq activeMqBroker = new EmbeddedActiveMq();
    String destination = createSafeUniqueId(new Object());

    JmsTransactedWorkflow workflow = createPollingWorkflow(activeMqBroker, "testMessagesOrderedUsingQueuePollingConsumer",
        destination);
    Channel channel = createStartableChannel(workflow);
    workflow.getServiceCollection().addService(new RandomlyFail());
    try {
      activeMqBroker.start();
      channel.requestStart();
      StandaloneProducer sender = new StandaloneProducer(activeMqBroker.getJmsConnection(new BasicActiveMqImplementation(), true),
          new PtpProducer(new ConfiguredProduceDestination(destination)));
      start(sender);
      for (int i = 0; i < msgCount; i++) {
        sender.doService(AdaptrisMessageFactory.getDefaultInstance().newMessage("" + i));
      }
      stop(sender);
      waitForMessages((MockMessageProducer) workflow.getProducer(), msgCount);
      List<AdaptrisMessage> receivedList = ((MockMessageProducer) workflow.getProducer()).getMessages();
      assertEquals(msgCount, receivedList.size());

      for (int i = 0; i < msgCount; i++) {
        assertEquals(String.valueOf(i), receivedList.get(i).getStringPayload());
      }
    }
    finally {
      channel.requestClose();
    }
    assertEquals(0, activeMqBroker.messagesOnQueue(destination));
    activeMqBroker.destroy();
  }

  private Channel createStartableChannel(Workflow w) throws Exception {
    Channel channel = new MockChannel();
    channel.getWorkflowList().add(w);
    channel.prepare();
    return channel;
  }

  private Channel createStartableChannel(EmbeddedActiveMq mq, boolean isPtp, String threadName, String dest) throws Exception {
    Channel channel = createPlainChannel(mq, isPtp);
    channel.getWorkflowList().add(createWorkflow(isPtp, threadName, dest));
    // channel.prepare();
    return channel;
  }

  private Channel createPlainChannel(EmbeddedActiveMq mq, boolean isPtp) throws Exception {
    Channel result = new MockChannel();
    result.setUniqueId(mq.getName() + "_channel");
    result.setConsumeConnection(isPtp ? mq.getJmsConnection() : mq.getJmsConnection());
    return result;
  }

  private JmsTransactedWorkflow createWorkflow(boolean isPtp, String threadName, String target) throws CoreException {
    JmsTransactedWorkflow workflow = new JmsTransactedWorkflow();
    workflow.setWaitPeriodAfterRollback(new TimeInterval(10L, TimeUnit.MILLISECONDS.name()));

    workflow.setProducer(new MockMessageProducer());
    JmsConsumerImpl jmsCons = isPtp ? new PtpConsumer(new ConfiguredConsumeDestination(target, null, threadName)) : new PasConsumer(
        new ConfiguredConsumeDestination(target, null, threadName));
    jmsCons.setMessageTranslator(new TextMessageTranslator(true, true));
    workflow.setConsumer(jmsCons);
    return workflow;
  }

  private JmsTransactedWorkflow createPollingWorkflow(EmbeddedActiveMq mq, String threadName, String target) throws CoreException {
    JmsTransactedWorkflow workflow = new JmsTransactedWorkflow();
    workflow.setProducer(new MockMessageProducer());
    workflow.setWaitPeriodAfterRollback(new TimeInterval(10L, TimeUnit.MILLISECONDS.name()));

    JmsPollingConsumerImpl jmsCons = new PtpPollingConsumer(new ConfiguredConsumeDestination(target, null, threadName));
    jmsCons.setReacquireLockBetweenMessages(true);
    jmsCons.setAdditionalDebug(true);
    jmsCons.setPoller(new FixedIntervalPoller(new TimeInterval(2L, TimeUnit.SECONDS)));
    BasicActiveMqImplementation vendorImpl = new BasicActiveMqImplementation();
    JmsConnection jmsConn = mq.getJmsConnection(vendorImpl, true);
    jmsCons.setVendorImplementation(jmsConn.getVendorImplementation());
    jmsCons.setMessageTranslator(new TextMessageTranslator(true, true));
    jmsCons.setClientId(jmsConn.getClientId());
    workflow.setConsumer(jmsCons);
    return workflow;
  }

  private void send(StandaloneProducer sender, int count) throws Exception {
    start(sender);
    for (int i = 0; i < count; i++) {
      sender.doService(new DefaultMessageFactory().newMessage(DEFAULT_PAYLOAD));
    }
    stop(sender);
  }

  private class RandomlyFail extends ServiceImp {
    public void doService(AdaptrisMessage msg) throws ServiceException {
      int i = new Random().nextInt(20) + 1;
      if ((i & i - 1) == 0) {
        throw new ServiceException(this.getClass().getSimpleName() + " failure, " + i + " is a power of 2");
      }
    }

    @Override
    protected void initService() throws CoreException {

    }

    @Override
    protected void closeService() {

    }
    @Override
    public void prepare() throws CoreException {}

  }
}
