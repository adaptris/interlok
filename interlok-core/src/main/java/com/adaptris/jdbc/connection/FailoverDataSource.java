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

import java.io.PrintWriter;
import java.sql.Array;
import java.sql.Blob;
import java.sql.CallableStatement;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.NClob;
import java.sql.PreparedStatement;
import java.sql.SQLClientInfoException;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.sql.SQLWarning;
import java.sql.SQLXML;
import java.sql.Savepoint;
import java.sql.Statement;
import java.sql.Struct;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Executor;
import javax.sql.DataSource;
import org.apache.commons.pool.PoolableObjectFactory;
import org.apache.commons.pool.impl.GenericObjectPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Datasource that has a connection capable of failover to an alternate
 * database.
 * <p>
 * Because of we want to failover to an alternate database, the implementation
 * of <code>getConnection(String username, String password)
 *  </code> simply discards the username and password and relies on the
 * configuration that was supplied when constructing this class. This is the
 * desired behaviour.
 * </p>
 * <p>
 * The underlying pool implementation is a
 * <code>org.apache.commons.pool.impl.GenericObjectPool</code>. The default size
 * of the pool is 10 with a wait time of 20seconds. If the pool is exhausted,
 * then a NoSuchElementException will be thrown after the wait time.
 * </p>
 *
 * @see DataSource
 * @see FailoverConnection
 * @see FailoverConfig
 */
public class FailoverDataSource implements DataSource {

  private static final String[] REQUIRED_PROPERTIES =
  {
      FailoverConfig.JDBC_DRIVER, FailoverConfig.JDBC_AUTO_COMMIT,
      FailoverConfig.JDBC_TEST_STATEMENT
  };

  /**
   * Resource Key for the maximum size of the pool.
   *
   */
  public static final String POOL_MAX_SIZE = "failover.pool.maximum";
  /**
   * Resource Key for the time to wait for an available connection.
   *
   */
  public static final String POOL_TIME_TO_WAIT = "failover.pool.timetowait";

  protected transient Logger logR = LoggerFactory.getLogger(this.getClass());

  private FailoverConfig databaseConfig;
  private GenericObjectPool pool = null;
  private int poolMaximum;
  private long poolTimeToWait;

  private FailoverDataSource() {
  }

  public FailoverDataSource(Properties p) {
    this();
    init(p);
  }

  private void init(Properties p) {
    if (p == null) {
      throw new RuntimeException("No Configuration available ");
    }
    for (int i = 0; i < REQUIRED_PROPERTIES.length; i++) {
      if (!p.containsKey(REQUIRED_PROPERTIES[i])) {
        throw new RuntimeException("Missing Configuration " + REQUIRED_PROPERTIES[i]);
      }
    }
    databaseConfig = new FailoverConfig(p);
    poolMaximum = Integer.parseInt(p.getProperty(POOL_MAX_SIZE, "10"));
    poolTimeToWait = Integer.parseInt(p.getProperty(POOL_TIME_TO_WAIT, "20000"));
    pool = new GenericObjectPool(new PoolAttendant(databaseConfig), poolMaximum, GenericObjectPool.WHEN_EXHAUSTED_BLOCK,
        poolTimeToWait);
    pool.setTestOnBorrow(true);
    pool.setTestWhileIdle(true);
  }

  protected FailoverConfig config() {
    return databaseConfig;
  }

  protected int maxPoolSize() {
    return poolMaximum;
  }

  protected long timeToWait() {
    return poolTimeToWait;
  }

  protected void overrideObjectPool(GenericObjectPool p) throws Exception {
    destroy();
    pool = p;
  }

  protected void destroy() throws Exception {
    pool.close();
  }

  /**
   * Get the configured connection.
   *
   * @see javax.sql.DataSource#getConnection()
   */
  @Override
  public Connection getConnection() throws SQLException {
    Connection c = null;
    try {
      c = (Connection) pool.borrowObject();
    }
    catch (Exception e) {
      throw wrapSQLException(e);
    }
    return c;
  }


