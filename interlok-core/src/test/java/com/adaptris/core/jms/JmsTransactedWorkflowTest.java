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
import static org.junit.Assert.assertNull;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.adaptris.core.Channel;
import com.adaptris.core.CoreException;
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

public class JmsTransactedWorkflowTest
    extends com.adaptris.interlok.junit.scaffolding.ExampleWorkflowCase {

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

  @Test
  public void testInit_UnsupportedConsumer() throws Exception {
    Channel channel = createStartableChannel(activeMqBroker);
    JmsTransactedWorkflow workflow = (JmsTransactedWorkflow) channel.getWorkflowList().get(0);
    workflow.setConsumer(new FsConsumer().withBaseDirectoryUrl("file:////path/to/directory"));
    try {
      channel.requestInit();
    }
    catch (CoreException expected) {
      assertEquals("JmsTransactedWorkflow must be used with a JMSConsumer", expected.getMessage());
    }
    finally {
      channel.requestClose();
    }
  }

  @Test
  public void testSetJmsPollingConsumer() throws Exception {
    JmsTransactedWorkflow workflow = new JmsTransactedWorkflow();
    workflow.setConsumer(new PtpPollingConsumer().withQueue("queue"));
  }

  @Test
  public void testSetJmsConsumer() throws Exception {
    JmsTransactedWorkflow workflow = new JmsTransactedWorkflow();
    workflow.setConsumer(new PtpConsumer().withQueue("queue"));
  }

  @Test
  public void testInit_UnsupportedProduceExceptionHandler() throws Exception {
    Channel channel = createStartableChannel(activeMqBroker);
    JmsTransactedWorkflow workflow = (JmsTransactedWorkflow) channel.getWorkflowList().get(0);
    workflow.setProduceExceptionHandler(new RestartProduceExceptionHandler());
    try {
      channel.requestInit();
    }
    catch (CoreException expected) {
      assertEquals("JmsTransactedWorkflow may not have a ProduceExceptionHandler set", expected.getMessage());

    }
    finally {
      channel.requestClose();
    }
  }

  @Test
  public void testInit() throws Exception {
    Channel channel = createStartableChannel(activeMqBroker);
    try {
      channel.requestInit();
    }
    finally {
      channel.requestClose();
    }
  }

  @Test
  public void testStart() throws Exception {
    Channel channel = createStartableChannel(activeMqBroker);
    try {
      channel.requestStart();
    }
    finally {
      channel.requestClose();
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
    PtpConsumer consumer = new PtpConsumer().withQueue(destinationName);
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
      workflow.setProducer(new FsProducer().withBaseDirectoryUrl("file:////path/to/directory"));
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
