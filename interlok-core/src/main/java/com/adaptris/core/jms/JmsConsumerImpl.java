/*
 * Copyright 2015 Adaptris Ltd.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/

package com.adaptris.core.jms;

import static com.adaptris.core.AdaptrisMessageFactory.defaultIfNull;
import static com.adaptris.core.jms.NullCorrelationIdSource.defaultIfNull;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.Session;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import org.apache.commons.lang.BooleanUtils;
import org.slf4j.Logger;
import com.adaptris.annotation.AdvancedConfig;
import com.adaptris.annotation.AutoPopulated;
import com.adaptris.core.AdaptrisMessageConsumerImp;
import com.adaptris.core.AdaptrisMessageListener;
import com.adaptris.core.ClosedState;
import com.adaptris.core.ConsumeDestination;
import com.adaptris.core.CoreException;
import com.adaptris.core.util.Args;
import com.adaptris.core.util.LifecycleHelper;

/**
 * <p>
 * Contains behaviour common to PTP and PAS JMS message consumers.
 * </p>
 */
public abstract class JmsConsumerImpl extends AdaptrisMessageConsumerImp implements MessageListener, JmsActorConfig {

  @NotNull
  @AutoPopulated
  @Pattern(regexp = "AUTO_ACKNOWLEDGE|CLIENT_ACKNOWLEDGE|DUPS_OK_ACKNOWLEDGE|[0-9]+")
  @AdvancedConfig
  private String acknowledgeMode;
  @NotNull
  @AutoPopulated
  @Valid
  private MessageTypeTranslator messageTranslator;
  @Valid
  @AdvancedConfig
  private CorrelationIdSource correlationIdSource;
  // not marshalled
  protected transient MessageConsumer consumer;
  private transient Session session;
  private transient OnMessageHandler onMessageHandler;
  private transient Boolean transacted;
  private transient boolean managedTransaction;
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
    changeState(ClosedState.getInstance());
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

  @Override
  public void prepare() throws CoreException {
    LifecycleHelper.prepare(getMessageTranslator());
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

  @Override
  public void stop() {
    consumer = nullify(consumer);
    LifecycleHelper.stop(messageTranslator);
  }

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

  @Override
  public void close() {
    LifecycleHelper.close(messageTranslator);
    session = nullify(session);
    consumer = nullify(consumer);
  }

  boolean isTransacted() {
    return isManagedTransaction() || BooleanUtils.toBooleanDefaultIfNull(getTransacted(), false);
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
    return getRollbackTimeout();
  }

  /**
   * <p>
   * Sets the MessageTypeTranslator to use.
   * </p>
   * 
   * @param translator the MessageTypeTranslator to use
   */
  public void setMessageTranslator(MessageTypeTranslator translator) {
    messageTranslator = Args.notNull(translator, "messageTranslator");
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
    correlationIdSource = c;
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
  
  public void setManagedTransaction(boolean managedTransaction) {
    this.managedTransaction = managedTransaction;
  }

  @Override
  public boolean isManagedTransaction() {
    return managedTransaction;
  }

  private static MessageConsumer nullify(MessageConsumer c) {
    JmsUtils.closeQuietly(c);
    return null;
  }

  private static Session nullify(Session s) {
    JmsUtils.closeQuietly(s);
    return null;
  }

  /**
   * Provides the metadata key {@value JmsConstants#JMS_DESTINATION} which will only be populated if
   * {@link MessageTypeTranslatorImp#getMoveJmsHeaders()} is true.
   * 
   * @since 3.9.0
   */
  @Override
  public String consumeLocationKey() {
    return JmsConstants.JMS_DESTINATION;
  }

}
