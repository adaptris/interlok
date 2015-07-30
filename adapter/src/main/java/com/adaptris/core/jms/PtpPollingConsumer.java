/*
 * $RCSfile: PtpPollingConsumer.java,v $
 * $Revision: 1.18 $
 * $Date: 2009/02/17 12:32:03 $
 * $Author: lchan $
 */
package com.adaptris.core.jms;

import javax.jms.JMSException;
import javax.jms.MessageConsumer;

import com.adaptris.core.ConsumeDestination;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * <p>
 * Queue implementation of {@link JmsPollingConsumerImpl}.
 * </p>
 * 
 * @config jms-queue-poller
 * @license BASIC, additional license requirements from the chosen MessageTypeTranslator and VendorImplementation.
 */
@XStreamAlias("jms-queue-poller")
public class PtpPollingConsumer extends JmsPollingConsumerImpl {

  public PtpPollingConsumer() {
    super();
  }

  public PtpPollingConsumer(ConsumeDestination d) {
    this();
    setDestination(d);
  }

  @Override
  protected MessageConsumer createConsumer() throws JMSException {
    return getVendorImplementation().createQueueReceiver(getDestination(), this);
  }
}
