package com.adaptris.util;

import junit.framework.TestCase;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class FifoMutexLockTest extends TestCase {

  protected transient Log log = LogFactory.getLog(this.getClass().getName());
  private FifoMutexLock lock;
  private int locksAcquired = 0;
  private int locksInterrupted = 0;
  public FifoMutexLockTest(String arg0) {
    super(arg0);
  }

  @Override
  protected void setUp() {
    lock = new FifoMutexLock(true);
  }

  public void testSingleThreaded() throws Exception {
    assertTrue(lock.permitAvailable() == true);
    assertTrue(lock.attempt(0L));
    assertTrue(lock.permitAvailable() == false);

    assertTrue(lock.attempt(0L) == false);
    assertTrue(lock.attempt(0L) == false);
    assertTrue(lock.permitAvailable() == false);

    lock.release();
    assertTrue(lock.permitAvailable() == true);

    assertTrue(lock.attempt(0L));
    assertTrue(lock.permitAvailable() == false);

    assertTrue(lock.attempt(0L) == false);
    assertTrue(lock.attempt(0L) == false);
    assertTrue(lock.permitAvailable() == false);

    lock.release();
    lock.release();
    assertTrue(lock.permitAvailable() == true);

    assertTrue(lock.attempt(0L));
    assertTrue(lock.permitAvailable() == false);

    assertTrue(lock.attempt(0L) == false);
    assertTrue(lock.attempt(0L) == false);
    assertTrue(lock.permitAvailable() == false);

    lock.release();
    assertTrue(lock.permitAvailable() == true);

    lock.acquire();
    assertTrue(lock.permitAvailable() == false);

    assertTrue(lock.attempt(0L) == false);
    assertTrue(lock.attempt(0L) == false);
    assertTrue(lock.permitAvailable() == false);

    lock.release();
    assertTrue(lock.permitAvailable() == true);
  }

  public void testMultiThreaded() throws Exception {
    int count = 10;
    for (int i = 0; i < count; i++) {
      new Thread() {
        @Override
        public void run() {
          try {
            lock.acquire();
            Thread.sleep(77);
            lock.release();
            locksAcquired++;
          }
          catch (InterruptedException e) {
            locksInterrupted++;
            lock.release();
          }
        }

      }.start();
    }
    while (locksAcquired < count) {
      Thread.sleep(78);
      if (locksAcquired + locksInterrupted >= count) {
        break;
      }
    }
    assertEquals(true, lock.attempt(0L));
    assertEquals(false, lock.permitAvailable());
    lock.release();
    assertEquals(true, lock.permitAvailable());
  }


  public void testLock() throws Exception {
    FifoMutexLock lock = new FifoMutexLock();

    // acquire the lock...
    lock.acquire();

    // check it is no longer available...
    assertTrue(!lock.permitAvailable());

    // check an attempt to get it fails...
    assertTrue(!lock.attempt(0L));

    // release it...
    lock.release();

    // check it is now available...
    assertTrue(lock.permitAvailable());

    // check it can be acquiredusing attempt...
    assertTrue(lock.attempt(0L));

    // release it
    lock.release();
  }
}

