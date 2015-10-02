package com.adaptris.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import com.adaptris.util.FifoMutexLock;
import com.adaptris.util.TimeInterval;

/**
 * Abstract MessageErrorHandler implementation that allows automatic retries for
 * a problem message.
 */
public abstract class RetryMessageErrorHandlerImp extends StandardProcessingExceptionHandler {

  private static final TimeInterval DEFAULT_RETRY_INTERVAL = new TimeInterval(10L, TimeUnit.MINUTES);
  private static final TimeInterval DEFAULT_LOCK_TIMEOUT = new TimeInterval(1L, TimeUnit.SECONDS);

  private static final int RETRY_LIMIT_DEFAULT = 10;
  protected static final String IS_RETRY_KEY = "autoRetryInProgress";
  protected static final String RETRY_COUNT_KEY = "autoRetryCount";

  /**
   * enumerated type for recording the current state of RetryMessageHandler implementations.
   *
   *
   */
  protected enum State {
    CLOSED {
      @Override
      boolean equivalent(State s) {
        return s == State.CLOSED;
      }
    },
    INITED {
      @Override
      boolean equivalent(State s) {
        return s == State.INITED || s == State.STOPPED;
      }
    },
    STARTED {
      @Override
      boolean equivalent(State s) {
        return s == State.STARTED;
      }
    },
    STOPPED {
      @Override
      boolean equivalent(State s) {
        return s == State.INITED || s == State.STOPPED;
      }
    };

    abstract boolean equivalent(State s);
  };

  private Integer retryLimit;

  private TimeInterval lockTimeout;
  private TimeInterval retryInterval;

  private transient State currentState;
  private transient Timer retryTimer = null;
  protected transient List<AdaptrisMessage> retryList;
  protected transient List<AdaptrisMessage> inProgress;
  private transient FifoMutexLock lock;

  /**
   * Default Constructor.
   * <p>
   * Creates a new instance wit the following defaults
   * </p>
   * <ul>
   * <li>retry-limit = 10</li>
   * <li>retry-interval = 10 Minutes</li>
   * <li>lock-timeout = 1 second.
   * </ul>
   */
  public RetryMessageErrorHandlerImp() {
    super();
    currentState = State.CLOSED;
    retryList = Collections.synchronizedList(new ArrayList<AdaptrisMessage>());
    inProgress = Collections.synchronizedList(new ArrayList<AdaptrisMessage>());
    lock = new FifoMutexLock();
  }

