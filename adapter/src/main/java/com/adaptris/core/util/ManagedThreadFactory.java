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

package com.adaptris.core.util;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.adaptris.util.TimeInterval;

/**
 * Simple {@link ThreadFactory} implementation for use within the adapter.
 * 
 * @author lchan
 * 
 */
public class ManagedThreadFactory implements ThreadFactory {

  private static final Set<Thread> CREATED_THREADS = Collections.newSetFromMap(new WeakHashMap<Thread, Boolean>());

  private final ThreadGroup myThreadGroup;
  private static final AtomicInteger factoryNumber = new AtomicInteger(1);
  private final AtomicInteger threadNumber = new AtomicInteger(1);
  private final String prefix;

  private static final Logger logger = LoggerFactory.getLogger(ManagedThreadFactory.class);
  private static final ManagedThreadFactory instance = new ManagedThreadFactory();

  public ManagedThreadFactory() {
    SecurityManager s = System.getSecurityManager();
    myThreadGroup = (s != null) ? s.getThreadGroup() : Thread.currentThread().getThreadGroup();
    prefix = ManagedThreadFactory.class.getSimpleName() + "-" + factoryNumber.getAndIncrement() + "-Thread-";
  }

  public static Thread createThread(Runnable r) {
    return instance.newThread(r);
  }

  public static Thread createThread(String name, Runnable r) {
    Thread t = createThread(r);
    t.setName(name);
    return t;
  }

  @Override
  public Thread newThread(Runnable runner) {
    return createThread(myThreadGroup, runner);
  }

  protected Thread createThread(ThreadGroup group, Runnable r) {
    Thread t = new Thread(group, r, createName(), 0);
    CREATED_THREADS.add(t);
    return t;
  }

  protected String createName() {
    return prefix + threadNumber.getAndIncrement();
  }

  public static void interruptManagedThreads() {
    logger.trace("Interrupt Request Received");
    for (Thread t : CREATED_THREADS) {
      if (t.isAlive() && !t.isInterrupted()) {
        // logger.trace("Interrupting " + t.getName());
        t.interrupt();
      }
    }
  }

  public static List<Runnable> shutdownQuietly(ExecutorService executor, TimeInterval timeout) {
    return shutdownQuietly(executor, timeout.toMilliseconds());
  }

  public static List<Runnable> shutdownQuietly(ExecutorService executor, long timeoutMs) {
    List<Runnable> result = Collections.EMPTY_LIST;
    if (executor != null) {
      executor.shutdown();
      boolean success = false;
      try {
        success = executor.awaitTermination(timeoutMs, TimeUnit.MILLISECONDS);
      } catch (InterruptedException e) {
      }
      if (!success) {
        logger.trace("Pool failed to shutdown in {}ms, forcing shutdown", timeoutMs);
        result = executor.shutdownNow();
      }
    }
    return result;
  }
}
