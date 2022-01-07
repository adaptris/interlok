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

import com.adaptris.annotation.AdvancedConfig;
import com.adaptris.annotation.InputFieldDefault;
import com.adaptris.annotation.InputFieldHint;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.CoreException;
import com.adaptris.core.ServiceException;
import com.adaptris.core.jdbc.JdbcService;
import com.adaptris.core.services.SequenceNumber;
import com.adaptris.core.util.Args;
import com.adaptris.core.util.ExceptionHelper;
import com.adaptris.core.util.JdbcUtil;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;

import javax.validation.constraints.NotNull;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DecimalFormat;
import java.text.NumberFormat;

/**
 * Abstract base class for adding sequence numbers into metadata.
 * 
 * <p>
 * The default database schema is assumed to be
 * 
 * <pre>
 * {@code 
 * CREATE TABLE SEQUENCES (ID VARCHAR(255) NOT NULL, SEQ_NUMBER INT)
 * }
 * </pre> The default SQL statements reflect this; and provided that a table called 'SEQUENCES' contains at least those two columns
 * then it should work without any changes to the SQL statements. Be aware that all statements default to upper-case which will have
 * an impact if your database is case-sensitive (such as MySQL on Linux).
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


  @Getter
  @Setter
  @NotNull
  private SequenceNumber sequenceNumber;


  @InputFieldHint(style = "SQL")
  @AdvancedConfig(rare = true)
  private String selectStatement = null;
  @InputFieldHint(style = "SQL")
  @AdvancedConfig(rare = true)
  private String insertStatement = null;
  @InputFieldHint(style = "SQL")
  @AdvancedConfig(rare = true)
  private String updateStatement = null;
  @InputFieldHint(style = "SQL")
  @AdvancedConfig(rare = true)
  private String resetStatement = null;
  @AdvancedConfig(rare = true)
  @InputFieldDefault(value = "false")
  private Boolean createDatabase;


  public AbstractJdbcSequenceNumberService() {
    super();
    sequenceNumber = new SequenceNumber();
  }

  @Override
  protected void initJdbcService() throws CoreException {
    try {
      Args.notNull(sequenceNumber.getMetadataKey(), "metadataKey");
      Args.notNull(sequenceNumber.getNumberFormat(), "numberFormat");
    } catch (Exception e) {
      throw ExceptionHelper.wrapCoreException(e);
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
    NumberFormat formatter = new DecimalFormat(sequenceNumber.getNumberFormat());
    Connection conn = null;
    ResultSet rs = null;
    DatabaseActor actor = null;

    if (!sequenceNumber.alwaysReplaceMetadata() && msg.headersContainsKey(sequenceNumber.getMetadataKey())) {
      log.debug("{} already exists, not updating", sequenceNumber.getMetadataKey());
      return;
    }
    String identity = getIdentity(msg);
    try {
      conn = getConnection(msg);
      actor = new DatabaseActor(conn).applyIdentityParameter(identity);
      rs = actor.executeSelect();
      if (rs.next()) {
        int count = rs.getInt(1);
        String countString = formatter.format(count);
        if (exceedsMaxSequence(count)) {
          count = 1;
          countString = formatter.format(count);
          actor.executeReset(count + 1);
        } else {
          if (hasOverflowed(countString)) {
            count = (int)sequenceNumber.getBehaviour(sequenceNumber.getOverflowBehaviour()).wrap(count);
            countString = formatter.format(count);
            actor.executeReset(count + 1);
          } else {
            actor.executeUpdate();
          }
        }
        msg.addMetadata(sequenceNumber.getMetadataKey(), countString);
      }
      else {
        msg.addMetadata(sequenceNumber.getMetadataKey(), formatter.format(1));
        actor.executeInsert();
      }
      JdbcUtil.commit(conn, msg);
    }
    catch (SQLException e) {
      JdbcUtil.rollback(conn, msg);
      throw new ServiceException("Failed whilst generating sequence number", e);
    }
    finally {
      JdbcUtil.closeQuietly(rs, actor, conn);
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

  protected boolean hasOverflowed(String formattedCount) {
    return formattedCount.length() > sequenceNumber.getNumberFormat().length();
  }

  protected boolean exceedsMaxSequence(int count) {
    return sequenceNumber.getMaximumSequenceNumber() != null ? count > sequenceNumber.getMaximumSequenceNumber() : false;
  }

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

  public Boolean getCreateDatabase() {
    return createDatabase;
  }

  String resetStatement() {
    return StringUtils.defaultIfBlank(getResetStatement(), DEFAULT_RESET_STATEMENT);
  }

  String insertStatement() {
    return StringUtils.defaultIfBlank(getInsertStatement(), DEFAULT_INSERT_STATEMENT);
  }

  String selectStatement() {
    return StringUtils.defaultIfBlank(getSelectStatement(), DEFAULT_SELECT_STATEMENT);
  }

  String updateStatement() {
    return StringUtils.defaultIfBlank(getUpdateStatement(), DEFAULT_UPDATE_STATEMENT);
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
    return BooleanUtils.toBooleanDefaultIfNull(getCreateDatabase(), false);
  }

  @Override
  public void prepareService() throws CoreException {
  }

  private class DatabaseActor implements AutoCloseable {
    private PreparedStatement select = null, insert = null, update = null, reset = null;
    private Connection sqlConnection;

    DatabaseActor(Connection c) throws SQLException {
      sqlConnection = c;
      if (createDatabase()) {
        createSequenceDatabase(DEFAULT_TABLE_NAME);
      }
      select = prepareStatement(sqlConnection, selectStatement());
      insert = prepareStatement(sqlConnection, insertStatement());
      update = prepareStatement(sqlConnection, updateStatement());
      reset = prepareStatement(sqlConnection, resetStatement());
    }

    DatabaseActor applyIdentityParameter(String identity) throws SQLException {
      if (identity != null) {
        select.setString(1, identity);
        insert.setString(1, identity);
        update.setString(1, identity);
        reset.setString(2, identity);
      }
      return this;
    }

    @Override
    public void close() {
      JdbcUtil.closeQuietly(select, insert, update, reset);
      sqlConnection = null;
    }

    ResultSet executeSelect() throws SQLException {
      return select.executeQuery();
    }

    void executeInsert() throws SQLException {
      insert.executeUpdate();
    }

    void executeUpdate() throws SQLException {
      update.executeUpdate();
    }

    void executeReset(int resetValue) throws SQLException {
      reset.setInt(1, resetValue);
      reset.executeUpdate();
    }

    private void createSequenceDatabase(String tableName) throws SQLException {
      Statement create = null;
      ResultSet rs = sqlConnection.getMetaData().getTables(null, null, tableName, null);
      try {
        // If ResultSet doesn't have any entries, then we can assume the table doesn't exist, so let's try and create it
        if (!rs.next()) {
          create = createStatement(sqlConnection);
          create.execute(DEFAULT_CREATE_STATEMENT);
          resetStatements();
        }
      } finally {
        JdbcUtil.closeQuietly(create);
        JdbcUtil.closeQuietly(rs);
      }
    }
  }

}
