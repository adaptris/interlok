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

package com.adaptris.jdbc;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JdbcResultBuilder {
  protected transient Logger log = LoggerFactory.getLogger(this.getClass());

  private JdbcResult result;

  public JdbcResultBuilder() {
    result = new JdbcResult();
  }

  public JdbcResultBuilder setInOutParameters(List<StoredProcedureParameter> parameters) {
    result.setParameters(parameters);
    return this;
  }

  public JdbcResultBuilder setHasResultSet(boolean hasResultSet) {
    result.setHasResultSet(hasResultSet);
    return this;
  }

  public JdbcResultBuilder setResultSet(ResultSet resultSet) throws SQLException {
    result.addResultSet(this.mapResultSet(resultSet));
    return this;
  }

  public JdbcResultBuilder setResultSet(Statement statement) throws SQLException {
    return setResultSet(statement, false);
  }

  public JdbcResultBuilder setResultSet(Statement statement, boolean moreResultsQuietly) throws SQLException {
    result.setStatement(statement);
    result.setResultSets(this.mapResultSet(statement, moreResultsQuietly));
    return this;
  }

  public JdbcResultBuilder setRowsUpdatedCount(int count) throws SQLException {
    result.setNumRowsUpdated(count);
    return this;
  }

  public JdbcResult build() {
    return result;
  }

  private JdbcResultSet mapResultSet(ResultSet resultSet) throws SQLException {
    return new JdbcResultSetImpl(resultSet);
  }

  private List<JdbcResultSet> mapResultSet(Statement statement, boolean ignoreException) throws SQLException {
    ArrayList<JdbcResultSet> result = new ArrayList<JdbcResultSet>();
    try {
      boolean multipleResultSets = statement.getConnection().getMetaData().supportsMultipleOpenResults();
      do {
        JdbcResultSet singleResultSet = new JdbcResultSetImpl(statement.getResultSet());
        result.add(singleResultSet);
        if (!multipleResultSets) break;
      }
      while (statement.getMoreResults(Statement.KEEP_CURRENT_RESULT));
    }
    catch (SQLException e) {
      if (ignoreException) { 
        log.debug("Ignoring SQLException({}) : {}", e.getClass().getSimpleName(), e.getMessage());
      }
      else {
        throw e;
      }
    }
    return result;
  }
}
