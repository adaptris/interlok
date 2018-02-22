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

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.quartz.SchedulerConfigException;
import org.quartz.spi.ThreadPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.adaptris.core.QuartzCronPoller;
import com.adaptris.util.TimeInterval;

/**
 * A simple non-blocking {@link ThreadPool} implementation for use with Quartz.
 * <p>
 * {@link QuartzCronPoller} <strong>will default</strong> to using this implementation unless explictly configured not to via
 * {@link QuartzCronPoller#setUseCustomThreadPool(Boolean)}. Under the covers it uses a
 * {@link Executors#newScheduledThreadPool(int)} as the thread execution pool. It does not need to block as polling consumers have
 * an internal locking mechanism that handles that behaviour. The reason for using it is to ensure that {@link ManagedThreadFactory}
 * is used to create any threads required for execution allowing a more graceful shutdown.
 * </p>
 */
public class NonBlockingQuartzThreadPool implements ThreadPool {
  private static final TimeInterval DEFAULT_SHUTDOWN_WAIT = new TimeInterval(1L, TimeUnit.MINUTES);
  private static final long DEFAULT_KEEP_ALIVE = 60;

  private transient String instanceId;
  private transient String instanceName;
  private transient int threadCount = 10;
  private transient long keepAliveTime = DEFAULT_KEEP_ALIVE;
  private transient int threadPriority = Thread.NORM_PRIORITY;

  private transient ExecutorService executor;
  private transient Logger log = LoggerFactory.getLogger(this.getClass());


  public NonBlockingQuartzThreadPool() {
  }

  @Override
  public int blockForAvailableThreads() {
    return 1;
  }

  public void setThreadCount(int count) {
    this.threadCount = count;
  }

  /**
   * Setting that matches {@code org.quartz.simpl.SimpleThreadPool#setThreadPriority(int)}
   * 
   */
  public void setThreadPriority(int prio) {
    this.threadPriority = prio;
  }

  @Override
  public void initialize() throws SchedulerConfigException {
    executor = Executors.newFixedThreadPool(threadCount, new ManagedThreadFactory(getClass().getSimpleName()));
    log.trace("Initialised Quartz ThreadPool: {}", executor);
  }

  @Override
  public boolean runInThread(Runnable runnable) {
    if (!executor.isShutdown()) {
      executor.execute(runnable);
      return true;
    }
    return false;
  }

  @Override
  public void shutdown(boolean waitForJobsToComplete) {
    log.trace("Shutdown requested(wait={})", waitForJobsToComplete);

    if (waitForJobsToComplete) {
      ManagedThreadFactory.shutdownQuietly(executor, DEFAULT_SHUTDOWN_WAIT);
    }
    else {
      executor.shutdownNow();
    }
  }

  @Override
  public int getPoolSize() {
    if (executor instanceof ThreadPoolExecutor) {
      return ((ThreadPoolExecutor) executor).getPoolSize();
    }
    return 0;
  }

  @Override
  public void setInstanceId(String schedInstId) {
    this.instanceId = schedInstId;
  }

  @Override
  public void setInstanceName(String schedName) {
    this.instanceName = schedName;
  }


  /**
   * NoOp Method mirroring {@code org.quartz.simpl.SimpleThreadPool#setThreadNamePrefix(String)}
   * 
   */
  public void setThreadNamePrefix(String prfx) {
  }

  /**
   * NoOp Method mirroring {@code org.quartz.simpl.SimpleThreadPool#setThreadsInheritGroupOfInitializingThread(boolean)}
   * 
   */
  public void setThreadsInheritGroupOfInitializingThread(boolean inheritGroup) {
  }

  /**
   * NoOp Method mirroring
   * {@code org.quartz.simpl.SimpleThreadPool#setThreadsInheritContextClassLoaderOfInitializingThread(boolean)}
   * 
   */
  public void setThreadsInheritContextClassLoaderOfInitializingThread(boolean inheritLoader) {
  }

  /**
   * NoOp Method mirroring {@code org.quartz.simpl.SimpleThreadPool#setMakeThreadsDaemons(boolean)}
   * 
   */
  public void setMakeThreadsDaemons(boolean b) {

  }
}
