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

import static org.apache.commons.lang.StringUtils.isEmpty;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.Session;
import javax.validation.Valid;
import org.apache.commons.lang3.BooleanUtils;
import com.adaptris.annotation.AdapterComponent;
import com.adaptris.annotation.AdvancedConfig;
import com.adaptris.annotation.AutoPopulated;
import com.adaptris.annotation.ComponentProfile;
import com.adaptris.annotation.DisplayOrder;
import com.adaptris.annotation.InputFieldDefault;
import com.adaptris.annotation.InputFieldHint;
import com.adaptris.core.AdaptrisMarshaller;
import com.adaptris.core.AllowsRetriesConnection;
import com.adaptris.core.ConnectionErrorHandler;
import com.adaptris.core.CoreException;
import com.adaptris.core.DefaultMarshaller;
import com.adaptris.core.jms.jndi.StandardJndiImplementation;
import com.adaptris.core.util.ExceptionHelper;
import com.adaptris.interlok.resolver.ExternalResolver;
import com.adaptris.security.password.Password;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * JMS 1.1 standard JMS connection.
 * <p>
 * In the adapter configuration file this class is aliased as <b>jms-connection</b> which is the preferred alternative to the fully
 * qualified classname when building your configuration.
 * </p>
 * 
 * @config jms-connection
 * 
 */
@XStreamAlias("jms-connection")
@AdapterComponent
@ComponentProfile(summary = "Connect to a JMS 1.1 broker", tag = "connections,jms")
@DisplayOrder(order = {"userName", "password", "clientId", "vendorImplementation"})
public class JmsConnection extends AllowsRetriesConnection implements JmsConnectionConfig, ConnectionComparator<JmsConnection> {

  protected transient Connection connection;
  private transient JmsConnectionErrorHandler connectionHandlerIfNotConfigured = new JmsConnectionErrorHandler();

  @InputFieldDefault(value = "")
  private String userName;
  @InputFieldHint(style = "PASSWORD", external = true)
  private String password;
  private String clientId;
  @AdvancedConfig
  @InputFieldDefault(value = "false")
  private Boolean additionalDebug;
  @Valid
  @AutoPopulated
  private VendorImplementation vendorImplementation;


  /**
   * <p>
   * Create a new instance. Default settings are:
   * <ul>
   * <li>vendor-implementation - StandardJndiImplementation</li>
   * <li>username - "" (i.e. blank)</li>
   * </ul>
   * </p>
   */
  public JmsConnection() {
    this(new StandardJndiImplementation());
  }

  public JmsConnection(VendorImplementation impl) {
    setUserName("");
    setVendorImplementation(impl);
  }

  /**
   * 
   * @see com.adaptris.core.AdaptrisConnectionImp#initConnection()
   */
  @Override
  protected void initConnection() throws CoreException {
    try {
      connect();
    }
    catch (Exception e) {
      throw ExceptionHelper.wrapCoreException(e);
    }
  }

  public Connection currentConnection() {
    return connection;
  }

  @Override
  public ConnectionErrorHandler connectionErrorHandler() {
    return getConnectionErrorHandler() != null ? getConnectionErrorHandler() : connectionHandlerIfNotConfigured;
  }

  /**
   * 
   * @see com.adaptris.core.AdaptrisConnectionImp#startConnection()
   */
  @Override
  protected void startConnection() throws CoreException {
    try {
      connection.start();
    }
    catch (Exception e) {
      throw ExceptionHelper.wrapCoreException(e);
    }
  }

  /**
   * @see com.adaptris.core.AdaptrisConnectionImp#stopConnection()
   */
  @Override
  protected void stopConnection() {
    JmsUtils.stopQuietly(connection);
  }

  /**
   * @see com.adaptris.core.AdaptrisConnectionImp#closeConnection()
   */
  @Override
  protected void closeConnection() {
    JmsUtils.closeQuietly(connection);
    connection = null;
  }

  /**
   * <p>
   * Creates a new <code>Session</code> on the underlying JMS <code>Connection</code>.
   * </p>
   * 
   * @param transacted true if transacted
   * @param acknowledgeMode acknowledge mode
   * @return a new <code>Session</code>
   * @throws JMSException if any occurs
   */
  public Session createSession(boolean transacted, int acknowledgeMode) throws JMSException {
    return getVendorImplementation().createSession(connection, transacted, acknowledgeMode);
  }

  public ConnectionFactory obtainConnectionFactory() throws Exception {
    return configuredVendorImplementation().createConnectionFactory();
  }

