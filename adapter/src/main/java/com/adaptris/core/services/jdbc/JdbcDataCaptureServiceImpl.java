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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import com.adaptris.annotation.AutoPopulated;
import com.adaptris.annotation.InputFieldHint;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.CoreException;
import com.adaptris.core.jdbc.JdbcService;
import com.adaptris.core.util.JdbcUtil;

/**
 * Base implementation for capturing data from an {@linkplain AdaptrisMessage} and storing it in a jdbc database.
 *
 */
public abstract class JdbcDataCaptureServiceImpl extends JdbcService {

  @NotNull
  @InputFieldHint(style = "SQL")
  private String statement = null;
  private Boolean saveReturnedKeys = null;
  private String saveReturnedKeysColumn = null;
  private String saveReturnedKeysTable = null;
  protected transient DatabaseActor actor;
  @NotNull
  @AutoPopulated
  @Valid
  private ParameterApplicator parameterApplicator;

  /**
   * <p>
   * Creates a new instance.
   * </p>
   */
  public JdbcDataCaptureServiceImpl() {
    setParameterApplicator(new SequentialParameterApplicator());
    actor = new DatabaseActor();
  }

  @Override
  protected void initService() throws CoreException {

  }

  @Override
  protected void startService() throws CoreException {

  }

  @Override
  protected void stopService() {
    actor.destroy();
  }

  @Override
  protected void closeService() {
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
  
  private String prepareStringStatement() {
    return this.getParameterApplicator().prepareParametersToStatement(getStatement());
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
   *
   * @param col the column
   */
  public void setSaveReturnedKeysColumn(String col) {
    saveReturnedKeysColumn = col;
  }

  /**
   * Return the column that forms the return value for the SQL statement.
   *
   * @return the column
   */
  public String getSaveReturnedKeysColumn() {
    return saveReturnedKeysColumn;
  }

  /**
   * The table the contains the return value for the SQL statement.
   *
   * @param table the table
   */
  public void setSaveReturnedKeysTable(String table) {
    saveReturnedKeysTable = table;
  }

  /**
   * Get the table that contains the return value.
   *
   * @return the table.
   */
  public String getSaveReturnedKeysTable() {
    return saveReturnedKeysTable;
  }

  protected void saveKeys(AdaptrisMessage msg) throws SQLException {
    ResultSet rs = null;
    Statement savedKeysQuery = null;

    try {
      if (saveReturnedKeys()) {
        if (!actor.isOldJbc()) {
          rs = actor.getInsertStatement().getGeneratedKeys();
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

  protected void configureActor(AdaptrisMessage msg) throws SQLException {
    Connection c = getConnection(msg);
    if (!c.equals(actor.getSqlConnection())) {
      actor.reInitialise(c);
    }
  }

  public ParameterApplicator getParameterApplicator() {
    return parameterApplicator;
  }

  /**
   * Specify how parameters will be applied to the SQL statement.
   * 
   * @param p the parameter applicator implementation; default is {@link SequentialParameterApplicator}
   * @see SequentialParameterApplicator
   * @see NamedParameterApplicator
   */
  public void setParameterApplicator(ParameterApplicator p) {
    this.parameterApplicator = p;
  }


  @Override
  protected void prepareService() throws CoreException {}

  protected class DatabaseActor {
    private PreparedStatement insertStatement = null;
    private Connection sqlConnection;
    private boolean oldJDBC;

    DatabaseActor() {

    }

    void reInitialise(Connection c) throws SQLException {
      destroy();
      sqlConnection = c;
      prepareStatements();
    }

    private void prepareStatements() throws SQLException {
      if (saveReturnedKeys()) {
        try {
          insertStatement = prepareStatement(sqlConnection, prepareStringStatement(), Statement.RETURN_GENERATED_KEYS);
        }
        catch (Throwable error) {
          oldJDBC = true;
          insertStatement = prepareStatement(sqlConnection, prepareStringStatement());
        }
      }
      else {
        insertStatement = prepareStatement(sqlConnection, prepareStringStatement());
      }
    }

    void destroy() {
      JdbcUtil.closeQuietly(insertStatement);
      JdbcUtil.closeQuietly(sqlConnection);
      sqlConnection = null;
    }

    public PreparedStatement getInsertStatement() {
      return insertStatement;
    }

    public Connection getSqlConnection() {
      return sqlConnection;
    }

    boolean isOldJbc() {
      return oldJDBC;
    }
  }
}
