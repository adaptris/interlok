/*
 * $RCSfile: JmsReplyToWorkflow.java,v $
 * $Revision: 1.10 $
 * $Date: 2009/05/20 08:46:28 $
 * $Author: lchan $
 */
package com.adaptris.core.jms;

import javax.jms.Destination;
import javax.jms.Queue;
import javax.jms.Topic;

import org.perf4j.aop.Profiled;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.CoreConstants;
import com.adaptris.core.CoreException;
import com.adaptris.core.ProduceDestination;
import com.adaptris.core.ProduceException;
import com.adaptris.core.ServiceException;
import com.adaptris.core.StandaloneProducer;
import com.adaptris.core.StandardWorkflow;
import com.adaptris.util.license.License;
import com.adaptris.util.license.License.LicenseType;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Extension of {@link StandardWorkflow} for use with JMS consumers and producers.
 * 
 * <p>
 * Generally it is preferred that you configure a {@link StandardWorkflow} and make use the
 * appropriate {@link JmsProducerImpl} implementation with a {@link JmsReplyToDestination} instead.
 * </p>
 * <p>
 * Key differences to {@link StandardWorkflow} are
 * <ul>
 * <li>Any configured {@link ProduceDestination} is ignored the configured producer; the appropriate
 * destination is derived from object metadata</li>
 * <li>The {@link JmsProducerImpl} implementations must correspond to the associated
 * {@link JmsConsumerImpl} implementation; i.e. {@link PtpProducer} must be used with
 * {@link PtpConsumer}.</li>
 * <li>Does not obey the use of {@link CoreConstants#KEY_WORKFLOW_SKIP_PRODUCER}, the producer is
 * always triggered.
 * </ul>
 * </p>
 * 
 * @config jms-reply-to-workflow
 * @license STANDARD
 * @deprecated Use a {@link StandardWorkflow} with a {@link StandaloneProducer} with a
 *             {@link JmsReplyToDestination}.
 */
@XStreamAlias("jms-reply-to-workflow")
@Deprecated
public final class JmsReplyToWorkflow extends StandardWorkflow {

  // not marshalled
  private transient boolean isPas;

  public JmsReplyToWorkflow() {
    super();
  }

  /**
   * <p>
   * If the configured <code>AdaptrisMessageConsumer</code> and
   * <code>AdaptrisMessageProducer</code> are JMS, calls <code>super.init
   * </code>, otherwise
   * throws Exception. Sets isPas flagged based on domain of producer.
   * </p>
   *
   * @see com.adaptris.core.WorkflowImp#initialiseWorkflow()
   */
  @Override
  protected void initialiseWorkflow() throws CoreException {

    if (this.verifyConfig()) {
      super.initialiseWorkflow();
    }
    else {
      throw new CoreException("attempting to use JmsReplyToWorkflow with"
          + " non-JMS consumer and / or producer");
    }
  }

  /**
   * <p>
   * Verifies that the configured consumer and producer are JMS, set producer
   * type flag.
   * </p>
   */
  private boolean verifyConfig() {
    boolean result = false;

    if (this.getConsumer() instanceof JmsConsumerImpl) {
      if (this.getProducer() instanceof PasProducer) {
        this.isPas = true;
        result = true;
      }
      else {
        if (this.getProducer() instanceof PtpProducer) {
          result = true;
        }
      }
    }

    return result;
  }

  @Override
  @Profiled(tag = "JmsReplyToWorkflow({$this.getConsumer().getDestination().getDeliveryThreadName()})", logger = "com.adaptris.perf4j.jms.TimingLogger")
  public synchronized void onAdaptrisMessage(AdaptrisMessage msg) {
    if (!obtainChannel().isAvailable()) {
      handleChannelUnavailable(msg); // make pluggable?
    }
    else {
      handleMessage(msg, true);
    }
  }

  /**
   * <p>
   * Attempts to obtain a JMS <code>Destination</code> object from
   * <code>AdaptrisMessage</code> metadata. If this object is not null, and its
   * type matches the <code>isPas</code> flag, <code>msg</code> is sent directly
   * to the <code>Destination</code>. Otherwise a <code>ProduceException</code>
   * is thrown.
   * </p>
   *
   * @see com.adaptris.core.Workflow#doProduce
   *      (com.adaptris.core.AdaptrisMessage)
   */
  @Override
  public void doProduce(AdaptrisMessage msg) throws ServiceException,
      ProduceException {

    Destination jmsDestination = (Destination) msg.getObjectMetadata().get(
        JmsConstants.OBJ_JMS_REPLY_TO_KEY);

    if (this.verifyDestinationDomain(jmsDestination)) {
      try {
        ((DefinedJmsProducer) this.getProducer()).produce(msg, jmsDestination, null);
      }
      catch (Exception e) {
        throw new ProduceException(e);
      }
    }
    else {
      throw new ProduceException("JMSReplyTo is null or wrong domain ["
          + jmsDestination + "]");
    }
  }

  /**
   * <p>
   * Verifies that the the JMS reply to Destination obtained from
   * AdaptrisMessage metadata is not null and its domain matches the isPas flag.
   * </p>
   */
  private boolean verifyDestinationDomain(Destination jmsDestination) {
    boolean result = false;

    if (jmsDestination != null) {
      if (isPas && jmsDestination instanceof Topic) {
        result = true;
      }
      else {
        if (!isPas && jmsDestination instanceof Queue) {
          result = true;
        }
      }
    }

    return result;
  }

  @Override
  protected boolean workflowIsEnabled(License l) {
    return l.isEnabled(LicenseType.Standard);
  }

}
