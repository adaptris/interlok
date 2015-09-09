package com.adaptris.util;

import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Wraps an instance of {@link java.util.concurrent.Semaphore} and adds
 * 'mut-ex' behaviour.
 * <p>
 * The problem with Semaphore is that if you create a new instance with 1 permit
 * (i.e. a mutex) then call release, the number of permits goes up to 2.
 * </p>
 */
public final class FifoMutexLock {

  private transient Logger log = LoggerFactory.getLogger(this.getClass());

  private Semaphore lock;
  private boolean enableDebugLogging;

  /**
   * <p>
   * Creates a new instance. Initialises underlying <code>FIFOSemaphore</code>
   * with a single permit.
   * </p>
   */
  public FifoMutexLock() {
    this(false);
  }

  /**
   * <p>
   * Creates a new instance.
   * </p>
   *
   * @param b if true debug logging will be enabled
   */
  public FifoMutexLock(boolean b) {
    enableDebugLogging = b;
    lock = new Semaphore(1, true);
  }

  /**
   * <p>
   * Wraps call to {@link Semaphore#acquire()}.
   * </p>
   *
   * @see java.util.concurrent.Semaphore
   * @throws InterruptedException if the lock is interrupted.
   */
  public void acquire() throws InterruptedException {
    log("blocking for lock...");
    lock.acquire();
    log("lock acquired");
  }

  /**
   * <p>
   * Wraps call to {@link Semaphore#tryAcquire(long, TimeUnit)}.
   * </p>
   *
   * @see java.util.concurrent.Semaphore
   * @param ms the amount of time in milliseconds
   * @return true if the lock as successful.
   * @throws InterruptedException if the lock is interrupted.
   */
  public boolean attempt(long ms) throws InterruptedException {
    boolean result = lock.tryAcquire(ms, TimeUnit.MILLISECONDS);

    if (result) {
      log("attempt to get lock successful");
    }
    else {
      log("attempt to get lock failed");
    }

    return result;
  }

  /**
   * <p>
   * Calls <code>release</code> on the underlying lock <b>if a oermit is
   * available to release</b>, otherwise logs a message indicating that releases
   * and acquires are not balanced.
   * </p>
   */
  public void release() {
    if (lock.availablePermits() == 0) {
      lock.release();
      log("permit released");
    }
    else {
      log("ignoring release where there are no permits to release");
    }
  }

  /**
   * <p>
   * Returns true if the permit is currently avaialable.
   * </p>
   *
   * @return true if the permit is currently avaialable
   */
  public boolean permitAvailable() {
    boolean result = false;

    if (lock.availablePermits() == 1) {
      result = true;
    }

    return result;
  }

  private void log(String logMessage) {
    if (enableDebugLogging) {
      log.trace(logMessage);
    }
  }
}
