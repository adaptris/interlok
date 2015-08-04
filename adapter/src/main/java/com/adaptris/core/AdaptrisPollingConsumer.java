package com.adaptris.core;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import com.adaptris.annotation.AdvancedConfig;
import com.adaptris.annotation.AutoPopulated;
import com.adaptris.core.util.LifecycleHelper;
import com.adaptris.util.FifoMutexLock;

/**
 * <p>
 * Partial implementation of <code>AdaptrisMessageConsumer</code> which
 * uses an implementation of <code>Poller</code> to schedule message delivery,
 * and a <code>FifoMutexLock</code> for thread safety.
 * </p>
 */
public abstract class AdaptrisPollingConsumer
  extends AdaptrisMessageConsumerImp {

  @NotNull
  @AutoPopulated
  @Valid
  private Poller poller;
  @AdvancedConfig
  private Boolean reacquireLockBetweenMessages;
  // make logging from FML configurable (default false) when util is released

  // transient
  private transient FifoMutexLock lock;

  /**
   * <p>
   * Creates a new instance.  Default <code>Poller</code> is a
   * <code>FixedIntervalPoller</code>.
   * </p>
   */
  public AdaptrisPollingConsumer() {
    lock = new FifoMutexLock();
    setPoller(new FixedIntervalPoller());
  }

  /** @see com.adaptris.core.AdaptrisComponent#init() */
  @Override
  public void init() throws CoreException {
    if (getDestination().getDestination() == null) {
      throw new CoreException("ConsumeDestination destination is null");
    }
    if ("".equals(getDestination().getDestination())) {
      throw new CoreException("ConsumeDestination destination is \"\"");
    }
    poller.registerConsumer(this);
    LifecycleHelper.init(poller);
  }

  /** @see com.adaptris.core.AdaptrisComponent#start() */
  @Override
  public void start() throws CoreException {
    lock.release(); // required when moving from stopped
    LifecycleHelper.start(poller);
  }

  /** @see com.adaptris.core.AdaptrisComponent#stop() */
  @Override
  public void stop() {
    try {
      // log.trace("Releasing and acquiring lock...");
    	lock.release();

      lock.acquire();
      // log.trace("lock acquired");
      LifecycleHelper.stop(poller);
    }
    catch (InterruptedException e) {
      ;
    }

  }

  /** @see com.adaptris.core.AdaptrisComponent#close() */
  @Override
  public void close() {
    LifecycleHelper.close(poller);
    lock.release(); // lock is held when stopped
    // log.trace("lock released");
  }

  /**
   * Reacquire the lock after processing messages.
   * 
   * @return true if the lock was re-acquired, or if it was never necessary.
   * @see #getReacquireLockBetweenMessages()
   * @see #reacquireLock()
   */
  public final boolean continueProcessingMessages() {
    if (!reacquireLockBetweenMessages()) {
      return true;
    }
    return reacquireLock();
  }

  /**
   * <p>
   * Release the <code>lock</code> and acquire it again.
   * </p>
   * @return true if the lock is acquired.
   */
  final boolean reacquireLock() {
    boolean result = false;
    try {
      lock.release();
      result = lock.attempt(0L);
    }
    catch (InterruptedException e) {
      log.warn("ignoring InterruptedException [" + e.getMessage() + "]");
    }

    // log.debug("lock reacquired? [" + result + "]");

    return result;
  }

  /**
   * <p>
   * Attempt to obtain the lock.  Returns true if the lock is obtained,
   * otherwise false.
   * </p>
   */
  boolean attemptLock() {
    boolean result = false;
    try {
      result = lock.attempt(0L);
    }
    catch (InterruptedException e) {
       log.debug("InteruptedException while attempting to get the lock"); // do nothing
      ;
    }

    return result;
  }

  /**
   * <p>
   * Release the lock.  If this method is called and the lock is not held it
   * has no effect.
   * </p>
   */
  protected void releaseLock() {
    lock.release();
  }

  /**
   * <p>
   * Implemented by protocol-specific sub-classes.
   * </p>
   */
  protected abstract int processMessages();

  /** @see java.lang.Object#toString() */
  @Override
  public String toString() {
    StringBuffer result = new StringBuffer();
    result.append(super.toString());
    result.append(" reacquire lock between messages [");
    result.append(reacquireLockBetweenMessages);
    result.append("]");

    return result.toString();
  }

  // gets & sets...

  /**
   * <p>
   * Specify whether concrete sub-classes should attempt to reacquire the lock in between processing messages. Releasing then
   * attemtping to reqcquire the log gives other threads an opportunity to obtain the lock. This is significant in high volume
   * environments, particularly where messages are not processed in discreet batches e.g. <code>JmsPollingConsumer</code>.
   * </p>
   * 
   * @param b the lock flag
   * @see #reacquireLock()
   */
  public void setReacquireLockBetweenMessages(Boolean b) {
    reacquireLockBetweenMessages = b;
  }

  /**
   * <p>
   * Get the reacquire lock flag.
   * </p>
   * 
   * @return true if the lock should be reacquired between messages
   * @see #setReacquireLockBetweenMessages(Boolean)
   */
  public Boolean getReacquireLockBetweenMessages() {
    return reacquireLockBetweenMessages;
  }

  private boolean reacquireLockBetweenMessages() {
    return getReacquireLockBetweenMessages() != null ? getReacquireLockBetweenMessages().booleanValue() : false;
  }

  public Poller getPoller() {
    return poller;
  }

  /**
   * Set the {@link Poller} to use.
   * 
   * @param s the poller
   */
  public void setPoller(Poller s) {
    poller = s;
    poller.registerConsumer(this);
  }
}
