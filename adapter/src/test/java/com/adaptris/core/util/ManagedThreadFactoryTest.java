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
package com.adaptris.core.util;

import static org.junit.Assert.assertNotNull;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.adaptris.util.TimeInterval;

public class ManagedThreadFactoryTest {

  @Before
  public void setUp() throws Exception {
  }

  @After
  public void tearDown() throws Exception {
  }

  @Test
  public void testCreateThreadRunnable() {
    assertNotNull(ManagedThreadFactory.createThread(new StayingAlive()));
  }

  @Test
  public void testCreateThreadStringRunnable() {
    assertNotNull(ManagedThreadFactory.createThread("myThreadName", new StayingAlive()));
  }

  @Test
  public void testInterruptManagedThreads() throws Exception {
    Thread t = ManagedThreadFactory.createThread(new StayingAlive());
    ManagedThreadFactory.createThread(new Runnable() {
      @Override
      public void run() {
      }

    }).start();
    t.start();
    ManagedThreadFactory.interruptManagedThreads();
    t.join();
  }

  @Test(expected = RejectedExecutionException.class)
  public void testShutdownQuietlyExecutors_Quick() throws Exception {
    ManagedThreadFactory.shutdownQuietly(null, new TimeInterval(1L, TimeUnit.SECONDS));
    ExecutorService executor = Executors.newCachedThreadPool(new ManagedThreadFactory());
    executor.submit(new Runnable() {
      @Override
      public void run() {}

    });
    ManagedThreadFactory.shutdownQuietly(executor, new TimeInterval(1L, TimeUnit.SECONDS));
    executor.submit(new Runnable() {
      @Override
      public void run() {}

    });
  }


  @Test(expected = RejectedExecutionException.class)
  public void testShutdownQuietlyExecutors_Slow() throws Exception {
    ExecutorService executor = Executors.newCachedThreadPool(new ManagedThreadFactory());
    executor.submit(new StayingAlive());
    ManagedThreadFactory.shutdownQuietly(executor, new TimeInterval(1L, TimeUnit.SECONDS));
    executor.submit(new Runnable() {
      @Override
      public void run() {}
      
    });
  }

  private class StayingAlive implements Runnable {

    @Override
    public void run() {
      try {
        TimeUnit.MINUTES.sleep(1);
      }
      catch (InterruptedException e) {
      }
    }

  }
}
