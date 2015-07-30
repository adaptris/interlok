package com.adaptris.core;

import java.util.HashMap;
import java.util.Map;

import javax.validation.Valid;

import com.adaptris.core.util.LifecycleHelper;
import com.adaptris.util.license.License;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * {@linkplain ProcessingExceptionHandler} implementation that contains a single service to apply when a message fails.
 * 
 * @config standard-processing-exception-handler
 * @see Service
 * @see ServiceList
 */
@XStreamAlias("standard-processing-exception-handler")
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
      log.error("Exception handling error msg [" + msg.toString(true) + "]", e);
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
  public synchronized void stop() {
    LifecycleHelper.stop(getProcessingExceptionService());
  }

  @Override
  public synchronized void close() {
    LifecycleHelper.close(getProcessingExceptionService());
  }

  @Override
  public boolean isEnabled(License license) throws CoreException {
    return getProcessingExceptionService() != null ? getProcessingExceptionService().isEnabled(license) : true;
  }


  protected void logErrorMessage(Service p, AdaptrisMessage m) {
    String id = "".equals(p.getUniqueId()) ? p.getClass().getSimpleName() : p.getUniqueId();
    log.error(id + " failed to handle " + m.getUniqueId());
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
