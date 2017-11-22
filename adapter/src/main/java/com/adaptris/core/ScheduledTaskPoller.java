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

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import com.adaptris.annotation.AdvancedConfig;
import com.adaptris.core.util.ManagedThreadFactory;
import com.adaptris.util.TimeInterval;

/**
 * @since 3.4.0
 */
public abstract class ScheduledTaskPoller extends PollerImp {

  private static final TimeInterval DEFAULT_POLL_INTERVAL = new TimeInterval(20L, TimeUnit.SECONDS);
  private static final TimeInterval DEFAULT_SHUTDOWN_WAIT = new TimeInterval(1L, TimeUnit.MINUTES);

  @AdvancedConfig
  private TimeInterval shutdownWaitTime;

  protected transient ScheduledExecutorService executor;
  protected transient ScheduledFuture pollerTask;

  public ScheduledTaskPoller() {
  }

  public void init() throws CoreException {
  }

  public void start() throws CoreException {
    executor = Executors.newSingleThreadScheduledExecutor(new ManagedThreadFactory());
    scheduleTask();
  }

  protected abstract void scheduleTask();

  public void stop() {
    cancelTask();
    shutdownExecutor();
  }

  private void shutdownExecutor() {
    ManagedThreadFactory.shutdownQuietly(executor, shutdownWaitTimeMs());
  }

  private void cancelTask() {
    if (pollerTask != null) {
      pollerTask.cancel(true);
      log.trace("Poller {} cancelled", pollerTask);
      pollerTask = null;
    }
  }

  public void close() {
    cancelTask();
    shutdownExecutor();
  }

  private class PollerTask implements Runnable {

    /** @see java.lang.Runnable#run() */
    @Override
    public void run() {
      processMessages();
    }
  }

  // properties...
  long shutdownWaitTimeMs() {
    return getShutdownWaitTime() != null ? getShutdownWaitTime().toMilliseconds() : DEFAULT_SHUTDOWN_WAIT.toMilliseconds();
  }
  

  public TimeInterval getShutdownWaitTime() {
    return shutdownWaitTime;
  }

  /**
   * Set the shutdown wait timeout for the pool.
   * <p>
   * When <code>stop()</code> is invoked, this causes a emptying and shutdown of the pool. The specified value is the amount of time
   * to wait for a clean shutdown. If this timeout is exceeded then a forced shutdown ensues, which may mean messages are in an
   * inconsistent state.
   * </p>
   *
   * @param interval the shutdown time (default is 60 seconds)
   * @see #stop()
   */
  public void setShutdownWaitTime(TimeInterval interval) {
    shutdownWaitTime = interval;
  }

  @Override
  public void prepare() throws CoreException {}
}
