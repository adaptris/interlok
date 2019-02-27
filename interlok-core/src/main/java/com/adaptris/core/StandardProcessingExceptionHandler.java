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

import java.util.HashMap;
import java.util.Map;
import javax.validation.Valid;
import com.adaptris.annotation.AdapterComponent;
import com.adaptris.annotation.ComponentProfile;
import com.adaptris.core.util.LifecycleHelper;
import com.adaptris.core.util.LoggingHelper;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * {@linkplain ProcessingExceptionHandler} implementation that contains a single service to apply when a message fails.
 * 
 * @config standard-processing-exception-handler
 * @see com.adaptris.core.Service
 * @see com.adaptris.core.ServiceList
 */
@XStreamAlias("standard-processing-exception-handler")
@AdapterComponent
@ComponentProfile(summary = "A configurable exception handling instance", tag = "error-handling,base")
public class StandardProcessingExceptionHandler extends RootProcessingExceptionHandler implements EventHandlerAware {

  @Valid
  private Service processingExceptionService;
  private transient Map<String, Workflow> workflows;
  private transient EventHandler eventHandler;

  /**
   * <p>
   * Creates a new instance. Defaults to null implementations.
   * </p>
   */
  public StandardProcessingExceptionHandler() {
    super();
    workflows = new HashMap<String, Workflow>();
  }

  public StandardProcessingExceptionHandler(ServiceList service) {
    this();
    setProcessingExceptionService(service);
  }

  public StandardProcessingExceptionHandler(Service... services) {
    this();
    setProcessingExceptionService(new ServiceList(services));
  }

  /**
   * <p>
   * Handles error messages from Workflows by sending them to the configured
   * error producer. The best practice would be for the service to write the
   * message to the filesystem, which is pretty unlikely to fail.
   * </p>
   *
   * @param msg the <code>AdaptrisMessage</code> to handle
   */
  @Override
  public synchronized void handleProcessingException(AdaptrisMessage msg) {
    try {
      if (getProcessingExceptionService() != null) {
        getProcessingExceptionService().doService(msg);
      }
    }
    catch (Exception e) {
      logErrorMessage(getProcessingExceptionService(), msg);
      log.error("Exception handling error msg [{}]",
          MessageLoggerImpl.LAST_RESORT_LOGGER.toString(msg), e);
    }
    notifyParent(msg);
  }

  @Override
  public void init() throws CoreException {
    LifecycleHelper.registerEventHandler(getProcessingExceptionService(), eventHandler);
    LifecycleHelper.init(getProcessingExceptionService());
  }

  @Override
  public void start() throws CoreException {
    LifecycleHelper.start(getProcessingExceptionService());
  }

  @Override
  public void stop() {
    LifecycleHelper.stop(getProcessingExceptionService());
  }

  @Override
  public synchronized void close() {
    LifecycleHelper.close(getProcessingExceptionService());
  }

  @Override
  public void prepare() throws CoreException {
    if (getProcessingExceptionService() != null) {
      getProcessingExceptionService().prepare();
    }
  }


  protected void logErrorMessage(Service p, AdaptrisMessage m) {
    log.error("{} failed to handle {}", LoggingHelper.friendlyName(p), m.getUniqueId());
  }

  /**
   * Get the service(s) that will be applied.
   *
   * @return the service
   */
  public Service getProcessingExceptionService() {
    return processingExceptionService;
  }

  /**
   * Set the service(s) that will be applied.
   *
   * @param s the service
   */
  public void setProcessingExceptionService(Service s) {
    processingExceptionService = s;
  }

  /**
   * Register a workflow against this error handler.
   *
   * @param w the workflow to register.
   */
  @Override
  public void registerWorkflow(Workflow w) {
    workflows.put(w.obtainWorkflowId(), w);
  }

  /**
   * Get the map of registered workflows.
   * <p>
   * The workflows are keyed against their unique-id.
   *
   * @return the internal map of registered workflows.
   */
  protected Map<String, Workflow> registeredWorkflows() {
    return workflows;
  }

  @Override
  public boolean hasConfiguredBehaviour() {
    // If someone has gone to the bother of configuring a service, then let's "presume" it does something.
    return getProcessingExceptionService() != null ? true : false;
  }

  @Override
  public void registerEventHandler(EventHandler eh) {
    eventHandler = eh;
  }
}
