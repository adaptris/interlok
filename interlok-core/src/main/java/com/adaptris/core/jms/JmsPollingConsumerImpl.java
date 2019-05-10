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

import java.util.concurrent.TimeUnit;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.IllegalStateException;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.Session;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

import org.apache.commons.lang3.BooleanUtils;
import org.slf4j.Logger;

import com.adaptris.annotation.AdvancedConfig;
import com.adaptris.annotation.AutoPopulated;
import com.adaptris.annotation.InputFieldDefault;
import com.adaptris.annotation.InputFieldHint;
import com.adaptris.core.AdaptrisMessageListener;
import com.adaptris.core.AdaptrisPollingConsumer;
import com.adaptris.core.CoreException;
import com.adaptris.core.jms.jndi.StandardJndiImplementation;
import com.adaptris.core.util.Args;
import com.adaptris.core.util.LifecycleHelper;
import com.adaptris.interlok.resolver.ExternalResolver;
import com.adaptris.security.password.Password;
import com.adaptris.util.TimeInterval;

/**
 * Abstract implementation of {@link AdaptrisPollingConsumer} for queues and topics.
 * <p>
 * The behaviour of this consumer differs from the standard {@link JmsConsumerImpl} concrete
 * implementations. This consumer has the following steps.
 * <ul>
 * <li>Connects to broker.</li>
 * <li>Attempts to obtain a {@link javax.jms.Message} within the given {@link #getReceiveTimeout()}.
 * </li>
 * <li>If successful, processes the {@code javax.jms.Message} and repeats until no more messages.</li>
 * <li>Closes the connection.</li>
 * </p>
 * <p>
 * If connection attempt fails, the poller sleeps for the configured interval then tries again.
 * </p>
 */
public abstract class JmsPollingConsumerImpl extends AdaptrisPollingConsumer implements JmsActorConfig, JmsConnectionConfig {

  private static final TimeInterval DEFAULT_RECEIVE_WAIT = new TimeInterval(2L, TimeUnit.SECONDS);

  private String userName;
  @InputFieldHint(style = "PASSWORD", external = true)
  private String password;
  private String clientId;

  @NotNull
  @AutoPopulated
  @Pattern(regexp = "AUTO_ACKNOWLEDGE|CLIENT_ACKNOWLEDGE|DUPS_OK_ACKNOWLEDGE|[0-9]+")
  @AdvancedConfig
  private String acknowledgeMode;
  @NotNull
  @AutoPopulated
  @Valid
  private VendorImplementation vendorImplementation;
  @NotNull
  @AutoPopulated
  @Valid
  private MessageTypeTranslator messageTranslator;
  @Valid
  @AdvancedConfig
  private CorrelationIdSource correlationIdSource;
  @AdvancedConfig
  @InputFieldDefault(value = "false")
  private Boolean additionalDebug;
  @Valid
  private TimeInterval receiveTimeout;

  private transient Boolean transacted;
  private transient boolean managedTransaction;
  private transient long rollbackTimeout = 30000;
  private transient Connection connection;
  private transient Session session;
  private transient MessageConsumer messageConsumer;
  private transient OnMessageHandler messageHandler;


  public JmsPollingConsumerImpl() {
    // defaults...
    setAcknowledgeMode(AcknowledgeMode.Mode.CLIENT_ACKNOWLEDGE.name());
    setVendorImplementation(new StandardJndiImplementation());
    setMessageTranslator(new AutoConvertMessageTranslator());
    setAdditionalDebug(false);
  }

  @Override
  public void prepareConsumer() throws CoreException {
    getMessageTranslator().prepare();
    getVendorImplementation().prepare();
  }

  @Override
  public void init() throws CoreException {
    super.init();
    messageHandler = new OnMessageHandler(this);
  }

  protected ConnectionFactory createConnectionFactory() throws CoreException {
    try {
      return getVendorImplementation().createConnectionFactory();
    }
    catch (JMSException e) {
      throw new CoreException(e);
    }
  }


  protected Connection createConnection(ConnectionFactory factory, String user, String password) throws JMSException {
    return factory.createConnection(user, password);
  }


  protected Session createSession(Connection connection, int acknowledgeMode, boolean transacted) throws JMSException {
    return getVendorImplementation().createSession(connection, transacted, acknowledgeMode);
  }


  private void initialiseConnection() throws Exception {
    long start = System.currentTimeMillis();
    ConnectionFactory factory = createConnectionFactory();
    connection = createConnection(factory, userName, Password.decode(ExternalResolver.resolve(configuredPassword())));

    if (clientId != null) {
      connection.setClientID(clientId);
    }
    connection.start(); // required to allow polling
    session = createSession(connection, AcknowledgeMode.getMode(acknowledgeMode), isTransacted());
    messageConsumer = createConsumer();
    messageTranslator.registerSession(session);
    messageTranslator.registerMessageFactory(defaultIfNull(getMessageFactory()));
    LifecycleHelper.init(messageTranslator);
    LifecycleHelper.start(messageTranslator);
    if (additionalDebug()) {
      log.trace("connected to broker in {}ms", (System.currentTimeMillis() - start));
    }
  }


  protected abstract MessageConsumer createConsumer() throws JMSException;

