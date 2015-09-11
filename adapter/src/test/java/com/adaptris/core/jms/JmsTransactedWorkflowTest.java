package com.adaptris.core.jms;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.adaptris.core.Channel;
import com.adaptris.core.ConfiguredConsumeDestination;
import com.adaptris.core.ConfiguredProduceDestination;
import com.adaptris.core.CoreException;
import com.adaptris.core.ExampleWorkflowCase;
import com.adaptris.core.NullProcessingExceptionHandler;
import com.adaptris.core.RestartProduceExceptionHandler;
import com.adaptris.core.Service;
import com.adaptris.core.fs.FsConsumer;
import com.adaptris.core.fs.FsProducer;
import com.adaptris.core.jms.activemq.EmbeddedActiveMq;
import com.adaptris.core.jms.jndi.StandardJndiImplementation;
import com.adaptris.core.services.WaitService;
import com.adaptris.core.services.exception.ConfiguredException;
import com.adaptris.core.services.exception.ThrowExceptionService;
import com.adaptris.core.stubs.MockChannel;
import com.adaptris.core.stubs.MockMessageProducer;
import com.adaptris.util.GuidGenerator;
import com.adaptris.util.TimeInterval;

public class JmsTransactedWorkflowTest extends ExampleWorkflowCase {

  private static Log logR = LogFactory.getLog(JmsTransactedWorkflowTest.class);

  public JmsTransactedWorkflowTest(String arg0) {
    super(arg0);
  }

  public void testSetStrict() throws Exception {
    JmsTransactedWorkflow workflow = new JmsTransactedWorkflow();
    assertEquals(true, workflow.isStrict());
    workflow.setStrict(null);
    assertEquals(true, workflow.isStrict());
    assertNull(workflow.getStrict());
    workflow.setStrict(Boolean.FALSE);
    assertEquals(false, workflow.isStrict());
    assertEquals(Boolean.FALSE, workflow.getStrict());
    workflow.setStrict(Boolean.TRUE);
    assertEquals(true, workflow.isStrict());
    assertEquals(Boolean.TRUE, workflow.getStrict());
  }

  @Override
  public void testSetConsumer() throws Exception {
    JmsTransactedWorkflow workflow = new JmsTransactedWorkflow();
    try {
      workflow.setConsumer(new FsConsumer(new ConfiguredConsumeDestination("file:////path/to/directory")));
      fail("setConsumer with Fsconsumer works, but shouldn't");
    }
    catch (IllegalArgumentException expected) {
      ;
    }
    try {
      workflow.setConsumer(null);
      fail();
    }
    catch (IllegalArgumentException expected) {
      ;
    }
  }

  public void testSetJmsPollingConsumer() throws Exception {
    JmsTransactedWorkflow workflow = new JmsTransactedWorkflow();
    workflow.setConsumer(new PtpPollingConsumer(new ConfiguredConsumeDestination("queue")));
  }

  public void testSetJmsConsumer() throws Exception {
    JmsTransactedWorkflow workflow = new JmsTransactedWorkflow();
    workflow.setConsumer(new PtpConsumer(new ConfiguredConsumeDestination("queue")));
  }

  @Override
  public void testSetProduceExceptionHandler() throws Exception {
    JmsTransactedWorkflow workflow = new JmsTransactedWorkflow();
    try {
      workflow.setProduceExceptionHandler(new RestartProduceExceptionHandler());
      fail("setProduceExceptionHandler with RestartProduceExceptionHandler works, but shouldn't");
    }
    catch (IllegalArgumentException expected) {
      ;
    }
  }

  public void testInit() throws Exception {
    EmbeddedActiveMq broker = new EmbeddedActiveMq();
    Channel channel = createStartableChannel(broker);
    try {
      broker.start();
      channel.requestInit();
    }
    finally {
      channel.requestClose();
      broker.destroy();
    }
  }

