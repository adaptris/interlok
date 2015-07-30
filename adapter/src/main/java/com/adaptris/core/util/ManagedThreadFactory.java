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
