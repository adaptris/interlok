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

package com.adaptris.core.stubs;

import com.adaptris.core.AdaptrisConnectionImp;
import com.adaptris.core.CoreException;
import com.adaptris.core.StateManagedComponent;
import com.adaptris.util.license.License;

/**
 * <p>
 * For testing.
 * </p>
 */
public class MockConnection extends AdaptrisConnectionImp implements StateManagedComponent {

  private transient long sleepTime = -1;
  private int startCount = 0, initCount = 0, stopCount = 0, closeCount = 0;

  public MockConnection() {
  }

  public MockConnection(long sleepTime) {
    this.sleepTime = sleepTime;
  }

  public MockConnection(String uniqueId) {
    this();
    setUniqueId(uniqueId);
  }

  public MockConnection(String uniqueId, long sleepTime) {
    this(uniqueId);
    this.sleepTime = sleepTime;
  }

  @Override
  protected void initConnection() throws CoreException {
    waitQuietly();
    initCount++;
  }

  @Override
  protected void startConnection() throws CoreException {
    waitQuietly();
    startCount++;
  }

  @Override
  protected void stopConnection() {
    waitQuietly();
    stopCount++;
  }

  @Override
  protected void closeConnection() {
    waitQuietly();
    closeCount++;
  }


  public int getStartCount() {
    return startCount;
  }

  public int getInitCount() {
    return initCount;
  }

  public int getStopCount() {
    return stopCount;
  }

  public int getCloseCount() {
    return closeCount;
  }

  /**
   * @see com.adaptris.core.AdaptrisComponent#isEnabled
   *      (com.adaptris.util.license.License)
   */
  public boolean isEnabled(License license) throws CoreException {
    return true;
  }

  private void waitQuietly() {
    if (sleepTime > 0) {
      try {
        Thread.sleep(sleepTime);
      }
      catch (InterruptedException e) {

      }
    }
  }
}
