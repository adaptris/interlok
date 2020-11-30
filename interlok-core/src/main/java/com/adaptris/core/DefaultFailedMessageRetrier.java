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
import java.util.function.Consumer;
import javax.management.MalformedObjectNameException;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import com.adaptris.annotation.AdapterComponent;
import com.adaptris.annotation.AdvancedConfig;
import com.adaptris.annotation.AutoPopulated;
import com.adaptris.annotation.ComponentProfile;
import com.adaptris.annotation.InputFieldDefault;
import com.adaptris.core.runtime.AdapterManager;
import com.adaptris.core.runtime.ParentRuntimeInfoComponent;
import com.adaptris.core.runtime.RuntimeInfoComponent;
import com.adaptris.core.runtime.RuntimeInfoComponentFactory;
import com.adaptris.core.util.Args;
import com.adaptris.core.util.LifecycleHelper;
import com.adaptris.core.util.LoggingHelper;
import com.adaptris.core.util.ManagedThreadFactory;
import com.adaptris.util.TimeInterval;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import lombok.Getter;
import lombok.Setter;

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
public class DefaultFailedMessageRetrier extends FailedMessageRetrierImp
    implements AdaptrisMessageListener {
  /**
   * The default shutdown wait.
   *
   */
  private static final TimeInterval DEFAULT_SHUTDOWN_WAIT = new TimeInterval(1L, TimeUnit.MINUTES.name());
  static {
    RuntimeInfoComponentFactory.registerComponentFactory(new JmxFactory());
  }

  private transient ExecutorService failedMessageExecutor = null;

  @NotNull
  @Valid
  @AutoPopulated
  @Getter
  private StandaloneConsumer standaloneConsumer;

  /**
   * Set the shutdown wait timeout for the pool.
   * <p>
   * When {@link #close()} is invoked, this shutdowns the internal retry submission pool used by
   * {@link DefaultFailedMessageRetrierJmxMBean}. The specified value is the amount of time to wait
   * for a clean shutdown. If this timeout is exceeded then a forced shutdown ensues, which may mean
   * some messages will not have been retried. The default is 60 seconds if not explicitly
   * specified.
   * </p>
   */
  @InputFieldDefault(value = "60 seconds")
  @AdvancedConfig(rare = true)
  @Getter
  @Setter
  private TimeInterval shutdownWaitTime;

  public DefaultFailedMessageRetrier() {
    setStandaloneConsumer(new StandaloneConsumer());
  }


  @Override
  public synchronized void onAdaptrisMessage(AdaptrisMessage msg, Consumer<AdaptrisMessage> success,
      Consumer<AdaptrisMessage> failure) {
    try {
      Workflow workflow = getWorkflow(msg);
      updateRetryCountMetadata(msg);
      workflow.onAdaptrisMessage(msg, success, failure); // workflow.onAM is sync'd...
    } catch (Exception e) { // inc. runtime, exc. Workflow
      log.error("exception retrying message", e);
      log.error("message {}", MessageLoggerImpl.LAST_RESORT_LOGGER.toString(msg));
    }
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

  @Override
  public void prepare() throws CoreException {
    LifecycleHelper.prepare(getStandaloneConsumer());
  }

  @Override
  public void init() throws CoreException {
    if (standaloneConsumer != null) {
      standaloneConsumer.registerAdaptrisMessageListener(this);
    }
    LifecycleHelper.init(standaloneConsumer);
    failedMessageExecutor = Executors.newSingleThreadExecutor();
  }

  /** @see com.adaptris.core.AdaptrisComponent#start() */
  @Override
  public void start() throws CoreException {
    LifecycleHelper.start(standaloneConsumer);
  }

  @Override
  public void stop() {
    LifecycleHelper.stop(standaloneConsumer);
  }

  @Override
  public void close() {
    LifecycleHelper.close(standaloneConsumer);
    shutdownExecutors();
  }

  private void shutdownExecutors() {
    ManagedThreadFactory.shutdownQuietly(failedMessageExecutor, shutdownWaitTimeMs());
  }

  private long shutdownWaitTimeMs() {
    return TimeInterval.toMillisecondsDefaultIfNull(getShutdownWaitTime(), DEFAULT_SHUTDOWN_WAIT);
  }

  public void setStandaloneConsumer(StandaloneConsumer consumer) {
    standaloneConsumer = Args.notNull(consumer, "consumer");
    standaloneConsumer.registerAdaptrisMessageListener(this);
  }


  @Override
  public String friendlyName() {
    return LoggingHelper.friendlyName(this);
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
    protected RuntimeInfoComponent createComponent(ParentRuntimeInfoComponent parent,
        AdaptrisComponent e) throws MalformedObjectNameException {
      return new DefaultFailedMessageRetrierJmx((AdapterManager) parent,
          (DefaultFailedMessageRetrier) e);
    }
  }

}
