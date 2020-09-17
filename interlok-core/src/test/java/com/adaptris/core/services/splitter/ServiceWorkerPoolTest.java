/*
 * Copyright 2018 Adaptris Ltd.
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
package com.adaptris.core.services.splitter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import java.util.concurrent.ExecutorService;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.PooledObjectFactory;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.core.CoreException;
import com.adaptris.core.NullService;
import com.adaptris.core.stubs.MockService;
import com.adaptris.core.stubs.MockService.FailureCondition;
import com.adaptris.core.util.ManagedThreadFactory;
import com.adaptris.util.TimeInterval;

public class ServiceWorkerPoolTest extends ServiceWorkerPool {

  public ServiceWorkerPoolTest() throws Exception {
    super(new NullService(), null, 10);
  }

  @Before
  public void setUp() throws Exception {
  }

  @After
  public void tearDown() throws Exception {
  }

  @Test
  public void testCreateObjectPool() throws Exception {
    GenericObjectPool<ServiceWorkerPool.Worker> pool = createCommonsObjectPool();
    assertNotNull(pool);
    assertEquals(10, pool.getMaxTotal());
    assertEquals(10, pool.getMinIdle());
    assertEquals(10, pool.getMaxIdle());
    assertEquals(-1, pool.getMaxWaitMillis());
    assertTrue(pool.getBlockWhenExhausted());
  }

  @Test
  public void testCreateExecutor() throws Exception {
    ExecutorService exec = createExecutor("testCreateExecutor");
    assertNotNull(exec);
    ManagedThreadFactory.shutdownQuietly(exec, new TimeInterval());
  }

  @Test
  @SuppressWarnings("deprecation")
  public void testCloseQuietly() {
    closeQuietly((GenericObjectPool) null);
    closeQuietly(new GenericObjectPool(new DummyPooledObjectFactory()));
    closeQuietly(new GenericObjectPool(new DummyPooledObjectFactory()) {

      @Override
      public void close() {
        throw new RuntimeException();
      }

    });
  }

  @Test
  public void testWorker() throws Exception {
    ServiceWorkerPool.Worker worker = new ServiceWorkerPool.Worker();
    worker.start();
    worker.stop();
    assertTrue(worker.isValid());
    assertNotNull(worker.doService(AdaptrisMessageFactory.getDefaultInstance().newMessage()));
  }

  @Test
  public void testWorkerFactory() throws Exception {
    ServiceWorkerPool.WorkerFactory workerFactory = new ServiceWorkerPool.WorkerFactory();
    PooledObject<ServiceWorkerPool.Worker> worker = workerFactory.makeObject();
    assertNotNull(worker);
    workerFactory.validateObject(worker);
    workerFactory.activateObject(worker);
    workerFactory.passivateObject(worker);
    workerFactory.destroyObject(worker);
  }

  @Test
  public void testWarmup() throws Exception {
    GenericObjectPool<ServiceWorkerPool.Worker> objPool = createCommonsObjectPool();
    warmup(objPool);
    assertEquals(10, objPool.getNumIdle());
    closeQuietly(objPool);
  }

  @Test
  public void testWarmup_WithException() throws Exception {
    GenericObjectPool<ServiceWorkerPool.Worker> objPool = null;
    try {
      ServiceWorkerPool worker = new ServiceWorkerPool(new MockService(FailureCondition.Lifecycle), null, 10);
      objPool= worker.createCommonsObjectPool();
      worker.warmup(objPool);
      fail();
    } catch (CoreException expected) {

    } finally {
      closeQuietly(objPool);
    }
  }


  private class DummyPooledObjectFactory implements PooledObjectFactory {

    @Override
    public PooledObject makeObject() throws Exception {
      return null;
    }

    @Override
    public void destroyObject(PooledObject p) throws Exception {
    }

    @Override
    public boolean validateObject(PooledObject p) {
      return false;
    }

    @Override
    public void activateObject(PooledObject p) throws Exception {
    }

    @Override
    public void passivateObject(PooledObject p) throws Exception {
    }

  }
}
