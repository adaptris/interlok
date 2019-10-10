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

/**
 * Interface for intercepting messages as they enter and exit a workflow.
 *
 * @author lchan
 */
public interface WorkflowInterceptor extends AdaptrisComponent, ComponentLifecycleExtension {

  /**
   * Mark the start of a workflow. This doesn't mean the message has started processing
   * but only that the message will - at some point - be processed by the workflow.
   *
   * @param inputMsg the message that will be processed by this workflow.
   */
  void workflowStart(AdaptrisMessage inputMsg);

  /**
   * Mark the start of processing a message. This method may be called on a different thread
   * from {#workflowStart}
   *
   * @param inputMsg
   */
  default void processingStart(AdaptrisMessage inputMsg) { }

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

}