  /**
   * Get the configured connection.
   * <p>
   * This class will ignore the supplied credentials, relying on the
   * configuration used to create this source instead.
   * </p>
   * <p>
   * This can be considered breaking the javax.sql.DataSource contract, however,
   * this is the desired behaviour when you are failing over to multiple
   * databases.
   * </p>
   *
   * @see DataSource#getConnection(String, String)
   */
  @Override
  public Connection getConnection(String username, String password)
      throws SQLException {
    return getConnection();
  }

  /**
   * @see javax.sql.DataSource#setLoginTimeout(int)
   */
  @Override
  public void setLoginTimeout(int loginTimeout) throws SQLException {
    DriverManager.setLoginTimeout(loginTimeout);
  }

  /**
   * @see javax.sql.DataSource#getLoginTimeout()
   */
  @Override
  public int getLoginTimeout() throws SQLException {
    return DriverManager.getLoginTimeout();
  }

  /**
   * @see javax.sql.DataSource#setLogWriter(java.io.PrintWriter)
   */
  @Override
  public void setLogWriter(PrintWriter logWriter) throws SQLException {
    DriverManager.setLogWriter(logWriter);
  }

  /**
   * @see javax.sql.DataSource#getLogWriter()
   */
  @Override
  public PrintWriter getLogWriter() throws SQLException {
    return DriverManager.getLogWriter();
  }

  void returnConnection(ConnectionProxy p) throws SQLException {
    try {
      pool.returnObject(p);
    }
    catch (Exception e) {
      throw wrapSQLException(e);
    }
  }

  /**
   * @see java.sql.Wrapper#isWrapperFor(java.lang.Class)
   */
  @Override
  public boolean isWrapperFor(Class<?> iface) throws SQLException {
    return false;
  }

  /**
   * @see java.sql.Wrapper#unwrap(java.lang.Class)
   */
  @Override
  public <T> T unwrap(Class<T> iface) throws SQLException {
    throw new SQLException("Nothing to unwrap");
  }

  @Override
  public java.util.logging.Logger getParentLogger()
      throws SQLFeatureNotSupportedException {
    return null;
  }

  protected static SQLClientInfoException wrapSQLClientInfoException(Exception e) {
    if (e instanceof SQLClientInfoException) {
      return (SQLClientInfoException) e;
    }
    SQLClientInfoException e2 = new SQLClientInfoException();
    e2.initCause(e);
    return e2;
  }

  protected static SQLException wrapSQLException(Exception e) {
    if (e instanceof SQLException) {
      return (SQLException) e;
    }
    return new SQLException(e.getMessage(), e);
  }

  /**
   * This class is reponsible for creating swimmers who swim in the Datasource
   * Pool.
   */
  protected class PoolAttendant implements PoolableObjectFactory {

    private FailoverConfig config;

    PoolAttendant(FailoverConfig config) {
      this.config = config;
    }

    /**
     * @see PoolableObjectFactory#makeObject()
     */
    @Override
    public Object makeObject() throws Exception {
      ConnectionProxy conn = new ConnectionProxy(config);
      return conn;
    }

    /**
     * Validate the object.
     * <p>
     * Basically attempts to get an instance of the underlying connection. If
     * the database has failed, it will throw an SQLException.
     *
     * @see PoolableObjectFactory#validateObject(java.lang.Object)
     *
     */
    @Override
    public boolean validateObject(Object obj) {
      if (null == obj) {
        return false;
      }
      try {
        ConnectionProxy conn = (ConnectionProxy) obj;
        conn.getWrappedConnection();
      }
      catch (Exception e) {
        return false;
      }
      return true;
    }

    /**
     * @see PoolableObjectFactory#destroyObject(java.lang.Object)
     */
    @Override
    public void destroyObject(Object arg0) throws Exception {
      if (arg0.getClass().equals(ConnectionProxy.class)) {
        ConnectionProxy c = (ConnectionProxy) arg0;
        c.getWrappedConnection().close();
      }
    }

    /**
     * In this implementation it does nothing.
     *
     * @see PoolableObjectFactory#activateObject(java.lang.Object)
     */
    @Override
    public void activateObject(Object arg0) throws Exception {
    }

