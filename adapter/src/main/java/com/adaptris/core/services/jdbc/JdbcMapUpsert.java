/*
 * Copyright 2017 Adaptris Ltd.
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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.adaptris.annotation.InputFieldDefault;
import com.adaptris.core.ServiceException;
import com.adaptris.core.util.ExceptionHelper;
import com.adaptris.core.util.JdbcUtil;

/**
 * 
 * Base behaviour for upserting objects directly into a db.
 *
 */
public abstract class JdbcMapUpsert extends JdbcMapInsert {

  public static String DEFAULT_ID_FIELD = "id";

  @InputFieldDefault(value = "id")
  private String idField;

  public JdbcMapUpsert() {
    super();
  }


  /**
   * @return the idPath
   */
  public String getIdField() {
    return idField;
  }


  /**
   * @param elem the field that is the ID, defaults to {@code id} if not specified.
   */
  public void setIdField(String elem) {
    this.idField = elem;
  }

  public JdbcMapUpsert withId(String elem) {
    setIdField(elem);
    return this;
  }

  protected String idField() {
    return getIdField() != null ? getIdField() : DEFAULT_ID_FIELD;
  }

  protected void handleUpsert(Connection conn, Map<String, String> object) throws ServiceException {
    PreparedStatement selectStmt = null, insertStmt = null, updateStmt = null;
    ResultSet rs = null;
    try {
      InsertWrapper inserter = new InsertWrapper(object);
      SelectWrapper selector = new SelectWrapper(object);
      UpdateWrapper updater = new UpdateWrapper(object);
      log.trace("SELECT [{}]", selector.statement);
      log.trace("INSERT [{}]", inserter.statement);
      log.trace("UPDATE [{}]", updater.statement);
      selectStmt = selector.addParams(prepareStatement(conn, selector.statement), object);
      rs = selectStmt.executeQuery();
      if (rs.next()) {
        updateStmt = updater.addParams(prepareStatement(conn, updater.statement), object);
        updateStmt.executeUpdate();
      }
      else {
        insertStmt = inserter.addParams(prepareStatement(conn, inserter.statement), object);
        insertStmt.executeUpdate();
      }
    }
    catch (SQLException e) {
      throw ExceptionHelper.wrapServiceException(e);
    }
    finally {
      JdbcUtil.closeQuietly(rs);
      JdbcUtil.closeQuietly(selectStmt);
      JdbcUtil.closeQuietly(insertStmt);
      JdbcUtil.closeQuietly(updateStmt);
    }
  }

  protected class UpdateWrapper {
    protected List<String> columns;
    protected String statement;

    protected UpdateWrapper(Map<String, String> obj) {
      columns = new ArrayList<>(obj.keySet());
      columns.remove(idField());
      StringBuilder statementBuilder = new StringBuilder(String.format("UPDATE %s SET", getTable()));
      // Add all the updates.
      for (Iterator<String> i = columns.iterator(); i.hasNext();) {
        String col = i.next();
        statementBuilder.append(String.format(" %s=? ", col));
        if (i.hasNext()) {
          statementBuilder.append(",");
        }
      }
      // Add the where clause
      statementBuilder.append(String.format(" WHERE %s=?", idField()));
      statement = statementBuilder.toString();
    }

    protected PreparedStatement addParams(PreparedStatement statement, Map<String, String> obj) throws SQLException {
      int paramIndex = 1;
      statement.clearParameters();
      // Set all the updates
      for (Iterator<String> i = columns.iterator(); i.hasNext(); paramIndex++) {
        String key = i.next();
        statement.setObject(paramIndex, obj.get(key));
      }
      // Set the WHERE.
      statement.setObject(columns.size() + 1, obj.get(idField()));
      return statement;
    }
  }

  protected class SelectWrapper {
    protected String statement;

    SelectWrapper(Map<String, String> obj) {
      statement = String.format("SELECT %s FROM %s WHERE %s = ?", idField(), getTable(), idField());
    }

    protected PreparedStatement addParams(PreparedStatement statement, Map<String, String> obj) throws SQLException {
      int paramIndex = 1;
      statement.clearParameters();
      statement.setObject(1, obj.get(idField()));
      return statement;
    }
  }

}
