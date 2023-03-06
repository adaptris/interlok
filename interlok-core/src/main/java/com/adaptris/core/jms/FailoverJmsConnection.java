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

import java.util.ArrayList;
import java.util.List;
import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.Session;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import org.apache.commons.lang3.BooleanUtils;
import com.adaptris.annotation.AdapterComponent;
import com.adaptris.annotation.AdvancedConfig;
import com.adaptris.annotation.AutoPopulated;
import com.adaptris.annotation.ComponentProfile;
import com.adaptris.annotation.DisplayOrder;
import com.adaptris.annotation.InputFieldDefault;
import com.adaptris.core.AdaptrisConnection;
import com.adaptris.core.CoreException;
import com.adaptris.core.StateManagedComponent;
import com.adaptris.core.util.Args;
import com.adaptris.core.util.LifecycleHelper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * JmsConnection implementation that wraps a list of concrete JMSConnection instances to provide failover.
 * <p>
 * This class is designed for use with JMS Brokers that do not provide any transparent failover facility. For instance, SonicMQ
 * provides transparent failover, so you should use that instead rather than having another concrete JMSConnection instance.
 * </p>
 * <p>
 * Note the while this JmsConnection implementation still exposes connection configuration, these will be ignored, as all connection
 * based activity is delegated to the underlying JmsConnections. The only configuration that overrides the underlying JmsConnection
 * configuration is the connection-attempts and connection-wait.
 * </p>
 * 
 * @config failover-jms-connection
 * 
 * @author sellidge
 * @author $Author: lchan $
 */
@JacksonXmlRootElement(localName = "failover-jms-connection")
@XStreamAlias("failover-jms-connection")
@AdapterComponent
@ComponentProfile(summary = "Connect to a JMS 1.1 broker supporting broker failover in a vendor independent way",
    tag = "connections,jms")
@DisplayOrder(order = {"registerOwner"})
public class FailoverJmsConnection extends JmsConnection {

  @NotNull
  @AutoPopulated
  @Valid
  private List<JmsConnection> connections = null;
  private transient JmsConnection current;
  @AdvancedConfig(rare = true)
  @InputFieldDefault(value = "false")
  private Boolean registerOwner;

  public FailoverJmsConnection() {
    super();
    setUserName(null);
    setPassword(null);
    setVendorImplementation(null);
    setConnections(new ArrayList<JmsConnection>());
  }

  FailoverJmsConnection(List<JmsConnection> c) {
    this();
    setConnections(c);
  }

  /**
   * @see com.adaptris.core.AdaptrisConnectionImp#init()
   */
  @Override
  protected void initConnection() throws CoreException {
    boolean connected = false;
    int attempts = 0;
    while (!connected) {
      attempts++;
      for (JmsConnection con : connections) {
        current = nullifyRetry(con);
        try {
          current.setConnectionAttempts(1);
          registerExceptionListeners(current);
          LifecycleHelper.init(current);
          super.connection = current.connection;
          connected = true;
          break;
        }
        catch (CoreException e) {
          // Nothing to do
        }
      }

      if (connected == false) {
        log.warn("Failed in all attempts to connect to brokers");
        if (attempts > connectionAttempts() && connectionAttempts() > -1) {
          throw new CoreException("Failed in all attempts to connect to brokers");
        }
        else {
          try {
            log.info("Failed to connect to all listed brokers, waiting for retry");
            Thread.sleep(connectionRetryInterval());
          }
          catch (InterruptedException e2) {
            throw new CoreException("Received interrupt before (re)connecting to brokers", e2);
          }
        }
      }
    }
  }

  private void registerExceptionListeners(AdaptrisConnection conn) {
    if (registerOwner()) {
      for (StateManagedComponent c : retrieveExceptionListeners()) {
        conn.addExceptionListener(c);
      }
    }
    else {
      conn.addExceptionListener(this);
    }
  }

  /**
   *
   * @see com.adaptris.core.jms.JmsConnection#startConnection()
   */
  @Override
  protected void startConnection() throws CoreException {
    LifecycleHelper.start(current);
  }

  /**
   *
   * @see com.adaptris.core.jms.JmsConnection#stopConnection()
   */
  @Override
  protected void stopConnection() {
    LifecycleHelper.stop(current);

  }

  /**
   *
   * @see com.adaptris.core.jms.JmsConnection#closeConnection()
   */
  @Override
  public void closeConnection() {
    LifecycleHelper.close(current);
  }

  /**
   * <p>
   * Creates a new <code>Session</code> on the underlying JMS
   * <code>Connection</code>.
   * </p>
   *
   * @param transacted true if transacted
   * @param acknowledgeMode acknowledge mode
   * @return a new <code>Session</code>
   * @throws JMSException if any occurs
   */
  @Override
  public Session createSession(boolean transacted, int acknowledgeMode)
      throws JMSException {
    return current.createSession(transacted, acknowledgeMode);
  }

  @Override
  protected void createConnection(ConnectionFactory factory) throws Exception {
    current.createConnection(factory);
  }

  public List<JmsConnection> getConnections() {
    return connections;
  }

  public void addConnection(JmsConnection c) {
    connections.add(nullifyRetry(c));
  }

  public void setConnections(List<JmsConnection> l) {
    connections = Args.notNull(l, "connections");
  }

  private JmsConnection nullifyRetry(JmsConnection c) {
    c.setConnectionRetryInterval(null);
    c.setConnectionAttempts(null);
    return c;
  }

  /**
   * @return the registerOwner
   * @see #setRegisterOwner(Boolean)
   */
  public Boolean getRegisterOwner() {
    return registerOwner;
  }

  /**
   * Specify whether to register this instance or its owner with the underlying
   * connection
   *
   * @param b the registerOwner to set, defaults to false which registers this
   *          instance as the owner of the underlying connection.
   */
  public void setRegisterOwner(Boolean b) {
    registerOwner = b;
  }

  boolean registerOwner() {
    return BooleanUtils.toBooleanDefaultIfNull(getRegisterOwner(), false);
  }

  JmsConnection currentJmsConnection() {
    return current;
  }

  @Override
  protected void prepareConnection() throws CoreException {
    for (JmsConnection c : connections) {
      LifecycleHelper.prepare(c);
    }
  }

  @Override
  public ConnectionFactory obtainConnectionFactory() throws Exception {
    return current.obtainConnectionFactory();
  }

  @Override
  public Connection currentConnection() {
    return current.currentConnection();
  }

  @Override
  public String configuredClientId() {
    return current.getClientId();
  }

  @Override
  public String configuredPassword() {
    return current.getPassword();
  }

  @Override
  public String configuredUserName() {
    return current.getUserName();
  }

  @Override
  public VendorImplementation configuredVendorImplementation() {
    return current.getVendorImplementation();
  }

  @Override
  public boolean connectionEquals(JmsConnection connection) {
    if (current != null) {
      return current.connectionEquals(connection);
    }
    return false;
  }
}