  private void connect() throws Exception {

    int attemptCount = 0;
    while (connection == null) {
      try {
        attemptCount++;
        if (additionalDebug()) {
          log.trace("Attempting connection to [{}]", getBrokerDetailsForLogging());
        }
        ConnectionFactory factory = obtainConnectionFactory();
        createConnection(factory);
      }
      catch (Exception e) {
        if (attemptCount == 1) {
          if (logWarning(attemptCount)) {
            log.warn("Connection attempt [{}] failed for {}", attemptCount, getBrokerDetailsForLogging(), e);
          }
          if (e instanceof JMSException) {
            if (((JMSException) e).getLinkedException() != null && additionalDebug()) {
              log.trace("Linked Exception Follows", ((JMSException) e).getLinkedException());
            }
          }
        }

        if (connectionAttempts() != -1 && attemptCount >= connectionAttempts()) {
          log.error("Failed to connect to broker [{}]", getBrokerDetailsForLogging(), e);
          throw e;
        }
        else {
          log.warn("Attempt [{}] failed for broker [{}], retrying", attemptCount, getBrokerDetailsForLogging());
          log.info(createLoggingStatement(attemptCount));
          Thread.sleep(connectionRetryInterval());
          continue;
        }
      }
      if (getClientId() != null && connection.getClientID() == null) {
        connection.setClientID(getClientId());
      }
    }
  }

  String getBrokerDetailsForLogging() {
    String result = configuredVendorImplementation() != null
        ? configuredVendorImplementation().retrieveBrokerDetailsForLogging()
        : null;

    if (result == null) {
      result = "No Connect Info";
    }

    return result;
  }

  protected void createConnection(ConnectionFactory factory) throws JMSException {
    try {
      if (isEmpty(configuredUserName())) {
        connection = factory.createConnection();
      }
      else {
        connection = factory.createConnection(configuredUserName(),
            Password.decode(ExternalResolver.resolve(configuredPassword())));
      }
    }
    catch (JMSException e) {
      throw e;
    }
    catch (Exception e) {
      JmsUtils.rethrowJMSException(e);
    }
  }

  @Override
  protected void prepareConnection() throws CoreException {
    getVendorImplementation().prepare();
  }

  public boolean connectionEquals(JmsConnection connection) {
    return this.getVendorImplementation().connectionEquals(connection.getVendorImplementation());
  }

  /**
   * <p>
   * Returns the broker user name.
   * </p>
   * 
   * @return the broker user name
   */
  public String getUserName() {
    return userName;
  }

  /**
   * <p>
   * Sets the broker user name.
   * </p>
   * 
   * @param s the broker user name
   */
  public void setUserName(String s) {
    userName = s;
  }

  /**
   * <p>
   * Sets the broker password.
   * </p>
   * 
   * @return the broker password
   */
  public String getPassword() {
    return password;
  }

  /**
   * <p>
   * Sets the broker password.
   * </p>
   * <p>
   * In additional to plain text passwords, the passwords can also be encoded using the appropriate {@link com.adaptris.security.password.Password}
   * </p>
   * 
   * @param s the broker password
   */
  public void setPassword(String s) {
    password = s;
  }

  /**
   * <p>
   * Sets the broker connection client ID.
   * </p>
   * 
   * @return the broker connection client ID
   */
  public String getClientId() {
    return clientId;
  }

  /**
   * <p>
   * Returns the broker connection client ID.
   * </p>
   * 
   * @param s the broker connection client ID
   */
  public void setClientId(String s) {
    clientId = s;
  }

  /**
   * <p>
   * Sets the <code>VendorImplementation</code> to use.
   * </p>
   * 
   * @return the <code>VendorImplementation</code> to use
   */
  public VendorImplementation getVendorImplementation() {
    return vendorImplementation;
  }

  /**
   * <p>
   * Returns the <code>VendorImplementation</code> to use.
   * </p>
   * 
   * @param imp the <code>VendorImplementation</code> to use
   */
  public void setVendorImplementation(VendorImplementation imp) {
    vendorImplementation = imp;
  }

  public Boolean getAdditionalDebug() {
    return additionalDebug;
  }

  /**
   * Whether or not to generate additional TRACE level debug when attempting connections.
   * 
   * @param b true to enable additional logging; default false.
   */
  public void setAdditionalDebug(Boolean b) {
    additionalDebug = b;
  }

  protected boolean additionalDebug() {
    return BooleanUtils.toBooleanDefaultIfNull(getAdditionalDebug(), false);
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

  @Override
  public JmsConnection cloneForTesting() throws CoreException {
    AdaptrisMarshaller m = DefaultMarshaller.getDefaultMarshaller();
    JmsConnection copy = (JmsConnection) m.unmarshal(m.marshal(this));
    // Set the client id to be null.
    copy.setClientId(null);
    return copy;
  }
}
