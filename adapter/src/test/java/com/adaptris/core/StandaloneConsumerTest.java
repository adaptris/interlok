/*
 * $RCSfile: StandaloneConsumerTest.java,v $
 * $Revision: 1.10 $
 * $Date: 2008/08/13 13:28:43 $
 * $Author: lchan $
 */
package com.adaptris.core;

import com.adaptris.core.stubs.MockChannel;
import com.adaptris.core.stubs.MockMessageConsumer;
import com.adaptris.core.stubs.MockMessageListener;
import com.adaptris.core.stubs.MockMessageProducer;
import com.adaptris.core.util.LifecycleHelper;
import com.adaptris.util.license.License;

public class StandaloneConsumerTest extends BaseCase {

  public StandaloneConsumerTest(String arg0) {
    super(arg0);
  }

  public void testSetAdaptrisMessageListener() throws Exception {
    MockMessageProducer prod = new MockMessageProducer();

    StandardWorkflow aml1 = new StandardWorkflow();
    aml1.setConsumer(new MockMessageConsumer());
    aml1.setProducer(prod);

    Channel channel = new MockChannel();
    channel.getWorkflowList().add(aml1);
    channel.prepare();

    LifecycleHelper.init(aml1);
    LifecycleHelper.start(aml1);


    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage("xxx");

    aml1.onAdaptrisMessage(msg);

    // test the WF directly...
    assertTrue(prod.getMessages().size() == 1);

    MockMessageConsumer consumer1 = new MockMessageConsumer();

    StandaloneConsumer sc = new StandaloneConsumer();
    sc.setConsumer(consumer1);
    sc.registerAdaptrisMessageListener(aml1);
    LifecycleHelper.init(sc);

    consumer1.submitMessage(msg);

    // basic test SC...
    assertTrue(((MockMessageProducer) aml1.getProducer()).getMessages()
        .size() == 2);
    stop(sc);
    stop(channel);
  }

  public void testPollingConsumerInitialise() {
    AdaptrisPollingConsumer consumer1 = new AdaptrisPollingConsumer() {

      @Override
      public boolean isEnabled(License l) throws CoreException {
        return true;
      }

      @Override
      protected int processMessages() {
        return 0;
      }
    };
    consumer1.setDestination(new ConfiguredConsumeDestination());
    StandaloneConsumer sc = new StandaloneConsumer(consumer1);
    sc.registerAdaptrisMessageListener(new MockMessageListener());
    try {
      LifecycleHelper.init(sc);
      fail("initialised with null destination");
    }
    catch (CoreException e) {
    }
    consumer1.setDestination(new ConfiguredConsumeDestination(""));
    try {
      LifecycleHelper.init(sc);
      fail("initialised with null destination");
    }
    catch (CoreException e) {
    }
  }

  public void testXmlRoundTrip() throws Exception {
    StandaloneConsumer input = new StandaloneConsumer();
    // input.setEncoder(new MimeEncoder());
    AdaptrisMarshaller m = DefaultMarshaller.getDefaultMarshaller();
    String xml = m.marshal(input);
    log.trace(xml);
    StandaloneConsumer output = (StandaloneConsumer) m.unmarshal(xml);
    assertRoundtripEquality(input, output);
  }

  public void testSetConsumer() throws Exception {
    NullMessageConsumer c = new NullMessageConsumer();
    c.setUniqueId("abc");
    StandaloneConsumer sc = new StandaloneConsumer(c);
    assertEquals(c, sc.getConsumer());
    try {
      sc.setConsumer(null);
      fail();
    }
    catch (IllegalArgumentException expected) {

    }
    assertEquals(c, sc.getConsumer());
  }

  public void testSetConnection() throws Exception {
    NullConnection c = new NullConnection();
    StandaloneConsumer sc = new StandaloneConsumer(c);
    assertEquals(c, sc.getConnection());
    try {
      sc.setConnection(null);
      fail();
    }
    catch (IllegalArgumentException expected) {

    }
    assertEquals(c, sc.getConnection());
  }

  public void testMessageEventGeneratorQualifier() throws Exception {
    NullMessageConsumer c = new NullMessageConsumer();
    c.setUniqueId("abc");
    StandaloneConsumer sc = new StandaloneConsumer(c);
    assertEquals("abc", sc.createQualifier());
    c.setUniqueId("");
    assertEquals("", sc.createQualifier());
  }

  public void testMessageEventGeneratorCreateName() throws Exception {
    NullMessageConsumer c = new NullMessageConsumer();
    StandaloneConsumer sc = new StandaloneConsumer(c);
    assertEquals(NullMessageConsumer.class.getName(), sc.createName());
    assertEquals(sc.getConsumer().createName(), sc.createName());
  }

  public void testBackReferences() throws Exception {
    StandaloneConsumer consumer = new StandaloneConsumer();
    NullConnection conn = new NullConnection();
    consumer.setConnection(conn);
    assertEquals(conn, consumer.getConnection());
    // redmineID #4468 - need to initialise consumer first before backrefs are ready.
    LifecycleHelper.init(consumer);
    assertEquals(1, conn.retrieveExceptionListeners().size());
    assertTrue(consumer == conn.retrieveExceptionListeners().toArray()[0]);

    // Now marshall and see if it's the same.
    XStreamMarshaller m = new XStreamMarshaller();
    String xml = m.marshal(consumer);
    StandaloneConsumer consumer2 = (StandaloneConsumer) m.unmarshal(xml);
    // If the setter has been used, then these two will be "true"
    assertNotNull(consumer2.getConnection());
    LifecycleHelper.init(consumer2);

    assertEquals(1, consumer2.getConnection().retrieveExceptionListeners().size());
    assertTrue(consumer2 == consumer2.getConnection().retrieveExceptionListeners().toArray()[0]);
  }

}
