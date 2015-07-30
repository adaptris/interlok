/*
 * $RCSfile: JmsReplyToWorkflowTest.java,v $
 * $Revision: 1.12 $
 * $Date: 2009/06/30 09:16:27 $
 * $Author: lchan $
 */
package com.adaptris.core.jms.activemq;

import java.util.concurrent.TimeUnit;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.core.BaseCase;
import com.adaptris.core.Channel;
import com.adaptris.core.ConfiguredConsumeDestination;
import com.adaptris.core.ConfiguredProduceDestination;
import com.adaptris.core.ServiceList;
import com.adaptris.core.StandaloneRequestor;
import com.adaptris.core.jms.JmsReplyToWorkflow;
import com.adaptris.core.jms.PasConsumer;
import com.adaptris.core.jms.PasProducer;
import com.adaptris.core.jms.PtpConsumer;
import com.adaptris.core.jms.PtpProducer;
import com.adaptris.core.services.metadata.PayloadFromMetadataService;
import com.adaptris.core.stubs.MockChannel;
import com.adaptris.core.stubs.MockSkipProducerService;
import com.adaptris.core.stubs.MockWorkflowInterceptor;
import com.adaptris.util.TimeInterval;

/**
 * <p>
 * Tests for JmsReplyToWorkflow.
 * </p>
 */
@SuppressWarnings("deprecation")
public class JmsReplyToWorkflowTest extends BaseCase {

  private static final String REQUEST_TEXT = "Hello World";
  private static final String REPLY_TEXT = "Goodbye Cruel World";

  /**
   * Constructor for JmsReplyToWorkflowTest.
   *
   * @param arg0
   */
  public JmsReplyToWorkflowTest(String arg0) {
    super(arg0);
  }

  @Override
  protected void setUp() throws Exception {
  }

  public void testInitWithNullProducerConsumer() throws Exception {
    EmbeddedActiveMq broker = new EmbeddedActiveMq();
    broker.start();
    JmsReplyToWorkflow workflow = new JmsReplyToWorkflow();
    Channel channel = createChannel(broker, true);
    channel.getWorkflowList().add(workflow);
    channel.prepare();
    try {
      channel.requestInit();
      fail("shouldn't init without JMS Producer & Consumer");
    }
    catch (Exception e) {
      // do nothing
    }
    broker.destroy();
  }

  public void testInitWithNullConsumer() throws Exception {
    EmbeddedActiveMq broker = new EmbeddedActiveMq();
    broker.start();
    JmsReplyToWorkflow workflow = new JmsReplyToWorkflow();
    Channel channel = createChannel(broker, true);
    workflow.setConsumer(new PtpConsumer(new ConfiguredConsumeDestination(getName())));
    channel.getWorkflowList().add(workflow);
    channel.prepare();
    try {
      channel.requestInit();
      fail("shouldn't init without JMS MessageConsumer");
    }
    catch (Exception e) {
      // do nothing
    }
    broker.destroy();
  }

  public void testInitWithNullProducer() throws Exception {
    EmbeddedActiveMq broker = new EmbeddedActiveMq();
    broker.start();
    JmsReplyToWorkflow workflow = new JmsReplyToWorkflow();
    Channel channel = createChannel(broker, true);
    workflow.setProducer(new PtpProducer());
    channel.getWorkflowList().add(workflow);
    channel.prepare();
    try {
      channel.requestInit();
      fail("shouldn't init without JMS Producer");
    }
    catch (Exception e) {
      // do nothing
    }
    broker.destroy();
  }

  public void testInitWithMisMatchedProducer() throws Exception {
    EmbeddedActiveMq broker = new EmbeddedActiveMq();
    broker.start();
    JmsReplyToWorkflow workflow = new JmsReplyToWorkflow();
    Channel channel = createChannel(broker, true);
    channel.setProduceConnection(broker.getJmsConnection());
    workflow.setProducer(new PasProducer());
    channel.getWorkflowList().add(workflow);
    channel.prepare();
    try {
      channel.requestInit();
      fail("shouldn't init with a Pas Producer / Ptp Consumer");
    }
    catch (Exception e) {
      // do nothing
    }
    broker.destroy();
  }

  public void testInit() throws Exception {
    EmbeddedActiveMq broker = new EmbeddedActiveMq();
    broker.start();
    JmsReplyToWorkflow workflow = new JmsReplyToWorkflow();
    Channel channel = createChannel(broker, true);
    workflow.setProducer(new PtpProducer());
    workflow.setConsumer(new PtpConsumer(new ConfiguredConsumeDestination(getName())));
    channel.getWorkflowList().add(workflow);
    channel.prepare();
    try {
      channel.requestInit();
    }
    finally {
      broker.destroy();
    }
  }

