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
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DecimalFormat;
import java.text.NumberFormat;

import org.hibernate.validator.constraints.NotBlank;

import com.adaptris.annotation.AdvancedConfig;
import com.adaptris.annotation.InputFieldHint;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.CoreException;
import com.adaptris.core.ServiceException;
import com.adaptris.core.jdbc.JdbcService;
import com.adaptris.core.util.JdbcUtil;

/**
 * Abstract base class for adding sequence numbers into metadata.
 * 
 * <p>
 * The basic database schema that is assumed to be default is
 * 
 * <pre>
 * {@code 
 * CREATE TABLE SEQUENCES (ID VARCHAR(255) NOT NULL, SEQ_NUMBER INT)
 * }
 * </pre>
 * The default SQL statements reflect this; and provided that a table called 'sequences' contains at least those two columns then it
 * should work without any changes to the SQL statements.
 * </p>
 * <p>
 * Concrete implementations of this simply have to derive the appropriate 'id' from the AdaptrisMessage; everything else is done in
 * this class.
 * </p>
 * 
 * @author lchan
 * 
 */
public abstract class AbstractJdbcSequenceNumberService extends JdbcService {

  public static final String DEFAULT_RESET_STATEMENT = "UPDATE SEQUENCES SET SEQ_NUMBER = ? WHERE ID = ?";
  public static final String DEFAULT_UPDATE_STATEMENT = "UPDATE SEQUENCES SET SEQ_NUMBER = SEQ_NUMBER+1 WHERE ID=?";
  public static final String DEFAULT_INSERT_STATEMENT = "INSERT INTO SEQUENCES (ID, SEQ_NUMBER) VALUES (?, 2)";
  public static final String DEFAULT_SELECT_STATEMENT = "SELECT SEQ_NUMBER from SEQUENCES where ID=?";
  private static final String DEFAULT_CREATE_STATEMENT = "CREATE TABLE SEQUENCES (ID VARCHAR(255) NOT NULL, SEQ_NUMBER INT)";
  private static final String DEFAULT_TABLE_NAME = "SEQUENCES";

  @NotBlank
  private String metadataKey;
  @NotBlank
  private String numberFormat;
  @AdvancedConfig
  private Boolean alwaysReplaceMetadata;
  @InputFieldHint(style = "SQL")
  @AdvancedConfig
  private String selectStatement = null;
  @InputFieldHint(style = "SQL")
  @AdvancedConfig
  private String insertStatement = null;
  @InputFieldHint(style = "SQL")
  @AdvancedConfig
  private String updateStatement = null;
  @InputFieldHint(style = "SQL")
  @AdvancedConfig
  private String resetStatement = null;
  @AdvancedConfig
  private Boolean createDatabase;

  private OverflowBehaviour overflowBehaviour;
  private transient DatabaseActor actor;

  private enum StatementType {
    Select {
      @Override
      void doConfigure(DatabaseActor actor, String identity) throws SQLException {
        actor.getSelect().setString(1, identity);
      }
    },
    Insert {
      @Override
      void doConfigure(DatabaseActor actor, String identity) throws SQLException {
        actor.getInsert().setString(1, identity);
      }
    },
    Update {
      @Override
      void doConfigure(DatabaseActor actor, String id) throws SQLException {
        actor.getUpdate().setString(1, id);
      }
    },
    Reset {
      @Override
      void doConfigure(DatabaseActor actor, String identity) throws SQLException {
        actor.getReset().setString(2, identity);
      }

    };
    void configure(DatabaseActor actor, String id) throws SQLException {
      if (id != null) {
        doConfigure(actor, id);
      }
    }

    abstract void doConfigure(DatabaseActor actor, String identity) throws SQLException;

  }

  /**
   * The behaviour of the sequence number generator when the number exceeds that specified by the number format.
   *
   *
   */
  public enum OverflowBehaviour {
    ResetToOne() {
      @Override
      int wrap(int i) {
        return 1;
      }

    },
    Continue() {
      @Override
      int wrap(int i) {
        return i;
      }
    };
    abstract int wrap(int i);
  }

  public AbstractJdbcSequenceNumberService() {
    super();
    actor = new DatabaseActor();
  }

  @Override
  protected void initJdbcService() throws CoreException {
    if (getMetadataKey() == null) {
      throw new CoreException("MetadataKey to set the " + "sequence number against is null");
    }
    if (getNumberFormat() == null) {
      throw new CoreException("Number format to format the " + "sequence number against is null");
    }
  }

  @Override
  protected void startService() throws CoreException {
  }

  @Override
  protected void stopService() {
  }

  @Override
  protected void closeJdbcService() {

  }


  private void resetStatements() {
    log.trace("Resetting SQL statements back to defaults...");
    selectStatement = null;
    insertStatement = null;
    updateStatement = null;
    resetStatement = null;

  }

