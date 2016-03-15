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

public class JdbcResultBuilder {

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
    result.setResultSets(this.mapResultSet(statement));
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
    return new JdbcResultSet(resultSet);
  }

  private List<JdbcResultSet> mapResultSet(Statement statement) throws SQLException {
    ArrayList<JdbcResultSet> result = new ArrayList<JdbcResultSet>();

    do {
      JdbcResultSet singleResultSet = new JdbcResultSet(statement.getResultSet());
      result.add(singleResultSet);
    } while(statement.getMoreResults());

    return result;
  }
}
