package com.adaptris.core;

/**
 * Interface for intercepting messages as they enter and exit a workflow.
 *
 * @author lchan
 */
public interface WorkflowInterceptor extends AdaptrisComponent {

  /**
   * Mark the start of a workflow.
   *
   * @param inputMsg the message that will be processed by this workflow.
   */
  void workflowStart(AdaptrisMessage inputMsg);

  /**
   * Mark the end of a workflow.
   *
   * @param inputMsg the original message that was originally submitted to the workflow; in the event of an exception during
   *          processing, the exception will be stored in object metadata in the inputMsg
   * @param outputMsg the message contaning any changes that may have been applied by the services within the workflow.
   * @see CoreConstants#OBJ_METADATA_EXCEPTION
   */
  void workflowEnd(AdaptrisMessage inputMsg, AdaptrisMessage outputMsg);

  /**
   * Register the parent channel for this WorkflowInterceptor.
   *
   * @param c the channel
   */
  void registerParentChannel(Channel c);

  /**
   * Register the parent workflow for this WorkflowInterceptor.
   *
   * @param w the workflow.
   */
  void registerParentWorkflow(Workflow w);

  /**
   * Get the unique-id of this WorkflowInterceptor instance.
   *
   * @return the unique-id
   */
  String getUniqueId();
}
