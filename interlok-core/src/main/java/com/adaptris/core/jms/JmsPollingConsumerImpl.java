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

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.MessageConsumer;
import javax.jms.Session;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.apache.commons.lang3.BooleanUtils;
import org.slf4j.Logger;

import com.adaptris.annotation.AdvancedConfig;
import com.adaptris.annotation.AutoPopulated;
import com.adaptris.annotation.InputFieldDefault;
import com.adaptris.annotation.InputFieldHint;
import com.adaptris.annotation.Removal;
import com.adaptris.core.AdaptrisPollingConsumer;
import com.adaptris.core.ConsumeDestination;
import com.adaptris.core.CoreException;
import com.adaptris.core.jms.jndi.StandardJndiImplementation;
import com.adaptris.core.util.DestinationHelper;
import com.adaptris.core.util.LifecycleHelper;
import com.adaptris.core.util.LoggingHelper;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

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
public abstract class JmsPollingConsumerImpl extends BaseJmsPollingConsumerImpl implements JmsConnectionConfig {

  private String userName;
  @InputFieldHint(style = "PASSWORD", external = true)
  private String password;
  private String clientId;

  @NotNull
  @AutoPopulated
  @Valid
  @NonNull
  @Getter
  @Setter
  private VendorImplementation vendorImplementation;

  /**
   * Set additional trace debug logs
   */
  @AdvancedConfig
  @InputFieldDefault(value = "false")
  @Getter
  @Setter
  private Boolean additionalDebug;

  /**
   * The consume destination represents the endpoint that we will receive JMS messages from.
   * <p>
   * Depending on the flavour of the concrete consumer it may be a RFC6167 style string, a queue or
   * a topic
   * </p>
   *
   * @deprecated since 3.11.0 use the endpoint/queue/topic configuration available on the concrete
   *             consumer
   */
  @Deprecated
  @Valid
  @Getter
  @Setter
  @Removal(version = "4.0.0",
  message = "since 3.11.0 use the endpoint/queue/topic configuration available on the concrete consumer")
  private ConsumeDestination destination;

  private transient Connection connection;
  private transient boolean destinationWarningLogged = false;


  public JmsPollingConsumerImpl() {
    // defaults...
    setVendorImplementation(new StandardJndiImplementation());
    setAdditionalDebug(false);
  }

  @Override
  protected void prepareConsumer() throws CoreException {
    DestinationHelper.logWarningIfNotNull(destinationWarningLogged,
        () -> destinationWarningLogged = true, getDestination(),
        "{} uses destination, this will be removed in a future release",
        LoggingHelper.friendlyName(this));
    DestinationHelper.mustHaveEither(configuredEndpoint(), getDestination());
  }

  protected String messageSelector() {
    return DestinationHelper.filterExpression(getMessageSelector(), getDestination());
  }


  protected String endpoint() {
    return DestinationHelper.consumeDestination(configuredEndpoint(), getDestination());
  }

  @Override
  protected String newThreadName() {
    return DestinationHelper.threadName(retrieveAdaptrisMessageListener(), getDestination());
  }


  protected abstract String configuredEndpoint();

  protected ConnectionFactory createConnectionFactory() throws Exception {
    return configuredVendorImplementation().createConnectionFactory();
  }

  @Override
  protected Session createSession(int acknowledgeMode, boolean transacted) throws JMSException {
    return configuredVendorImplementation().createSession(connection, transacted, acknowledgeMode);
  }

  private void initialiseConnection() throws Exception {
    long start = System.currentTimeMillis();
    ConnectionFactory factory = createConnectionFactory();
    connection = configuredVendorImplementation().createConnection(factory, this);

    if (clientId != null) {
      connection.setClientID(clientId);
    }
    connection.start(); // required to allow polling
    initSession();
    initConsumer();
    configuredMessageTranslator().registerSession(currentSession());
    configuredMessageTranslator().registerMessageFactory(defaultIfNull(getMessageFactory()));
    LifecycleHelper.initAndStart(configuredMessageTranslator());
    if (additionalDebug()) {
      log.trace("connected to broker in {}ms", System.currentTimeMillis() - start);
    }
  }

  @Override
  protected abstract MessageConsumer createConsumer() throws JMSException;

  @Override
  protected int processMessages() {
    int count = 0;
    String oldName = renameThread();

    try {
      initialiseConnection();
      count = doProcessMessage();
    } catch (Exception e) {
      log.warn("Failed to initialise JMS Connection, will retry at next PollInterval");
      if (additionalDebug()) {
        log.error("Exception Message :", e);
      }
    } finally {
      closeConnection();
      Thread.currentThread().setName(oldName);
    }

    return count;
  }

  private void closeConnection() {
    JmsUtils.closeQuietly(connection, true);
    connection = null;
  }

  /** @see com.adaptris.core.AdaptrisComponent#close() */
  @Override
  public void close() {
    if (additionalDebug()) {
      log.trace("closing connection...");
    }
    long start = System.currentTimeMillis();

    super.close();

    closeConnection();
    if (additionalDebug()) {
      log.trace("disconnected from broker in [{}]", System.currentTimeMillis() - start);
    }
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

  public boolean additionalDebug() {
    return BooleanUtils.toBooleanDefaultIfNull(getAdditionalDebug(), false);
  }

}
