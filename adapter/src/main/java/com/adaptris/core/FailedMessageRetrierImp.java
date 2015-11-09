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

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.adaptris.annotation.AutoPopulated;
import com.adaptris.core.util.Args;
import com.adaptris.core.util.LifecycleHelper;

/**
 * <p>
 * Component which consumes <code>AdaptrisMessage</code>s and, based on message metadata, resubmits them to the
 * <code>Workflow</code> which processed them originally.
 * </p>
 */
public abstract class FailedMessageRetrierImp implements FailedMessageRetrier {

  protected transient Logger log = LoggerFactory.getLogger(this.getClass().getName());

  @NotNull
  @Valid
  @AutoPopulated
  private StandaloneConsumer standaloneConsumer;
  private transient Map<String, Workflow> workflows;
  private String uniqueId;

  /**
   * <p>
   * Creates a new instance. Defaults to new empty <code>StandaloneConsumer</code>, which will do nothing.
   * </p>
   */
  public FailedMessageRetrierImp() {
    workflows = Collections.synchronizedMap(new HashMap<String, Workflow>());
    setStandaloneConsumer(new StandaloneConsumer());
  }

  /**
   * <p>
   * This method is <code>synchronized</code> in case client code is multi-threaded.
   * </p>
   * 
   * @see com.adaptris.core.AdaptrisMessageListener #onAdaptrisMessage(com.adaptris.core.AdaptrisMessage)
   */
  @Override
  public synchronized void onAdaptrisMessage(AdaptrisMessage msg) {
    try {
      Workflow workflow = getWorkflow(msg);
      updateRetryCountMetadata(msg);
      workflow.onAdaptrisMessage(msg); // workflow.onAM is sync'd...
    }
    catch (Exception e) { // inc. runtime, exc. Workflow
      log.error("exception retrying message", e);
      log.error("message " + msg.toString(true));
    }
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
        count = new Integer(countString).intValue() + 1;
      }
      catch (NumberFormatException e) {
        log.warn("illegal retry count metadata [" + countString + "] resetting count to 1");
        count = 1;
      }
    }
    else {
      count = 1;
    }
    msg.addMetadata(CoreConstants.RETRY_COUNT_KEY, new Integer(count).toString());
  }

  /** @see com.adaptris.core.AdaptrisComponent#init() */
  @Override
  public void init() throws CoreException {
    if (standaloneConsumer != null) {
      standaloneConsumer.registerAdaptrisMessageListener(this);
    }
    LifecycleHelper.init(standaloneConsumer);
  }

  /** @see com.adaptris.core.AdaptrisComponent#start() */
  @Override
  public void start() throws CoreException {
    LifecycleHelper.start(standaloneConsumer);
  }

  /** @see com.adaptris.core.AdaptrisComponent#stop() */
  @Override
  public void stop() {
    LifecycleHelper.stop(standaloneConsumer);
  }

  /** @see com.adaptris.core.AdaptrisComponent#close() */
  @Override
  public void close() {
    LifecycleHelper.close(standaloneConsumer);
  }


  /**
   * Add a <code>Workflow</code>.
   * <p>
   * Add a <code>Workflow</code> to the internal store. If the generated key is not unique a <code>CoreException</code> can be
   * thrown.
   * </p>
   * 
   * @param workflow the workflow to add
   * @throws CoreException if it is considered a duplicate
   */
  @Override
  public abstract void addWorkflow(Workflow workflow) throws CoreException;

  @Override
  public void clearWorkflows() {
    getWorkflows().clear();
  }

  @Override
  public Collection<String> registeredWorkflowIds() {
    return new ArrayList<String>(workflows.keySet());
  }

  /**
   * <p>
   * Returns the <code>StandaloneConsumer</code> to use.
   * </p>
   * 
   * @return the <code>StandaloneConsumer</code> to use
   */
  public StandaloneConsumer getStandaloneConsumer() {
    return standaloneConsumer;
  }

  /**
   * <p>
   * Sets the <code>StandaloneConsumer</code> to use. May not be null. Sets <code>this</code> as the consumer's
   * <code>AdaptrisMessageListener</code>.
   * </p>
   * 
   * @param consumer the <code>StandaloneConsumer</code> to use
   */
  public void setStandaloneConsumer(StandaloneConsumer consumer) {
    standaloneConsumer = Args.notNull(consumer, "consumer");
    standaloneConsumer.registerAdaptrisMessageListener(this);
  }

  protected Map<String, Workflow> getWorkflows() {
    return workflows;
  }

  /**
   * @return the uniqueId
   */
  public String getUniqueId() {
    return uniqueId;
  }

  /**
   * @param uniqueId the uniqueId to set
   */
  public void setUniqueId(String uniqueId) {
    this.uniqueId = uniqueId;
  }

  @Override
  public void prepare() throws CoreException {
    getStandaloneConsumer().prepare();
  }


}
