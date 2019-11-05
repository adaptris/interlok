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

package com.adaptris.core;

import java.util.UUID;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import com.adaptris.core.jms.MockJmsConnection;
import com.adaptris.core.jms.MockNoOpConnectionErrorHandler;
import com.adaptris.core.jms.UrlVendorImplementation;


public class ChannelTest extends ExampleChannelCase {

  private static final String DUP_CEH_MESSAGE =
      "This channel has been configured with 2 ErrorHandlers that are incompatible with each other";
  private StandardWorkflow workflow;
  private AdaptrisMessageConsumer consumer;
  private AdaptrisMessageProducer producer;

  public ChannelTest(java.lang.String testName) {
    super(testName);
  }


  @Override
  protected Object retrieveObjectForSampleConfig() {
    ChannelList cl = new ChannelList();
    Channel c = new Channel();
    try {
      c.setUniqueId(UUID.randomUUID().toString());
      c.getWorkflowList().add(createDefaultWorkflow());
      c.getWorkflowList().add(configureWorkflow(new PoolingWorkflow()));
      c.setComments("Comments Ignored At Runtime");
      cl.addChannel(c);
    } catch (CoreException e) {
      throw new RuntimeException(e);
    }
    return cl;
  }

  @Override
  protected String createBaseFileName(Object object) {
    return Channel.class.getName();
  }

  public void testComments() throws Exception {
    ConfigCommentHelper.testComments(new Channel());
  }

  public void testLifecycle_Init() throws Exception {
    Channel c = new Channel();
    c.getWorkflowList().add(createDefaultWorkflow());
    c.requestInit();
    assertEquals(InitialisedState.getInstance(), c.retrieveComponentState());
  }

  public void testLifecycle_Start() throws Exception {
    Channel c = new Channel();
    c.getWorkflowList().add(createDefaultWorkflow());
    c.requestStart();
    assertEquals(StartedState.getInstance(), c.retrieveComponentState());

  }

  public void testLifecycle_Stop() throws Exception {
    Channel c = new Channel();
    c.getWorkflowList().add(createDefaultWorkflow());
    c.requestStart();
    c.requestStop();
    assertEquals(StoppedState.getInstance(), c.retrieveComponentState());

  }

  public void testLifecycle_Close() throws Exception {
    Channel c = new Channel();
    c.getWorkflowList().add(createDefaultWorkflow());
    c.requestStart();
    c.requestClose();
    assertEquals(ClosedState.getInstance(), c.retrieveComponentState());
  }

  public void testChannel_StateManagedComponentContainerInit() throws Exception {
    Channel channel = new Channel();
    Workflow workflow = createDefaultWorkflow();
    channel.getWorkflowList().add(workflow);
    channel.requestInit();
    workflow.requestClose();
    assertEquals(ClosedState.getInstance(), workflow.retrieveComponentState());
    assertEquals(InitialisedState.getInstance(), channel.retrieveComponentState());
    // This should reinitialise the workflow.
    channel.requestInit();
    assertEquals(InitialisedState.getInstance(), workflow.retrieveComponentState());
  }

  public void testChannel_StateManagedComponentContainerStart() throws Exception {
    Channel channel = new Channel();
    Workflow workflow = createDefaultWorkflow();
    channel.getWorkflowList().add(workflow);
    channel.requestStart();
    workflow.requestClose();
    assertEquals(ClosedState.getInstance(), workflow.retrieveComponentState());
    assertEquals(StartedState.getInstance(), channel.retrieveComponentState());
    // This should restart the workflow.
    channel.requestStart();
    assertEquals(StartedState.getInstance(), workflow.retrieveComponentState());
  }

  public void testChannel_StateManagedComponentContainerStop() throws Exception {
    Channel channel = new Channel();
    Workflow workflow = createDefaultWorkflow();
    channel.getWorkflowList().add(workflow);
    channel.requestStart();
    channel.requestStop();
    workflow.requestStart();
    assertEquals(StartedState.getInstance(), workflow.retrieveComponentState());
    assertEquals(StoppedState.getInstance(), channel.retrieveComponentState());
    // This should stop the workflow.
    channel.requestStop();
    assertEquals(StoppedState.getInstance(), workflow.retrieveComponentState());
  }

  public void testChannel_StateManagedComponentContainerClose() throws Exception {
    Channel channel = new Channel();
    Workflow workflow = createDefaultWorkflow();
    channel.getWorkflowList().add(workflow);
    channel.requestStart();
    workflow.requestClose();
    assertEquals(ClosedState.getInstance(), workflow.retrieveComponentState());
    assertEquals(StartedState.getInstance(), channel.retrieveComponentState());
    // This should stop the workflow.
    channel.requestStart();
    assertEquals(StartedState.getInstance(), workflow.retrieveComponentState());
  }


