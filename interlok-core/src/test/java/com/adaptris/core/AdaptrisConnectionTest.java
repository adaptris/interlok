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

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.adaptris.core.stubs.MockConnection;
import com.adaptris.core.stubs.MockMessageConsumer;
import com.adaptris.core.stubs.MockMessageProducer;

public class AdaptrisConnectionTest extends BaseCase {

  private static final String CLOSE = "close";
  private static final String STOP = "stop";
  private static final String START = "start";
  private static final String INIT = "init";

  public AdaptrisConnectionTest(java.lang.String testName) {
    super(testName);
  }

  @Override
  protected void setUp() throws Exception {
  }

  public void testConnectionErrorHandler() throws Exception {
    MockConnection mc = new MockConnection();
    assertNull(mc.getConnectionErrorHandler());
    NullConnectionErrorHandler nc = new NullConnectionErrorHandler();
    mc.setConnectionErrorHandler(nc);
    assertEquals(nc, mc.getConnectionErrorHandler());
  }

  public void testAddMessageConsumer() throws Exception {
    MockConnection mc = new MockConnection();
    List<MockMessageConsumer> consumers = createConsumers();
    mc.addMessageConsumer(null);
    for (MockMessageConsumer c : consumers) {
      mc.addMessageConsumer(c);
    }
    for (MockMessageConsumer c : consumers) {
      mc.addMessageConsumer(c);
    }
    assertEquals(mc.retrieveMessageConsumers().size(), consumers.size());
  }

  public void testAddMessageProducers() throws Exception {
    MockConnection mc = new MockConnection();
    List<MockMessageProducer> producers = createProducers();
    mc.addMessageProducer(null);
    for (MockMessageProducer c : producers) {
      mc.addMessageProducer(c);
    }
    for (MockMessageProducer c : producers) {
      mc.addMessageProducer(c);
    }
    assertEquals(mc.retrieveMessageProducers().size(), producers.size());
  }

  public void testCloseWithWorkerLifecycle() throws Exception {
    MockConnection mc = new MockConnection();
    List<MockMessageConsumer> consumers = createConsumers();
    List<MockMessageProducer> producers = createProducers();
    for (MockMessageConsumer c : consumers) {
      mc.addMessageConsumer(c);
    }
    for (MockMessageProducer c : producers) {
      mc.addMessageProducer(c);
    }
    mc.setWorkersFirstOnShutdown(true);
    invoke(mc, INIT);
    invoke(producers, INIT);
    invoke(consumers, INIT);

    invoke(mc, START);
    invoke(producers, START);
    invoke(consumers, START);

    invoke(mc, STOP);
    assertState(producers, StoppedState.getInstance());
    assertState(consumers, StoppedState.getInstance());
    invoke(mc, CLOSE);
    assertState(producers, ClosedState.getInstance());
    assertState(consumers, ClosedState.getInstance());
  }

  public void testCloseWithoutWorkferLifecycle() throws Exception {
    MockConnection mc = new MockConnection();
    List<MockMessageConsumer> consumers = createConsumers();
    List<MockMessageProducer> producers = createProducers();
    for (MockMessageConsumer c : consumers) {
      mc.addMessageConsumer(c);
    }
    for (MockMessageProducer c : producers) {
      mc.addMessageProducer(c);
    }
    invoke(mc, INIT);
    invoke(producers, INIT);
    invoke(consumers, INIT);

    invoke(mc, START);
    invoke(producers, START);
    invoke(consumers, START);
    invoke(mc, STOP);
    assertState(producers, StartedState.getInstance());
    assertState(consumers, StartedState.getInstance());
    invoke(producers, STOP);
    invoke(consumers, STOP);
    invoke(mc, CLOSE);
    assertState(producers, StoppedState.getInstance());
    assertState(consumers, StoppedState.getInstance());
    invoke(producers, CLOSE);
    invoke(consumers, CLOSE);
  }

  public void testCloneForTesting() throws Exception {
    MockConnection mc = new MockConnection();
    assertEquals(MockConnection.class, mc.cloneForTesting().getClass());
  }

  private void assertState(List list, ComponentState state) {
    for (Object c : list) {
      assertEquals("" + state, state, ((StateManagedComponent) c).retrieveComponentState());
    }
  }

  private static void invoke(List list, String operation) throws Exception {
    for (Object c : list) {
      invoke(c, operation);
    }
  }

  private List<MockMessageConsumer> createConsumers() {
    List<MockMessageConsumer> result = new ArrayList<MockMessageConsumer>();
    int rnd = new Random().nextInt(20) + 1;
    for (int i = 0; i < rnd; i++) {
      result.add(new MockMessageConsumer());
    }
    return result;
  }

  private List<MockMessageProducer> createProducers() {
    List<MockMessageProducer> result = new ArrayList<MockMessageProducer>();
    int rnd = new Random().nextInt(20) + 1;
    for (int i = 0; i < rnd; i++) {
      result.add(new MockMessageProducer());
    }
    return result;
  }

  private static void invoke(Object obj, String methodName) throws Exception {
    Method m = obj.getClass().getMethod(methodName, (Class[]) null);
    if (m != null) {
      m.invoke(obj, (Object[]) null);
    }
    else {
      throw new Exception(methodName + " not found");
    }
    return;
  }
}
