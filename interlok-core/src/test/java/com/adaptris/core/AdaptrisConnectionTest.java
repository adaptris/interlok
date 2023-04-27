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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import java.lang.reflect.Method;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicBoolean;

import org.awaitility.Awaitility;
import org.junit.Test;
import com.adaptris.core.stubs.MockConnection;
import com.adaptris.core.stubs.MockMessageConsumer;
import com.adaptris.core.stubs.MockMessageProducer;
import com.adaptris.interlok.junit.scaffolding.jms.MockConsumer;
import com.adaptris.interlok.junit.scaffolding.jms.MockProducer;

public class AdaptrisConnectionTest extends com.adaptris.interlok.junit.scaffolding.BaseCase {

  private static final String CLOSE = "close";
  private static final String STOP = "stop";
  private static final String START = "start";
  private static final String INIT = "init";

  public AdaptrisConnectionTest() {
  }

  @Test
  public void testConnectionErrorHandler() throws Exception {
    MockConnection mc = new MockConnection();
    assertNull(mc.getConnectionErrorHandler());
    NullConnectionErrorHandler nc = new NullConnectionErrorHandler();
    mc.setConnectionErrorHandler(nc);
    assertEquals(nc, mc.getConnectionErrorHandler());
  }
  
  // INTERLOK-4039
  @Test
  public void testConcurrentListenerRegistration() throws Exception {
    int threadCount = 100;
    final MockConnection connection = new MockConnection();
    
    ThreadFactory tf = new ThreadFactory() {
      @Override
      public Thread newThread(Runnable r) {
        return new Thread(r);
      }
    };
    ExecutorService newFixedThreadPool = Executors.newFixedThreadPool(threadCount, tf);
    
    List<Callable<Boolean>> callables = new ArrayList<>();
    for(int index = 0; index < threadCount; index ++) {
      Callable<Boolean> call = () -> {
        StateManagedComponent comp = new ServiceList();
        AdaptrisMessageConsumer consumer = new MockConsumer();
        AdaptrisMessageProducer producer = new MockProducer();
        connection.addExceptionListener(comp);
        connection.addMessageConsumer(consumer);
        connection.addMessageProducer(producer);
        return true;
      };
      callables.add(call);
    }
    
    newFixedThreadPool.invokeAll(callables);
    
    Awaitility
      .await()
      .atMost(Duration.ofSeconds(10))
      .with()
      .pollInterval(Duration.ofMillis(100))
      .untilTrue(new AtomicBoolean(connection.retrieveExceptionListeners().size() == threadCount));
  }

  @Test
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

  @Test
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

  @Test
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

  @Test
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

  @Test
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