  /**
   * @see com.adaptris.core.Service#doService(com.adaptris.core.AdaptrisMessage)
   */
  @Override
  public void doService(AdaptrisMessage msg) throws ServiceException {
    NumberFormat formatter = new DecimalFormat(getNumberFormat());
    Connection conn = null;
    ResultSet rs = null;
    DatabaseActor actor = new DatabaseActor();

    if (!alwaysReplaceMetadata() && msg.containsKey(getMetadataKey())) {
      log.debug(getMetadataKey() + " already exists, not updating");
      return;
    }
    try {
      conn = getConnection(msg);
      if (conn != actor.getSqlConnection()) {
        actor.reInitialise(conn);
      }
      String identity = getIdentity(msg);
      actor.configure(StatementType.Select, identity);
      rs = actor.getSelect().executeQuery();
      if (rs.next()) {
        int count = rs.getInt(1);
        String countString = formatter.format(count);
        if (countString.length() > getNumberFormat().length()) {
          count = getBehaviour(getOverflowBehaviour()).wrap(count);
          countString = formatter.format(count);
          actor.getReset().setInt(1, count + 1);
          actor.configure(StatementType.Reset, identity);
          actor.getReset().executeUpdate();
        }
        else {
          actor.configure(StatementType.Update, identity);
          actor.getUpdate().executeUpdate();
        }
        msg.addMetadata(getMetadataKey(), countString);
      }
      else {
        msg.addMetadata(getMetadataKey(), formatter.format(1));
        actor.configure(StatementType.Insert, identity);
        actor.getInsert().executeUpdate();
      }
      commit(conn, msg);
    }
    catch (SQLException e) {
      rollback(conn, msg);
      throw new ServiceException("Failed whilst generating sequence number", e);
    }
    finally {
      JdbcUtil.closeQuietly(rs);
      actor.destroy();
      JdbcUtil.closeQuietly(conn);
    }
  }

  /**
   * Get the appropriate identity from the msg.
   *
   * <p>
   * For sequence numbers, the identity returned here will be used as a substitution for an SQL statement.
   * </p>
   *
   * @param msg the message currently being processed
   * @return the identity to be used as a parameter in the SQL statements, or null if no identity is required.
   * @throws ServiceException wrapping any exception.
   */
  protected abstract String getIdentity(AdaptrisMessage msg) throws ServiceException;

  /**
   * @return the selectStatement
   */
  public String getSelectStatement() {
    return selectStatement;
  }

  /**
   * Set the select statement to use when getting a sequence number.
   * 
   * @param stmt the update statement to set, default if not specified is {@value #DEFAULT_SELECT_STATEMENT}
   */
  public void setSelectStatement(String stmt) {
    selectStatement = stmt;
  }

  /**
   * @return the insertStatement
   */
  public String getInsertStatement() {
    return insertStatement;
  }

  /**
   * Set the insert statement to use when creating a new row in the sequence number table.
   * <p>
   * This statement is only executed if the configured select statement returns no rows.
   * </p>
   * 
   * @param stmt the update statement to set, default if not specified is {@value #DEFAULT_INSERT_STATEMENT}
   */
  public void setInsertStatement(String stmt) {
    insertStatement = stmt;
  }

  /**
   * @return the updateStatement
   */
  public String getUpdateStatement() {
    return updateStatement;
  }

  /**
   * Set the update statement to be used after extracting the sequence number.
   * 
   * @param stmt the update statement to set, default if not specified is {@value #DEFAULT_UPDATE_STATEMENT}
   */
  public void setUpdateStatement(String stmt) {
    updateStatement = stmt;
  }

  /**
   * @return the metadataKey
   */
  public String getMetadataKey() {
    return metadataKey;
  }

  /**
   * Set the metadata key where the resulting sequence number will be stored.
   *
   * @param key the metadataKey to set
   */
  public void setMetadataKey(String key) {
    metadataKey = key;
  }

  /**
   * @return the numberFormat
   */
  public String getNumberFormat() {
    return numberFormat;
  }

  /**
   * Metadata will be formatted using the pattern specified.
   *
   * <p>
   * This allows you to format the number precisely to the value that is required; e.g if you use "000000000" then the metadata
   * value is always 9 characters long, the number being prefixed by leading zeros
   * </p>
   *
   * @see java.text.DecimalFormat
   * @param format the numberFormat to set, you must set this otherwise the service will not initialise. If you use '0' with an
   *          overflow behaviour of 'Continue' then this will just use the raw number.
   */
  public void setNumberFormat(String format) {
    numberFormat = format;
  }

  /**
   * @return the alwaysReplaceMetadata
   */
  public Boolean getAlwaysReplaceMetadata() {
    return alwaysReplaceMetadata;
  }

