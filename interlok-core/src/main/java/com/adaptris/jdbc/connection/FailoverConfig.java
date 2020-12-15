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

package com.adaptris.jdbc.connection;

import static org.apache.commons.lang3.StringUtils.isEmpty;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import com.adaptris.core.util.Args;

/**
 * Class that is used to configure a failover database connection.
 *
 * @see FailoverConnection
 * @author lchan
 * @author $Author: lchan $
 */
public final class FailoverConfig implements Cloneable {

  private boolean verbose;
  private List<String> connectionUrls;
  private String databaseDriver;
  private boolean autoCommit;
  private boolean alwaysValidateConnection;
  private String username;
  private String password;
  private Properties connectionProperties;

  private static final String MYSQL_DRIVER = "com.mysql.jdbc.Driver";
  private static final String MYSQL_TEST_STMT = "SELECT DATABASE(), VERSION(), NOW(), USER();";

  /**
   * resource key for driver classname
   */
  public static final String JDBC_DRIVER = "jdbc.driver.classname";

  /**
   * resource key for driver url
   */
  public static final String JDBC_URL_ROOT = "jdbc.driver.url";

  /**
   * resource key for driver autocommit setting
   */
  public static final String JDBC_AUTO_COMMIT = "jdbc.driver.autocommit";

  /**
   * resource key for connection verify setting
   */
  public static final String JDBC_ALWAYS_VERIFY = "jdbc.driver.alwaysverify";

  /**
   * resource key specifying extra debug.
   *
   */
  public static final String JDBC_DEBUG = "jdbc.driver.debug";

  /**
   * resource key for specifying the username
   */
  public static final String JDBC_USERNAME = "jdbc.username";

  /**
   * resource key for specifying the password
   */
  public static final String JDBC_PASSWORD = "jdbc.password";

  /**
   * @see Object#Object()
   *
   *
   */
  public FailoverConfig() {
    setConnectionUrls(new ArrayList<String>());
    setDatabaseDriver(MYSQL_DRIVER);
    setAutoCommit(true);
    setAlwaysValidateConnection(true);
    setConnectionProperties(new Properties());
    setUsername(null);
    setPassword(null);
  }

  /**
   * Constructor using a pre-existing map.
   * <p>
   * This allows us to easily configure the repository from a set of properties.
   * </p>
   * <p>
   * The properties object is expected to contain the following keys with associated values
   * <ul>
   * <li>jdbc.driver.classname - this represents the jdbc driver classname</li>
   * <li>jdbc.driver.url.n - where n is some unique identifier. Each of these entries will specify a database connection string in
   * the general form <code>jdbc:mysql://localhost:3306/portal</code>. There must be at least one of these entries. Each entry is
   * used to as a parameter to FailoverConfig.addConnectionUrl(String s)
   * <li>jdbc.driver.autocommit - either true or false, the default is true
   * <li>jdbc.username - the username for the connection - this is optional, if all connection urls have a username/password inline.
   * <li>jdbc.password - password for the connection - this is optional, if all connection urls have a username/password inline.
   * </ul>
   * </p>
   * <p>
   * If there is more than one <code>jdbc.driver.url</code> key then the natural ordering of the keys is used to specify the order
   * in which urls are added to the <code>FailoverConfig</code> object.
   * <p>
   * An Example property file would be:-
   * 
   * <pre>
   * {@code 
   * jdbc.driver.classname=com.mysql.jdbc.Driver
   * jdbc.driver.url.1=jdbc:mysql://master:3306/portal?user=user
   * jdbc.driver.url.2=jdbc:mysql://slave1:3306/portal?user=user
   * jdbc.driver.url.3=jdbc:mysql://slave2:3306/portal?user=user
   * }
   * </pre>
   * 
   * @param map the <code>Map</code> from which we will initialise from.
   * @see #JDBC_DRIVER
   * @see #JDBC_URL_ROOT
   * @see FailoverConfig#addConnectionUrl(String)
   */
  public FailoverConfig(Properties map) {
    this();
    initialise(map);
  }

  /**
   * Set a Connection URL to the configured list.
   *
   * @param list a list of connection urls
   * @throws IllegalArgumentException if the string is null.
   */
  public void setConnectionUrls(List<String> list) {
    connectionUrls = Args.notNull(list, "connectionURLs");
  }

  /**
   * Add a Connection URL to the configured list.
   *
   * @param string a connection url
   * @throws IllegalArgumentException if the string is null.
   */
  public void addConnectionUrl(String string) {
    connectionUrls.add(Args.notBlank(string, "connectionURL"));
  }

