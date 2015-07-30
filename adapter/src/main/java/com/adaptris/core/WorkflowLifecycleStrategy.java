package com.adaptris.core;

import java.util.List;

/**
 * Strategy for handling workflow lifecycle within a {@link WorkflowList}.
 * 
 * @author lchan
 * @author $Author: lchan $
 */
public interface WorkflowLifecycleStrategy {

  /**
   * Start a list of workflows.
   *
   * @param workflowList a list of workflows that have already been initialised.
   * @throws CoreException wrapping any underlying exception.
   */
  void start(List<Workflow> workflowList) throws CoreException;

  /**
   * Initialise a list of channels.
   *
   * @param workflowList a list of workflows that require initialising.
   * @throws CoreException wrapping any underlying exception.
   */
  void init(List<Workflow> workflowList) throws CoreException;

  /**
   * Stop a list of workflows.
   *
   * @param workflowList a list of workflows that have previously been started.
   */
  void stop(List<Workflow> workflowList);

  /**
   * Close a list of workflows.
   *
   * @param workflowList a list of workflows.
   */
  void close(List<Workflow> workflowList);
}
