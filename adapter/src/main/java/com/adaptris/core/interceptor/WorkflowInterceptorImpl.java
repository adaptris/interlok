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

package com.adaptris.core.interceptor;

import static com.adaptris.core.CoreConstants.UNIQUE_ID_JMX_PATTERN;

import javax.validation.constraints.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.adaptris.core.AdaptrisComponentImp;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.Channel;
import com.adaptris.core.ClosedState;
import com.adaptris.core.ComponentState;
import com.adaptris.core.CoreConstants;
import com.adaptris.core.CoreException;
import com.adaptris.core.StateManagedComponent;
import com.adaptris.core.Workflow;
import com.adaptris.core.WorkflowInterceptor;
import com.adaptris.util.GuidGenerator;

/**
 * Abstract WorkflowInterceptor implementation.
 * 
 * @author amcgrath
 */
public abstract class WorkflowInterceptorImpl extends AdaptrisComponentImp implements WorkflowInterceptor, StateManagedComponent {

  @Pattern(regexp = UNIQUE_ID_JMX_PATTERN)
  private String uniqueId;
  private transient ComponentState state;
  private transient Channel parentChannel;
  private transient Workflow parentWorkflow;

  protected transient Logger log = LoggerFactory.getLogger(this.getClass());

  protected WorkflowInterceptorImpl() {
    state = ClosedState.getInstance();
    setUniqueId(new GuidGenerator().getUUID());
  }

  public void setUniqueId(String uniqueId) {
    this.uniqueId = uniqueId;
  }

  /**
   * Register the parent channel for this WorkflowInterceptor.
   *
   * @param c the channel
   */
  @Override
  public void registerParentChannel(Channel c) {
    parentChannel = c;
  }

  /**
   * Register the parent workflow for this WorkflowInterceptor.
   *
   * @param w the workflow.
   */
  @Override
  public void registerParentWorkflow(Workflow w) {
    parentWorkflow = w;
  }

  protected Channel parentChannel() {
    return parentChannel;
  }

  protected Workflow parentWorkflow() {
    return parentWorkflow;
  }

  /**
   * <p>
   * Returns the configured unique ID for this object.
   * </p>
   * @return the configured unique ID for this object
   */
  public String getUniqueId() {
    return uniqueId;
  }

  public void changeState(ComponentState s) {
    state = s;
  }

  /** @see com.adaptris.core.StateManagedComponent#requestInit() */
  public void requestInit() throws CoreException {
    state.requestInit(this);
  }

  /** @see com.adaptris.core.StateManagedComponent#requestStart() */
  public void requestStart() throws CoreException {
    state.requestStart(this);
  }

  /** @see com.adaptris.core.StateManagedComponent#requestStop() */
  public void requestStop() {
    state.requestStop(this);
  }

  /** @see com.adaptris.core.StateManagedComponent#requestClose() */
  public void requestClose() {
    state.requestClose(this);
  }

  public ComponentState retrieveComponentState() {
    return state;
  }

  @Override
  public void prepare() throws CoreException {
  }

  protected static boolean wasSuccessful(AdaptrisMessage... msgs) {
    boolean result = true;
    for (AdaptrisMessage msg : msgs) {
      if (msg.getObjectHeaders().containsKey(CoreConstants.OBJ_METADATA_EXCEPTION)) {
        result = false;
        break;
      }
    }
    return result;
  }

  protected static class WorkflowInterceptorException extends RuntimeException {
    private static final long serialVersionUID = 2018080301L;

    public WorkflowInterceptorException(Throwable cause) {
      super(cause);
    }

  }
}
