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

import com.adaptris.annotation.AdapterComponent;
import com.adaptris.annotation.ComponentProfile;
import com.adaptris.annotation.DisplayOrder;
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
 * 
 */
@XStreamAlias("standard-workflow")
@AdapterComponent
@ComponentProfile(summary = "Basic Single Threaded Workflow", tag = "workflow,base")
@DisplayOrder(order =
{
    "disableDefaultMessageCount", "sendEvents", "logPayload"
})
public class StandardWorkflow extends StandardWorkflowImpl {

  public StandardWorkflow() {
    super();
  }

  public StandardWorkflow(AdaptrisMessageConsumer consumer, AdaptrisMessageProducer producer) {
    this(consumer, new ServiceList(), producer);
  }

  public StandardWorkflow(AdaptrisMessageConsumer consumer, ServiceCollection services, AdaptrisMessageProducer producer) {
    this();
    setConsumer(consumer);
    setServiceCollection(services != null ? services : new ServiceList());
    setProducer(producer);
  }

  @Override
  public synchronized void onAdaptrisMessage(AdaptrisMessage msg) {
    if (!obtainChannel().isAvailable()) {
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

  protected void handleMessage(final AdaptrisMessage msg, boolean clone) {
    AdaptrisMessage wip = addConsumeLocation(msg);
    workflowStart(msg);
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
      logSuccess(wip, start);
    }
    catch (ServiceException e) {
      handleBadMessage("Exception from ServiceCollection", e, copyExceptionHeaders(wip, msg));
    }
    catch (ProduceException e) {
      wip.addEvent(getProducer(), false); // generate event
      handleBadMessage("Exception producing msg", e, copyExceptionHeaders(wip, msg));
      handleProduceException();
    }
    catch (Exception e) { // all other Exc. inc. runtime
      handleBadMessage("Exception processing message", e, copyExceptionHeaders(wip, msg));
    }
    finally {
      sendMessageLifecycleEvent(wip);
    }
    workflowEnd(msg, wip);
  }

}
