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

import org.apache.commons.lang3.BooleanUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.adaptris.annotation.AdvancedConfig;
import com.adaptris.annotation.InputFieldDefault;
import com.adaptris.core.runtime.MessageErrorDigester;

/**
 * RootProcessingExceptionHandler which allows you to register a Digester for aggregating information about all the errors that have
 * occurred.
 * 
 * @author amcgrath
 * @author lchan
 */
public abstract class RootProcessingExceptionHandler implements ProcessingExceptionHandler, StateManagedComponent {

  protected transient Logger log = LoggerFactory.getLogger(this.getClass().getName());

  private transient ProcessingExceptionHandler parent;
  private transient MessageErrorDigester messageErrorDigester;

  private transient ComponentState state;
  private String uniqueId;
  @AdvancedConfig
  @InputFieldDefault(value = "false")
  private Boolean alwaysHandleException;

  public RootProcessingExceptionHandler() {
    state = ClosedState.getInstance();
  }

  @Override
  public void registerParent(ProcessingExceptionHandler handler) {
    parent = handler;
  }

  public ProcessingExceptionHandler getParent() {
    return parent;
  }

  @Override
  public void notifyParent(AdaptrisMessage message) {
    if (getParent() != null) {
      getParent().onChildError(message);
    }
    else {
      // Get the digester and add our stuff to it.
      addErrorToDigest(message);
    }
  }

  private void addErrorToDigest(AdaptrisMessage message) {
    if (retrieveDigester() != null) {
      retrieveDigester().digest(message);
    }
    else {
      log.warn("An error has occured and we have not configured a digester.");
    }
  }

  @Override
  public void onChildError(AdaptrisMessage message) {
    if (alwaysHandleException()) {
      // This should handle the notification of the parent.
      handleProcessingException(message);
    }
    else {
      notifyParent(message);
    }
  }

  public void requestInit() throws CoreException {
    state.requestInit(this);
  }

  public void requestStart() throws CoreException {
    state.requestStart(this);
  }

  public void requestStop() {
    state.requestStop(this);
  }

  public void requestClose() {
    state.requestClose(this);
  }

  public ComponentState retrieveComponentState() {
    return state;
  }

  public void changeState(ComponentState newState) {
    state = newState;
  }

  protected MessageErrorDigester retrieveDigester() {
    return messageErrorDigester;
  }

  public void registerDigester(MessageErrorDigester digest) {
    messageErrorDigester = digest;
  }

  public String getUniqueId() {
    return uniqueId;
  }

  public void setUniqueId(String uniqueId) {
    this.uniqueId = uniqueId;
  }

  public Boolean getAlwaysHandleException() {
    return alwaysHandleException;
  }

  /**
   * Always handle any exceptions in this error handler.
   * <p>
   * This is primarily useful if you have a chain of error handling (i.e you have configured an explicit error handling at one of
   * the workflow / channel levels); by setting this to be true, then you fire the error handling on each of the configured error
   * handlers in turn as they propogate back to the configured adapter error handler. This means that you can have a chain of error
   * handling behaviour like (narrowest first).
   * </p>
   * <ol>
   * <li>workflow-error-handler : send a HTTP 500 response.</li>
   * <li>adapter-error-handler : write to filesystem /opt/adaptris/bad; always-handle-exception=true</li>
   * </ol>
   * <p>
   * This will cause the adapter-error-handler to write the original message to {@code /opt/adaptris/bad}, as well as sending a 500
   * response back to the client.
   * </p>
   * <p>
   * Note that setting this to be true may have some behavioural oddities if you have chained {@link RetryMessageErrorHandler}
   * instances in configuration; The number of attempts that each configured {@code RetryMessageErrorHandler} will attempt to handle
   * a message will vary; the total number of attempts should not exceed the highest configured max-retry-count on any individual
   * {@code RetryMessageErrorHandler}
   * </p>
   * 
   * @param b true or false, default null (false).
   */
  public void setAlwaysHandleException(Boolean b) {
    this.alwaysHandleException = b;
  }

  boolean alwaysHandleException() {
    return BooleanUtils.toBooleanDefaultIfNull(getAlwaysHandleException(), false);
  }
}
