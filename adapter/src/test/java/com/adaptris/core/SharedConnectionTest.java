/*
 * Copyright 2017 Adaptris Ltd.
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.adaptris.core.util.LifecycleHelper;
import com.adaptris.util.GuidGenerator;

public class SharedConnectionTest {

  private static GuidGenerator guid = new GuidGenerator();

  @Before
  public void setUp() throws Exception {
  }

  @After
  public void tearDown() throws Exception {
  }

  @Test
  public void testNoOpState() throws Exception {
    Adapter a = createAndStart();
    NullConnection nc = (NullConnection) a.getSharedComponents().getConnections().get(0);
    SharedConnection c = new SharedConnection(nc.getUniqueId());
    try {
      c.init();
      assertEquals(StartedState.getInstance(), nc.retrieveComponentState());
      assertEquals(StartedState.getInstance(), c.retrieveComponentState());
      c.start();
      assertEquals(StartedState.getInstance(), nc.retrieveComponentState());
      assertEquals(StartedState.getInstance(), c.retrieveComponentState());
      c.stop();
      assertEquals(StartedState.getInstance(), nc.retrieveComponentState());
      assertEquals(StartedState.getInstance(), c.retrieveComponentState());
      c.close();
      assertEquals(StartedState.getInstance(), nc.retrieveComponentState());
      assertEquals(StartedState.getInstance(), c.retrieveComponentState());
      c.requestInit();
      assertEquals(StartedState.getInstance(), nc.retrieveComponentState());
      assertEquals(StartedState.getInstance(), c.retrieveComponentState());
      c.requestStart();
      assertEquals(StartedState.getInstance(), nc.retrieveComponentState());
      assertEquals(StartedState.getInstance(), c.retrieveComponentState());
      c.requestStop();
      assertEquals(StartedState.getInstance(), nc.retrieveComponentState());
      assertEquals(StartedState.getInstance(), c.retrieveComponentState());
      c.requestClose();
      assertEquals(StartedState.getInstance(), nc.retrieveComponentState());
      assertEquals(StartedState.getInstance(), c.retrieveComponentState());
      c.changeState(ClosedState.getInstance());
      assertEquals(StartedState.getInstance(), nc.retrieveComponentState());
      assertEquals(StartedState.getInstance(), c.retrieveComponentState());
    }
    finally {
      LifecycleHelper.stopAndClose(a);
      LifecycleHelper.stopAndClose(c);
    }

  }


  @Test
  public void testGetUniqueId() throws Exception {
    Adapter a = createAndStart();
    NullConnection nc = (NullConnection) a.getSharedComponents().getConnections().get(0);
    SharedConnection c = new SharedConnection(nc.getUniqueId());
    try {
      assertEquals(nc.getUniqueId(), c.getUniqueId());
    }
    finally {
      LifecycleHelper.stopAndClose(a);
      LifecycleHelper.stopAndClose(c);
    }
  }

  @Test
  public void testExceptionListeners() throws Exception {
    Adapter a = createAndStart();
    NullConnection nc = (NullConnection) a.getSharedComponents().getConnections().get(0);
    SharedConnection c = new SharedConnection(nc.getUniqueId());
    try {
      LifecycleHelper.initAndStart(c);
      assertEquals(0, c.retrieveExceptionListeners().size());
      assertEquals(0, nc.retrieveExceptionListeners().size());
      c.addExceptionListener(new StandaloneConsumer());
      assertEquals(1, c.retrieveExceptionListeners().size());
      assertEquals(1, nc.retrieveExceptionListeners().size());
    }
    finally {
      LifecycleHelper.stopAndClose(a);
      LifecycleHelper.stopAndClose(c);
    }
  }

  @Test
  public void testMessageProducers() throws Exception {
    Adapter a = createAndStart();
    NullConnection nc = (NullConnection) a.getSharedComponents().getConnections().get(0);
    SharedConnection c = new SharedConnection(nc.getUniqueId());
    try {
      LifecycleHelper.initAndStart(c);
      assertEquals(0, c.retrieveMessageProducers().size());
      assertEquals(0, nc.retrieveMessageProducers().size());
      c.addMessageProducer(new NullMessageProducer());
      assertEquals(1, c.retrieveMessageProducers().size());
      assertEquals(1, nc.retrieveMessageProducers().size());
    }
    finally {
      LifecycleHelper.stopAndClose(a);
      LifecycleHelper.stopAndClose(c);
    }
  }

  @Test
  public void testMessageConsumers() throws Exception {
    Adapter a = createAndStart();
    NullConnection nc = (NullConnection) a.getSharedComponents().getConnections().get(0);
    SharedConnection c = new SharedConnection(nc.getUniqueId());
    try {
      LifecycleHelper.initAndStart(c);
      assertEquals(0, c.retrieveMessageConsumers().size());
      assertEquals(0, nc.retrieveMessageConsumers().size());
      c.addMessageConsumer(new StandaloneConsumer());
      assertEquals(1, c.retrieveMessageConsumers().size());
      assertEquals(1, nc.retrieveMessageConsumers().size());
    }
    finally {
      LifecycleHelper.stopAndClose(a);
      LifecycleHelper.stopAndClose(c);
    }
  }

  @Test
  public void testConnectionErrorHandler() throws Exception {
    Adapter a = createAndStart();
    NullConnection nc = (NullConnection) a.getSharedComponents().getConnections().get(0);
    SharedConnection c = new SharedConnection(nc.getUniqueId());
    try {
      LifecycleHelper.initAndStart(c);
      NullConnectionErrorHandler nceh = new NullConnectionErrorHandler();
      c.setConnectionErrorHandler(nceh);
      assertEquals(nceh, c.getConnectionErrorHandler());
      assertEquals(nceh, nc.getConnectionErrorHandler());
      assertEquals(nceh, c.connectionErrorHandler());
    }
    finally {
      LifecycleHelper.stopAndClose(a);
      LifecycleHelper.stopAndClose(c);
    }
  }

  @Test
  public void testConnection() throws Exception {
    Adapter a = createAndStart();
    NullConnection nc = (NullConnection) a.getSharedComponents().getConnections().get(0);
    SharedConnection c = new SharedConnection(nc.getUniqueId());
    SharedConnection c2 = new SharedConnection("hello world");
    try {
      LifecycleHelper.initAndStart(c);
      assertEquals(nc, c.retrieveConnection(NullConnection.class));
      try {
        c2.requestInit();
        fail();
      }
      catch (RuntimeException expected) {
        // should be RTE from getProxedConnection();
        expected.printStackTrace();
      }
    }
    finally {
      LifecycleHelper.stopAndClose(a);
      LifecycleHelper.stopAndClose(c);
    }
  }

  private Adapter createAndStart() throws Exception {
    Adapter adapter = new Adapter();
    adapter.setUniqueId(guid.safeUUID());
    adapter.getSharedComponents().addConnection(new NullConnection(guid.safeUUID()));
    return LifecycleHelper.initAndStart(adapter);
  }

}
