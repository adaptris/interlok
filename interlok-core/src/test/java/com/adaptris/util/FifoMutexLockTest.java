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

package com.adaptris.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import junit.framework.TestCase;

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

