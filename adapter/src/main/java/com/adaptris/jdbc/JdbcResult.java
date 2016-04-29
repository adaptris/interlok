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

import java.io.Closeable;
import java.io.IOException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import com.adaptris.core.util.JdbcUtil;

/**
 * 
 * @author Aaron McGrath
 *
 */
public class JdbcResult implements Closeable {
  
  private boolean hasResultSet;
  
  private int numRowsUpdated;
  
  private Statement statement;
  
  private List<StoredProcedureParameter> parameters;
  
  private List<JdbcResultSet> resultSets;
  
  public JdbcResult() {
    this.setParameters(new ArrayList<StoredProcedureParameter>());
    this.setResultSets(new ArrayList<JdbcResultSet>());
  }

  public boolean isHasResultSet() {
    return hasResultSet;
  }

  public void setHasResultSet(boolean hasResultSet) {
    this.hasResultSet = hasResultSet;
  }

  public List<StoredProcedureParameter> getParameters() {
    return parameters;
  }

  public void setParameters(List<StoredProcedureParameter> parameters) {
    this.parameters = parameters;
  }

  public List<JdbcResultSet> getResultSets() {
    return resultSets;
  }

  public void setResultSets(List<JdbcResultSet> resultSet) {
    this.resultSets = resultSet;
  }

  public void addResultSet(JdbcResultSet resultSet) {
    this.getResultSets().add(resultSet);
  }
  
  public int countResultSets() {
    return this.getResultSets().size();
  }
  
  public JdbcResultSet getResultSet(int index) {
    return this.getResultSets().get(index);
  }
  
  public Statement getStatement() {
    return statement;
  }

  public void setStatement(Statement statement) {
    this.statement = statement;
  }
  
  public int getNumRowsUpdated() {
    return numRowsUpdated;
  }

  public void setNumRowsUpdated(int numRowsUpdated) {
    this.numRowsUpdated = numRowsUpdated;
  }
  
  @Override
  public void close() throws IOException {
    for(JdbcResultSet rs: getResultSets()) {
      rs.close();
    }
    JdbcUtil.closeQuietly(getStatement());
  }

  public String toString() {
    StringBuffer buffer = new StringBuffer();
    buffer.append(this.getClass().getSimpleName() + "\n");
    buffer.append("HasResultSet = " + this.isHasResultSet() + "\n");
    buffer.append("Parameters;\n");
    for(StoredProcedureParameter param : this.getParameters())
      buffer.append("\t" + param.toString() + "\n");
    buffer.append("ResultSets;\n");
    for(JdbcResultSet resultSet : this.getResultSets())
      buffer.append("\t" + resultSet.toString() + "\n");
    
    return buffer.toString();
  }

}
