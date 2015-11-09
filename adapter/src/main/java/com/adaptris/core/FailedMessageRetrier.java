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

import java.util.Collection;

/**
 * <p>
 * Component which consumes {@linkplain AdaptrisMessage}s and, based on message
 * metadata, resubmits them to the {@linkplain Workflow} which processed them
 * originally.
 * </p>
 */
public interface FailedMessageRetrier extends AdaptrisComponent, AdaptrisMessageListener, ComponentLifecycleExtension {

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
