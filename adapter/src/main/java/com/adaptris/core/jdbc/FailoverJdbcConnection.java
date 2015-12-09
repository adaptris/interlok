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

package com.adaptris.core.jdbc;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.validation.constraints.NotNull;

import com.adaptris.annotation.AdapterComponent;
import com.adaptris.annotation.AutoPopulated;
import com.adaptris.annotation.ComponentProfile;
import com.adaptris.core.CoreException;
import com.adaptris.jdbc.connection.FailoverConfig;
import com.adaptris.jdbc.connection.FailoverConnection;
import com.adaptris.security.exc.PasswordException;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamImplicit;

/**
 * <p>
 * An <code>AdaptrisConnection</code> implementation for a failover database connection.
 * </p>
 * <p>
 * This class simply proxies <code>com.adaptris.jdbc.connection.FailoverConnection</code> with the required AdaptrisConnection
 * methods.
 * </p>
 * 
 * @config failover-jdbc-connection
 * 
 * @author lchan
 * @see com.adaptris.jdbc.connection.FailoverConnection
 */
@XStreamAlias("failover-jdbc-connection")
@AdapterComponent
@ComponentProfile(summary = "Connect using a database using a JDBC driver supporting database failover in a vendor neutral fashion",
    tag = "connections,jdbc")
public class FailoverJdbcConnection extends DatabaseConnection {

  private transient FailoverConnection failover = null;
  @NotNull
  @AutoPopulated
  @XStreamImplicit(itemFieldName = "connect-url")
  private List<String> connectUrls;

  /**
   * <p>
   * Creates a new instance.
   * </p>
   * <ul>
   * </li>alwaysValidateConnection is true, changing that to false has no
   * meaning as it would be impossible to failover to another connection</li>
   * </ul>
   */
  public FailoverJdbcConnection() {
    super();
    setConnectUrls(new ArrayList<String>());
    setAlwaysValidateConnection(true);
  }

  /** @see com.adaptris.core.jdbc.DatabaseConnection#makeConnection() */
  @Override
  protected Connection makeConnection() throws SQLException {
    return failover.getConnection();
  }

  @Override
  protected void stopDatabaseConnection() {
    // Nothing to do.
  }

  /**
   *
   * @see com.adaptris.core.AdaptrisConnectionImp#closeConnection()
   */
  @Override
  protected void closeDatabaseConnection() {
    try {
      failover.close();
    }
    catch (Exception e) {
      log.trace("Failed to shutdown component cleanly, logging exception for informational purposes only", e);
    }
    finally {
      failover = null;
    }
  }

  /**
   *
   * @see com.adaptris.core.jdbc.DatabaseConnection#initConnection()
   */
  @Override
  protected void startDatabaseConnection() throws CoreException {
  }

  /**
   *
   * @see com.adaptris.core.jdbc.DatabaseConnection#initConnection()
   */
  @Override
  protected void initialiseDatabaseConnection() throws CoreException {
    FailoverConfig config = new FailoverConfig();
    try {
      config.getConnectionUrls().addAll(connectUrls);
      config.setAutoCommit(autoCommit());
      config.setDatabaseDriver(getDriverImp());
      config.setDebugMode(debugMode());
      config.setTestStatement(getTestStatement());
      config.setAlwaysValidateConnection(alwaysValidateConnection());
      config.setConnectionProperties(connectionProperties());
      failover = new FailoverConnection(config);
      connect();
    }
    catch (PasswordException e) {
      log.error("Failed to decode password for database");
      throw new CoreException(e);
    }
    catch (Exception e) {
      throw new CoreException(e);
    }
  }

  /**
   * Add a Connection URL to the configured list.
   *
   * @param s a connection url
   */
  public void addConnectUrl(String s) {
    connectUrls.add(s);
  }

  /**
   * Get the configured list of URLs.
   *
   * @return the list of urls
   */
  public List<String> getConnectUrls() {
    return connectUrls;
  }

  /**
   * Set the configured list of URLs.
   *
   * @param l the list of urls
   */
  public void setConnectUrls(List<String> l) {
    connectUrls = l;
  }

  /** @see java.lang.Object#toString() */
  @Override
  public String toString() {
    StringBuffer result = new StringBuffer();

    result.append("[");
    result.append(this.getClass().getName());
    result.append("] driverImp [");
    result.append(getDriverImp());
    result.append("] connectUrls [");
    result.append(getConnectUrls());
    result.append("] autoCommit [");
    result.append(autoCommit());
    result.append("]");

    return result.toString();
  }

  /**
   * <p>
   * Another <code>FailoverJdbcConnection</code> is semantically equal to
   * <code>this</code> if the results of the following method calls on both
   * objects are equal.
   * <ul>
   * <li>getConnectUrls</li>
   * <li>getAlwaysValidateConnection</li>
   * <li>getAutoCommit</li>
   * <li>getDebugMode</li>
   * <li>getDriverImp</li>
   * <li>getTestStatement</li>
   * </ul>
   * </p>
   *
   * @see java.lang.Object#equals(java.lang.Object)
   */
  @Override
  public boolean equals(Object o) {
    boolean rc = false;
    if (o instanceof FailoverJdbcConnection) {
      FailoverJdbcConnection f = (FailoverJdbcConnection) o;
      rc = getConnectUrls().equals(f.getConnectUrls()) && alwaysValidateConnection() == f.alwaysValidateConnection()
          && autoCommit() == f.autoCommit() && debugMode() == f.debugMode() && getDriverImp().equals(f.getDriverImp())
          && getTestStatement().equals(getTestStatement());
    }
    return rc;
  }

  /**
   * <p>
   * Hashcode is determined by the <code>hashCode</code>s of the return values
   * of the methods specified in <code>equals</code>.
   * </p>
   *
   * @return the hashCode
   * @see FailoverJdbcConnection#equals
   * @see java.lang.Object#hashCode()
   * */
  @Override
  public int hashCode() {
    return getConnectUrls().hashCode() + Boolean.valueOf(alwaysValidateConnection()).hashCode()
        + Boolean.valueOf(autoCommit()).hashCode() + Boolean.valueOf(debugMode()).hashCode() + getDriverImp().hashCode()
        + getTestStatement().hashCode();
  }

  /** @see com.adaptris.core.jdbc.DatabaseConnection#getConnectionName() */
  @Override
  protected String getConnectionName() {
    return getConnectUrls().toString();
  }

}
