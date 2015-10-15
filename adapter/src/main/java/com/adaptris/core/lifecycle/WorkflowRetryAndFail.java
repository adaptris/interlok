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

import com.adaptris.core.CoreException;
import com.adaptris.core.Workflow;
import com.adaptris.util.TimeInterval;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Strategy for starting workflows that retries a workflow init or start a number of times.
 * <p>
 * This implementation allows you to retry the init and start for a given workflow a configurable number of times. Each workflow is
 * attempted in sequence, and the failing workflow is retried until either it is successful or the max number of retry attempts has
 * been reached. If the workflow has still failed at this point, an exception is thrown back to the parent channel.
 * </p>
 * 
 * @config workflow-retry-and-fail
 * @author lchan
 * @author $Author: lchan $
 */
@XStreamAlias("workflow-retry-and-fail")
public class WorkflowRetryAndFail extends WorkflowRetryAndContinue {

  public WorkflowRetryAndFail() {

  }

  public WorkflowRetryAndFail(Integer max, TimeInterval interval) {
    this();
    setMaxRetries(max);
    setWaitBetweenRetries(interval);
  }

  @Override
  void execute(WorkflowAction action, Workflow wf) throws CoreException {
    int attempts = 0;
    while (true) {
      attempts++;
      try {
        action.execute(wf);
        break;
      }
      catch (CoreException e) {
        giveup(e, attempts);
        waitQuietly();
      }
    }
  }

  private void giveup(CoreException e, int attempts) throws CoreException {
    if (maxRetries() == INFINITE_RETRIES) {
      return;
    }
    if (attempts > maxRetries()) {
      throw e;
    }
  }
}