  public void testStart() throws Exception {
    EmbeddedActiveMq broker = new EmbeddedActiveMq();
    Channel channel = createStartableChannel(broker);
    try {
      broker.start();
      channel.requestStart();
    }
    finally {
      channel.requestClose();
      broker.destroy();
    }
  }

  private Channel createStartableChannel(EmbeddedActiveMq broker) throws Exception {
    return createStartableChannel(new ArrayList<Service>(), broker, getName());
  }

  private Channel createStartableChannel(List<Service> services, EmbeddedActiveMq broker, String destinationName) throws Exception {
    Channel channel = createChannel(broker);
    channel.getWorkflowList().add(createWorkflow(services, destinationName));
    channel.prepare();
    return channel;
  }

  private Channel createChannel(EmbeddedActiveMq broker) throws Exception {
    Channel result = createPlainChannel();
    result.setConsumeConnection(broker.getJmsConnection());
    return result;
  }

  private Channel createPlainChannel() throws Exception {
    GuidGenerator guid = new GuidGenerator();
    Channel result = new MockChannel();
    result.setUniqueId(guid.create(result));
    result.setMessageErrorHandler(new NullProcessingExceptionHandler());
    return result;
  }

  private JmsTransactedWorkflow createWorkflow(String destinationName) throws CoreException {
    return createWorkflow(new ArrayList<Service>(), destinationName);
  }

  private JmsTransactedWorkflow createWorkflow(List<Service> services, String destinationName) {
    JmsTransactedWorkflow workflow = new JmsTransactedWorkflow();
    workflow.setWaitPeriodAfterRollback(new TimeInterval(50L, TimeUnit.MILLISECONDS.name()));
    PtpConsumer consumer = new PtpConsumer(new ConfiguredConsumeDestination(destinationName));
    workflow.setProducer(new MockMessageProducer());
    workflow.setConsumer(consumer);
    workflow.getServiceCollection().addAll(services);
    return workflow;
  }


  @Override
  protected Object retrieveObjectForSampleConfig() {
    Channel c = new Channel();
    try {
      c.setUniqueId(UUID.randomUUID().toString());
      c.setConsumeConnection(configure(new JmsConnection()));
      JmsTransactedWorkflow workflow = createWorkflow("Sample_Queue_1");
      workflow.setUniqueId(UUID.randomUUID().toString());
      workflow.setWaitPeriodAfterRollback(new TimeInterval(30L, TimeUnit.SECONDS.name()));
      workflow.getServiceCollection().addService(new WaitService());
      workflow.getServiceCollection().addService(new ThrowExceptionService(new ConfiguredException("Fail")));
      workflow.setProducer(new FsProducer(new ConfiguredProduceDestination("file:////path/to/directory")));
      c.getWorkflowList().add(workflow);
    }
    catch (CoreException e) {
      throw new RuntimeException(e);
    }
    return c;
  }

  @Override
  protected String createBaseFileName(Object object) {
    return JmsTransactedWorkflow.class.getName();
  }

  @Override
  protected String getExampleCommentHeader(Object obj) {
    return super.getExampleCommentHeader(obj) + "\n<!--"
        + "\nWith the given example, this workflow will never receive a message from the queue, as the"
        + "\nThrowExceptionService will always cause the session rollback."
        + "\nYou will see the same jms-message-id be processed over and over again; "
        + "\nAfter each rollback() there will be a wait of 30seconds before it is ready to"
        + "\nreceive a new message from the JMS Provider" + "\n-->\n";
  }

  @Override
  protected JmsTransactedWorkflow createWorkflowForGenericTests() {
    return new JmsTransactedWorkflow();
  }


  static JmsConnection configure(JmsConnection c) {
    StandardJndiImplementation jndi = new StandardJndiImplementation();
    jndi.setJndiName("JNDI_Name_For_Connection_Factory");
    c.setVendorImplementation(jndi);
    return c;
  }
}
