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

import com.adaptris.core.event.AdapterCloseEvent;
import com.adaptris.core.jms.JmsConnection;
import com.adaptris.core.jms.PtpProducer;
import com.adaptris.core.jms.jndi.StandardJndiImplementation;
import com.adaptris.core.stubs.FailFirstMockMessageProducer;
import com.adaptris.core.stubs.LicenseStub;
import com.adaptris.core.stubs.MockMessageProducer;
import com.adaptris.core.util.LifecycleHelper;
import com.adaptris.util.license.License;

public class DefaultEventHandlerTest extends ExampleEventHandlerCase {

  public DefaultEventHandlerTest(java.lang.String testName) {
    super(testName);
  }

  @Override
  protected void setUp() throws CoreException {
  }

  @Override
  protected void tearDown() throws Exception {
  }

  @Override
  protected DefaultEventHandler newEventHandler(String uniqueId) throws CoreException {
    DefaultEventHandler result = new DefaultEventHandler();
    result.setUniqueId(uniqueId);
    return result;
  }

  @Override
  protected DefaultEventHandler applyConfiguration(EventHandler eh) throws CoreException {
    DefaultEventHandler eventHandler = (DefaultEventHandler) eh;
    eventHandler.setUniqueId(UUID.randomUUID().toString());
    eventHandler.setConnection(new NullConnection());
    eventHandler.setProducer(new MockMessageProducer());
    eventHandler.setMarshaller(DefaultMarshaller.getDefaultMarshaller());
    return eventHandler;
  }

  public void testSetters() throws Exception {
    DefaultEventHandler eventHandler = newEventHandler("testSetters");
    try {
      eventHandler.setConnection(null);
      fail();
    }
    catch (IllegalArgumentException expected) {

    }
    try {
      eventHandler.setProducer(null);
      fail();
    }
    catch (IllegalArgumentException expected) {

    }
  }

  public void testLicenseCombinations() throws Exception {
    assertEquals(true, createEventHandler(true, true).isEnabled(new LicenseStub()));
    assertEquals(false, createEventHandler(false, true).isEnabled(new LicenseStub()));
    assertEquals(false, createEventHandler(false, false).isEnabled(new LicenseStub()));
    assertEquals(false, createEventHandler(true, false).isEnabled(new LicenseStub()));
  }

  private EventHandler createEventHandler(boolean connectionEnabled, boolean producerEnabled)
      throws Exception {
    DefaultEventHandler eventHandler = new DefaultEventHandler();
    if (connectionEnabled) {
      eventHandler.setConnection(new NullConnection());
    }
    else {
      eventHandler.setConnection(new NullConnection() {
        @Override
        public boolean isEnabled(License l) {
          return false;
        }
      });

    }
    if (producerEnabled) {
      eventHandler.setProducer(new MockMessageProducer());
    }
    else {
      eventHandler.setProducer(new MockMessageProducer() {
        @Override
        public boolean isEnabled(License l) {
          return false;
        }
      });
    }
    eventHandler.setMarshaller(DefaultMarshaller.getDefaultMarshaller());
    return eventHandler;
  }


  public void testSendEventWithException() throws Exception {
    Event e = EventFactory.create(AdapterCloseEvent.class);
    DefaultEventHandler eh = applyConfiguration(newEventHandler("testSendEventWithException"));
    eh.setProducer(new FailFirstMockMessageProducer());
    eh.requestStart();
    eh.send(e);
    eh.requestClose();
    doAssertions(eh, 0, e.getClass());
  }

  public void testGettersWhenClosed() throws Exception {
    DefaultEventHandler input = applyConfiguration(newEventHandler("testGettersWhenClosed"));
    AdaptrisMessageProducer p1 = input.getProducer();
    AdaptrisConnection con1 = input.getConnection();
    input.requestStart();
    input.requestClose();
    AdaptrisMessageProducer p2 = input.getProducer();
    AdaptrisConnection con2 = input.getConnection();
    assertEquals("Producers when closed", p1, p2);
    assertEquals("Connections when closed", con1, con2);
    assertRoundtripEquality(p1, p2);
    assertRoundtripEquality(con1, con2);
  }