  public void testSetConsumeConnection() {
    Channel c = new Channel();
    NullConnection conn = new NullConnection();
    c.setConsumeConnection(conn);
    assertEquals(conn, c.getConsumeConnection());
    try {
      c.setConsumeConnection(null);
      fail();
    } catch (IllegalArgumentException expected) {

    }
    assertEquals(conn, c.getConsumeConnection());
  }

  public void testSetProduceConnection() {
    Channel c = new Channel();
    NullConnection conn = new NullConnection();
    c.setProduceConnection(conn);
    assertEquals(conn, c.getProduceConnection());
    try {
      c.setProduceConnection(null);
      fail();
    } catch (IllegalArgumentException expected) {

    }
    assertEquals(conn, c.getProduceConnection());
  }

  public void testSetMessageErrorHandler() {
    Channel c = new Channel();
    ProcessingExceptionHandler obj = new StandardProcessingExceptionHandler();
    c.setMessageErrorHandler(obj);
    assertEquals(obj, c.getMessageErrorHandler());
    try {
      c.setMessageErrorHandler(null);
      fail();
    } catch (IllegalArgumentException expected) {

    }
    assertEquals(obj, c.getMessageErrorHandler());
  }

  public void testRegisterMessageErrorHandler() {
    Channel c = new Channel();
    ProcessingExceptionHandler obj = new StandardProcessingExceptionHandler();
    c.registerActiveMsgErrorHandler(obj);
    assertNotSame(obj, c.getMessageErrorHandler());
    assertEquals(obj, c.retrieveActiveMsgErrorHandler());
    try {
      c.registerActiveMsgErrorHandler(null);
      fail();
    } catch (IllegalArgumentException expected) {

    }
    assertNotSame(obj, c.getMessageErrorHandler());
    assertEquals(obj, c.retrieveActiveMsgErrorHandler());
  }

  public void testSetWorkflowList() {
    Channel c = new Channel();
    WorkflowList wl = new WorkflowList();
    c.setWorkflowList(wl);
    assertEquals(wl, c.getWorkflowList());
    try {
      c.setWorkflowList(null);
      fail();
    } catch (IllegalArgumentException expected) {

    }
    assertEquals(wl, c.getWorkflowList());
  }

  public void testBackReferences() throws Exception {
    Channel channel = new Channel();
    NullConnection consConn = new NullConnection();
    channel.setConsumeConnection(consConn);
    assertEquals(consConn, channel.getConsumeConnection());
    assertEquals(0, consConn.retrieveExceptionListeners().size());

    NullConnection prodConn = new NullConnection();
    channel.setProduceConnection(prodConn);
    assertEquals(prodConn, channel.getProduceConnection());
    assertEquals(0, prodConn.retrieveExceptionListeners().size());

    // Now marshall and see if it's the same.
    XStreamMarshaller m = new XStreamMarshaller();
    String xml = m.marshal(channel);
    Channel channel2 = (Channel) m.unmarshal(xml);
    // If the setter has been used, then these two will be "true"
    // The above line is now not true! We have removed behaviour from the setters and is now done in
    // the prepare.
    // So the back refs are no longer created in the setConsumeConnection/setProduceConnection.
    // assertEquals(1, channel2.getConsumeConnection().retrieveExceptionListeners().size());
    // assertTrue(channel2 ==
    // channel2.getConsumeConnection().retrieveExceptionListeners().toArray()[0]);
    //
    // assertEquals(1, channel2.getProduceConnection().retrieveExceptionListeners().size());
    // assertTrue(channel2 ==
    // channel2.getProduceConnection().retrieveExceptionListeners().toArray()[0]);

    // So let's make sure that there is no behaviour in the setters!
    assertEquals(0, channel2.getConsumeConnection().retrieveExceptionListeners().size());
    assertEquals(0, channel2.getProduceConnection().retrieveExceptionListeners().size());

    // Now if we call the prepare method, let's see if our back refs have been set correctly;
    channel2.prepare();

    assertEquals(1, channel2.getConsumeConnection().retrieveExceptionListeners().size());
    assertEquals(1, channel2.getProduceConnection().retrieveExceptionListeners().size());
  }

