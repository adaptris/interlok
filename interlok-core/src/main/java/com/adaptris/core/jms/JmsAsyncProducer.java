package com.adaptris.core.jms;

import javax.jms.CompletionListener;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.validation.constraints.NotNull;

import com.adaptris.annotation.AdapterComponent;
import com.adaptris.annotation.ComponentProfile;
import com.adaptris.annotation.DisplayOrder;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.CoreException;
import com.adaptris.core.StandardProcessingExceptionHandler;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * JMS 2.0 Producer implementation that extends all features of {@link JmsProducer}, but allows us to send messages asynchronously. <br />
 * Be aware, you must have a JMS 2.0 compatible broker, this producer is not backward compatible to JMS 1.1
 * <p>
 * Essentially the producer sending the message to the JMS provider will not wait for a response that would normally confirm the message
 * has been received and persisted.  Instead the producer sends the JMS message in a "fire-and-forget" manner. <br />
 * At some future point in time, the JMS provider will call us back with confirmation or inform us of an error for each sent message.
 * </p>
 * <p>
 * Should the message have failed to be fully received or persisted, you can configure an async-message-error-handler.
 * </p>
 * <p>
 * One of the benefits to sending messages asynchronously simply comes down to processing speed.  During any producer, it is generally the time waiting for the JMS provider
 * to return control back to the client after the client submits a message that takes the most time.  With asynchronous message producing, we no longer have to wait for the JMS
 * provider, allowing us to move onto the next message.
 * </p>
 * <p>
 * <b>NOTE:</b> Once this producer has sent a message it is assumed to have succeeded, at least until a success or failure callback is received. <br />
 * This means that if this producer is one of your workflow producers, the workflow itself will immediately deem this 
 * message to have been processed and will move onto the next available message. <br/>
 * Generally this may not be an issue, however if processing a message triggers a form of transaction committing or JMS acknowledging, 
 * then the commit or ack could be completed regardless if the sent JMS message eventually succeeds or fails.
 * </p>
 * 
 * @config jms-async-producer
 * 
 */
@XStreamAlias("jms-async-producer")
@AdapterComponent
@ComponentProfile(summary = "Place message on a JMS queue or topic asynchronously", tag = "producer,jms", recommended = {JmsConnection.class})
@DisplayOrder(order = {"destination", "asyncMessageErrorHandler", "messageTypeTranslator", "deliveryMode", "priority", "ttl", "acknowledgeMode"})
public class JmsAsyncProducer extends JmsProducer implements CompletionListener {
  
  @NotNull
  private StandardProcessingExceptionHandler asyncMessageErrorHandler;

  protected void produce(AdaptrisMessage msg, JmsDestination jmsDest) throws JMSException, CoreException {
    try {
      setupSession(msg);
      Message jmsMsg = translate(msg, jmsDest.getReplyToDestination());
      if (!perMessageProperties()) {
        producerSession.getProducer().send(jmsDest.getDestination(), jmsMsg, this);
      } else {
        producerSession.getProducer().send(jmsDest.getDestination(), jmsMsg,
            calculateDeliveryMode(msg, jmsDest.deliveryMode()),
            calculatePriority(msg, jmsDest.priority()),
            calculateTimeToLive(msg, jmsDest.timeToLive()),
            this);
      }
      if (captureOutgoingMessageDetails()) {
        captureOutgoingMessageDetails(jmsMsg, msg);
      }
      log.info("msg produced to destination [{}]", jmsDest);
    } catch (Throwable ex) {
      throw new CoreException("JMS runtime exception", ex);
    }
  }

  @Override
  public void onCompletion(Message message) {
    try {
      log.trace("Async message succesfully received with id {}", message.getJMSMessageID());
    } catch (JMSException e) {}
  }

  @Override
  public void onException(Message message, Exception exception) {
    log.error("Async Message failed.", exception);
    
    try {
      this.getAsyncMessageErrorHandler().handleProcessingException(this.getMessageTranslator().translate(message));
    } catch (JMSException e) {
      log.error("Failed to translate the failed JMS message to execute the exception handler.", e);
    }
  }

  public StandardProcessingExceptionHandler getAsyncMessageErrorHandler() {
    return asyncMessageErrorHandler;
  }

  public void setAsyncMessageErrorHandler(StandardProcessingExceptionHandler asyncMessageErrorHandler) {
    this.asyncMessageErrorHandler = asyncMessageErrorHandler;
  }
  
}
