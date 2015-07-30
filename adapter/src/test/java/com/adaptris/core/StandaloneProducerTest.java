/*
 * $RCSfile: StandaloneProducerTest.java,v $
 * $Revision: 1.7 $
 * $Date: 2008/08/13 13:28:43 $
 * $Author: lchan $
 */
package com.adaptris.core;

import com.adaptris.core.fs.FsProducer;
import com.adaptris.core.stubs.MockMessageProducer;
import com.adaptris.core.util.LifecycleHelper;

public class StandaloneProducerTest extends GeneralServiceExample {

  public StandaloneProducerTest(String name) {
    super(name);
  }

  @Override
  protected void setUp() throws Exception {
  }

  public void testDoService() throws Exception {
    MockMessageProducer m = new MockMessageProducer();
    StandaloneProducer service = new StandaloneProducer(m);
    service.setProducer(m);
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage("XYZ");
    execute(service, msg);
    assertEquals(1, m.getMessages().size());
  }

  public void testDoProduce() throws Exception {
    MockMessageProducer m = new MockMessageProducer();
    StandaloneProducer service = new StandaloneProducer(m);
    service.setProducer(m);
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage("XYZ");
    try {
      start(service);
      service.produce(msg);
      assertEquals(1, m.getMessages().size());
    }
    finally {
      stop(service);
    }
  }

  public void testDoProduceWithDest() throws Exception {
    MockMessageProducer m = new MockMessageProducer();
    StandaloneProducer service = new StandaloneProducer(m);
    service.setProducer(m);
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage("XYZ");
    try {
      start(service);
      service.produce(msg, new ConfiguredProduceDestination("ThisIsTheDest"));
      assertEquals(1, m.getMessages().size());
    }
    finally {
      stop(service);
    }
  }

  public void testCreateName() throws Exception {
    StandaloneProducer service = new StandaloneProducer(new MockMessageProducer());
    assertEquals(MockMessageProducer.class.getName(), service.createName());
    assertEquals(service.getProducer().createName(), service.createName());
  }

  public void testCreateQualifier() throws Exception {
    FsProducer mp = new FsProducer();
    mp.setUniqueId("abc");
    StandaloneProducer service = new StandaloneProducer(mp);
    assertEquals("abc", service.createQualifier());
    assertEquals(service.getProducer().createQualifier(), service.createQualifier());
    mp.setUniqueId("");
    service.setUniqueId(StandaloneProducer.class.getSimpleName());
    assertEquals(StandaloneProducer.class.getSimpleName(), service.createQualifier());
    assertNotSame(service.getProducer().createQualifier(), service.createQualifier());
  }

  public void testSetConnection() throws Exception {
    StandaloneProducer service = new StandaloneProducer();
    try {
      service.setConnection(null);
      fail("Connection allows nulls");
    }
    catch (IllegalArgumentException e) {
      ;
    }
  }

  public void testSetProducer() throws Exception {
    StandaloneProducer service = new StandaloneProducer();
    try {
      service.setProducer(null);
      fail("Producer allows nulls");
    }
    catch (IllegalArgumentException e) {
      ;
    }
  }

  public void testBackReferences() throws Exception {
    StandaloneProducer producer = new StandaloneProducer();
    NullConnection conn = new NullConnection();
    producer.setConnection(conn);
    assertEquals(conn, producer.getConnection());
    // No longer true because of redmineID #4452
    // assertEquals(1, conn.retrieveExceptionListeners().size());
    // assertTrue(producer == conn.retrieveExceptionListeners().toArray()[0]);
    LifecycleHelper.init(producer);
    // Now it's true again.
    assertEquals(1, conn.retrieveExceptionListeners().size());
    assertTrue(producer == conn.retrieveExceptionListeners().toArray()[0]);

    // Now marshall and see if it's the same.
    XStreamMarshaller m = new XStreamMarshaller();
    String xml = m.marshal(producer);
    StandaloneProducer producer2 = (StandaloneProducer) m.unmarshal(xml);
    // If the setter has been used, then these two will be "true"
    assertNotNull(producer2.getConnection());
    LifecycleHelper.init(producer2);
    assertEquals(1, producer2.getConnection().retrieveExceptionListeners().size());
    assertTrue(producer2 == producer2.getConnection().retrieveExceptionListeners().toArray()[0]);

  }


  @Override
  protected Object retrieveObjectForSampleConfig() {
    return new StandaloneProducer();
  }
}
