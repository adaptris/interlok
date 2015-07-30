package com.adaptris.core.jms;

import static com.adaptris.core.AdaptrisMessageFactory.defaultIfNull;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.Session;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

import org.slf4j.Logger;

import com.adaptris.annotation.AutoPopulated;
import com.adaptris.core.AdaptrisMessageConsumerImp;
import com.adaptris.core.AdaptrisMessageListener;
import com.adaptris.core.ConsumeDestination;
import com.adaptris.core.CoreException;
import com.adaptris.core.util.LifecycleHelper;
import com.adaptris.util.license.License;
import com.adaptris.util.license.License.LicenseType;

/**
 * <p>
 * Contains behaviour common to PTP and PAS JMS message consumers.
 * </p>
 */
public abstract class JmsConsumerImpl extends AdaptrisMessageConsumerImp implements MessageListener, JmsActorConfig {

  @NotNull
  @AutoPopulated
  @Pattern(regexp = "AUTO_ACKNOWLEDGE|CLIENT_ACKNOWLEDGE|DUPS_OK_ACKNOWLEDGE|[0-9]+")
  private String acknowledgeMode;
  @NotNull
  @AutoPopulated
  private MessageTypeTranslator messageTranslator;
  @NotNull
  @AutoPopulated
  @Valid
  private CorrelationIdSource correlationIdSource;
  // not marshalled
  protected transient MessageConsumer consumer;
  private transient Session session;
  private transient OnMessageHandler onMessageHandler;
  private transient Boolean transacted;
  private transient long rollbackTimeout = 30000;

  /**
   * <p>
   * Creates a new instance. Default session type is client acknowledge, default JMS message type is text.
   * </p>
   */
  public JmsConsumerImpl() {
    // defaults
    acknowledgeMode = AcknowledgeMode.Mode.CLIENT_ACKNOWLEDGE.name();
    messageTranslator = new AutoConvertMessageTranslator();
    setCorrelationIdSource(new NullCorrelationIdSource());
  }

  // Here for test purposes.
  JmsConsumerImpl(boolean transacted) {
    this();
    this.transacted = Boolean.valueOf(transacted);
  }

  public JmsConsumerImpl(ConsumeDestination d) {
    this();
    setDestination(d);
  }

  /**
   * <p>
   * Called by the JMS <code>Session</code> to deliver messages.
   * </p>
   * 
   * @param msg a <code>javax.jms.Message</code>
   */
  @Override
  public void onMessage(Message msg) {
    String oldName = renameThread();
    onMessageHandler.onMessage(msg);
    Thread.currentThread().setName(oldName);
  }

  /** @see com.adaptris.core.AdaptrisComponent#init() */
  @Override
  public void init() throws CoreException {
    try {
      session = retrieveConnection(JmsConnection.class)
          .createSession(isTransacted(), AcknowledgeMode.getMode(getAcknowledgeMode()));

      messageTranslator.registerSession(session);
      messageTranslator.registerMessageFactory(defaultIfNull(getMessageFactory()));
      LifecycleHelper.init(messageTranslator);
    }
    catch (JMSException e) {
      throw new CoreException(e);
    }
    onMessageHandler = new OnMessageHandler(this);
  }

  protected abstract MessageConsumer createConsumer() throws JMSException, CoreException;

  /** @see com.adaptris.core.AdaptrisComponent#stop() */
  @Override
  public void stop() {
    try {
      if (consumer != null) {
        consumer.close();
      }
      consumer = null;
    }
    catch (JMSException e) {
      log.trace("Failed to close consumer, logging exception for informational purposes only", e);
    }
    LifecycleHelper.stop(messageTranslator);
  }

  /** @see com.adaptris.core.AdaptrisComponent#start() */
  @Override
  public void start() throws CoreException {
    try {
      consumer = createConsumer();
      consumer.setMessageListener(this);
    }
    catch (JMSException e) {
      throw new CoreException(e);
    }
    LifecycleHelper.start(messageTranslator);
  }

  /** @see com.adaptris.core.AdaptrisComponent#close() */
  @Override
  public void close() {
    LifecycleHelper.close(messageTranslator);
    JmsUtils.closeQuietly(session);
    JmsUtils.closeQuietly(consumer);
    session = null;
    consumer = null;
  }

  boolean isTransacted() {
    return transacted != null ? transacted.booleanValue() : false;
  }

  void setTransacted(Boolean b) {
    transacted = b;
  }

  Boolean getTransacted() {
    return transacted;
  }

  /**
   * @return the rollbackTimeout
   */
  long getRollbackTimeout() {
    return rollbackTimeout;
  }

  /**
   * Not directly configurable, as it is done by JmsTransactedWorkflow.
   * 
   * @param l the rollbackTimeout to set
   */
  void setRollbackTimeout(long l) {
    rollbackTimeout = l;
  }

  @Override
  public long rollbackTimeout() {
    return rollbackTimeout;
  }

  /**
   * <p>
   * Sets the MessageTypeTranslator to use.
   * </p>
   * 
   * @param translator the MessageTypeTranslator to use
   */
  public void setMessageTranslator(MessageTypeTranslator translator) {
    if (translator == null) {
      throw new IllegalArgumentException();
    }
    messageTranslator = translator;
  }

  /**
   * <p>
   * Returns the MessageTypeTranslator to use.
   * </p>
   * 
   * @return the MessageTypeTranslator to use
   */
  public MessageTypeTranslator getMessageTranslator() {
    return messageTranslator;
  }

  /**
   * <p>
   * Sets the JMS acknowledge mode to use.
   * </p>
   * <p>
   * The value may be AUTO_KNOWLEDGE, CLIENT_ACKNOWLEDGE, DUPS_OK_ACKNOWLEDGE or the int values corresponding to the JMS Session
   * Constant
   * </p>
   * 
   * @param i the JMS acknowledge mode to use
   */
  public void setAcknowledgeMode(String i) {
    acknowledgeMode = i;
  }

  /**
   * <p>
   * Returns the JMS acknowledge mode to use.
   * </p>
   * 
   * @return the JMS acknowledge mode to use
   */
  public String getAcknowledgeMode() {
    return acknowledgeMode;
  }

  /**
   * 
   * @see com.adaptris.core.AdaptrisComponent#isEnabled(License)
   */
  @Override
  public boolean isEnabled(License l) throws CoreException {
    return l.isEnabled(LicenseType.Basic) && getMessageTranslator().isEnabled(l);
  }

  /**
   * <p>
   * Returns correlationIdSource.
   * </p>
   * 
   * @return correlationIdSource
   */
  public CorrelationIdSource getCorrelationIdSource() {
    return correlationIdSource;
  }

  /**
   * <p>
   * Sets correlationIdSource.
   * </p>
   * 
   * @param c the correlationIdSource to set
   */
  public void setCorrelationIdSource(CorrelationIdSource c) {
    if (c == null) {
      throw new IllegalArgumentException("null param");
    }
    correlationIdSource = c;
  }

  @Override
  public CorrelationIdSource configuredCorrelationIdSource() {
    return getCorrelationIdSource();
  }

  @Override
  public MessageTypeTranslator configuredMessageTranslator() {
    return getMessageTranslator();
  }

  @Override
  public int configuredAcknowledgeMode() {
    return AcknowledgeMode.getMode(getAcknowledgeMode());
  }

  @Override
  public AdaptrisMessageListener configuredMessageListener() {
    return retrieveAdaptrisMessageListener();
  }

  @Override
  public Session currentSession() {
    return session;
  }

  @Override
  public Logger currentLogger() {
    return log;
  }

}
