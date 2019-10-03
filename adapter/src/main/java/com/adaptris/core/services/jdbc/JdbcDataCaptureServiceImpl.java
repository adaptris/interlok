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

package com.adaptris.core.services.jdbc;

import static org.apache.commons.lang.StringUtils.isBlank;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;

import javax.validation.constraints.NotNull;

import com.adaptris.annotation.AdvancedConfig;
import com.adaptris.annotation.InputFieldDefault;
import com.adaptris.annotation.InputFieldHint;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.CoreException;
import com.adaptris.core.util.JdbcUtil;

/**
 * Base implementation for capturing data from an {@linkplain com.adaptris.core.AdaptrisMessage} and storing it in a jdbc database.
 *
 */
public abstract class JdbcDataCaptureServiceImpl extends JdbcServiceWithParameters {

  @NotNull
  @InputFieldHint(style = "SQL", expression = true)
  private String statement = null;
  @AdvancedConfig
  @InputFieldDefault(value = "false")
  private Boolean saveReturnedKeys = null;
  @AdvancedConfig
  @Deprecated
  private String saveReturnedKeysColumn = null;
  @AdvancedConfig
  @Deprecated
  private String saveReturnedKeysTable = null;
  protected transient DatabaseActor actor;

  private static boolean warningLogged = false;

  public JdbcDataCaptureServiceImpl() {
    super();
    actor = new DatabaseActor();
  }

  @Override
  protected void initJdbcService() throws CoreException {
    if (!isBlank(getSaveReturnedKeysColumn()) || !isBlank(getSaveReturnedKeysTable())) {
      if (!warningLogged) {
        log.warn("saveReturnedKeysColumn/saveReturnedKeysTable is deprecated; "
                + "surely your JDBC driver supports Statement#RETURN_GENERATED_KEYS by now");
        warningLogged = true;
      }
    }
  }

  @Override
  protected void startService() throws CoreException {

  }

  @Override
  protected void stopService() {
    actor.destroy();
  }

  @Override
  protected void closeJdbcService() {
  }

  /**
   * SQL statement to perform.
   *
   * @param s the statement.
   */
  public void setStatement(String s) {
    statement = s;
  }

  /**
   * The configured Statement.
   *
   * @return the SQL statement.
   */
  public String getStatement() {
    return statement;
  }


  public boolean saveReturnedKeys() {
    return getSaveReturnedKeys() != null ? getSaveReturnedKeys().booleanValue() : false;
  }

  /**
   * Store any return value from the SQL statement as metadata.
   *
   * @param save the falg.
   */
  public void setSaveReturnedKeys(Boolean save) {
    saveReturnedKeys = save;
  }

  /**
   * Get the configured flag.
   *
   * @return the flag.
   */
  public Boolean getSaveReturnedKeys() {
    return saveReturnedKeys;
  }

  /**
   * The column that forms the return value for the SQL statement.
   * <p>
   * This is only applicable of the JDBC driver in question doesn't support {@link Statement#RETURN_GENERATED_KEYS}
   * </p>
   *
   * @param col the column
   * @deprecated since 3.6.2 {@link Statement#RETURN_GENERATED_KEYS} has been available since java 1.4, surely your JDBC driver is
   *             newer than that!
   */
  public void setSaveReturnedKeysColumn(String col) {
    saveReturnedKeysColumn = col;
  }

  /**
   * Return the column that forms the return value for the SQL statement.
   * <p>
   * This is only applicable of the JDBC driver in question doesn't support {@link Statement#RETURN_GENERATED_KEYS}
   * </p>
   *
   * @return the column
   * @deprecated since 3.6.2 {@link Statement#RETURN_GENERATED_KEYS} has been available since java 1.4, surely your JDBC driver is
   *             newer than that!
   */
  public String getSaveReturnedKeysColumn() {
    return saveReturnedKeysColumn;
  }

