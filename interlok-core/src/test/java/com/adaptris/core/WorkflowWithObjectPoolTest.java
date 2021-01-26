/*
 * Copyright 2021 Adaptris Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package com.adaptris.core;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import java.util.concurrent.TimeUnit;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.adaptris.core.stubs.MockService;
import com.adaptris.interlok.util.Closer;
import com.adaptris.util.TimeInterval;
import lombok.NoArgsConstructor;

// Should actually test more, but this tests all the edge cases that
// aren't tested via PoolingWorkflow etc.
@NoArgsConstructor
public class WorkflowWithObjectPoolTest extends WorkflowWithObjectPool {

  private static final Logger log = LoggerFactory.getLogger(WorkflowWithObjectPoolTest.class);

  @Override
  protected void onMessage(AdaptrisMessage msg) {

  }

  @Override
  protected void initialiseWorkflow() throws CoreException {
  }

  @Override
  protected void startWorkflow() throws CoreException {
  }

  @Override
  protected void stopWorkflow() {
  }

  @Override
  protected void closeWorkflow() {
  }

  @Test
  public void testWorkerFactory() throws Exception {
    WorkerFactory factory = new WorkerFactory(new ServiceList());
    PooledObject<Worker> obj = factory.makeObject();
    factory.activateObject(obj);
    factory.passivateObject(obj);
    assertTrue(factory.validateObject(obj));
    factory.destroyObject(obj);

  }

  @Test(expected = CoreException.class)
  public void testWorkerFactory_Uncloneable() throws Exception {
    WorkerFactory factory = new WorkerFactory(new ServiceList(new UnserializableService("")));
    PooledObject<Worker> obj = factory.makeObject();
  }

  @Test
  public void testPopulatePool() throws Exception {
    setMinIdle(10);
    setInitWaitTime(new TimeInterval(1L, TimeUnit.SECONDS));
    GenericObjectPool<Worker> pool = (GenericObjectPool<Worker>) createObjectPool();
    try {
      populatePool(pool);
    } finally {
      Closer.closeQuietly(pool);
    }
  }

  @Test(expected = CoreException.class)
  public void testPopulatePool_FailToInit() throws Exception {
    setMinIdle(10);
    setServiceCollection(new ServiceList(new MockService(MockService.FailureCondition.Lifecycle)));
    setInitWaitTime(new TimeInterval(1L, TimeUnit.SECONDS));
    GenericObjectPool<Worker> pool = (GenericObjectPool<Worker>) createObjectPool();
    try {
      populatePool(pool);
    } finally {
      Closer.closeQuietly(pool);
    }
  }

  @Test
  public void testReturnObject() throws Exception {
    setMinIdle(1);
    GenericObjectPool<Worker> pool = (GenericObjectPool<Worker>) createObjectPool();
    Worker worker = pool.borrowObject();
    returnObject(pool, worker);
    assertEquals(1, pool.getNumIdle());
  }

  @Test
  public void testReturnObject_InvalidObject() throws Exception {
    setMinIdle(1);
    GenericObjectPool<Worker> pool = dummyPool();
    Worker invalidWorker = new DummyWorker();
    // worker that isn't associated, should ultimately throu
    // an IllegalStateException...
    returnObject(pool, invalidWorker);

  }

  @Test
  public void testReturnObject_FailureToReturn() throws Exception {
    setMinIdle(1);
    GenericObjectPool<Worker> pool = dummyPool();
    Worker worker = pool.borrowObject();
    returnObject(pool, worker);
    // On the 2nd return it should cause an IllegalState since it can't deallocate it?
    // But the object itself can be destroyed/invalidated.
    returnObject(pool, worker);
  }

  private GenericObjectPool<Worker> dummyPool() {
    GenericObjectPool<Worker> pool =
        new GenericObjectPool<>(new DummyFactory());
    pool.setMaxTotal(poolSize());
    pool.setMinIdle(minIdle());
    pool.setMaxIdle(maxIdle());
    pool.setMaxWaitMillis(-1L);
    pool.setBlockWhenExhausted(true);
    return pool;
  }

  public class UnserializableService extends NullService {
    // no parameter less constructor breaks XStream.
    public UnserializableService(String x) {
      super();
    }
  }

  public class DummyFactory extends WorkerFactory {

    public DummyFactory() {
      super(new ServiceList());
    }

    @Override
    public PooledObject<Worker> makeObject() throws Exception {
      return new DefaultPooledObject<>(new DummyWorker());
    }

  }

  public class DummyWorker extends Worker {

    public DummyWorker() {
      super(new ServiceList());
    }
  }

}