    /**
     * In this implementation it does nothing.
     *
     * @see PoolableObjectFactory#passivateObject(java.lang.Object)
     */
    @Override
    public void passivateObject(Object arg0) throws Exception {
    }
  }

  /**
   * This class proxies the underlying FailoverConnection class.
   * <p>
   * It directly implements Connection which is a front to the underlying
   * FailoverConnection.getConnection(), the only difference is that when
   * close() is called, it is simply replaced back into the pool until required.
   * <p>
   * Finalization of the Datasource itself will shutdown the pool, and close any
   * resources that are held by this object.
   * </p>
   */
  @SuppressWarnings({"lgtm [java/sql-injection]"})
  protected class ConnectionProxy implements Connection {
    private FailoverConnection conn;

    public ConnectionProxy(FailoverConfig conf) throws SQLException {
      conn = new FailoverConnection(conf);
    }

    Connection getWrappedConnection() throws SQLException {
      return conn.getConnection();
    }

    /**
     * @see java.sql.Connection#createStatement()
     */
    @Override
    public Statement createStatement() throws SQLException {
      return getWrappedConnection().createStatement();
    }

    /**
     * @see java.sql.Connection#prepareStatement(java.lang.String)
     */
    @Override
    public PreparedStatement prepareStatement(String sql) throws SQLException {
      return getWrappedConnection().prepareStatement(sql);
    }

    /**
     * @see java.sql.Connection#prepareCall(java.lang.String)
     */
    @Override
    @SuppressWarnings({"lgtm[java/sql-injection]"})
    public CallableStatement prepareCall(String sql) throws SQLException {
      return getWrappedConnection().prepareCall(sql);
    }

    /**
     * @see java.sql.Connection#nativeSQL(java.lang.String)
     */
    @Override
    public String nativeSQL(String sql) throws SQLException {
      return getWrappedConnection().nativeSQL(sql);
    }

    /**
     * @see java.sql.Connection#setAutoCommit(boolean)
     */
    @Override
    public void setAutoCommit(boolean autoCommit) throws SQLException {
      getWrappedConnection().setAutoCommit(autoCommit);
    }

    /**
     * @see java.sql.Connection#getAutoCommit()
     */
    @Override
    public boolean getAutoCommit() throws SQLException {
      return getWrappedConnection().getAutoCommit();
    }

    /**
     * @see java.sql.Connection#commit()
     */
    @Override
    public void commit() throws SQLException {
      getWrappedConnection().commit();
    }

    /**
     * @see java.sql.Connection#rollback()
     */
    @Override
    public void rollback() throws SQLException {
      getWrappedConnection().rollback();
    }

    /**
     * @see java.sql.Connection#close()
     */
    @Override
    public void close() throws SQLException {
      returnConnection(this);
    }

    /**
     * @see java.sql.Connection#isClosed()
     */
    @Override
    public boolean isClosed() throws SQLException {
      return getWrappedConnection().isClosed();
    }

    /**
     * @see java.sql.Connection#getMetaData()
     */
    @Override
    public DatabaseMetaData getMetaData() throws SQLException {
      return getWrappedConnection().getMetaData();
    }

    /**
     * @see java.sql.Connection#setReadOnly(boolean)
     */
    @Override
    public void setReadOnly(boolean readOnly) throws SQLException {
      getWrappedConnection().setReadOnly(readOnly);
    }

    /**
     * @see java.sql.Connection#isReadOnly()
     */
    @Override
    public boolean isReadOnly() throws SQLException {
      return getWrappedConnection().isReadOnly();
    }

    /**
     * @see java.sql.Connection#setCatalog(java.lang.String)
     */
    @Override
    public void setCatalog(String catalog) throws SQLException {
      getWrappedConnection().setCatalog(catalog);
    }

    /**
     * @see java.sql.Connection#getCatalog()
     */
    @Override
    public String getCatalog() throws SQLException {
      return getWrappedConnection().getCatalog();
    }

    /**
     * @see java.sql.Connection#setTransactionIsolation(int)
     */
    @Override
    public void setTransactionIsolation(int level) throws SQLException {
      getWrappedConnection().setTransactionIsolation(level);
    }

