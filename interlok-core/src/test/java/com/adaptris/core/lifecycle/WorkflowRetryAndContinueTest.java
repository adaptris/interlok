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
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import java.util.concurrent.TimeUnit;
import org.junit.Test;
import com.adaptris.core.Channel;
import com.adaptris.core.ClosedState;
import com.adaptris.core.CoreException;
import com.adaptris.core.InitialisedState;
import com.adaptris.core.StandardWorkflow;
import com.adaptris.core.StartedState;
import com.adaptris.core.WorkflowList;
import com.adaptris.util.TimeInterval;

public class WorkflowRetryAndContinueTest extends WorkflowLifecycleStrategyCase {

  @Override
  protected WorkflowRetryAndContinue createStrategy() {
    return new WorkflowRetryAndContinue();
  }

  @Test
  public void testSetMaxRetries() {
    WorkflowRetryAndContinue strategy = createStrategy();
    assertNull(strategy.getMaxRetries());
    assertEquals(WorkflowRetryAndFail.DEFAULT_MAX_RETRIES, strategy.maxRetries());
    strategy.setMaxRetries(10);
    assertEquals(Integer.valueOf(10), strategy.getMaxRetries());
    assertEquals(10, strategy.maxRetries());
    strategy.setMaxRetries(null);
    assertEquals(WorkflowRetryAndFail.DEFAULT_MAX_RETRIES, strategy.maxRetries());
  }

  @Test
  public void testSetTimeInterval() {
    WorkflowRetryAndContinue strategy = createStrategy();
    assertNull(strategy.getWaitBetweenRetries());
    assertEquals(10000, strategy.waitInterval());
    TimeInterval ti = new TimeInterval(10000L, TimeUnit.NANOSECONDS.name());
    strategy.setWaitBetweenRetries(ti);
    assertEquals(ti, strategy.getWaitBetweenRetries());
    assertEquals(TimeUnit.NANOSECONDS.toMillis(10000L), strategy.waitInterval());
  }

  @Test
  public void testFailedInitialise_Retries() throws Exception {
    WorkflowList wfl = new WorkflowList();
    WorkflowRetryAndContinue strategy = createStrategy();
    strategy.setMaxRetries(10);
    strategy.setWaitBetweenRetries(new TimeInterval(10L, TimeUnit.MILLISECONDS.name()));
    FailInit workflow = new FailInit(3);
    wfl.add(workflow);
    wfl.setLifecycleStrategy(strategy);
    Channel c = createChannel(wfl);
    c.requestInit();
    assertEquals(4, workflow.getAttempts());
    assertEquals(InitialisedState.getInstance(), workflow.retrieveComponentState());

  }

  @Test
  public void testFailedInitialise_Retries_Infinite() throws Exception {
    WorkflowList wfl = new WorkflowList();
    WorkflowRetryAndContinue strategy = createStrategy();
    strategy.setMaxRetries(-1);
    strategy.setWaitBetweenRetries(new TimeInterval(10L, TimeUnit.MILLISECONDS.name()));
    FailInit workflow = new FailInit(3);
    wfl.add(workflow);
    wfl.setLifecycleStrategy(strategy);
    Channel c = createChannel(wfl);
    c.requestInit();
    assertEquals(4, workflow.getAttempts());
    assertEquals(InitialisedState.getInstance(), workflow.retrieveComponentState());

  }

  @Test
  public void testFailedInitialise_ExceedsMax() throws Exception {
    WorkflowList wfl = new WorkflowList();
    WorkflowRetryAndContinue strategy = createStrategy();
    strategy.setMaxRetries(2);
    strategy.setWaitBetweenRetries(new TimeInterval(10L, TimeUnit.MILLISECONDS.name()));
    FailInit workflow = new FailInit(3);
    wfl.add(workflow);
    wfl.setLifecycleStrategy(strategy);
    Channel c = createChannel(wfl);
    c.requestInit();
    assertEquals(ClosedState.getInstance(), workflow.retrieveComponentState());
  }

  @Test
  public void testFailedStart_Retries() throws Exception {
    WorkflowList wfl = new WorkflowList();
    WorkflowRetryAndContinue strategy = createStrategy();
    strategy.setMaxRetries(10);
    strategy.setWaitBetweenRetries(new TimeInterval(10L, TimeUnit.MILLISECONDS.name()));
    FailStart workflow = new FailStart(3);
    wfl.add(workflow);
    wfl.setLifecycleStrategy(strategy);
    Channel c = createChannel(wfl);
    c.requestStart();
    assertEquals(4, workflow.getAttempts());
    assertEquals(StartedState.getInstance(), workflow.retrieveComponentState());

  }

  @Test
  public void testFailedStart_Retries_Infinite() throws Exception {
    WorkflowList wfl = new WorkflowList();
    WorkflowRetryAndContinue strategy = createStrategy();
    strategy.setMaxRetries(-1);
    strategy.setWaitBetweenRetries(new TimeInterval(10L, TimeUnit.MILLISECONDS.name()));
    FailStart workflow = new FailStart(3);
    wfl.add(workflow);
    wfl.setLifecycleStrategy(strategy);
    Channel c = createChannel(wfl);
    c.requestStart();
    assertEquals(4, workflow.getAttempts());
    assertEquals(StartedState.getInstance(), workflow.retrieveComponentState());

  }

  @Test
  public void testFailedStart_ExceedsMax() throws Exception {
    WorkflowList wfl = new WorkflowList();
    WorkflowRetryAndContinue strategy = createStrategy();
    strategy.setMaxRetries(2);
    strategy.setWaitBetweenRetries(new TimeInterval(10L, TimeUnit.MILLISECONDS.name()));
    FailStart workflow = new FailStart(3);
    wfl.add(workflow);
    wfl.setLifecycleStrategy(strategy);
    Channel c = createChannel(wfl);
    c.requestStart();
    assertEquals(InitialisedState.getInstance(), workflow.retrieveComponentState());
  }

  protected class FailInit extends StandardWorkflow {
    private int successMarker;
    private int attempts = 0;

    public FailInit(int count) {
      successMarker = count;
    }

    @Override
    public void initialiseWorkflow() throws CoreException {
      if (attempts > successMarker) {
        super.startWorkflow();
      }
      else {
        throw new CoreException("Attempt " + attempts++);
      }
    }

    int getAttempts() {
      return attempts;
    }

  }

  protected class FailStart extends StandardWorkflow {
    private int successMarker;
    private int attempts = 0;

    public FailStart(int count) {
      successMarker = count;
    }

    @Override
    public void startWorkflow() throws CoreException {
      if (attempts > successMarker) {
        super.startWorkflow();
      }
      else {
        throw new CoreException("Attempt " + attempts++);
      }
    }

    int getAttempts() {
      return attempts;
    }
  }
}