  /**
   * The table the contains the return value for the SQL statement.
   *
   * <p>
   * This is only applicable of the JDBC driver in question doesn't support {@link Statement#RETURN_GENERATED_KEYS}
   * </p>
   *
   * @param table the table
   * @deprecated since 3.6.2 {@link Statement#RETURN_GENERATED_KEYS} has been available since java 1.4, surely your JDBC driver is
   *             newer than that!
   */
  public void setSaveReturnedKeysTable(String table) {
    saveReturnedKeysTable = table;
  }

  /**
   * Get the table that contains the return value.
   * <p>
   * This is only applicable of the JDBC driver in question doesn't support {@link Statement#RETURN_GENERATED_KEYS}
   * </p>
   *
   * @return the table.
   * @deprecated since 3.6.2 {@link Statement#RETURN_GENERATED_KEYS} has been available since java 1.4, surely your JDBC driver is
   *             newer than that!
   */
  public String getSaveReturnedKeysTable() {
    return saveReturnedKeysTable;
  }

  protected void saveKeys(AdaptrisMessage msg, Statement stmt) throws SQLException {
    ResultSet rs = null;
    Statement savedKeysQuery = null;

    try {
      if (saveReturnedKeys()) {
        if (!actor.isOldJbc()) {
          rs = stmt.getGeneratedKeys();
          rs.next();
          ResultSetMetaData rsmd = rs.getMetaData();
          for (int i = 1; i <= rsmd.getColumnCount(); i++) {
            String name = rsmd.getColumnName(i);
            String value = rs.getObject(name).toString();
            msg.addMetadata(name, value);
          }
        }
        else {
          savedKeysQuery = createStatement(actor.getSqlConnection());
          rs = savedKeysQuery.executeQuery("select max(" + saveReturnedKeysColumn + ") from " + saveReturnedKeysTable + ";");
          rs.next();
          String value = rs.getObject(saveReturnedKeysColumn).toString();
          msg.addMetadata(saveReturnedKeysColumn, value);
        }
      }
    }
    finally {
      JdbcUtil.closeQuietly(savedKeysQuery);
      JdbcUtil.closeQuietly(rs);
    }
  }

  protected DatabaseActor configureActor(AdaptrisMessage msg) throws SQLException {
    Connection c = getConnection(msg);
    if (!c.equals(actor.getSqlConnection())) {
      actor.reInitialise(c);
    }
    return actor;
  }

  @Override
  protected void prepareService() throws CoreException {}

  protected class DatabaseActor {
    private PreparedStatement insertStatement = null;
    private String lastInsertStatement = "";
    private Connection sqlConnection;
    private boolean oldJDBC;

    DatabaseActor() {

    }

    void reInitialise(Connection c) throws SQLException {
      destroy();
      sqlConnection = c;
    }

    public PreparedStatement getInsertStatement(AdaptrisMessage msg) throws SQLException {
      String currentStatement = getParameterApplicator().prepareParametersToStatement(msg.resolve(getStatement()));
      if (!lastInsertStatement.equals(currentStatement) || insertStatement == null) {
        JdbcUtil.closeQuietly(insertStatement);
        insertStatement = prepare(currentStatement);
        lastInsertStatement = currentStatement;
      }
      return insertStatement;
    }

    private PreparedStatement prepare(String statement) throws SQLException {
      PreparedStatement result = null;
      if (saveReturnedKeys()) {
        try {
          result = prepareStatement(sqlConnection, statement, Statement.RETURN_GENERATED_KEYS);
        }
        catch (Throwable error) {
          oldJDBC = true;
          result = prepareStatement(sqlConnection, statement);
        }
      }
      else {
        result = prepareStatement(sqlConnection, statement);
      }
      return result;
    }

    void destroy() {
      JdbcUtil.closeQuietly(insertStatement);
      JdbcUtil.closeQuietly(sqlConnection);
      sqlConnection = null;
      insertStatement = null;
    }

    public Connection getSqlConnection() {
      return sqlConnection;
    }

    boolean isOldJbc() {
      return oldJDBC;
    }
  }
}
