package com.adaptris.core;

import com.adaptris.util.license.License;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Null implementation of Processing Exceptions.
 * 
 * @config null-processing-exception-handler
 * 
 * @author lchan
 * 
 */
@XStreamAlias("null-processing-exception-handler")
public class NullProcessingExceptionHandler extends RootProcessingExceptionHandler {

  public NullProcessingExceptionHandler() {
    super();
  }

  public void handleProcessingException(AdaptrisMessage msg) {
    notifyParent(msg);
  }

  public void registerWorkflow(Workflow w) {
  }

  @Override
  public void init() throws CoreException {
  }

  @Override
  public void start() throws CoreException {
  }

  @Override
  public synchronized void stop() {
  }

  @Override
  public synchronized void close() {
  }

  public boolean isEnabled(License license) throws CoreException {
    return true;
  }

  public boolean hasConfiguredBehaviour() {
    return false;
  }

}
