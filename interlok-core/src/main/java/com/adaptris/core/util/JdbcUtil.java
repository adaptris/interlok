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

import static org.apache.commons.lang.StringUtils.isEmpty;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Savepoint;
import java.sql.Statement;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.adaptris.security.exc.PasswordException;
import com.adaptris.security.password.Password;

/**
 * Helper methods used internally to support JDBC operations within the framework.
 *
 */
public abstract class JdbcUtil {

  private static Logger log = LoggerFactory.getLogger(JdbcUtil.class);

  public static void closeQuietly(AutoCloseable... closeables) {
    if (closeables != null) {
      for (AutoCloseable c : closeables) {
        try {
          if (c != null) c.close();
        } catch (Exception e) {
        }
      }
    }
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

  public static Connection testConnection(Connection sqlConnection, String testStatement, boolean debugMode) throws SQLException {
    Statement stmt = sqlConnection.createStatement();
    ResultSet rs = null;
    try {
      if (isEmpty(testStatement)) {
        return sqlConnection;
      }
      if (debugMode) {
        rs = stmt.executeQuery(testStatement);
        if (rs.next()) {
          StringBuffer sb = new StringBuffer("TestStatement Results - ");
          ResultSetMetaData rsm = rs.getMetaData();
          for (int i = 1; i <= rsm.getColumnCount(); i++) {
            sb.append("[");
            sb.append(rsm.getColumnName(i));
            sb.append("=");
            sb.append(rs.getObject(i));
            sb.append("] ");
          }
          log.trace(sb.toString());
        }
      } else {
        stmt.execute(testStatement);
      }
    } finally {
      JdbcUtil.closeQuietly(rs);
      JdbcUtil.closeQuietly(stmt);
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
}
