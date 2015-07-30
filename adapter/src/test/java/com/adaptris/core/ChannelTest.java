/*
 * $RCSfile: ChannelTest.java,v $ $Revision: 1.14 $ $Date: 2008/04/25 13:08:38 $ $Author: lchan $
 */
package com.adaptris.core;

import javax.jms.ConnectionFactory;
import javax.jms.JMSException;

import com.adaptris.core.jms.MockJmsConnection;
import com.adaptris.core.jms.MockNoOpConnectionErrorHandler;
import com.adaptris.core.jms.UrlVendorImplementation;
import com.adaptris.core.stubs.LicenseStub;
import com.adaptris.util.license.License;


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
      c.getWorkflowList().add(createDefaultWorkflow());
      c.getWorkflowList().add(configureWorkflow(new PoolingWorkflow()));
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

  public void testLicenseCombinations() throws Exception {
    // OMG, there must be a better way of doing this...
    // Too much possibility of missing a possible options out of the matrix
    assertEquals(false, createChannel(false, true, true).isEnabled(new LicenseStub()));
    assertEquals(false, createChannel(false, true, false).isEnabled(new LicenseStub()));
    assertEquals(false, createChannel(false, false, true).isEnabled(new LicenseStub()));
    assertEquals(false, createChannel(false, false, false).isEnabled(new LicenseStub()));

    assertEquals(true, createChannel(true, true, true).isEnabled(new LicenseStub()));
    assertEquals(false, createChannel(true, true, false).isEnabled(new LicenseStub()));
    assertEquals(false, createChannel(true, false, false).isEnabled(new LicenseStub()));
    assertEquals(false, createChannel(true, false, true).isEnabled(new LicenseStub()));

    assertEquals(true, createChannel(true, true, true, true).isEnabled(new LicenseStub()));
    assertEquals(false, createChannel(false, true, true, true).isEnabled(new LicenseStub()));
    assertEquals(false, createChannel(false, false, true, true).isEnabled(new LicenseStub()));
    assertEquals(false, createChannel(false, false, false, true).isEnabled(new LicenseStub()));
    assertEquals(false, createChannel(false, false, false, false).isEnabled(new LicenseStub()));
    assertEquals(false, createChannel(false, true, false, true).isEnabled(new LicenseStub()));
    assertEquals(false, createChannel(false, true, false, false).isEnabled(new LicenseStub()));
    assertEquals(false, createChannel(false, true, true, false).isEnabled(new LicenseStub()));
    assertEquals(false, createChannel(true, false, true, true).isEnabled(new LicenseStub()));
    assertEquals(false, createChannel(true, false, false, true).isEnabled(new LicenseStub()));
    assertEquals(false, createChannel(true, false, false, false).isEnabled(new LicenseStub()));
    assertEquals(false, createChannel(true, true, false, true).isEnabled(new LicenseStub()));
    assertEquals(false, createChannel(true, true, false, false).isEnabled(new LicenseStub()));
    assertEquals(false, createChannel(true, true, true, false).isEnabled(new LicenseStub()));

  }

  private Channel createChannel(boolean consumeLicensed, boolean produceLicensed,
      boolean workflowLicensed, boolean messageErrorHandlerLicensed) throws Exception {
    Channel c = createChannel(consumeLicensed, produceLicensed, workflowLicensed);
    if (!messageErrorHandlerLicensed) {
      c.setMessageErrorHandler(new StandardProcessingExceptionHandler() {
        @Override
        public boolean isEnabled(License l) {
          return false;
        }
      });

    } else {
      c.setMessageErrorHandler(new StandardProcessingExceptionHandler());
    }
    return c;
  }

  private Channel createChannel(boolean consumeLicensed, boolean produceLicensed,
      boolean workflowLicensed) throws Exception {
    Channel c = new Channel();
    c.getWorkflowList().add(createDefaultWorkflow());
    if (!consumeLicensed) {
      c.setConsumeConnection(new NullConnection() {
        @Override
        public boolean isEnabled(License l) {
          return false;
        }
      });
    }
    if (!produceLicensed) {
      c.setProduceConnection(new NullConnection() {
        @Override
        public boolean isEnabled(License l) {
          return false;
        }
      });
    }
    if (!workflowLicensed) {
      c.getWorkflowList().add(new StandardWorkflow() {
        @Override
        protected boolean doAdditionalLicenseChecks(License l) {
          return false;
        }
      });
    }
    return c;
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
    adapter.registerLicense(new LicenseStub());
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
    adapter.registerLicense(new LicenseStub());
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
    adapter.registerLicense(new LicenseStub());
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
