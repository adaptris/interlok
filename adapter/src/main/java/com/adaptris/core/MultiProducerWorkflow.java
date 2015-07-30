/*
 * $RCSfile: MultiProducerWorkflow.java,v $
 * $Revision: 1.13 $
 * $Date: 2009/03/03 19:35:18 $
 * $Author: lchan $
 */
package com.adaptris.core;

import java.util.ArrayList;
import java.util.List;

import org.perf4j.aop.Profiled;

import com.adaptris.core.util.LifecycleHelper;
import com.adaptris.util.license.License;
import com.adaptris.util.license.License.LicenseType;
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
 * @license STANDARD
 */
@XStreamAlias("multi-producer-workflow")
public class MultiProducerWorkflow extends StandardWorkflow {

  private List<StandaloneProducer> standaloneProducers;
  private boolean useProcessedMessage;

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
  @Profiled(tag = "{$this.getClass().getSimpleName()}({$this.getConsumer().getDestination().getDeliveryThreadName()})", logger = "com.adaptris.perf4j.TimingLogger")
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
      handleBadMessage("Exception from ServiceCollection", e, msg);
    }
    catch (ProduceException e) {
      wip.addEvent(getProducer(), false); // generate event
      handleBadMessage("Exception producing msg", e, msg);
      handleProduceException();
    }
    catch (Exception e) { // all other Exc. inc. runtime
      handleBadMessage("Exception processing message", e, msg);
    }
    finally {
      sendMessageLifecycleEvent(wip);
    }
    workflowEnd(msg, wip);
  }

  private void sendProcessedMessage(AdaptrisMessage wip, AdaptrisMessage msg) {
    AdaptrisMessage msgToSend = msg;

    if (getUseProcessedMessage()) {
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


  @Override
  protected boolean doAdditionalLicenseChecks(License l) throws CoreException {
    for (StandaloneProducer p : standaloneProducers) {
      if (!p.isEnabled(l)) {
        return false;
      }
    }
    return true;
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
  public void setUseProcessedMessage(boolean useProc) {
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
  public boolean getUseProcessedMessage() {
    return useProcessedMessage;
  }

  @Override
  protected boolean workflowIsEnabled(License l) {
    return l.isEnabled(LicenseType.Standard);
  }
}
