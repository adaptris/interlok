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

import org.perf4j.aop.Profiled;

import com.adaptris.core.util.LifecycleHelper;
import com.adaptris.util.license.License;
import com.adaptris.util.license.License.LicenseType;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * <p>
 * Standard implementation of <code>Workflow</code>. Links specific destinations in a <code>Channel</code>. Consumes
 * <code>AdaptrisMessage</code>s from a single <code>ConsumeDestination</code>, processes these messages using a
 * <code>ServiceCollection</code>, then produces the processed message using a <code>AdaptrisMessageProducer</code>. In the event of
 * an <code>Exception</code> processing a message, passes the problem message to a configured <code>AdaptrisMessageListener</code>.
 * </p>
 * 
 * @config standard-workflow
 * 
 * @license BASIC
 */
@XStreamAlias("standard-workflow")
public class StandardWorkflow extends WorkflowImp {

  /**
   * <p>
   * Creates a new instance with defaults to prevent NullPointerExceptions.
   * </p>
   */
  public StandardWorkflow() {
    super();
  }

  /**
   * @see com.adaptris.core.WorkflowImp#initialiseWorkflow()
   */
  @Override
  protected void initialiseWorkflow() throws CoreException {
    LifecycleHelper.init(getProducer());
    LifecycleHelper.init(getServiceCollection());
    getConsumer().registerAdaptrisMessageListener(this); // before init
    LifecycleHelper.init(getConsumer());
  }

  /**
   * @see com.adaptris.core.WorkflowImp#startWorkflow()
   */
  @Override
  protected void startWorkflow() throws CoreException {
    LifecycleHelper.start(getProducer());
    LifecycleHelper.start(getServiceCollection());
    LifecycleHelper.start(getConsumer());
  }

  /**
   * @see com.adaptris.core.WorkflowImp#stopWorkflow()
   */
  @Override
  protected void stopWorkflow() {
    LifecycleHelper.stop(getConsumer());
    LifecycleHelper.stop(getServiceCollection());
    LifecycleHelper.stop(getProducer());
  }

  /**
   * @see com.adaptris.core.WorkflowImp#closeWorkflow()
   */
  @Override
  protected void closeWorkflow() {
    LifecycleHelper.close(getConsumer());
    LifecycleHelper.close(getServiceCollection());
    LifecycleHelper.close(getProducer());
  }

  /**
   * <p>
   * This method is <code>synchronized</code> in case client code is
   * multi-threaded.
   * </p>
   *
   * @see AdaptrisMessageListener#onAdaptrisMessage(AdaptrisMessage)
   */
  @Override
  @Profiled(tag = "{$this.getClass().getSimpleName()}({$this.getConsumer().getDestination().getDeliveryThreadName()})", logger = "com.adaptris.perf4j.TimingLogger")
  public synchronized void onAdaptrisMessage(AdaptrisMessage msg) {
    if (!obtainChannel().isAvailable()) {

      //
      // ...causes deadlock...
      // if (this.getChannel().getComponentState() !=
      // StartedState.getInstance()) {
      //
      handleChannelUnavailable(msg); // make pluggable?
    }
    else {
      handleMessage(msg, true);
    }
  }

  /**
   * @see WorkflowImp#resubmitMessage(com.adaptris.core.AdaptrisMessage)
   */
  @Override
  protected void resubmitMessage(AdaptrisMessage msg) {
    handleMessage(msg, true);
  }

  @Override
  protected boolean workflowIsEnabled(License l) {
    return l.isEnabled(LicenseType.Basic);
  }
}