  /**
   * Get the configured list of URLs.
   *
   * @return The configured collection of urls
   */
  public List<String> getConnectionUrls() {
    return connectionUrls;
  }

  /**
   * Get the configured database Driver for this config
   *
   * @return the database driver
   */
  public String getDatabaseDriver() {
    return databaseDriver;
  }

  /**
   * Set the database driver.
   *
   * @param string the database driver
   * @throws IllegalArgumentException if the driver is null.
   */
  public void setDatabaseDriver(String string) {
    databaseDriver = Args.notBlank(string, "databaseDriver");
  }

  /**
   * @see java.lang.Object#equals(java.lang.Object)
   */
  @Override
  public boolean equals(Object o) {
    if (o == null) {
      return false;
    }
    if (o == this) {
      return true;
    }
    if (o instanceof FailoverConfig) {
      FailoverConfig rhs = (FailoverConfig) o;
      return new EqualsBuilder()
          .append(getDebugMode(), rhs.getDebugMode())
          .append(getDatabaseDriver(), rhs.getDatabaseDriver()).append(getAutoCommit(), rhs.getAutoCommit())
          .append(getAlwaysValidateConnection(), rhs.getAlwaysValidateConnection())
          .append(getConnectionUrls(), rhs.getConnectionUrls()).isEquals();
    }
    return false;
  }

  /**
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode() {
    return new HashCodeBuilder(31, 17).append(getDatabaseDriver())
        .append(getAutoCommit()).append(getAlwaysValidateConnection()).append(getConnectionUrls())
        .append(getDebugMode())
        .toHashCode();
  }

  @Override
  public String toString() {
    return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE).append("driver", getDatabaseDriver())
        .append("connectionUrls", getConnectionUrls())
        .append("debugMode", getDebugMode())
        .append("autoCommit", getAutoCommit())
        .append("alwaysValidate", getAlwaysValidateConnection())
        .toString();
  }

  /**
   * Get autoCommit flag for this configuration
   *
   * @return the autoCommit flag
   */
  public boolean getAutoCommit() {
    return autoCommit;
  }

  /**
   * Set the autocommit flag for this configuration
   *
   * @param b the new flag
   */
  public void setAutoCommit(boolean b) {
    autoCommit = b;
  }

  /**
   * Return whether the database connection should have further verbose logging.
   *
   * @return true or false.
   */
  public boolean getDebugMode() {
    return verbose;
  }

  /**
   * Specify verbose logging.
   *
   * @param b true or false.
   */
  public void setDebugMode(boolean b) {
    verbose = b;
  }

  /**
   * Perform initialisation from some map.
   *
   * @param p the map containing the properties.
   */
  private void initialise(Properties p) {
    setDatabaseDriver(p.getProperty(JDBC_DRIVER, MYSQL_DRIVER));
    setAutoCommit(Boolean.valueOf(p.getProperty(JDBC_AUTO_COMMIT, "true")));
    setDebugMode(Boolean.valueOf(p.getProperty(JDBC_DEBUG, "false")));
    setAlwaysValidateConnection(Boolean.valueOf(p.getProperty(JDBC_ALWAYS_VERIFY, "true")));
    if (!isEmpty(p.getProperty(JDBC_USERNAME))) {
      setUsername(p.getProperty(JDBC_USERNAME));
    }
    if (!isEmpty(p.getProperty(JDBC_PASSWORD))) {
      setPassword(p.getProperty(JDBC_PASSWORD));
    }
    List<String> connectionUrls = new ArrayList<String>();
    for (Iterator i = p.keySet().iterator(); i.hasNext();) {
      String key = (String) i.next();
      if (key.startsWith(JDBC_URL_ROOT)) {
        connectionUrls.add(p.getProperty(key));
      }
    }
    // Bug#2555
    // for (String key : p.stringPropertyNames()) {
    // if (key.startsWith(JDBC_URL_ROOT)) {
    // connectionUrls.add(p.getProperty(key));
    // }
    // }
    setConnectionUrls(connectionUrls);
  }

  /**
   * @see java.lang.Object#clone()
   */
  @Override
  public Object clone() throws CloneNotSupportedException {
    FailoverConfig result = (FailoverConfig) super.clone();
    result.setConnectionUrls(new ArrayList<String>(getConnectionUrls()));
    return result;
  }

  public boolean getAlwaysValidateConnection() {
    return alwaysValidateConnection;
  }

  public void setAlwaysValidateConnection(boolean b) {
    alwaysValidateConnection = b;
  }

  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
  }

  public Properties getConnectionProperties() {
    return connectionProperties;
  }

  public void setConnectionProperties(Properties p) {
    this.connectionProperties = p;
  }
}
