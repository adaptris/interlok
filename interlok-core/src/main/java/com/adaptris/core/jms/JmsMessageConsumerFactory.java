package com.adaptris.core.jms;

import static org.apache.commons.lang3.StringUtils.isEmpty;

import javax.jms.JMSException;
import javax.jms.MessageConsumer;
import javax.jms.Session;
import javax.jms.Topic;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.adaptris.core.jms.JmsDestination.DestinationType;

public class JmsMessageConsumerFactory {

  private Logger log = LoggerFactory.getLogger(this.getClass().getName());

  private VendorImplementation vendor;
  private Session session;
  private String rfc6167;
  private boolean deferConsumerCreationToVendor;
  private String filterExp;
  private JmsActorConfig jmsActorConfig;

  public JmsMessageConsumerFactory(VendorImplementation vendor, Session session, String rfc6167, boolean deferConsumerCreationToVendor,
      String filterExp, JmsActorConfig jmsActorConfig) {
    this.vendor = vendor;
    this.session = session;
    this.rfc6167 = rfc6167;
    this.deferConsumerCreationToVendor = deferConsumerCreationToVendor;
    this.filterExp = filterExp;
    this.jmsActorConfig = jmsActorConfig;
  }

  public MessageConsumer create() throws JMSException {
    JmsDestination destination = vendor.createDestination(rfc6167, jmsActorConfig);

    MessageConsumer consumer = null;
    if (deferConsumerCreationToVendor) {
      consumer = vendor.createConsumer(destination, filterExp, jmsActorConfig);
    } else {
      if (destination.destinationType().equals(DestinationType.TOPIC)) {
        if (!isEmpty(destination.subscriptionId())) { // then durable, maybe shared
          if (!isEmpty(destination.sharedConsumerId())) {
            log.trace("Creating new shared durable consumer.");
            consumer = ((ConsumerCreator) (session, dest, filterExpression) -> session
                .createSharedDurableConsumer((Topic) dest.getDestination(), dest.subscriptionId(), filterExpression))
                .createConsumer(session, destination, filterExp);
          } else {
            log.trace("Creating new durable consumer.");
            consumer = ((ConsumerCreator) (session, dest, filterExpression) -> session
                .createDurableSubscriber((Topic) dest.getDestination(), filterExpression)).createConsumer(session, destination,
                    filterExp);
          }
        } else if (!isEmpty(destination.sharedConsumerId())) {
          log.trace("Creating new shared consumer.");
          consumer = ((ConsumerCreator) (session, dest, filterExpression) -> session.createSharedConsumer((Topic) dest.getDestination(),
              dest.sharedConsumerId(), filterExpression)).createConsumer(session, destination, filterExp);
        }
      }

      if (consumer == null) {
        log.trace("Creating new standard consumer.");
        consumer = ((ConsumerCreator) (session, dest, filterExpression) -> session.createConsumer(dest.getDestination(), filterExpression))
            .createConsumer(session, destination, filterExp);
      }
    }

    return consumer;
  }

}
