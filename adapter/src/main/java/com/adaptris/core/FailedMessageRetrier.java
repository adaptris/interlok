package com.adaptris.core;

import java.util.Collection;

/**
 * <p>
 * Component which consumes {@linkplain AdaptrisMessage}s and, based on message
 * metadata, resubmits them to the {@linkplain Workflow} which processed them
 * originally.
 * </p>
 */
public interface FailedMessageRetrier extends AdaptrisComponent, AdaptrisMessageListener {

  /**
   * Add a {@linkplain Workflow} to the internal register of workflows
   * <p>
   * Add a {@linkplain Workflow} to the internal store. If the generated key is
   * not unique a{@linkplain CoreException} is thrown.
   * </p>
   * 
   * @param workflow the workflow to add
   * @throws CoreException if it is considered a duplicate
   */
  void addWorkflow(Workflow workflow) throws CoreException;

  /**
   * Clear the internal store of workflows.
   */
  void clearWorkflows();

  /**
   * Return the list of workflow-ids registered.
   *
   * @return collection containing a list of all the workflow unique-ids.
   */
  Collection<String> registeredWorkflowIds();
}
