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
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import javax.validation.Valid;
import com.adaptris.core.AdaptrisConnection;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.ConnectedService;
import com.adaptris.core.CoreException;
import com.adaptris.core.ServiceImp;
import com.adaptris.core.util.JdbcUtil;
import com.adaptris.core.util.LifecycleHelper;
import com.adaptris.util.TimeInterval;

/**
 * <p>
 * Provides database connection for JDBC-based {@link com.adaptris.core.Service} implementations.
 * </p>
 */
public abstract class JdbcService extends ServiceImp implements ConnectedService {

  // marshalled...
  // And might be null as of redmineID #3968
  @Valid
  private AdaptrisConnection connection;
  @Valid
  private TimeInterval statementTimeout;

  public JdbcService() {
    super();
  }

  @Override
  public final void initService() throws CoreException {
    if (connection != null) {
      connection.addExceptionListener(this);
    }
    LifecycleHelper.init(connection);
    initJdbcService();
  }

  @Override
  public final void prepare() throws CoreException {
    if (connection != null) {
      connection.prepare();
    }
    prepareService();
  }

  protected abstract void prepareService() throws CoreException;


  protected abstract void initJdbcService() throws CoreException;

  @Override
  public final void closeService() {
    closeJdbcService();
    LifecycleHelper.close(connection);
  }

  /**
   * Close the service.
   * <p>
   * This is called before the connection is closed
   * </p>
   */
  protected abstract void closeJdbcService();

  @Override
  public void start() throws CoreException {
    super.start();
    LifecycleHelper.start(connection);
    startService();
  }

  /**
   * Start the service.
   * <p>
   * This is called after the connection is started
   * </p>
   *
   * @throws CoreException
   */
  protected abstract void startService() throws CoreException;

  @Override
  public void stop() {
    super.stop();
    stopService();
    LifecycleHelper.stop(connection);
  }

  /**
   * Stop the service.
   * <p>
   * This is called after before the connection is stopped
   * </p>
   */
  protected abstract void stopService();

  @Override
  public void setConnection(AdaptrisConnection conn) {
    connection = conn;
  }

  @Override
  public AdaptrisConnection getConnection() {
    return connection;
  }

  /**
   * Get the {@link Connection} either from the {@link com.adaptris.core.AdaptrisMessage} object or from configuration.
   * 
   * @param msg the adaptrisMessage object
   * @return the connection either from the adaptris message or from configuration.
   */
  protected Connection getConnection(AdaptrisMessage msg) throws SQLException {
    return JdbcUtil.getConnection(msg, getConnection());
  }

  /**
   * Rollback to the stored savepoint.
   * <p>
   * If a database connection exists in the AdaptrisMessage object metadata then you don't want to rollback, you want to let the
   * parent (presumably a {@link com.adaptris.core.services.jdbc.JdbcServiceList}) to do it for you.
   * </p>
   *
   * @param sqlConnection the database connection.
   * @param msg the AdaptrisMessage
   */
  protected void rollback(Connection sqlConnection, AdaptrisMessage msg) {
    JdbcUtil.rollback(sqlConnection, msg);
  }

  /**
   * Commit the connection
   * <p>
   * If a database connection exists in the AdaptrisMessage object metadata then you don't want to rollback, you want to let the
   * parent (presumably a {@link com.adaptris.core.services.jdbc.JdbcServiceList}) to do it for you.
   * </p>
   *
   * @param sqlConnection the SQL Connection
   * @param msg the AdaptrisMessage currently being processed.
   * @throws SQLException if the commit fails.
   */
  @Deprecated
  protected void commit(Connection sqlConnection, AdaptrisMessage msg) throws SQLException {
    JdbcUtil.commit(sqlConnection, msg);
  }

  public TimeInterval getStatementTimeout() {
    return statementTimeout;
  }

  /**
   * Set the statement timeout.
   * 
   * @param statementTimeout the statement timeout.
   * @since 3.0.1
   */
  public void setStatementTimeout(TimeInterval statementTimeout) {
    this.statementTimeout = statementTimeout;
  }

  protected Statement createStatement(Connection c) throws SQLException {
    Statement s = c.createStatement();
    applyTimeout(s);
    return s;
  }

  protected PreparedStatement prepareStatement(Connection c, String sql) throws SQLException {
    PreparedStatement p = c.prepareStatement(sql);
    applyTimeout(p);
    return p;
  }

  protected PreparedStatement prepareStatement(Connection c, String sql, int autoGenKeys) throws SQLException {
    PreparedStatement p = c.prepareStatement(sql, autoGenKeys);
    applyTimeout(p);
    return p;
  }

  protected void applyTimeout(Statement stmt) throws SQLException {
    if (getStatementTimeout() != null) {
      int seconds =
          Long.valueOf(TimeInterval.toSecondsDefaultIfNull(getStatementTimeout(), 0)).intValue();
      stmt.setQueryTimeout(seconds);
    }
  }

}
