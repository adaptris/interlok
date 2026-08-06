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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;

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

    CountDownLatch startGate = new CountDownLatch(1);
    CountDownLatch doneGate = new CountDownLatch(threadCount);
    ExecutorService pool = Executors.newFixedThreadPool(threadCount);

    List<Future<Boolean>> futures = new ArrayList<>();
    for (int index = 0; index < threadCount; index++) {
      Callable<Boolean> call = () -> {
        try {
          startGate.await();
          StateManagedComponent comp = new ServiceList();
          AdaptrisMessageConsumer consumer = new MockConsumer();
          AdaptrisMessageProducer producer = new MockProducer();
          connection.addExceptionListener(comp);
          connection.addMessageConsumer(consumer);
          connection.addMessageProducer(producer);
          return true;
        }
        finally {
          doneGate.countDown();
        }
      };
      futures.add(pool.submit(call));
    }

    startGate.countDown();
    assertTrue(doneGate.await(20, TimeUnit.SECONDS), "Timed out waiting for concurrent registration tasks to finish");

    for (Future<Boolean> future : futures) {
      assertTrue(future.get(), "Concurrent registration task did not complete successfully");
    }

    assertEquals(threadCount, connection.retrieveExceptionListeners().size());
    assertEquals(threadCount, connection.retrieveMessageConsumers().size());
    assertEquals(threadCount, connection.retrieveMessageProducers().size());

    pool.shutdownNow();
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

  @Test
  void testConnectionStateHandler() {
    MockConnection mc = new MockConnection();
    assertNull(mc.getConnectionStateHandler());
    ConnectionStateHandler csh = new ConnectionStateHandlerImp(){};
    mc.setConnectionStateHandler(csh);
    assertEquals(csh, mc.getConnectionStateHandler());
  }

  @Test
  void testConnectionStateHandlerRegisteredOnPrepare() throws Exception {
    MockConnection connection = new MockConnection();
    ConnectionStateHandler handler = mock(ConnectionStateHandler.class);
    connection.setConnectionStateHandler(handler);

    connection.prepare();

    verify(handler).registerConnection(connection);
  }

  @Test
  void testInterfaceDefaultMethods() {
    AdaptrisConnection connection = new DefaultMethodConnection();

    // Default implementation is no-op and does not retain the handler.
    connection.setConnectionStateHandler(new ConnectionStateHandlerImp() {
    });
    assertNull(connection.getConnectionStateHandler());
    assertNull(connection.connectionStateHandler());

    // Default runtime component setting is FALSE and setter is no-op.
    assertEquals(Boolean.FALSE, connection.getRuntimeComponent());
    connection.setRuntimeComponent(Boolean.TRUE);
    assertEquals(Boolean.FALSE, connection.getRuntimeComponent());
  }

  private void assertState(List list, ComponentState state) {
    for (Object c : list) {
      assertEquals(state, ((StateManagedComponent) c).retrieveComponentState(), "" + state);
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

  private static class DefaultMethodConnection implements AdaptrisConnection {

    @Override
    public java.util.Set<StateManagedComponent> retrieveExceptionListeners() {
      return Collections.emptySet();
    }

    @Override
    public void addExceptionListener(StateManagedComponent comp) {
      //test only
    }

    @Override
    public void addMessageProducer(AdaptrisMessageProducer producer) throws CoreException {
      //test only
    }

    @Override
    public java.util.Set<AdaptrisMessageProducer> retrieveMessageProducers() {
      return Collections.emptySet();
    }

    @Override
    public void addMessageConsumer(AdaptrisMessageConsumer consumer) throws CoreException {
      //test only
    }

    @Override
    public java.util.Set<AdaptrisMessageConsumer> retrieveMessageConsumers() {
      return Collections.emptySet();
    }

    @Override
    public void setConnectionErrorHandler(ConnectionErrorHandler handler) {
      //test only
    }

    @Override
    public ConnectionErrorHandler getConnectionErrorHandler() {
      return null;
    }

    @Override
    public ConnectionErrorHandler connectionErrorHandler() {
      return null;
    }

    @Override
    public <T> T retrieveConnection(Class<T> type) {
      return null;
    }

    @Override
    public AdaptrisConnection cloneForTesting() throws CoreException {
      return this;
    }

    @Override
    public String getUniqueId() {
      return "default-method-connection";
    }

    @Override
    public ComponentState retrieveComponentState() {
      return ClosedState.getInstance();
    }

    @Override
    public void changeState(ComponentState newState) {
      //test only
    }

    @Override
    public void requestInit() throws CoreException {
      //test only
    }

    @Override
    public void requestStart() throws CoreException {
      //test only
    }

    @Override
    public void requestStop() {
      //test only
    }

    @Override
    public void requestClose() {
      //test only
    }

    @Override
    public void prepare() throws CoreException {
      //test only
    }
  }

}