  public void testSettersWhenClosed() throws Exception {
    DefaultEventHandler input = applyConfiguration(newEventHandler("testSettersWhenClosed"));
    AdaptrisMessageProducer p1 = input.getProducer();
    AdaptrisConnection con1 = input.getConnection();
    input.requestStart();
    input.requestClose();
    input.setConnection(con1);
    input.setProducer(p1);
  }

  public void testSettersWhenInitialised() throws Exception {
    DefaultEventHandler input = applyConfiguration(newEventHandler("testSettersWhenInitialised"));
    AdaptrisMessageProducer p1 = input.getProducer();
    AdaptrisConnection con1 = input.getConnection();
    input.requestInit();
    try {
      input.setConnection(con1);
      fail("Should not be able to reset connection once initialised");
    }
    catch (IllegalStateException expected) {
    }
    try {
      input.setProducer(p1);
      fail("Should not be able to reset producer once initialised");
    }
    catch (IllegalStateException expected) {
    }
  }

 public void testSettersWhenStarted() throws Exception {
    DefaultEventHandler input = applyConfiguration(newEventHandler("testSettersWhenStarted"));
    AdaptrisMessageProducer p1 = input.getProducer();
    AdaptrisConnection con1 = input.getConnection();
    input.requestStart();
    try {
      input.setConnection(con1);
      fail("Should not be able to reset connection once initialised");
    }
    catch (IllegalStateException expected) {
    }
    try {
      input.setProducer(p1);
      fail("Should not be able to reset producer once initialised");
    }
    catch (IllegalStateException expected) {
    }
  }

  public void testSettersWhenStopped() throws Exception {
    DefaultEventHandler input = applyConfiguration(newEventHandler("testSettersWhenStopped"));
    AdaptrisMessageProducer p1 = input.getProducer();
    AdaptrisConnection con1 = input.getConnection();
    input.requestStart();
    input.requestStop();
    try {
      input.setConnection(con1);
      fail("Should not be able to reset connection once initialised");
    }
    catch (IllegalStateException expected) {
    }
    try {
      input.setProducer(p1);
      fail("Should not be able to reset producer once initialised");
    }
    catch (IllegalStateException expected) {
    }
  }

  public void testBackReferences() throws Exception {
    DefaultEventHandler evh = new DefaultEventHandler();
    NullConnection conn = new NullConnection();
    evh.setConnection(conn);
    assertEquals(conn, evh.getConnection());
    // Back references don't exist until "init".
    try {
      LifecycleHelper.init(evh);

      assertEquals(1, conn.retrieveExceptionListeners().size());
      assertTrue(evh == conn.retrieveExceptionListeners().toArray()[0]);
    }
    finally {
      LifecycleHelper.close(evh);
    }
  }

  /**
   * @see com.adaptris.core.ExampleConfigCase#retrieveObjectForSampleConfig()
   */
  @Override
  protected Object retrieveObjectForSampleConfig() {
    Adapter result = null;

    try {
      AdaptrisMessageProducer p = new PtpProducer();
      p.setDestination(new ConfiguredProduceDestination("publishEventsTo"));
      DefaultEventHandler eh = new DefaultEventHandler();
      eh.setConnection(new JmsConnection(new StandardJndiImplementation("MyConnectionFactoryName")));
      eh.setProducer(p);
      eh.setMarshaller(DefaultMarshaller.getDefaultMarshaller());
      result = new Adapter();
      result.setChannelList(new ChannelList());
      result.setEventHandler(eh);
      result.setUniqueId(UUID.randomUUID().toString());
    }
    catch (Exception e) {
      throw new RuntimeException(e);
    }
    return result;
  }
}
