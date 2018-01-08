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

import com.adaptris.core.AllowsRetriesConnection;
import com.adaptris.core.CoreException;

/**
 * <p>
 * For testing.
 * </p>
 */
public class MockAllowsRetriesConnection extends AllowsRetriesConnection {
  private int startCount = 0, initCount = 0, stopCount = 0, closeCount = 0;

  public MockAllowsRetriesConnection() {
  }

  public MockAllowsRetriesConnection(int maxRetries) {
    this();
    setConnectionAttempts(maxRetries);
  }


  @Override
  protected void initConnection() throws CoreException {
    initCount++;
  }

  @Override
  protected void startConnection() throws CoreException {
    startCount++;
  }

  @Override
  protected void stopConnection() {
    stopCount++;
  }

  @Override
  protected void closeConnection() {
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

  @Override
  protected void prepareConnection() throws CoreException {
  }
}
