package com.adaptris.core.jms;

import javax.jms.JMSException;
import javax.jms.MessageConsumer;

import com.adaptris.core.AdaptrisMessageConsumer;
import com.adaptris.core.ConsumeDestination;
import com.adaptris.core.CoreException;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * <p>
 * JMS Queue implementation of {@link AdaptrisMessageConsumer}
 * </p>
 * 
 * @config jms-queue-consumer
 * @license BASIC, additional license requirements from the chosen MessageTypeTranslator
 */
@XStreamAlias("jms-queue-consumer")
public class PtpConsumer extends JmsConsumerImpl {

  public PtpConsumer() {
    super();
  }

  PtpConsumer(boolean b) {
    super(b);
  }

  public PtpConsumer(ConsumeDestination d) {
    super(d);
  }

  @Override
  protected MessageConsumer createConsumer() throws JMSException, CoreException {
    return retrieveConnection(JmsConnection.class).configuredVendorImplementation().createQueueReceiver(getDestination(), this);
  }
}
