package com.adaptris.core.jms;

import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import javax.jms.CompletionListener;
import javax.jms.JMSException;
import javax.jms.Message;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.ComponentLifecycle;
import com.adaptris.core.CoreConstants;
import com.adaptris.core.CoreException;

import lombok.Getter;
import lombok.Setter;

public class JmsAsyncProducerEventHandler implements CompletionListener, ComponentLifecycle {

  protected transient Logger log = LoggerFactory.getLogger(this.getClass().getName());
  
  private static final String ID_HEADER = "interlokMessageId";
    
  @Getter
  @Setter
  private transient Map<String, CallbackConsumers> unAckedMessages;
  
  private transient JmsProducer producer;
  @Getter
  @Setter
  private volatile boolean acceptSuccessCallbacks;
  
  public JmsAsyncProducerEventHandler(JmsProducer producer) {
    this.registerProducer(producer);
  }
  
  @Override
  public void onCompletion(Message message) {
    if(this.getAcceptSuccessCallbacks()) {
      try {
        log.debug("Success callback received for message id {}", message.getStringProperty(ID_HEADER));
        CallbackConsumers callbackConsumers = this.getUnAckedMessages().get(message.getStringProperty(ID_HEADER));
        if(callbackConsumers == null) {
          log.warn("Received success callback for an unknown message {}", message.getStringProperty(ID_HEADER));
        } else {
          defaultIfNull(callbackConsumers.getOnSuccess(), (msg) -> {   }).accept(callbackConsumers.getMessage());
          getUnAckedMessages().remove(message.getStringProperty(ID_HEADER));
        }
      } catch (JMSException ex) {
        log.error("Error retriving the Jms message ID while handling a success callback.  Executing failure callback.", ex);
        this.onException(message, ex);
      }
      logRemainingUnAckedMessages();
    } else {
      log.warn("Executing producer restart, not accepting further success callbacks until complete.");
    }
  }

  @Override
  public void onException(Message message, Exception exception) {
    try {
      setAcceptSuccessCallbacks(false);
      log.error("Received failure callback for message id {}", message.getStringProperty(ID_HEADER));
      logRemainingUnAckedMessages();
      
      CallbackConsumers callbackConsumers = this.getUnAckedMessages().get(message.getStringProperty(ID_HEADER));
      if(callbackConsumers == null) {
        log.warn("Received failure callback for an unknown message {}", message.getStringProperty(ID_HEADER));
      } else {
        defaultIfNull(callbackConsumers.getOnFailure(), (msg) -> {   }).accept(callbackConsumers.getMessage());
        getUnAckedMessages().remove(message.getStringProperty(ID_HEADER));
      }
    } catch (JMSException ex) {
      log.error("Error retrieving JMs message ID when handling an error.", ex);
    } finally {
      registeredProducer().retrieveConnection(JmsConnection.class).getConnectionErrorHandler().handleConnectionException();
    }
  }
  
  public void addUnAckedMessage(String messageId, AdaptrisMessage message) {
    log.trace("Adding message to un'acked list with id {}", messageId);
    CallbackConsumers callbackConsumers = new CallbackConsumers(message,
        (Consumer<AdaptrisMessage>) message.getObjectHeaders().get(CoreConstants.OBJ_METADATA_ON_SUCCESS_CALLBACK),
        (Consumer<AdaptrisMessage>) message.getObjectHeaders().get(CoreConstants.OBJ_METADATA_ON_FAILURE_CALLBACK));
    this.getUnAckedMessages().put(messageId, callbackConsumers);
  }

  private void logRemainingUnAckedMessages() {
    log.trace("{} messages waiting for async callback.", this.getUnAckedMessages().size());
  }
  
  @Override
  public void init() throws CoreException {
    this.setUnAckedMessages(new HashMap<>());
    this.setAcceptSuccessCallbacks(true);
  }

  class CallbackConsumers {
   @Getter
   @Setter
    private AdaptrisMessage message;
   @Getter
   @Setter
    private Consumer<AdaptrisMessage> onSuccess;
   @Getter
   @Setter
    private Consumer<AdaptrisMessage> onFailure;
    
    CallbackConsumers(AdaptrisMessage message, Consumer<AdaptrisMessage> onSuccess, Consumer<AdaptrisMessage> onFailure) {
      setMessage(message);
      setOnSuccess(onSuccess);
      setOnFailure(onFailure);
    }
  }

  public JmsProducer registeredProducer() {
    return producer;
  }

  public void registerProducer(JmsProducer producer) {
    this.producer = producer;
  }
}
