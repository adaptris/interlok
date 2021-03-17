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

package com.adaptris.core.util;

import static org.apache.commons.lang3.StringUtils.isEmpty;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Savepoint;
import java.util.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.adaptris.core.AdaptrisConnection;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.jdbc.DatabaseConnection;
import com.adaptris.core.jdbc.JdbcConstants;
import com.adaptris.interlok.util.Closer;
import com.adaptris.security.exc.PasswordException;
import com.adaptris.security.password.Password;

/**
 * Helper methods used internally to support JDBC operations within the framework.
 *
 */
public abstract class JdbcUtil {

  private static Logger log = LoggerFactory.getLogger(JdbcUtil.class);
  public static final int NUM_SECONDS_TIMEOUT_CONN_VALIDATE = 5;

  public static void closeQuietly(AutoCloseable... closeables) {
    Closer.closeQuietly(closeables);
  }

  /**
   * Rollback to the stored savepoint.
   * <p>
   * If {@link Connection#getAutoCommit()} is true, then this operation does nothing.
   * </p>
   *
   * @param svp the savepoint (if null, no rollback occurs).
   * @param sqlConnection the database connection.
   */
  public static void rollback(Savepoint svp, Connection sqlConnection) {
    if (sqlConnection == null) {
      return;
    }
    try {
      if (sqlConnection.getAutoCommit()) {
        return;
      }
      if (svp != null) {
        sqlConnection.rollback(svp);
      }
    }
    catch (Exception ignoredIntentionally) {
    }
  }

  /**
   * Rollback the connection.
   * <p>
   * If {@link Connection#getAutoCommit()} is true, then this operation does nothing.
   * </p>
   *
   * @param sqlConnection the database connection.
   */
  public static void rollback(Connection sqlConnection) {
    if (sqlConnection == null) {
      return;
    }
    try {
      if (sqlConnection.getAutoCommit()) {
        return;
      }
      else {
        sqlConnection.rollback();
      }
    }
    catch (Exception ignoredIntentionally) {
    }
  }

  /**
   * Commit any pending transactions on the Connection.
   * <p>
   * If {@link Connection#getAutoCommit()} is true, then this operation does nothing.
   * </p>
   *
   * @param sqlConnection the SQL Connection
   * @throws SQLException if the commit fails.
   */
  public static void commit(Connection sqlConnection) throws SQLException {
    if (sqlConnection == null) {
      return;
    }
    if (!sqlConnection.getAutoCommit()) {
      sqlConnection.commit();
    }
  }

  /**
   * Create a Savepoint on the connection
   * <p>
   * If {@link Connection#getAutoCommit()} is true, then this operation returns null.
   * </p>
   *
   * @param sqlConnection the SQL Connection
   * @return a created Savepoint or null if the connection does not require it.
   * @throws SQLException if the operation fails.
   */
  public static Savepoint createSavepoint(Connection sqlConnection) throws SQLException {
    if (sqlConnection == null) {
      return null;
    }
    if (!sqlConnection.getAutoCommit()) {
      return sqlConnection.setSavepoint();
    }
    return null;
  }

  public static Connection testConnection(Connection sqlConnection, boolean execute)
      throws SQLException {
    if (execute) {
      if (!sqlConnection.isValid(NUM_SECONDS_TIMEOUT_CONN_VALIDATE)) {
        throw new SQLException("Connection is not valid");
      }
    }
    return sqlConnection;
  }

  public static Properties mergeConnectionProperties(Properties p, String username, String password) throws PasswordException {
    if (!isEmpty(username)) {
      p.setProperty("user", username);
    }
    if (!isEmpty(password)) {
      p.setProperty("password", Password.decode(password));
    }
    return p;
  }

  /**
   * Convenience method to get the {@link Connection} either from the
   * {@link com.adaptris.core.AdaptrisMessage} object or from configuration.
   *
   * @param msg the adaptrisMessage object
   * @param adpCon the configured {@link AdaptrisConnection}
   * @return the connection either from the adaptris message or from configuration.
   */
  public static Connection getConnection(AdaptrisMessage msg, AdaptrisConnection adpCon)
      throws SQLException {
    Connection conn =
        (Connection) msg.getObjectHeaders().get(JdbcConstants.OBJ_METADATA_DATABASE_CONNECTION_KEY);
    if (conn != null && !conn.isClosed()) {
      return conn;
    } else {
      return adpCon.retrieveConnection(DatabaseConnection.class).connect();
    }
  }

  /**
   * Rollback to the stored savepoint.
   * <p>
   * If a database connection exists in the AdaptrisMessage object metadata then you don't want to
   * rollback, you want to let the parent (presumably a
   * {@link com.adaptris.core.services.jdbc.JdbcServiceList}) to do it for you.
   * </p>
   *
   * @param sqlConnection the database connection.
   * @param msg the AdaptrisMessage
   */
  public static void rollback(Connection sqlConnection, AdaptrisMessage msg) {
    if (msg.getObjectHeaders().containsKey(JdbcConstants.OBJ_METADATA_DATABASE_CONNECTION_KEY)) {
      return;
    }
    rollback(sqlConnection);
  }

  /**
   * Commit the connection
   * <p>
   * If a database connection exists in the AdaptrisMessage object metadata then you don't want to
   * rollback, you want to let the parent (presumably a
   * {@link com.adaptris.core.services.jdbc.JdbcServiceList}) to do it for you.
   * </p>
   *
   * @param sqlConnection the SQL Connection
   * @param msg the AdaptrisMessage currently being processed.
   * @throws SQLException if the commit fails.
   */
  public static void commit(Connection sqlConnection, AdaptrisMessage msg) throws SQLException {
    if (msg.getObjectHeaders().containsKey(JdbcConstants.OBJ_METADATA_DATABASE_CONNECTION_KEY)) {
      return;
    }
    commit(sqlConnection);
  }

}
