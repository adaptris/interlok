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
import java.util.Set;
import java.util.WeakHashMap;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

  public ManagedThreadFactory() {
    SecurityManager s = System.getSecurityManager();
    myThreadGroup = (s != null) ? s.getThreadGroup() : Thread.currentThread().getThreadGroup();
    prefix = ManagedThreadFactory.class.getSimpleName() + "-" + factoryNumber.getAndIncrement() + "-Thread-";
  }

  
  @Override
  public Thread newThread(Runnable runner) {
    Thread t = createThread(myThreadGroup, runner);
    CREATED_THREADS.add(t);
    return t;
  }

  protected Thread createThread(ThreadGroup group, Runnable r) {
    return new Thread(group, r, createName(), 0);
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
}
