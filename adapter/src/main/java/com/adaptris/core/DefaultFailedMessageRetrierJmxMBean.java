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

import java.io.File;
import java.io.IOException;

import com.adaptris.core.runtime.ChildRuntimeInfoComponentMBean;

/**
 * JMX Interface for {@link DefaultFailedMessageRetrier}
 *
 * @author lchan
 *
 */
public interface DefaultFailedMessageRetrierJmxMBean extends ChildRuntimeInfoComponentMBean {

  /**
   * Retry a message.
   * <p>
   * Using metadata that is already present in the message, namely {@link Workflow#WORKFLOW_ID_KEY}, retry the message. Note that
   * the return code only indicates that a workflow was found, and the message was successfully submitted to the workflow. It does
   * not indicate anything about the successful processing (or not) of the message.
   * </p>
   *
   * @param msg the message
   * @return true if the message was successfully submitted to a workflow, false otherwise.
   * @throws CoreException if there was an error unwrapping the file using {@link DefaultSerializableMessageTranslator}
   */
  boolean retryMessage(SerializableAdaptrisMessage msg) throws CoreException;

  /**
   * Retry a message that has been written to the filesystem.
   * 
   * <p>
   * This assumes that the contents of the file has previously been encoded using a {@link MimeEncoder} and written out to the
   * filesystem. It will decode the file and then submit it using {@link #retryMessage(SerializableAdaptrisMessage)}. Note that the
   * return code only indicates that a workflow was found, and the message was successfully submitted to the workflow. It does not
   * indicate anything about the successful processing (or not) of the message.
   * </p>
   * 
   * @param file the file, local to the adapter that contains the message to be retried.
   * @return true if the message was successfully submitted to a workflow, false otherwise.
   * @throws IOException if there was an error reading the file.
   * @throws CoreException if there was an error decoding the file using {@link MimeEncoder}.
   */
  boolean retryMessage(File file) throws IOException, CoreException;
}