    /**
     * @see java.sql.Connection#getTransactionIsolation()
     */
    @Override
    public int getTransactionIsolation() throws SQLException {
      return getWrappedConnection().getTransactionIsolation();
    }

    /**
     * @see java.sql.Connection#getWarnings()
     */
    @Override
    public SQLWarning getWarnings() throws SQLException {
      return getWrappedConnection().getWarnings();
    }

    /**
     * @see java.sql.Connection#clearWarnings()
     */
    @Override
    public void clearWarnings() throws SQLException {
      getWrappedConnection().clearWarnings();
    }

    /**
     * @see java.sql.Connection#createStatement(int, int)
     */
    @Override
    public Statement createStatement(int i, int j) throws SQLException {
      return getWrappedConnection().createStatement(i, j);
    }

    /**
     * @see java.sql.Connection#prepareStatement(String, int, int)
     */
    @Override
    public PreparedStatement prepareStatement(String sql, int i, int j)
        throws SQLException {
      return getWrappedConnection().prepareStatement(sql, i, j);
    }

    /**
     * @see java.sql.Connection#prepareCall(java.lang.String, int, int)
     */
    @Override
    public CallableStatement prepareCall(String sql, int i, int j)
        throws SQLException {
      return getWrappedConnection().prepareCall(sql, i, j);
    }

    /**
     * @see java.sql.Connection#getTypeMap()
     */
    @Override
    public Map getTypeMap() throws SQLException {
      return getWrappedConnection().getTypeMap();
    }

    /**
     * @see java.sql.Connection#setHoldability(int)
     */
    @Override
    public void setHoldability(int holdability) throws SQLException {
      getWrappedConnection().setHoldability(holdability);
    }

    /**
     * @see java.sql.Connection#getHoldability()
     */
    @Override
    public int getHoldability() throws SQLException {
      return getWrappedConnection().getHoldability();
    }

    /**
     * @see java.sql.Connection#setSavepoint()
     */
    @Override
    public Savepoint setSavepoint() throws SQLException {
      return getWrappedConnection().setSavepoint();
    }

    /**
     * @see java.sql.Connection#setSavepoint(java.lang.String)
     */
    @Override
    public Savepoint setSavepoint(String name) throws SQLException {
      return getWrappedConnection().setSavepoint(name);
    }

    /**
     * @see java.sql.Connection#rollback(java.sql.Savepoint)
     */
    @Override
    public void rollback(Savepoint savepoint) throws SQLException {
      getWrappedConnection().rollback(savepoint);
    }

    /**
     * @see java.sql.Connection#releaseSavepoint(java.sql.Savepoint)
     */
    @Override
    public void releaseSavepoint(Savepoint savepoint) throws SQLException {
      getWrappedConnection().releaseSavepoint(savepoint);
    }

    /**
     * @see java.sql.Connection#createStatement(int, int, int)
     */
    @Override
    public Statement createStatement(int i, int j, int k) throws SQLException {
      return getWrappedConnection().createStatement(i, j, k);
    }

    /**
     * @see java.sql.Connection#prepareStatement(String, int, int, int)
     */
    @Override
    public PreparedStatement prepareStatement(String sql, int i, int j, int k)
        throws SQLException {
      return getWrappedConnection().prepareStatement(sql, i, j, k);
    }

    /**
     * @see java.sql.Connection#prepareCall(java.lang.String, int, int, int)
     */
    @Override
    public CallableStatement prepareCall(String sql, int i, int j, int k)
        throws SQLException {
      return getWrappedConnection().prepareCall(sql, i, j, k);
    }

    /**
     * @see java.sql.Connection#prepareStatement(java.lang.String, int)
     */
    @Override
    public PreparedStatement prepareStatement(String sql, int i)
        throws SQLException {
      return getWrappedConnection().prepareStatement(sql, i);
    }

    /**
     * @see java.sql.Connection#prepareStatement(java.lang.String, int[])
     */
    @Override
    public PreparedStatement prepareStatement(String sql, int[] columnIndexes)
        throws SQLException {
      return getWrappedConnection().prepareStatement(sql, columnIndexes);
    }

