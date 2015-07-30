package com.adaptris.core;


/**
 * Standard interface for handling errors during processing of a message.
 *
 * @author lchan
 */
public interface ProcessingExceptionHandler extends AdaptrisComponent, ErrorHandlerRegistrar {

  /**
   * Handles a message that has deemed to have failed.
   */
  void handleProcessingException(AdaptrisMessage msg);

  /**
   * Register a workflow against this error handler.
   *
   * @param w the workflow to register.
   */
  void registerWorkflow(Workflow w);

  /**
   * Simply report back to the owning component whether or not this ProcessingExceptionHandler is actually going to do anything.
   *
   * @return true if there is actual concrete behaviour to this implementation.
   */
  boolean hasConfiguredBehaviour();

}