  @Override
  public void handleProcessingException(AdaptrisMessage msg) {
    if (!currentState.equivalent(State.STARTED)) {
      try {
        start();
      }
      catch (CoreException e) {
        IllegalStateException ie = new IllegalStateException("Component was not started correctly");
        ie.initCause(e);
        throw ie;
      }
    }
    if (shouldFail(msg)) {
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
    log.trace("Next retry on [" + msg.getUniqueId() + "] is retry " + (count + 1));
    int rl = retryLimit();
    return rl <= 0 ? false : count >= rl;
  }

  /** @see com.adaptris.core.AdaptrisComponent#init() */
  @Override
  public void init() throws CoreException {
    if (currentState.equivalent(State.INITED)) {
      return;
    }
    super.init();
    currentState = State.INITED;
  }

  /** @see com.adaptris.core.AdaptrisComponent#start() */
  @Override
  public void start() throws CoreException {
    if (currentState.equivalent(State.STARTED)) {
      return;
    }
    init();
    retryTimer = new Timer(true);
    super.start();
    currentState = State.STARTED;
  }

  /** @see com.adaptris.core.AdaptrisComponent#stop() */
  @Override
  public void stop() {
    if (!currentState.equivalent(State.STARTED)) {
      return;
    }
    if (retryTimer != null) {
      retryTimer.cancel();
    }
    failAllMessages();
    super.stop();
    currentState = State.STOPPED;
  }

  /** @see com.adaptris.core.AdaptrisComponent#close() */
  @Override
  public void close() {
    if (currentState.equivalent(State.CLOSED)) {
      return;
    }
    stop();
    super.close();
    currentState = State.CLOSED;
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
    return getRetryLimit() != null ? getRetryLimit().intValue() : RETRY_LIMIT_DEFAULT;
  }

  long retryIntervalMs() {
    return getRetryInterval() != null ? getRetryInterval().toMilliseconds() : DEFAULT_RETRY_INTERVAL.toMilliseconds();
  }

  long lockTimeoutMs() {
    return getLockTimeout() != null ? getLockTimeout().toMilliseconds() : DEFAULT_LOCK_TIMEOUT.toMilliseconds();
  }

  public TimeInterval getLockTimeout() {
    return lockTimeout;
  }

  /**
   * Set the interval when trying to lock to re-submit a message.
   *
   * @param interval the interval; default is 1 second if not explicitly configured.
   *
   */
  public void setLockTimeout(TimeInterval interval) {
    lockTimeout = interval;
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

  private void failAllMessages() {
    try {
      lock.acquire();
    }
    catch (InterruptedException e) {
      log.trace("Failed to shutdown component cleanly, logging exception for informational purposes only", e);
    }
    Iterator i = retryList.iterator();
    while (i.hasNext()) {
      AdaptrisMessage msg = (AdaptrisMessage) i.next();
      failMessage(msg);
    }
    lock.release();
  }

  private void failMessage(AdaptrisMessage msg) {
    log.error("Message [" + msg.getUniqueId() + "] deemed to have failed");
    if (msg.getObjectHeaders().containsKey(CoreConstants.OBJ_METADATA_EXCEPTION)) {
      Exception e = (Exception) msg.getObjectHeaders().get(CoreConstants.OBJ_METADATA_EXCEPTION);
      log.error(e.getMessage(), e);
    }
    super.handleProcessingException(msg);
  }

  protected void scheduleNextRun(AdaptrisMessage msg) {
    log.trace("Message [" + msg.getUniqueId() + "] should be retried");
    retryTimer.schedule(new RetryThread(msg), retryIntervalMs());
  }

  /**
   * Private thread used to resubmit messages.
   *
   */
  protected class RetryThread extends TimerTask {
    private AdaptrisMessage msg;

    RetryThread(AdaptrisMessage m) {
      msg = m;
      retryList.add(msg);
      // try {
      // msg = (AdaptrisMessage) m.clone();
      // }
      // catch (Exception e) {
      // log.trace("AdaptrisMessage not cloneable, using original");
      // msg = m;
      // }
    }

    @Override
    public void run() {
      String oldName = Thread.currentThread().getName();
      Thread.currentThread().setName(toString());
      try {
        if (lock.attempt(lockTimeoutMs())) {
          retryList.remove(msg);
          inProgress.add(msg);
        }
        else {
          log.trace("Failed to acquire lock, rescheduling");
          retryTimer.schedule(this, retryIntervalMs());
          return;
        }

        log.trace("Retrying message [" + msg.getUniqueId() + "]");
        Workflow workflow = registeredWorkflows().get(msg.getMetadataValue(Workflow.WORKFLOW_ID_KEY));
        if (workflow != null) {
          log.trace("Retrying message [" + msg.getUniqueId() + "] in workflow [" + workflow.obtainWorkflowId() + "]");
          workflow.onAdaptrisMessage(msg);
          inProgress.remove(msg);
        }
        else {
          log.warn("Workflow [" + msg.getMetadataValue(Workflow.WORKFLOW_ID_KEY) + "] not registered, failing message");
          log.warn("Registered Workflows :" + registeredWorkflows().keySet());
          failMessage(msg);
          inProgress.remove(msg);
        }
      }
      catch (InterruptedException e) {
        // We were interrupted, while waiting for a lock.
        retryTimer.schedule(this, retryIntervalMs());
        return;
      }
      finally {
        lock.release();
        Thread.currentThread().setName(oldName);
      }
    }

    public String toString() {
      return "RetryMessageErrorHandler#RetryThread";
    }
  }

}
