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

package com.adaptris.core;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import lombok.Getter;
import lombok.Setter;

/**
 * <p>
 * Component which consumes <code>AdaptrisMessage</code>s and, based on message metadata, resubmits them to the
 * <code>Workflow</code> which processed them originally.
 * </p>
 */
public abstract class FailedMessageRetrierImp implements FailedMessageRetrier {

  protected transient Logger log = LoggerFactory.getLogger(this.getClass().getName());

  private transient Map<String, Workflow> workflows;
  @Getter
  @Setter
  private String uniqueId;

  /**
   * <p>
   * Creates a new instance. Defaults to new empty <code>StandaloneConsumer</code>, which will do nothing.
   * </p>
   */
  public FailedMessageRetrierImp() {
    workflows = Collections.synchronizedMap(new HashMap<String, Workflow>());
  }

  protected Workflow getWorkflow(AdaptrisMessage msg) throws CoreException {
    Workflow workflow = getWorkflows().get(msg.getMetadataValue(Workflow.WORKFLOW_ID_KEY));
    if (workflow == null) {
      throw new CoreException("No Workflow [" + msg.getMetadataValue(Workflow.WORKFLOW_ID_KEY) + "] found");
    }
    if (!StartedState.getInstance().equals(workflow.retrieveComponentState())) {
      throw new CoreException("Workflow [" + workflow.obtainWorkflowId() + "] is not started.");
    }
    return workflow;
  }

  protected void updateRetryCountMetadata(AdaptrisMessage msg) {
    String countString = msg.getMetadataValue(CoreConstants.RETRY_COUNT_KEY);
    int count;

    if (countString != null) {
      try {
        count = Integer.parseInt(countString) + 1;
      }
      catch (NumberFormatException e) {
        log.warn("illegal retry count metadata [{}] resetting count to 1", countString);
        count = 1;
      }
    }
    else {
      count = 1;
    }
    msg.addMetadata(CoreConstants.RETRY_COUNT_KEY, String.valueOf(count));
  }

  @Override
  public void addWorkflow(Workflow workflow) throws CoreException {
    String key = workflow.obtainWorkflowId();
    if (getWorkflows().keySet().contains(key)) {
      log.warn("duplicate workflow ID [" + key + "]");
      throw new CoreException("Workflows cannot be uniquely identified");
    }
    log.debug("adding workflow with key [{}]", key);
    getWorkflows().put(key, workflow);
  }

  @Override
  public void clearWorkflows() {
    getWorkflows().clear();
  }

  @Override
  public Collection<String> registeredWorkflowIds() {
    return new ArrayList<String>(workflows.keySet());
  }

  protected Map<String, Workflow> getWorkflows() {
    return workflows;
  }

}
