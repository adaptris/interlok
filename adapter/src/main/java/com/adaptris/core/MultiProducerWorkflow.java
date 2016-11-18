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

import java.util.ArrayList;
import java.util.List;

import javax.validation.constraints.NotNull;

import com.adaptris.annotation.AdapterComponent;
import com.adaptris.annotation.AdvancedConfig;
import com.adaptris.annotation.AutoPopulated;
import com.adaptris.annotation.ComponentProfile;
import com.adaptris.annotation.DisplayOrder;
import com.adaptris.annotation.InputFieldDefault;
import com.adaptris.core.util.LifecycleHelper;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Implementation of {@link Workflow} that contains multiple producers.
 * <p>
 * Implementation of <code>Workflow</code> that over-rides <code>sendProcessedMessage</code> to use a <code>List</code> of
 * <code>StandaloneProducer</code>s. NB the main 'produce', from which the success or failure of the workflow is determined, is
 * still configured in <code>Workflow</code> itself, and any failures producing below are only logged.
 * </p>
 * 
 * @config multi-producer-workflow.
 * 
 * 
 */
@XStreamAlias("multi-producer-workflow")
@AdapterComponent
@ComponentProfile(summary = "Workflow that has multiple additional producers in addition to the normal producer",
    tag = "workflow,base")
@DisplayOrder(order = {"useProcessedMessage", "disableDefaultMessageCount", "sendEvents", "logPayload"})
public class MultiProducerWorkflow extends StandardWorkflow {

  @NotNull
  @AutoPopulated
  private List<StandaloneProducer> standaloneProducers;
  @AdvancedConfig
  @InputFieldDefault(value = "false")
  private Boolean useProcessedMessage;

  /**
   * <p>
   * Creates a new instance.
   * </p>
   */
  public MultiProducerWorkflow() {
    super();
    standaloneProducers = new ArrayList<StandaloneProducer>();
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
  public synchronized void onAdaptrisMessage(AdaptrisMessage msg) {
    if (!obtainChannel().isAvailable()) {
      handleChannelUnavailable(msg); // make pluggable?
    }
    else {
      onMessage(msg);
    }
  }

  @Override
  protected void resubmitMessage(AdaptrisMessage msg) {
    onMessage(msg);
  }

  private void onMessage(final AdaptrisMessage msg) {
    AdaptrisMessage wip = null;
    workflowStart(msg);
    try {
      long start = System.currentTimeMillis();
      log.debug("start processing msg [" + msg.toString(logPayload()) + "]");
      wip = (AdaptrisMessage) msg.clone(); // retain orig. for error handling
      // Set the channel id and workflow id on the message lifecycle.
      wip.getMessageLifecycleEvent().setChannelId(obtainChannel().getUniqueId());
      wip.getMessageLifecycleEvent().setWorkflowId(obtainWorkflowId());
      wip.addEvent(getConsumer(), true); // initial receive event
      getServiceCollection().doService(wip);
      doProduce(wip);
      logSuccess(msg, start);
      sendProcessedMessage(wip, msg); // only if produce succeeds
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

  private void sendProcessedMessage(AdaptrisMessage wip, AdaptrisMessage msg) {
    AdaptrisMessage msgToSend = msg;

    if (useProcessedMessage()) {
      msgToSend = wip;
    }
    for (StandaloneProducer p : standaloneProducers) {
      try { // deliberately inside loop
        p.produce(msgToSend);
      }
      catch (Exception e) { // inc Runtime...
        log.debug("exc. in MultiProducerWorkflow in post-produce tasks", e);
        // do not call handleBadMessage for post-produce task
      }
    }
  }


  /** @see com.adaptris.core.WorkflowImp#initialiseWorkflow() */
  @Override
  protected void initialiseWorkflow() throws CoreException {
    for (StandaloneProducer p : standaloneProducers) {
      LifecycleHelper.init(p);
    }
    super.initialiseWorkflow();
  }

  /** @see com.adaptris.core.WorkflowImp#startWorkflow() */
  @Override
  protected void startWorkflow() throws CoreException {
    for (StandaloneProducer p : standaloneProducers) {
      LifecycleHelper.start(p);
    }
    super.startWorkflow();
  }

  /** @see com.adaptris.core.WorkflowImp#stopWorkflow() */
  @Override
  protected void stopWorkflow() {
    super.stopWorkflow();
    for (StandaloneProducer p : standaloneProducers) {
      LifecycleHelper.stop(p);
    }
  }

  /** @see com.adaptris.core.WorkflowImp#closeWorkflow() */
  @Override
  protected void closeWorkflow() {
    super.closeWorkflow();

    for (StandaloneProducer p : standaloneProducers) {
      LifecycleHelper.close(p);
    }
  }

  /** @see java.lang.Object#toString() */
  @Override
  public String toString() {
    StringBuffer result = new StringBuffer(super.toString());
    result.append(" ");
    result.append(getStandaloneProducers());

    return result.toString();
  }

  /**
   * <p>
   * Adds a <code>StandaloneProducer</code> to the <code>List</code> of
   * producers which will be used to send the processed message.
   * </p>
   *
   * @param producer the <code>StandaloneProducer</code> to add.
   */
  public void addStandaloneProducer(StandaloneProducer producer) {
    if (producer == null) {
      throw new IllegalArgumentException("param is null");
    }
    standaloneProducers.add(producer);
  }

  /**
   * <p>
   * Returns the <code>List</code> of underlying <code>StandaloneProducer</code>
   * s used to send processed messages.
   * </p>
   *
   * @return the <code>List</code> of underlying <code>StandaloneProducer</code>
   *         s used to send processed messages
   */
  public List<StandaloneProducer> getStandaloneProducers() {
    return standaloneProducers;
  }

  /**
   * <p>
   * Set the <code>List</code> of underlying <code>StandaloneProducer</code>s
   * used to send processed messages.
   * </p>
   *
   * @param l the <code>List</code> of underlying
   *          <code>StandaloneProducer</code>s used to send processed messages
   */
  public void setStandaloneProducers(List<StandaloneProducer> l) {
    if (l == null) {
      throw new IllegalArgumentException("null parameter for standalone producers");
    }
    standaloneProducers = l;
  }

  /**
   * <p>
   * Sets whether the processed message should be used by the processed message
   * producer (as opposed to the original incoming message).
   * </p>
   *
   * @param useProc whether the processed message should be used by the
   *          processed message producer
   */
  public void setUseProcessedMessage(Boolean useProc) {
    useProcessedMessage = useProc;
  }

  /**
   * <p>
   * Returns whether the processed message should be used by the processed
   * message producer.
   * </p>
   *
   * @return whether the processed message should be used by the processed
   *         message producer
   */
  public Boolean getUseProcessedMessage() {
    return useProcessedMessage;
  }

  boolean useProcessedMessage() {
    return getUseProcessedMessage() != null ? getUseProcessedMessage().booleanValue() : false;
  }

  @Override
  protected void prepareWorkflow() throws CoreException {
    for (StandaloneProducer p : getStandaloneProducers()) {
      p.prepare();
    }
  }
}