  /**
   * Whether or not to always replace the metadata key.
   *
   * @param t the alwaysReplaceMetadata to set, default is true.
   */
  public void setAlwaysReplaceMetadata(Boolean t) {
    alwaysReplaceMetadata = t;
  }

  boolean alwaysReplaceMetadata() {
    return getAlwaysReplaceMetadata() != null ? getAlwaysReplaceMetadata().booleanValue() : true;
  }

  /**
   *
   * @return the resetStatement
   */
  public String getResetStatement() {
    return resetStatement;
  }

  /**
   * Set the statement that will be executed to reset the sequence number when the overflow behaviour of <code>ResetToOne</code> is
   * triggered.
   * 
   * @param s the resetStatement to set, default if not specified is {@value #DEFAULT_RESET_STATEMENT}
   */
  public void setResetStatement(String s) {
    this.resetStatement = s;
  }

  /**
   * @return the wrapBehaviour
   */
  public OverflowBehaviour getOverflowBehaviour() {
    return overflowBehaviour;
  }

  /**
   * Set the behaviour when the sequence number exceeds that specified by the number format.
   * 
   * @param s the behaviour to set (default is {@link OverflowBehaviour#Continue})
   * @see OverflowBehaviour
   */
  public void setOverflowBehaviour(OverflowBehaviour s) {
    overflowBehaviour = s;
  }

  private static OverflowBehaviour getBehaviour(OverflowBehaviour s) {
    return s == null ? OverflowBehaviour.Continue : s;
  }

  public Boolean getCreateDatabase() {
    return createDatabase;
  }

  String resetStatement() {
    return getResetStatement() != null ? getResetStatement() : DEFAULT_RESET_STATEMENT;
  }

  String insertStatement() {
    return getInsertStatement() != null ? getInsertStatement() : DEFAULT_INSERT_STATEMENT;

  }

  String selectStatement() {
    return getSelectStatement() != null ? getSelectStatement() : DEFAULT_SELECT_STATEMENT;
  }

  String updateStatement() {
    return getUpdateStatement() != null ? getUpdateStatement() : DEFAULT_UPDATE_STATEMENT;
  }

  /**
   * Specify whether or not to attempt to create a standard database table if one cannot be found.
   * <p>
   * By specifying this to be true, then you are implicitly accepting the default statements for update/insert/reset/select as you
   * will not be able to modify the create statement itself. The update/insert/reset/select statements will be reset back to their
   * defaults <strong>regardless of your configuration</strong> if the database had to be created.
   * </p>
   * <p>
   * You would use this in the event that you wanted to maintain sequence numbers and wanted a hands-off configuration of a derby
   * (or similar) database.
   * </p>
   *
   * @param b true to create the database; default is false.
   */
  public void setCreateDatabase(Boolean b) {
    createDatabase = b;
  }

  private boolean createDatabase() {
    return getCreateDatabase() != null ? getCreateDatabase().booleanValue() : false;
  }

  @Override
  public void prepareService() throws CoreException {
  }

  private class DatabaseActor {
    private PreparedStatement select = null, insert = null, update = null, reset = null;
    private Connection sqlConnection;

    DatabaseActor() {

    }

    void reInitialise(Connection c) throws SQLException {
      destroy();
      sqlConnection = c;
      if (createDatabase()) {
        createSequenceDatabase(DEFAULT_TABLE_NAME);
      }
      select = prepareStatement(sqlConnection, selectStatement());
      insert = prepareStatement(sqlConnection, insertStatement());
      update = prepareStatement(sqlConnection, updateStatement());
      reset = prepareStatement(sqlConnection, resetStatement());
    }

    void configure(StatementType type, String identity) throws SQLException {
      type.configure(this, identity);
    }

    void destroy() {
      JdbcUtil.closeQuietly(select);
      JdbcUtil.closeQuietly(insert);
      JdbcUtil.closeQuietly(update);
      JdbcUtil.closeQuietly(reset);
      JdbcUtil.closeQuietly(sqlConnection);
      sqlConnection = null;
    }

    PreparedStatement getSelect() {
      return select;
    }

    PreparedStatement getInsert() {
      return insert;
    }

    PreparedStatement getUpdate() {
      return update;
    }

    PreparedStatement getReset() {
      return reset;
    }

    Connection getSqlConnection() {
      return sqlConnection;
    }

    private void createSequenceDatabase(String tableName) throws SQLException {
      Statement create = null;
      ResultSet rs = getSqlConnection().getMetaData().getTables(null, null, tableName, null);
      try {
        // If ResultSet doesn't have any entries, then we can assume the table doesn't exist, so let's try and create it
        if (!rs.next()) {
          create = createStatement(getSqlConnection());
          create.execute(DEFAULT_CREATE_STATEMENT);
          resetStatements();
        }
      }
      finally {
        JdbcUtil.closeQuietly(create);
        JdbcUtil.closeQuietly(rs);
      }
    }
  }

}