  @Override
  protected int processMessages() {
    int count = 0;
    String oldName = renameThread();

    try {
      initialiseConnection();
      try {
        Message jmsMsg = null;

        do { // always want to try to obtain a Message
          try {
            jmsMsg = messageConsumer.receive(receiveTimeout());
          }
          catch (IllegalStateException e) {
            log.debug("Session closed upon attempt to process message");
            break;
          }

          if (jmsMsg != null) {
            messageHandler.onMessage(jmsMsg); // no Exc. ever
            if (!continueProcessingMessages(++count)) {
              break;
            }
          }
        }
        while (jmsMsg != null);

      }
      catch (Throwable e) {
        log.error("Unhandled Throwable processing message", e);
      }
    }
    catch (Exception e) {
      log.warn("Failed to initialise JMS Connection, will retry at next PollInterval");
      if (additionalDebug()) {
        log.error("Exception Message :", e);
      }
    }
    finally {
      closeConnection();
      Thread.currentThread().setName(oldName);
    }
    // log.debug("processed [" + count + "] messages");

    return count;
  }

  private void closeConnection() {
    if (additionalDebug()) {
      log.trace("closing connection...");
    }
    long start = System.currentTimeMillis();
    messageTranslator.registerSession(null);
    LifecycleHelper.stop(messageTranslator);
    LifecycleHelper.close(messageTranslator);
    JmsUtils.closeQuietly(messageConsumer);
    JmsUtils.closeQuietly(session);
    JmsUtils.closeQuietly(connection, true);
    connection = null;
    messageConsumer = null;
    session = null;
    if (additionalDebug()) {
      log.trace("disconnected from broker in [{}]", (System.currentTimeMillis() - start));
    }
  }

  /** @see com.adaptris.core.AdaptrisComponent#close() */
  @Override
  public void close() {
    super.close();
    closeConnection();
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

  public String getAcknowledgeMode() {
    return acknowledgeMode;
  }

  /**
   * <p>
   * Sets the JMS acknowledge mode to use.
   * </p>
   * <p>
   * The value may be AUTO_KNOWLEDGE, CLIENT_ACKNOWLEDGE, DUPS_OK_ACKNOWLEDGE or
   * the int values corresponding to the JMS Session Constant
   * </p>
   *
   * @param s the JMS acknowledge mode to use
   */
  public void setAcknowledgeMode(String s) {
    acknowledgeMode = s;
  }

  public String getClientId() {
    return clientId;
  }

  /**
   * <p>
   * Sets the optional JMS client ID. May not be empty, null means don't use client ID.
   * </p>
   * 
   * @param s the optional JMS client ID, defaults to null.
   */
  public void setClientId(String s) {
    if ("".equals(s)) {
      throw new IllegalArgumentException("empty param");
    }
    clientId = s;
  }


  public MessageTypeTranslator getMessageTranslator() {
    return messageTranslator;
  }

  public void setMessageTranslator(MessageTypeTranslator m) {
    messageTranslator = Args.notNull(m, "messageTranslator");
  }

  public String getPassword() {
    return password;
  }

  /**
   * <p>
   * Sets the password for the specified user.
   * </p>
   * <p>
   * In additional to plain text passwords, the passwords can also be encrypted
   * using the appropriate {@link com.adaptris.security.password.Password}
   * </p>
   *
   * @param s the password for the specified user
   */
  public void setPassword(String s) {
    password = s;
  }

  public String getUserName() {
    return userName;
  }


  public void setUserName(String s) {
    userName = s;
  }

  public VendorImplementation getVendorImplementation() {
    return vendorImplementation;
  }

  public void setVendorImplementation(VendorImplementation v) {
    vendorImplementation = Args.notNull(v, "vendorImplementation");
  }

  long receiveTimeout() {
    long period = TimeInterval.toMillisecondsDefaultIfNull(getReceiveTimeout(), DEFAULT_RECEIVE_WAIT);
    if (period < 0) {
      period = DEFAULT_RECEIVE_WAIT.toMilliseconds();
    }
    return period;
  }

  public TimeInterval getReceiveTimeout() {
    return receiveTimeout;
  }

  /**
   * Sets the period that this class should wait for the broker to deliver a message.
   * <p>
   * The default value of 2 seconds should be suitable in most situations. If there is a high degree of network latency and this
   * class does not consume messages from Queues / Topics as expected try setting a higher value.
   * </p>
   *
   * @param l the period that this class should wait for the broker to deliver a message, if &lt; 0 then the default (2secs) will be
   *          used.
   */
  public void setReceiveTimeout(TimeInterval l) {
    receiveTimeout = l;
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

  @Override
  public String configuredClientId() {
    return getClientId();
  }

  @Override
  public String configuredPassword() {
    return getPassword();
  }

  @Override
  public String configuredUserName() {
    return getUserName();
  }

  @Override
  public VendorImplementation configuredVendorImplementation() {
    return getVendorImplementation();
  }

  /**
   * @return the additionalDebug
   */
  public Boolean getAdditionalDebug() {
    return additionalDebug;
  }

  public boolean additionalDebug() {
    return BooleanUtils.toBooleanDefaultIfNull(getAdditionalDebug(), false);
  }

  /**
   * @param b the additionalDebug to set
   */
  public void setAdditionalDebug(Boolean b) {
    additionalDebug = b;
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
  
  public void setManagedTransaction(boolean managedTransaction) {
    this.managedTransaction = managedTransaction;
  }

  @Override
  public boolean isManagedTransaction() {
    return managedTransaction;
  }

}