  public void testPtpWorkflow() throws Exception {
    EmbeddedActiveMq broker = new EmbeddedActiveMq();
    broker.start();
    JmsReplyToWorkflow workflow = new JmsReplyToWorkflow();
    Channel channel = createChannel(broker, true);
    workflow.setProducer(new PtpProducer());
    workflow.setConsumer(new PtpConsumer(new ConfiguredConsumeDestination(getName())));
    workflow.setServiceCollection(createServiceList());
    channel.getWorkflowList().add(workflow);
    channel.prepare();
    StandaloneRequestor sender = new StandaloneRequestor(broker.getJmsConnection(), new PtpProducer(
        new ConfiguredProduceDestination(getName())));
    try {
      channel.requestStart();
      sender.setReplyTimeout(new TimeInterval(10L, TimeUnit.SECONDS));
      start(sender);
      AdaptrisMessage m = AdaptrisMessageFactory.getDefaultInstance().newMessage(REQUEST_TEXT);
      sender.doService(m);
      assertEquals(REPLY_TEXT, m.getStringPayload());
    }
    finally {
      channel.requestClose();
      stop(sender);
      broker.destroy();
    }
  }

  public void testPasWorkflow() throws Exception {
    EmbeddedActiveMq broker = new EmbeddedActiveMq();
    broker.start();
    JmsReplyToWorkflow workflow = new JmsReplyToWorkflow();
    Channel channel = createChannel(broker, false);
    workflow.setProducer(new PasProducer());
    workflow.setConsumer(new PasConsumer(new ConfiguredConsumeDestination(getName())));
    workflow.setServiceCollection(createServiceList());
    channel.getWorkflowList().add(workflow);
    channel.prepare();
    StandaloneRequestor sender = new StandaloneRequestor(broker.getJmsConnection(), new PasProducer(
        new ConfiguredProduceDestination(getName())));
    try {
      channel.requestStart();
      sender.setReplyTimeout(new TimeInterval(10L, TimeUnit.SECONDS));
      start(sender);
      AdaptrisMessage m = AdaptrisMessageFactory.getDefaultInstance().newMessage(REQUEST_TEXT);
      sender.doService(m);
      assertEquals(REPLY_TEXT, m.getStringPayload());
    }
    finally {
      channel.requestClose();
      stop(sender);
      broker.destroy();
    }
  }

  public void testWorkflow_SkipProducer_HasNoEffect() throws Exception {

    EmbeddedActiveMq broker = new EmbeddedActiveMq();
    broker.start();
    JmsReplyToWorkflow workflow = new JmsReplyToWorkflow();
    Channel channel = createChannel(broker, true);
    workflow.setProducer(new PtpProducer());
    workflow.setConsumer(new PtpConsumer(new ConfiguredConsumeDestination(getName())));

    PayloadFromMetadataService pm = new PayloadFromMetadataService();
    pm.setTemplate(REPLY_TEXT);

    workflow.getServiceCollection().add(pm);
    workflow.getServiceCollection().add(new MockSkipProducerService());
    channel.getWorkflowList().add(workflow);
    channel.prepare();
    StandaloneRequestor sender = new StandaloneRequestor(broker.getJmsConnection(), new PtpProducer(
        new ConfiguredProduceDestination(getName())));
    try {
      start(channel);
      sender.setReplyTimeout(new TimeInterval(10L, TimeUnit.SECONDS));
      start(sender);
      AdaptrisMessage m = AdaptrisMessageFactory.getDefaultInstance().newMessage(REQUEST_TEXT);
      sender.doService(m);
      assertEquals(REPLY_TEXT, m.getStringPayload());
    }
    finally {
      stop(channel);
      stop(sender);
      broker.destroy();
    }
  }

  public void testWorkflowWithInterceptor() throws Exception {
    MockWorkflowInterceptor interceptor = new MockWorkflowInterceptor();
    EmbeddedActiveMq broker = new EmbeddedActiveMq();
    broker.start();
    JmsReplyToWorkflow workflow = new JmsReplyToWorkflow();
    workflow.addInterceptor(interceptor);
    Channel channel = createChannel(broker, false);
    workflow.setProducer(new PasProducer());
    workflow.setConsumer(new PasConsumer(new ConfiguredConsumeDestination(getName())));
    workflow.setServiceCollection(createServiceList());
    channel.getWorkflowList().add(workflow);
    channel.prepare();
    StandaloneRequestor sender = new StandaloneRequestor(broker.getJmsConnection(), new PasProducer(
        new ConfiguredProduceDestination(getName())));
    try {
      channel.requestStart();
      sender.setReplyTimeout(new TimeInterval(10L, TimeUnit.SECONDS));
      start(sender);
      AdaptrisMessage m = AdaptrisMessageFactory.getDefaultInstance().newMessage(REQUEST_TEXT);
      sender.doService(m);
      assertEquals(REPLY_TEXT, m.getStringPayload());
      assertEquals(1, interceptor.messageCount());
    }
    finally {
      channel.requestClose();
      stop(sender);
      broker.destroy();
    }
  }

  private Channel createChannel(EmbeddedActiveMq broker, boolean isPtp) throws Exception {
    Channel channel = new MockChannel();
    channel.setConsumeConnection(isPtp ? broker.getJmsConnection() : broker.getJmsConnection());
    channel.setProduceConnection(isPtp ? broker.getJmsConnection() : broker.getJmsConnection());

    return channel;
  }

  private ServiceList createServiceList() {
    ServiceList result = new ServiceList();
    PayloadFromMetadataService pm = new PayloadFromMetadataService();
    pm.setTemplate(REPLY_TEXT);
    result.addService(pm);
    return result;
  }

}
