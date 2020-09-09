package com.adaptris.core.jms;

import static com.adaptris.core.jms.NullCorrelationIdSource.defaultIfNull;

import java.util.concurrent.TimeUnit;

import javax.jms.IllegalStateException;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.Session;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

import org.apache.commons.lang3.BooleanUtils;

import com.adaptris.annotation.AdvancedConfig;
import com.adaptris.annotation.AutoPopulated;
import com.adaptris.core.AdaptrisMessageListener;
import com.adaptris.core.AdaptrisPollingConsumer;
import com.adaptris.core.CoreException;
import com.adaptris.core.util.LifecycleHelper;
import com.adaptris.util.TimeInterval;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

public abstract class BaseJmsPollingConsumerImpl extends AdaptrisPollingConsumer implements JmsActorConfig {

  private static final TimeInterval DEFAULT_RECEIVE_WAIT = new TimeInterval(2L, TimeUnit.SECONDS);

  /**
   * <p>
   * Sets the JMS acknowledge mode to use.
   * </p>
   * <p>
   * The value may be AUTO_KNOWLEDGE, CLIENT_ACKNOWLEDGE, DUPS_OK_ACKNOWLEDGE or the int values corresponding to the JMS Session Constant
   * </p>
   *
   * @param i
   *          the JMS acknowledge mode to use
   */
  @NotNull
  @AutoPopulated
  @Pattern(regexp = "AUTO_ACKNOWLEDGE|CLIENT_ACKNOWLEDGE|DUPS_OK_ACKNOWLEDGE|[0-9]+")
  @AdvancedConfig
  @Getter
  @Setter
  private String acknowledgeMode;

  /**
   * <p>
   * Sets the MessageTypeTranslator to use.
   * </p>
   *
   * @param translator
   *          the MessageTypeTranslator to use
   */
  @NotNull
  @AutoPopulated
  @Valid
  @NonNull
  @Getter
  @Setter
  private MessageTypeTranslator messageTranslator;

  /**
   * <p>
   * Sets correlationIdSource.
   * </p>
   *
   * @param c
   *          the correlationIdSource to set
   */
  @Valid
  @AdvancedConfig
  @Getter
  @Setter
  private CorrelationIdSource correlationIdSource;

  /**
   * Sets the period that this class should wait for the broker to deliver a message.
   * <p>
   * The default value of 2 seconds should be suitable in most situations. If there is a high degree of network latency and this class does
   * not consume messages from Queues / Topics as expected try setting a higher value.
   * </p>
   *
   * @param l
   *          the period that this class should wait for the broker to deliver a message, if < 0 then the default (2secs) will be used.
   */
  @Valid
  @Getter
  @Setter
  private TimeInterval receiveTimeout;

  /**
   * The filter expression to use when matching messages to consume
   */
  @Getter
  @Setter
  private String messageSelector;

  private transient Boolean transacted;
  private transient boolean managedTransaction;
  private transient long rollbackTimeout = 30000;
  private transient Session session;
  private transient MessageConsumer messageConsumer;
  private transient OnMessageHandler messageHandler;

  public BaseJmsPollingConsumerImpl() {
    // defaults...
    setAcknowledgeMode(AcknowledgeMode.Mode.CLIENT_ACKNOWLEDGE.name());
    setMessageTranslator(new AutoConvertMessageTranslator());
  }

  @Override
  public void init() throws CoreException {
    super.init();
    messageHandler = new OnMessageHandler(this);
  }

  @Override
  public void close() {
    super.close();
    configuredMessageTranslator().registerSession(null);
    LifecycleHelper.stopAndClose(messageTranslator);
    closeMessageConsumer();
    closeSession();
  }

  protected void initConsumer() throws JMSException, CoreException {
    messageConsumer = createConsumer();
  }

  protected abstract MessageConsumer createConsumer() throws JMSException, CoreException;

  public MessageConsumer messageConsumer() {
    return messageConsumer;
  }

  protected void closeMessageConsumer() {
    JmsUtils.closeQuietly(messageConsumer);
    messageConsumer = null;
  }

  public OnMessageHandler messageHandler() {
    return messageHandler;
  }

  protected int doProcessMessage() {
    int count = 0;

    try {
      Message jmsMsg = null;

      do { // always want to try to obtain a Message
        try {
          jmsMsg = messageConsumer.receive(receiveTimeout());
        } catch (IllegalStateException e) {
          log.debug("Session closed upon attempt to process message");
          break;
        }

        if (jmsMsg != null) {
          messageHandler.onMessage(jmsMsg); // no Exc. ever
          if (!continueProcessingMessages(++count)) {
            break;
          }
        }
      } while (jmsMsg != null);

    } catch (Throwable e) {
      log.error("Unhandled Throwable processing message", e);
    }

    // log.debug("processed [" + count + "] messages");

    return count;
  }

  boolean isTransacted() {
    return isManagedTransaction() || BooleanUtils.toBooleanDefaultIfNull(transacted, false);
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
   * @param l
   *          the rollbackTimeout to set
   */
  void setRollbackTimeout(long l) {
    rollbackTimeout = l;
  }

  @Override
  public long rollbackTimeout() {
    return getRollbackTimeout();
  }

  long receiveTimeout() {
    long period = TimeInterval.toMillisecondsDefaultIfNull(getReceiveTimeout(), DEFAULT_RECEIVE_WAIT);
    if (period < 0) {
      period = DEFAULT_RECEIVE_WAIT.toMilliseconds();
    }
    return period;
  }

  @Override
  public CorrelationIdSource configuredCorrelationIdSource() {
    return defaultIfNull(getCorrelationIdSource());
  }

  @Override
  public MessageTypeTranslator configuredMessageTranslator() {
    return getMessageTranslator();
  }

  @Override
  public int configuredAcknowledgeMode() {
    return AcknowledgeMode.getMode(getAcknowledgeMode());
  }

  protected void initSession() throws JMSException, CoreException {
    session = createSession(configuredAcknowledgeMode(), isTransacted());
  }

  protected abstract Session createSession(int acknowledgeMode, boolean transacted) throws JMSException;

  @Override
  public Session currentSession() {
    return session;
  }

  protected void closeSession() {
    JmsUtils.closeQuietly(session);
    session = null;
  }

  public void setManagedTransaction(boolean managedTransaction) {
    this.managedTransaction = managedTransaction;
  }

  @Override
  public boolean isManagedTransaction() {
    return managedTransaction;
  }

  @Override
  public AdaptrisMessageListener configuredMessageListener() {
    return retrieveAdaptrisMessageListener();
  }

  /**
   * Provides the metadata key {@value com.adaptris.core.jms.JmsConstants#JMS_DESTINATION} which will only be populated if
   * {@link MessageTypeTranslatorImp#getMoveJmsHeaders()} is true.
   *
   * @since 3.9.0
   */
  @Override
  public String consumeLocationKey() {
    return JmsConstants.JMS_DESTINATION;
  }

}
