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

import java.sql.Array;
import java.sql.Blob;
import java.sql.CallableStatement;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.NClob;
import java.sql.PreparedStatement;
import java.sql.SQLClientInfoException;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.SQLXML;
import java.sql.Savepoint;
import java.sql.Statement;
import java.sql.Struct;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Executor;
import com.adaptris.core.util.JdbcUtil;

public class ProxySqlConnection implements Connection {

  private Connection connection;

  public ProxySqlConnection(Connection c) {
    connection = c;
  }

  /**
   * @see java.sql.Connection#createStatement()
   */
  @Override
  public Statement createStatement() throws SQLException {
    return getConnection().createStatement();
  }

  /**
   * @see java.sql.Connection#prepareStatement(java.lang.String)
   */
  @Override
  public PreparedStatement prepareStatement(String sql) throws SQLException {
    return getConnection().prepareStatement(sql);
  }

  /**
   * @see java.sql.Connection#prepareCall(java.lang.String)
   */
  @Override
  @SuppressWarnings({"lgtm[java/sql-injection]"})
  public CallableStatement prepareCall(String sql) throws SQLException {
    return getConnection().prepareCall(sql);
  }

  /**
   * @see java.sql.Connection#nativeSQL(java.lang.String)
   */
  @Override
  public String nativeSQL(String sql) throws SQLException {
    return getConnection().nativeSQL(sql);
  }

  /**
   * @see java.sql.Connection#setAutoCommit(boolean)
   */
  @Override
  public void setAutoCommit(boolean autoCommit) throws SQLException {
    getConnection().setAutoCommit(autoCommit);
  }

  /**
   * @see java.sql.Connection#getAutoCommit()
   */
  @Override
  public boolean getAutoCommit() throws SQLException {
    return getConnection().getAutoCommit();
  }

  /**
   * @see java.sql.Connection#commit()
   */
  @Override
  public void commit() throws SQLException {
    getConnection().commit();
  }

  /**
   * @see java.sql.Connection#rollback()
   */
  @Override
  public void rollback() throws SQLException {
    getConnection().rollback();
  }

  /**
   * @see java.sql.Connection#close()
   */
  @Override
  public void close() throws SQLException {
    getConnection().close();
  }

  public void stop() {
    JdbcUtil.closeQuietly(getConnection());
  }

  /**
   * @see java.sql.Connection#isClosed()
   */
  @Override
  public boolean isClosed() throws SQLException {
    return getConnection().isClosed();
  }

  /**
   * @see java.sql.Connection#getMetaData()
   */
  @Override
  public DatabaseMetaData getMetaData() throws SQLException {
    return getConnection().getMetaData();
  }

  /**
   * @see java.sql.Connection#setReadOnly(boolean)
   */
  @Override
  public void setReadOnly(boolean readOnly) throws SQLException {
    getConnection().setReadOnly(readOnly);
  }

  /**
   * @see java.sql.Connection#isReadOnly()
   */
  @Override
  public boolean isReadOnly() throws SQLException {
    return getConnection().isReadOnly();
  }

  /**
   * @see java.sql.Connection#setCatalog(java.lang.String)
   */
  @Override
  public void setCatalog(String catalog) throws SQLException {
    getConnection().setCatalog(catalog);
  }

  /**
   * @see java.sql.Connection#getCatalog()
   */
  @Override
  public String getCatalog() throws SQLException {
    return getConnection().getCatalog();
  }

  /**
   * @see java.sql.Connection#setTransactionIsolation(int)
   */
  @Override
  public void setTransactionIsolation(int level) throws SQLException {
    getConnection().setTransactionIsolation(level);
  }

  /**
   * @see java.sql.Connection#getTransactionIsolation()
   */
  @Override
  public int getTransactionIsolation() throws SQLException {
    return getConnection().getTransactionIsolation();
  }

  /**
   * @see java.sql.Connection#getWarnings()
   */
  @Override
  public SQLWarning getWarnings() throws SQLException {
    return getConnection().getWarnings();
  }

  /**
   * @see java.sql.Connection#clearWarnings()
   */
  @Override
  public void clearWarnings() throws SQLException {
    getConnection().clearWarnings();
  }

  /**
   * @see java.sql.Connection#createStatement(int, int)
   */
  @Override
  public Statement createStatement(int i, int j) throws SQLException {
    return getConnection().createStatement(i, j);
  }

  /**
   * @see java.sql.Connection#prepareStatement(String, int, int)
   */
  @Override
  public PreparedStatement prepareStatement(String sql, int i, int j)
      throws SQLException {
    return getConnection().prepareStatement(sql, i, j);
  }

  /**
   * @see java.sql.Connection#prepareCall(java.lang.String, int, int)
   */
  @Override
  public CallableStatement prepareCall(String sql, int i, int j)
      throws SQLException {
    return getConnection().prepareCall(sql, i, j);
  }

  /**
   * @see java.sql.Connection#getTypeMap()
   */
  @Override
  public Map getTypeMap() throws SQLException {
    return getConnection().getTypeMap();
  }

  /**
   * @see java.sql.Connection#setHoldability(int)
   */
  @Override
  public void setHoldability(int holdability) throws SQLException {
    getConnection().setHoldability(holdability);
  }

  /**
   * @see java.sql.Connection#getHoldability()
   */
  @Override
  public int getHoldability() throws SQLException {
    return getConnection().getHoldability();
  }

  /**
   * @see java.sql.Connection#setSavepoint()
   */
  @Override
  public Savepoint setSavepoint() throws SQLException {
    return getConnection().setSavepoint();
  }

  /**
   * @see java.sql.Connection#setSavepoint(java.lang.String)
   */
  @Override
  public Savepoint setSavepoint(String name) throws SQLException {
    return getConnection().setSavepoint(name);
  }

