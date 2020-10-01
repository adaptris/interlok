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

import static com.adaptris.core.CoreConstants.OBJ_METADATA_ON_FAILURE_CALLBACK;
import static com.adaptris.core.CoreConstants.OBJ_METADATA_ON_SUCCESS_CALLBACK;
import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;
import static org.apache.commons.lang3.StringUtils.isEmpty;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import javax.management.MalformedObjectNameException;
import com.adaptris.annotation.InputFieldDefault;
import com.adaptris.core.runtime.ParentRuntimeInfoComponent;
import com.adaptris.core.runtime.RuntimeInfoComponent;
import com.adaptris.core.runtime.RuntimeInfoComponentFactory;
import com.adaptris.core.util.ManagedThreadFactory;
import com.adaptris.util.NumberUtils;
import com.adaptris.util.TimeInterval;

/**
 * Abstract MessageErrorHandler implementation that allows automatic retries for
 * a problem message.
 */
public abstract class RetryMessageErrorHandlerImp extends StandardProcessingExceptionHandler {

  private static final TimeInterval DEFAULT_RETRY_INTERVAL = new TimeInterval(10L, TimeUnit.MINUTES);
  private static final TimeInterval DEFAULT_POOL_TIMEOUT = new TimeInterval(30L, TimeUnit.SECONDS);

  private static final int RETRY_LIMIT_DEFAULT = 10;
  protected static final String IS_RETRY_KEY = "autoRetryInProgress";
  protected static final String RETRY_COUNT_KEY = "autoRetryCount";

  @InputFieldDefault(value = "10")
  private Integer retryLimit;

  @InputFieldDefault(value = "10 minutes")
  private TimeInterval retryInterval;

  protected transient ScheduledExecutorService executor;
  protected transient List<AdaptrisMessage> retryList;
  protected transient List<AdaptrisMessage> inProgress;
  private transient Set<ScheduledFuture> retries = Collections.newSetFromMap(new WeakHashMap<ScheduledFuture, Boolean>());
  private transient ScheduledFuture sweeper = null;
  private transient boolean failAll = false;

  static {
    RuntimeInfoComponentFactory.registerComponentFactory(new JmxFactory());
  }

  public RetryMessageErrorHandlerImp() {
    super();
    retryList = Collections.synchronizedList(new ArrayList<AdaptrisMessage>());
    inProgress = Collections.synchronizedList(new ArrayList<AdaptrisMessage>());
  }

  @Override
  public synchronized void handleProcessingException(AdaptrisMessage msg) {
    if (shouldFail(msg) || failAll) {
      failMessage(msg);
    }
    else {
      scheduleNextRun(msg);
    }
  }

  private boolean shouldFail(AdaptrisMessage msg) {
    int count;
    Map md = msg.getObjectHeaders();
    if (md.containsKey(IS_RETRY_KEY)) {
      count = Integer.parseInt((String) md.get(RETRY_COUNT_KEY));
      count++;
      md.put(RETRY_COUNT_KEY, String.valueOf(count));
    }
    else {
      count = 0;
      md.put(IS_RETRY_KEY, "true");
      md.put(RETRY_COUNT_KEY, String.valueOf(count));
    }
    log.trace("Next retry on [{}] is retry {}", msg.getUniqueId(), count + 1);
    int rl = retryLimit();
    return rl <= 0 ? false : count >= rl;
  }


  @Override
  public void start() throws CoreException {
    executor = Executors.newScheduledThreadPool(1, new ManagedThreadFactory(getClass().getSimpleName()));
    sweeper = executor.scheduleWithFixedDelay(new CleanupTask(), 100L, retryIntervalMs(), TimeUnit.MILLISECONDS);
    failAll = false;
    super.start();
  }

  @Override
  public void stop() {
    failAllMessages();
    shutdownExecutor();
    super.stop();
  }

  private void shutdownExecutor() {
    sweeper.cancel(true);
    for (ScheduledFuture f : retries) {
      f.cancel(true);
    }
    retries.clear();
    ManagedThreadFactory.shutdownQuietly(executor, DEFAULT_POOL_TIMEOUT);
    executor = null;
  }

  /**
   * Set the limit on the number of retries that a message may have.
   *
   * @param i the number of retries, if less than or equal to 0, then this is
   *          considered to be an infinite number of retries.
   */
  public void setRetryLimit(Integer i) {
    retryLimit = i;
  }

  /**
   * Get the retry limit.
   *
   * @return the retry limit.
   */
  public Integer getRetryLimit() {
    return retryLimit;
  }

  int retryLimit() {
    return NumberUtils.toIntDefaultIfNull(getRetryLimit(), RETRY_LIMIT_DEFAULT);
  }

  long retryIntervalMs() {
    return TimeInterval.toMillisecondsDefaultIfNull(getRetryInterval(), DEFAULT_RETRY_INTERVAL);
  }

  public TimeInterval getRetryInterval() {
    return retryInterval;
  }

  /**
   * Set the interval between attempts to retry a failed message.
   * 
   * @param interval the interval; default is 10 minutes if not explicitly configured.
   */
  public void setRetryInterval(TimeInterval interval) {
    retryInterval = interval;
  }

