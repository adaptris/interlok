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
import com.adaptris.core.ClosedState;
import com.adaptris.core.ComponentState;
import com.adaptris.core.CoreException;
import com.adaptris.core.InitialisedState;
import com.adaptris.core.StartedState;
import com.adaptris.core.StoppedState;
import com.adaptris.core.util.ExceptionHelper;
import com.adaptris.util.license.License;

public class MockFailingConnection extends AllowsRetriesConnection {

  private enum ComponentStateString {

    Close(ClosedState.getInstance()), Init(InitialisedState.getInstance()), Start(StartedState.getInstance()), Stop(StoppedState
        .getInstance());
    private ComponentState actualState;

    private ComponentStateString(ComponentState actual) {
      actualState = actual;
    }

    public ComponentState toState() {
      return actualState;
    }
  }

  private enum WhenToFail {
    // No exceptions thrown during stop/close, so no point failing
    OnInit(InitialisedState.getInstance()), OnStart(StartedState.getInstance());

    private ComponentState failureState;

    private WhenToFail(ComponentState failureState) {
      this.failureState = failureState;
    }

    public void maybeFail(ComponentState when) throws CoreException {
      if (failureState.equals(when)) {
        throw new CoreException("Configured to fail when transitioning to " + when);
      }
    }
  }

  private transient int startCount = 0, initCount = 0, stopCount = 0, closeCount = 0;

  private String failureState;

  public MockFailingConnection() {
    super();
  }

  public MockFailingConnection(String uniqueId) {
    setUniqueId(uniqueId);
  }

  public MockFailingConnection(String uniqueId, String whenToFail) {
    this(uniqueId);
    failureState = whenToFail;
  }

  @Override
  protected void initConnection() throws CoreException {
    execute(WhenToFail.OnInit);
    initCount++;
  }

  @Override
  protected void startConnection() throws CoreException {
    execute(WhenToFail.OnStart);
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

  public boolean isEnabled(License license) throws CoreException {
    return true;
  }

  private void execute(WhenToFail wtf) throws CoreException {

    int attemptCount = 0;
    boolean executed = false;
    try {
      while (executed == false) {
        try {
          attemptCount++;
          wtf.maybeFail(ComponentStateString.valueOf(getFailureState()).toState());
          executed = true;
        }
        catch (Exception e) {
          if (connectionAttempts() != -1 && attemptCount >= connectionAttempts()) {
            ExceptionHelper.rethrowCoreException(e);
          }
          else {
            Thread.sleep(connectionRetryInterval());
            continue;
          }
        }
      }
    }
    catch (InterruptedException e) {
      ExceptionHelper.rethrowCoreException(e);
    }
  }

  public String getFailureState() {
    return failureState;
  }

  public void setFailureState(String s) {
    this.failureState = s;
  }
}
