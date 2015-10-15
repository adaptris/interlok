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

package com.adaptris.core.lms;

import org.perf4j.aop.Profiled;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageListener;
import com.adaptris.core.CoreException;
import com.adaptris.core.StandardWorkflow;
import com.adaptris.core.WorkflowImp;
import com.adaptris.util.license.License;
import com.adaptris.util.license.License.LicenseType;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Workflow that does not store the original message for error-handling purposes.
 * <p>
 * This workflow is intended to provide better performance when faced with extremely large messages. The behaviour of
 * StandardWorkflow is to attempt to clone the message before attempting trigger the workflow; This can take an exceptionally long
 * time if the message is greater than 300Mb, leading to a simple FsConsumer to FsProducer taking something like 300000ms.
 * </p>
 * <p>
 * While error handling is supported, the current message in transit is provided to the
 * {@link com.adaptris.core.ProcessingExceptionHandler}; so best practise when using this workflow is to immediately archive it with
 * {@link LargeFsProducer} and use that for recovery.
 * </p>
 * 
 * @config large-message-workflow
 * @license STANDARD
 */
@XStreamAlias("large-message-workflow")
public class LargeMessageWorkflow extends StandardWorkflow {

  /**
   * <p>
   * Creates a new instance with defaults to prevent NullPointerExceptions.
   * </p>
   */
  public LargeMessageWorkflow() {
    super();
  }

  @Override
  protected void startWorkflow() throws CoreException {
    super.startWorkflow();
  }

  @Override
  protected void stopWorkflow() {
    super.stopWorkflow();
  }

  /**
   * <p>
   * This method is <code>synchronized</code> in case client code is multi-threaded.
   * </p>
   *
   * @see AdaptrisMessageListener#onAdaptrisMessage(AdaptrisMessage)
   */
  @Override
  @Profiled(tag = "{$this.getClass().getSimpleName()}({$this.getConsumer().getDestination().getDeliveryThreadName()})", logger = "com.adaptris.perf4j.lms.TimingLogger")
  public synchronized void onAdaptrisMessage(AdaptrisMessage msg) {
    if (!obtainChannel().isAvailable()) {
      handleChannelUnavailable(msg); // make pluggable?
    }
    else {
      handleMessage(msg, false);
    }
  }

  /**
   * @see WorkflowImp#resubmitMessage(com.adaptris.core.AdaptrisMessage)
   */
  @Override
  protected void resubmitMessage(AdaptrisMessage msg) {
    handleMessage(msg, false);
  }

  @Override
  protected void handleBadMessage(String logMsg, Exception e, AdaptrisMessage msg) {
    super.handleBadMessage(logMsg, e, msg);
  }

  @Override
  protected boolean workflowIsEnabled(License l) {
    return l.isEnabled(LicenseType.Standard);
  }
}