  protected void failAllMessages() {
    for (AdaptrisMessage msg : new ArrayList<AdaptrisMessage>(retryList)) {
      failMessage(msg);
    }
    retryList.clear();
  }

  protected void failFutureMessages(boolean failFuture) {
    this.failAll = failFuture;
  }

  protected Collection<String> waitingForRetry() {
    Set<String> result = new HashSet<String>();
    for (AdaptrisMessage msg : new ArrayList<AdaptrisMessage>(retryList)) {
      result.add(msg.getUniqueId());
    }
    return result;
  }

  protected void failMessage(String s) {
    for (AdaptrisMessage msg : new ArrayList<AdaptrisMessage>(retryList)) {
      if (msg.getUniqueId().equals(s)) {
        failMessage(msg);
      }
    }
  }

  protected void failMessage(AdaptrisMessage msg) {
    log.error("Message [{}] deemed to have failed", msg.getUniqueId());
    if (msg.getObjectHeaders().containsKey(CoreConstants.OBJ_METADATA_EXCEPTION)) {
      Exception e = (Exception) msg.getObjectHeaders().get(CoreConstants.OBJ_METADATA_EXCEPTION);
      log.error(e.getMessage(), e);
    }
    super.handleProcessingException(msg);
    msg.getObjectHeaders().put(CoreConstants.OBJ_METADATA_MESSAGE_FAILED, true);
  }

  protected void scheduleNextRun(AdaptrisMessage msg) {
    log.trace("Message [{}] should be retried", msg.getUniqueId());
    try {
      ScheduledFuture f = executor.schedule(new RetryThread(msg), retryIntervalMs(), TimeUnit.MILLISECONDS);
      retries.add(f);
    }
    catch (Exception e) {
      log.warn("Failed to reschedule retry, failing message");
      failMessage(msg);
    }
  }


  protected static Map<String, Workflow> filterStarted(Map<String, Workflow> workflows) {
    Map<String, Workflow> result = new HashMap<>(workflows.size());
    for (Map.Entry<String, Workflow> entry : workflows.entrySet()) {
      if (StartedState.getInstance().equals(entry.getValue().retrieveComponentState())) {
        result.put(entry.getKey(), entry.getValue());
      }
    }
    return result;
  }
  /**
   * Private thread used to resubmit messages.
   *
   */
  protected class RetryThread implements Runnable {
    private AdaptrisMessage msg;

    RetryThread(AdaptrisMessage m) {
      msg = m;
      retryList.add(msg);
    }

    @Override
    public void run() {
      String oldName = Thread.currentThread().getName();
      Thread.currentThread().setName(toString());
      try {
        retryList.remove(msg);
        inProgress.add(msg);
        log.trace("Retrying message [{}]", msg.getUniqueId());
        Workflow workflow = filterStarted(registeredWorkflows()).get(msg.getMetadataValue(Workflow.WORKFLOW_ID_KEY));
        if (workflow != null) {
          log.trace("Retrying message [{}] in workflow [{}]", msg.getUniqueId(), workflow.obtainWorkflowId());
          workflow.onAdaptrisMessage(msg,
              defaultIfNull((Consumer) msg.getObjectHeaders().get(OBJ_METADATA_ON_SUCCESS_CALLBACK), (o) -> {   }),
              defaultIfNull((Consumer) msg.getObjectHeaders().get(OBJ_METADATA_ON_FAILURE_CALLBACK), (o) -> {   }));
        }
        else {
          log.warn("Workflow [{}] not registered, or not started, failing message", msg.getMetadataValue(Workflow.WORKFLOW_ID_KEY));
          log.debug("Registered Workflows :{}", registeredWorkflows().keySet());
          failMessage(msg);
        }
        inProgress.remove(msg);
      }
      finally {
        Thread.currentThread().setName(oldName);
      }
    }

    @Override
    public String toString() {
      return "RetryMessageErrorHandler#RetryThread";
    }

  }

  private class CleanupTask implements Runnable {

    @Override
    public void run() {
      retries.removeAll(filter(new ArrayList<ScheduledFuture>(retries)));
    }

    private List<ScheduledFuture> filter(Collection<ScheduledFuture> workingSet) {
      // surely time for a filter() lambda...
      // return workingSet.stream().filter(future -> future.isDone()).collect(Collectors.toList());
      List<ScheduledFuture> done = new ArrayList<>();
      for (ScheduledFuture f : workingSet) {
        if (f.isDone()) {
          done.add(f);
        }
      }
      return done;
    }
  }

  private static class JmxFactory extends RuntimeInfoComponentFactory {

    @Override
    protected boolean isSupported(AdaptrisComponent e) {
      if (e != null && e instanceof RetryMessageErrorHandlerImp) {
        return !isEmpty(e.getUniqueId());
      }
      return false;
    }

    @Override
    protected RuntimeInfoComponent createComponent(ParentRuntimeInfoComponent parent, AdaptrisComponent e)
        throws MalformedObjectNameException {
      return new RetryMessageErrorHandlerMonitor(parent, (RetryMessageErrorHandlerImp) e);
    }

  }
}
