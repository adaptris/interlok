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

package com.adaptris.core.lifecycle;

import java.util.concurrent.TimeUnit;

import com.adaptris.core.Channel;
import com.adaptris.core.ClosedState;
import com.adaptris.core.CoreException;
import com.adaptris.core.InitialisedState;
import com.adaptris.core.WorkflowList;
import com.adaptris.util.TimeInterval;

public class WorkflowRetryAndFailTest extends WorkflowRetryAndContinueTest {

  public WorkflowRetryAndFailTest(java.lang.String testName) {
    super(testName);
  }

  @Override
  public void setUp() throws Exception {
    super.setUp();
  }

  @Override
  protected WorkflowRetryAndFail createStrategy() {
    return new WorkflowRetryAndFail();
  }

  @Override
  public void testFailedInitialise_ExceedsMax() throws Exception {
    WorkflowList wfl = new WorkflowList();
    WorkflowRetryAndFail strategy = new WorkflowRetryAndFail(2, new TimeInterval(10L, TimeUnit.MILLISECONDS.name()));
    FailInit workflow = new FailInit(3);
    wfl.add(workflow);
    wfl.setLifecycleStrategy(strategy);
    Channel c = createChannel(wfl);
    try {
      c.requestInit();
    }
    catch (CoreException expected) {

    }
    // That's right 1 + 2 retries.
    assertEquals(3, workflow.getAttempts());
    assertEquals(ClosedState.getInstance(), workflow.retrieveComponentState());
  }


  @Override
  public void testFailedStart_ExceedsMax() throws Exception {
    WorkflowList wfl = new WorkflowList();
    WorkflowRetryAndFail strategy = new WorkflowRetryAndFail(2, new TimeInterval(10L, TimeUnit.MILLISECONDS.name()));
    FailStart workflow = new FailStart(3);
    wfl.add(workflow);
    wfl.setLifecycleStrategy(strategy);
    Channel c = createChannel(wfl);
    c.requestInit();
    try {
      c.requestStart();
    }
    catch (CoreException expected) {

    }
    // That's right 1 + 2 retries.
    assertEquals(3, workflow.getAttempts());
    assertEquals(InitialisedState.getInstance(), workflow.retrieveComponentState());

  }

}