  public void testConnectionEqualityCheck2DifferentConnections() throws Exception {
    Channel channel = new Channel();

    MockJmsConnection produceConnection = createMockJmsConnection(null, "broker1");
    MockJmsConnection consumeConnection = createMockJmsConnection(null, "broker2");

    channel.setConsumeConnection(consumeConnection);
    channel.setProduceConnection(produceConnection);

    consumeConnection.init();
    produceConnection.init();

    channel.init();
  }

  public void testConnectionEqualityCheck2ConnectionsToSameBroker() throws Exception {
    Channel channel = new Channel();

    MockJmsConnection produceConnection = createMockJmsConnection(null, "broker1");
    MockJmsConnection consumeConnection = createMockJmsConnection(null, "broker1");

    channel.setConsumeConnection(consumeConnection);
    channel.setProduceConnection(produceConnection);

    try {
      channel.init();
      fail("Should fail, 2 identical connections with error handlers");
    } catch (CoreException ex) {
      assertEquals(DUP_CEH_MESSAGE, ex.getMessage());
    }
  }

  public void testConnectionEqualityCheck_SharedConnectionSameBroker() throws Exception {
    Adapter adapter = new Adapter();
    adapter.setUniqueId(getName());
    Channel channel = new Channel();

    adapter.getSharedComponents().addConnection(createMockJmsConnection(getName(), "b1"));
    SharedConnection consumeConnection = new SharedConnection(getName());
    SharedConnection produceConnection = new SharedConnection(getName());

    channel.setConsumeConnection(consumeConnection);
    channel.setProduceConnection(produceConnection);
    adapter.getChannelList().add(channel);
    try {
      adapter.requestInit();
    } finally {
      adapter.requestClose();
    }
  }

  public void testConnectionEqualityCheck_MultipleSharedConnections() throws Exception {

    Adapter adapter = new Adapter();
    adapter.setUniqueId(getName());
    Channel channel = new Channel();

    adapter.getSharedComponents().addConnection(createMockJmsConnection(getName(), "b1"));
    adapter.getSharedComponents().addConnection(createMockJmsConnection(getName() + "2", "b2"));


    SharedConnection consumeConnection = new SharedConnection(getName());
    SharedConnection produceConnection = new SharedConnection(getName() + "2");

    channel.setConsumeConnection(consumeConnection);
    channel.setProduceConnection(produceConnection);
    adapter.getChannelList().add(channel);
    try {
      adapter.requestInit();
    } finally {
      adapter.requestClose();
    }
  }

  public void testConnectionEqualityCheck_MultipleSharedConnections_SameBroker() throws Exception {
    Adapter adapter = new Adapter();
    adapter.setUniqueId(getName());
    Channel channel = new Channel();

    adapter.getSharedComponents().addConnection(createMockJmsConnection(getName(), "b1"));
    adapter.getSharedComponents().addConnection(createMockJmsConnection(getName() + "2", "b1"));

    SharedConnection consumeConnection = new SharedConnection(getName());
    SharedConnection produceConnection = new SharedConnection(getName() + "2");

    channel.setConsumeConnection(consumeConnection);
    channel.setProduceConnection(produceConnection);
    adapter.getChannelList().add(channel);
    try {
      adapter.requestInit();
      fail("Should fail, 2 identical connections with error handlers");
    } catch (CoreException expected) {
      assertEquals(DUP_CEH_MESSAGE, expected.getMessage());
    } finally {
      adapter.requestClose();
    }
  }

  public void testConnectionEqualityCheckSameConnectionInstances() throws Exception {
    Channel channel = new Channel();

    MockJmsConnection connection = createMockJmsConnection(null, "broker1");

    channel.setConsumeConnection(connection);
    channel.setProduceConnection(connection);

    channel.init();
  }

  public void testHasUniqueId() {
    Channel c = new Channel();
    assertEquals(false, c.hasUniqueId());
    c.setUniqueId("");
    assertEquals(false, c.hasUniqueId());
    c.setUniqueId("unique-id");
    assertEquals(true, c.hasUniqueId());
  }


  private MockJmsConnection createMockJmsConnection(String uid, String brokerUrl) {
    MockJmsConnection connection = new MockJmsConnection();
    if (uid != null)
      connection.setUniqueId(uid);
    UrlVendorImplementation vendorImp = new UrlVendorImplementation() {
      @Override
      public ConnectionFactory createConnectionFactory() throws JMSException {
        return null;
      }
    };
    vendorImp.setBrokerUrl(brokerUrl);
    connection.setVendorImplementation(vendorImp);
    connection.setConnectionErrorHandler(new MockNoOpConnectionErrorHandler());
    return connection;
  }
}
