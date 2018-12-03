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
