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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;
import javax.management.MalformedObjectNameException;
import com.adaptris.annotation.AdapterComponent;
import com.adaptris.annotation.AdvancedConfig;
import com.adaptris.annotation.ComponentProfile;
import com.adaptris.annotation.InputFieldDefault;
import com.adaptris.core.runtime.AdapterManager;
import com.adaptris.core.runtime.ParentRuntimeInfoComponent;
import com.adaptris.core.runtime.RuntimeInfoComponent;
import com.adaptris.core.runtime.RuntimeInfoComponentFactory;
import com.adaptris.core.util.ManagedThreadFactory;
import com.adaptris.util.TimeInterval;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * <p>
 * Implementation of <code>FailedMessageRetrier</code> that does not allow duplicate workflows.
 * </p>
 * 
 * @config default-failed-message-retrier
 */
@XStreamAlias("default-failed-message-retrier")
@AdapterComponent
@ComponentProfile(summary = "A Configurable Failed Message Retrier", tag = "error-handling,base")
public class DefaultFailedMessageRetrier extends FailedMessageRetrierImp {
  /**
   * The default shutdown wait.
   *
   */
  private static final TimeInterval DEFAULT_SHUTDOWN_WAIT = new TimeInterval(1L, TimeUnit.MINUTES.name());
  static {
    RuntimeInfoComponentFactory.registerComponentFactory(new JmxFactory());
  }

  private transient ExecutorService failedMessageExecutor = null;

  @InputFieldDefault(value = "60 seconds")
  @AdvancedConfig(rare = true)
  private TimeInterval shutdownWaitTime;

  @Override
  public void addWorkflow(Workflow workflow) throws CoreException {
    String key = workflow.obtainWorkflowId();
    if (getWorkflows().keySet().contains(key)) {
      log.warn("duplicate workflow ID [" + key + "]");
      throw new CoreException("Workflows cannot be uniquely identified");
    }
    log.debug("adding workflow with key [" + key + "]");
    getWorkflows().put(key, workflow);
  }

  boolean retryMessage(final AdaptrisMessage msg) {
    boolean submitted = false;
    try {
      getWorkflow(msg);
      failedMessageExecutor.execute(new Thread() {
        @Override
        public void run() {
          Thread.currentThread().setName("Retry Failed Message");
          onAdaptrisMessage(msg);
        }
      });
      submitted = true;
    }
    catch (CoreException e) {
      log.warn(e.getMessage(), e);
      // getWorkflow() throws a CoreException if it can't find it, so it can't have been submitted properly.
      submitted = false;
    }
    catch (RejectedExecutionException e) {
      // The pool is "closed".
      log.warn("Failed to submit message for retry", e);
      submitted = false;
    }
    return submitted;
  }

  boolean retryMessage(File file) throws IOException, CoreException {
    MimeEncoder encoder = new MimeEncoder();
    encoder.setRetainUniqueId(true);
    AdaptrisMessage msg = null;
    try (InputStream in = new FileInputStream(file)) {
      msg = encoder.readMessage(in);
    }
    return retryMessage(msg);
  }

  private static class JmxFactory extends RuntimeInfoComponentFactory {

    @Override
    protected boolean isSupported(AdaptrisComponent e) {
      if (e != null && e instanceof DefaultFailedMessageRetrier) {
        return true;
      }
      return false;
    }

    @Override
    protected RuntimeInfoComponent createComponent(ParentRuntimeInfoComponent parent, AdaptrisComponent e)
        throws MalformedObjectNameException {
      return new DefaultFailedMessageRetrierJmx((AdapterManager) parent, (DefaultFailedMessageRetrier) e);
    }
  }

  @Override
  public void init() throws CoreException {
    super.init();
    failedMessageExecutor = Executors.newSingleThreadExecutor();
  }

  @Override
  public void start() throws CoreException {
    super.start();
  }

  @Override
  public void stop() {
    super.stop();
  }

  @Override
  public void close() {
    shutdownExecutors();
    super.close();
  }

  private void shutdownExecutors() {
    ManagedThreadFactory.shutdownQuietly(failedMessageExecutor, shutdownWaitTimeMs());
  }

  private long shutdownWaitTimeMs() {
    return TimeInterval.toMillisecondsDefaultIfNull(getShutdownWaitTime(), DEFAULT_SHUTDOWN_WAIT);
  }

  public TimeInterval getShutdownWaitTime() {
    return shutdownWaitTime;
  }

  /**
   * Set the shutdown wait timeout for the pool.
   * <p>
   * When {@link #close()} is invoked, this shutdowns the internal retry submission pool used by
   * {@link DefaultFailedMessageRetrierJmxMBean}. The specified value is the amount of time to wait for a clean shutdown. If this
   * timeout is exceeded then a forced shutdown ensues, which may mean some messages will not have been retried.
   * </p>
   * 
   * @param interval the shutdown time (default is 60 seconds)
   * @see #stop()
   */
  public void setShutdownWaitTime(TimeInterval interval) {
    shutdownWaitTime = interval;
  }

}
