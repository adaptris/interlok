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
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.hibernate.validator.constraints.NotBlank;

import com.adaptris.core.CoreException;
import com.adaptris.core.ServiceException;
import com.adaptris.core.jdbc.JdbcService;
import com.adaptris.core.util.Args;
import com.adaptris.core.util.ExceptionHelper;
import com.adaptris.core.util.JdbcUtil;

/**
 * 
 * Base behaviour of inserting Objects directly into a db.
 *
 */
public abstract class JdbcMapInsert extends JdbcService {
  @NotBlank
  private String table;

  public JdbcMapInsert() {
    super();
  }

  /**
   * @return the table
   */
  public String getTable() {
    return table;
  }

  /**
   * @param s the table to insert on.
   */
  public void setTable(String s) {
    this.table = Args.notBlank(s, "table");
  }

  public JdbcMapInsert withTable(String s) {
    setTable(s);
    return this;
  }

  @Override
  protected void closeJdbcService() {}

  @Override
  protected void initJdbcService() throws CoreException {
    try {
      Args.notBlank(getTable(), "table-name");
    } catch (IllegalArgumentException e) {
      throw ExceptionHelper.wrapCoreException(e);
    }
  }

  @Override
  protected void prepareService() throws CoreException {}

  @Override
  protected void startService() throws CoreException {}

  @Override
  protected void stopService() {}


  protected void handleInsert(Connection conn, Map<String, String> obj) throws ServiceException {
    PreparedStatement insertStmt = null;
    try {
      InsertWrapper inserter = new InsertWrapper(obj);
      log.trace("INSERT [{}]", inserter.statement);
      insertStmt = inserter.addParams(prepareStatement(conn, inserter.statement), obj);
      insertStmt.executeUpdate();
    }
    catch (SQLException e) {
      throw ExceptionHelper.wrapServiceException(e);
    }
    finally {
      JdbcUtil.closeQuietly(insertStmt);
    }
  }

  protected class InsertWrapper {
    protected List<String> columns;
    protected String statement;

    InsertWrapper(Map<String, String> json) {
      columns = new ArrayList<>(json.keySet());
      statement = String.format("INSERT into %s (%s) VALUES (%s)", getTable(), createString(true), createString(false));
    }

    private String createString(boolean columnsNotQuestionMarks) {
      StringBuilder sb = new StringBuilder();
      for (Iterator<String> i = columns.iterator(); i.hasNext();) {
        String s = i.next();
        sb.append(columnsNotQuestionMarks ? s : "?");
        if (i.hasNext()) {
          sb.append(",");
        }
      }
      return sb.toString();
    }

    protected PreparedStatement addParams(PreparedStatement statement, Map<String, String> obj) throws SQLException {
      int paramIndex = 1;
      statement.clearParameters();
      for (Iterator<String> i = columns.iterator(); i.hasNext(); paramIndex++) {
        String key = i.next();
        statement.setObject(paramIndex, obj.get(key));
      }
      return statement;
    }
  }
}
