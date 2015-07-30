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
   * Dummy setting that matches {@link org.quartz.simpl.SimpleThreadPool#setThreadPriority(int)}
   * 
   */
  public void setThreadPriority(int prio) {
    this.threadPriority = prio;
  }

  @Override
  public void initialize() throws SchedulerConfigException {
    executor = Executors.newFixedThreadPool(threadCount, new ManagedThreadFactory());
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
      executor.shutdown();
      boolean success = false;
      try {
        success = executor.awaitTermination(DEFAULT_SHUTDOWN_WAIT.toMilliseconds(), TimeUnit.MILLISECONDS);
      }
      catch (InterruptedException e) {

      }
      if (!success) {
        executor.shutdownNow();
      }
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
   * Dummy setting that matches {@link org.quartz.simpl.SimpleThreadPool#setThreadNamePrefix(String)}
   * 
   */
  public void setThreadNamePrefix(String prfx) {
  }

  /**
   * Dummy setting that matches {@link org.quartz.simpl.SimpleThreadPool#setThreadsInheritGroupOfInitializingThread(boolean)}
   * 
   */
  public void setThreadsInheritGroupOfInitializingThread(boolean inheritGroup) {
  }

  /**
   * Dummy setting that matches
   * {@link org.quartz.simpl.SimpleThreadPool#setThreadsInheritContextClassLoaderOfInitializingThread(boolean)}
   * 
   */
  public void setThreadsInheritContextClassLoaderOfInitializingThread(boolean inheritLoader) {
  }

  /**
   * Dummy setting that matches {@link org.quartz.simpl.SimpleThreadPool#setMakeThreadsDaemons(boolean)}
   * 
   */
  public void setMakeThreadsDaemons(boolean b) {

  }
}