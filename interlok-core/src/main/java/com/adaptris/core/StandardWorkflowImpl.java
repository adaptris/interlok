/*
 * Copyright 2018 Adaptris Ltd.
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

import java.util.function.Consumer;
import com.adaptris.core.util.LifecycleHelper;

public abstract class StandardWorkflowImpl extends WorkflowImp {

  public StandardWorkflowImpl() {
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

  @Override
  protected void prepareWorkflow() throws CoreException {
    // Consumers / services / producers already prepared.
  }

  @Override
  public void onAdaptrisMessage(AdaptrisMessage msg, Consumer<AdaptrisMessage> success) {
    ListenerCallbackHelper.prepare(msg, success);
    if (!obtainChannel().isAvailable()) {
      handleChannelUnavailable(msg); // make pluggable?
    } else {
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

  protected void handleMessage(final AdaptrisMessage msg, boolean clone) {
    AdaptrisMessage wip = addConsumeLocation(msg);
    workflowStart(msg);
    processingStart(msg);
    try {
      long start = System.currentTimeMillis();
      log.debug("start processing msg [{}]", messageLogger().toString(msg));
      if (clone) {
        wip = (AdaptrisMessage) msg.clone(); // retain orig. for error handling
      }
      wip.getMessageLifecycleEvent().setChannelId(obtainChannel().getUniqueId());
      wip.getMessageLifecycleEvent().setWorkflowId(obtainWorkflowId());
      wip.addEvent(getConsumer(), true); // initial receive event
      getServiceCollection().doService(wip);
      doProduce(wip);
      // handle success callback here.
      // failure callback will be handled by the message-error-handler that's configured...
      ListenerCallbackHelper.handleSuccessCallback(wip);
      logSuccess(wip, start);
    } catch (ServiceException e) {
      handleBadMessage("Exception from ServiceCollection", e, copyExceptionHeaders(wip, msg));
    } catch (ProduceException e) {
      wip.addEvent(getProducer(), false); // generate event
      handleBadMessage("Exception producing msg", e, copyExceptionHeaders(wip, msg));
      handleProduceException();
    } catch (Exception e) { // all other Exc. inc. runtime
      handleBadMessage("Exception processing message", e, copyExceptionHeaders(wip, msg));
    } finally {
      sendMessageLifecycleEvent(wip);
    }
    workflowEnd(msg, wip);
  }
}