    /**
     * @see java.sql.Connection#prepareStatement(String, String[])
     */
    @Override
    public PreparedStatement prepareStatement(String sql, String[] columnNames)
        throws SQLException {
      return getWrappedConnection().prepareStatement(sql, columnNames);
    }

    /**
     * @see java.sql.Connection#createArrayOf(java.lang.String,
     *      java.lang.Object[])
     */
    @Override
    public Array createArrayOf(String typeName, Object[] elements)
        throws SQLException {
      return getWrappedConnection().createArrayOf(typeName, elements);
    }

    /**
     * @see java.sql.Connection#createBlob()
     */
    @Override
    public Blob createBlob() throws SQLException {
      return getWrappedConnection().createBlob();
    }

    /**
     * @see java.sql.Connection#createClob()
     */
    @Override
    public Clob createClob() throws SQLException {
      return getWrappedConnection().createClob();
    }

    /**
     * @see java.sql.Connection#createNClob()
     */
    @Override
    public NClob createNClob() throws SQLException {
      return getWrappedConnection().createNClob();
    }

    /**
     * @see java.sql.Connection#createSQLXML()
     */
    @Override
    public SQLXML createSQLXML() throws SQLException {
      return getWrappedConnection().createSQLXML();
    }

    /**
     * @see java.sql.Connection#createStruct(java.lang.String,
     *      java.lang.Object[])
     */
    @Override
    public Struct createStruct(String typeName, Object[] attributes)
        throws SQLException {
      return getWrappedConnection().createStruct(typeName, attributes);
    }

    /**
     * @see java.sql.Connection#getClientInfo()
     */
    @Override
    public Properties getClientInfo() throws SQLException {
      return getWrappedConnection().getClientInfo();
    }

    /**
     * @see java.sql.Connection#getClientInfo(java.lang.String)
     */
    @Override
    public String getClientInfo(String name) throws SQLException {
      return getWrappedConnection().getClientInfo(name);
    }

    /**
     * @see java.sql.Connection#isValid(int)
     */
    @Override
    public boolean isValid(int timeout) throws SQLException {
      return getWrappedConnection().isValid(timeout);
    }

    /**
     * @see java.sql.Connection#setClientInfo(java.util.Properties)
     */
    @Override
    public void setClientInfo(Properties properties)
        throws SQLClientInfoException {
      try {
        getWrappedConnection().setClientInfo(properties);
      }
      catch (SQLException e) {
        throw wrapSQLClientInfoException(e);

      }
    }

    /**
     * @see java.sql.Connection#setClientInfo(java.lang.String,
     *      java.lang.String)
     */
    @Override
    public void setClientInfo(String name, String value)
        throws SQLClientInfoException {
      try {
        getWrappedConnection().setClientInfo(name, value);
      }
      catch (SQLException e) {
        throw wrapSQLClientInfoException(e);
      }
    }

    /**
     * @see java.sql.Connection#setTypeMap(java.util.Map)
     */
    @Override
    public void setTypeMap(Map<String, Class<?>> map) throws SQLException {
      getWrappedConnection().setTypeMap(map);
    }

    /**
     * @see java.sql.Wrapper#isWrapperFor(java.lang.Class)
     */
    @Override
    public boolean isWrapperFor(Class<?> iface) throws SQLException {
      return getWrappedConnection().isWrapperFor(iface);
    }

    /**
     * @see java.sql.Wrapper#unwrap(java.lang.Class)
     */
    @Override
    public <T> T unwrap(Class<T> iface) throws SQLException {
      return getWrappedConnection().unwrap(iface);
    }

    @Override
    public void setSchema(String schema) throws SQLException {
      getWrappedConnection().setSchema(schema);
    }

    @Override
    public String getSchema() throws SQLException {
      return getWrappedConnection().getSchema();
    }

    @Override
    public void abort(Executor executor) throws SQLException {
      getWrappedConnection().abort(executor);
    }

    @Override
    public void setNetworkTimeout(Executor executor, int milliseconds) throws SQLException {
      getWrappedConnection().setNetworkTimeout(executor, milliseconds);
    }

    @Override
    public int getNetworkTimeout() throws SQLException {
      return getWrappedConnection().getNetworkTimeout();
    }
  }

}
