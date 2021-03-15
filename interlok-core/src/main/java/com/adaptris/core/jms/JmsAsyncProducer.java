package com.adaptris.core.jms;

import javax.jms.JMSException;
import javax.jms.Message;

import com.adaptris.annotation.AdapterComponent;
import com.adaptris.annotation.ComponentProfile;
import com.adaptris.annotation.DisplayOrder;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.CoreConstants;
import com.adaptris.core.CoreException;
import com.adaptris.core.util.ExceptionHelper;
import com.thoughtworks.xstream.annotations.XStreamAlias;

import lombok.Getter;
import lombok.Setter;

/**
 * JMS 2.0 Producer implementation that extends all features of {@link JmsProducer}, but allows us to send messages asynchronously. <br />
 * Be aware, you must have a JMS 2.0 compatible broker, this producer is not backward compatible to JMS 1.1
 * <p>
 * Essentially the producer sending the message to the JMS provider will not wait for a response that would normally confirm the message
 * has been received and persisted.  Instead the producer sends the JMS message in a "fire-and-forget" manner. <br />
 * At some future point in time, the JMS provider will call us back with confirmation or inform us of an error for each sent message.
 * </p>
 * <p>
 * One of the benefits to sending messages asynchronously simply comes down to processing speed.  During any producer, it is generally the time waiting for the JMS provider
 * to return control back to the client after the client submits a message that takes the most time.  With asynchronous message producing, we no longer have to wait for the JMS
 * provider, allowing us to move onto the next message.
 * </p>
 *
 * @config jms-async-producer
 *
 */
@XStreamAlias("jms-async-producer")
@AdapterComponent
@ComponentProfile(summary = "Place message on a JMS queue or topic asynchronously", tag = "producer,jms", recommended = {JmsConnection.class})
@DisplayOrder(order = {"endpoint", "asyncMessageErrorHandler",
    "messageTranslator", "deliveryMode", "priority", "ttl", "acknowledgeMode"})
public class JmsAsyncProducer extends JmsProducer {

  private static final String ID_HEADER = "interlokMessageId";
  
  @Getter
  @Setter
  private transient JmsAsyncProducerEventHandler eventHandler; 

  public JmsAsyncProducer() {
    setEventHandler(new JmsAsyncProducerEventHandler(this));
  }
  
  @Override
  protected void produce(AdaptrisMessage msg, JmsDestination jmsDest) throws JMSException, CoreException {
    try {
      setupSession(msg);
      Message jmsMsg = translate(msg, jmsDest.getReplyToDestination());
      jmsMsg.setStringProperty(ID_HEADER, msg.getUniqueId());
      
      if (!perMessageProperties()) {
        producerSession().getProducer().send(jmsDest.getDestination(), jmsMsg, getEventHandler());
      } else {
        producerSession().getProducer().send(jmsDest.getDestination(), jmsMsg,
            calculateDeliveryMode(msg, jmsDest.deliveryMode()),
            calculatePriority(msg, jmsDest.priority()),
            calculateTimeToLive(msg, jmsDest.timeToLive()),
            getEventHandler());
      }
      // in real time speed JMSMessageID may not yet be set, therefore we set a header.
      getEventHandler().addUnAckedMessage(jmsMsg.getStringProperty(ID_HEADER), msg);
      // Standard workflow will attempt to execute this after the produce, 
      // let's remove them so it's handled by our async event handler.
      msg.getObjectHeaders().remove(CoreConstants.OBJ_METADATA_ON_SUCCESS_CALLBACK);
      msg.getObjectHeaders().remove(CoreConstants.OBJ_METADATA_ON_FAILURE_CALLBACK);
      
      captureOutgoingMessageDetails(jmsMsg, msg);
      log.info("msg produced to destination [{}]", jmsDest);
    } catch (Throwable ex) {
      ExceptionHelper.rethrowProduceException(ex);
    }
  }
  
  @Override
  public void init() throws CoreException {
    super.init();
    getEventHandler().init();
  }

}