  /**
   * @see java.sql.Connection#rollback(java.sql.Savepoint)
   */
  @Override
  public void rollback(Savepoint savepoint) throws SQLException {
    getConnection().rollback(savepoint);
  }

  /**
   * @see java.sql.Connection#releaseSavepoint(java.sql.Savepoint)
   */
  @Override
  public void releaseSavepoint(Savepoint savepoint) throws SQLException {
    getConnection().releaseSavepoint(savepoint);
  }

  /**
   * @see java.sql.Connection#createStatement(int, int, int)
   */
  @Override
  public Statement createStatement(int i, int j, int k) throws SQLException {
    return getConnection().createStatement(i, j, k);
  }

  /**
   * @see java.sql.Connection#prepareStatement(String, int, int, int)
   */
  @Override
  public PreparedStatement prepareStatement(String sql, int i, int j, int k)
      throws SQLException {
    return getConnection().prepareStatement(sql, i, j, k);
  }

  /**
   * @see java.sql.Connection#prepareCall(java.lang.String, int, int, int)
   */
  @Override
  public CallableStatement prepareCall(String sql, int i, int j, int k)
      throws SQLException {
    return getConnection().prepareCall(sql, i, j, k);
  }

  /**
   * @see java.sql.Connection#prepareStatement(java.lang.String, int)
   */
  @Override
  public PreparedStatement prepareStatement(String sql, int i)
      throws SQLException {
    return getConnection().prepareStatement(sql, i);
  }

  /**
   * @see java.sql.Connection#prepareStatement(java.lang.String, int[])
   */
  @Override
  public PreparedStatement prepareStatement(String sql, int[] columnIndexes)
      throws SQLException {
    return getConnection().prepareStatement(sql, columnIndexes);
  }

  /**
   * @see java.sql.Connection#prepareStatement(String, String[])
   */
  @Override
  public PreparedStatement prepareStatement(String sql, String[] columnNames)
      throws SQLException {
    return getConnection().prepareStatement(sql, columnNames);
  }

  /**
   * @see java.sql.Connection#createArrayOf(java.lang.String,
   *      java.lang.Object[])
   */
  @Override
  public Array createArrayOf(String typeName, Object[] elements)
      throws SQLException {
    return getConnection().createArrayOf(typeName, elements);
  }

  /**
   * @see java.sql.Connection#createBlob()
   */
  @Override
  public Blob createBlob() throws SQLException {
    return getConnection().createBlob();
  }

  /**
   * @see java.sql.Connection#createClob()
   */
  @Override
  public Clob createClob() throws SQLException {
    return getConnection().createClob();
  }

  /**
   * @see java.sql.Connection#createNClob()
   */
  @Override
  public NClob createNClob() throws SQLException {
    return getConnection().createNClob();
  }

  /**
   * @see java.sql.Connection#createSQLXML()
   */
  @Override
  public SQLXML createSQLXML() throws SQLException {
    return getConnection().createSQLXML();
  }

  /**
   * @see java.sql.Connection#createStruct(java.lang.String,
   *      java.lang.Object[])
   */
  @Override
  public Struct createStruct(String typeName, Object[] attributes)
      throws SQLException {
    return getConnection().createStruct(typeName, attributes);
  }

  /**
   * @see java.sql.Connection#getClientInfo()
   */
  @Override
  public Properties getClientInfo() throws SQLException {
    return getConnection().getClientInfo();
  }

  /**
   * @see java.sql.Connection#getClientInfo(java.lang.String)
   */
  @Override
  public String getClientInfo(String name) throws SQLException {
    return getConnection().getClientInfo(name);
  }

  /**
   * @see java.sql.Connection#isValid(int)
   */
  @Override
  public boolean isValid(int timeout) throws SQLException {
    return getConnection().isValid(timeout);
  }

  /**
   * @see java.sql.Connection#setClientInfo(java.util.Properties)
   */
  @Override
  public void setClientInfo(Properties properties)
      throws SQLClientInfoException {
    getConnection().setClientInfo(properties);
  }

  /**
   * @see java.sql.Connection#setClientInfo(java.lang.String,
   *      java.lang.String)
   */
  @Override
  public void setClientInfo(String name, String value)
      throws SQLClientInfoException {
    getConnection().setClientInfo(name, value);
  }

  /**
   * @see java.sql.Connection#setTypeMap(java.util.Map)
   */
  @Override
  public void setTypeMap(Map<String, Class<?>> map) throws SQLException {
    getConnection().setTypeMap(map);
  }

  /**
   * @see java.sql.Wrapper#isWrapperFor(java.lang.Class)
   */
  @Override
  public boolean isWrapperFor(Class<?> iface) throws SQLException {
    return getConnection().isWrapperFor(iface);
  }

  /**
   * @see java.sql.Wrapper#unwrap(java.lang.Class)
   */
  @Override
  public <T> T unwrap(Class<T> iface) throws SQLException {
    return getConnection().unwrap(iface);
  }

  @Override
  public void setSchema(String schema) throws SQLException {
    getConnection().setSchema(schema);
  }

  @Override
  public String getSchema() throws SQLException {
    return getConnection().getSchema();
  }

  @Override
  public void abort(Executor executor) throws SQLException {
    getConnection().abort(executor);
  }

  @Override
  public void setNetworkTimeout(Executor executor, int milliseconds) throws SQLException {
    getConnection().setNetworkTimeout(executor, milliseconds);
  }

  @Override
  public int getNetworkTimeout() throws SQLException {
    return getConnection().getNetworkTimeout();
  }

  public Connection getConnection() {
    return connection;
  }

  public void setConnection(Connection connection) {
    this.connection = connection;
  }

}
