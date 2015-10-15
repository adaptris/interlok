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

package com.adaptris.core.runtime;

import com.adaptris.core.CoreException;
import com.adaptris.core.SerializableAdaptrisMessage;
import com.adaptris.interlok.management.MessageProcessor;

/**
 * Interface specifying controls for a single workflow.
 */
public interface WorkflowManagerMBean extends AdapterComponentMBean, ChildComponentMBean, ParentRuntimeInfoComponentMBean,
    HierarchicalMBean, MessageProcessor {

  /**
   * Allows you to inject a serialised message into a workflow.
   * 
   * @param serialisedMessage an adaptris message
   * @return the contents of the message after processing from the workflow.
   * @throws CoreException wrapping any underlying Exception
   * @since 3.0.4
   * @deprecated since 3.0.5 use {@link #process(com.adaptris.interlok.types.SerializableMessage)}
   *             instead.
   */
  @Deprecated
  SerializableAdaptrisMessage injectMessageWithReply(SerializableAdaptrisMessage serialisedMessage) throws CoreException;

  /**
   * Allows you to inject a serialised message into a workflow.
   * 
   * @param serialisedMessage an adaptris message
   * @return true - if the message cold be handed to the workflow
   * @throws CoreException wrapping any underlying Exception
   * @deprecated since 3.0.5 use
   *             {@link #processAsync(com.adaptris.interlok.types.SerializableMessage)} instead.
   */
  @Deprecated
  boolean injectMessage(SerializableAdaptrisMessage serialisedMessage) throws CoreException;

}
