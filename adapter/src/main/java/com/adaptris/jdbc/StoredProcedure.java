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

import static org.apache.commons.lang.StringUtils.isEmpty;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.adaptris.core.CoreException;

public class StoredProcedure {

  private transient Logger log = LoggerFactory.getLogger(this.getClass());

  private String name;

  private List<StoredProcedureParameter> parameters;

  private Connection connection = null;

  private CallableStatementCreator statementCreator;

  private CallableStatementExecutor statementExecutor;
  
  private long timeout;

  public StoredProcedure() {
    setParameters(new ArrayList<StoredProcedureParameter>());
  }

  public StoredProcedure(String name, List<StoredProcedureParameter> parameters, Connection connection, CallableStatementCreator statementCreator) {
    setName(name);
    setConnection(connection);
    setStatementCreator(statementCreator);
    setParameters(new ArrayList<StoredProcedureParameter>());
  }

  public JdbcResult execute() throws CoreException {
    
    try {
      String sqlStatement =getStatementCreator().createCall(getName(), getParameters().size());
      log.trace("Generated SQL Statement [{}]", sqlStatement);
      CallableStatement statement = getConnection().prepareCall(sqlStatement);
      if (timeout > 0) {
        statement.setQueryTimeout((int) TimeUnit.MILLISECONDS.toSeconds(this.getTimeout()));
      }
      applyInParameters(statement);

      JdbcResult results = statementExecutor.executeCallableStatement(statement);
      results.setCallableStatement(statement);

      return results;

    } catch (SQLException e) {
      throw new CoreException(e);
    }
  }

  private void applyInParameters(CallableStatement statement) throws SQLException {
    for(StoredProcedureParameter param : getParameters()) {
      if(param.getParameterType().equals(ParameterType.IN) || param.getParameterType().equals(ParameterType.INOUT)) {
        if(!isEmpty(param.getName())) {
          statement.setObject(param.getName(), param.getInValue(), param.getParameterValueType().getValue());
          log.debug("Applying 'IN' parameter with name '" + param.getName() + "' and value '" + param.getInValue() + "'");
        }
        else {
          statement.setObject(param.getOrder(), param.getInValue(), param.getParameterValueType().getValue());
          log.debug("Applying 'IN' parameter with order '" + param.getOrder() + "' and value '" + param.getInValue() + "'");
        }
      }
        
      if(param.getParameterType().equals(ParameterType.OUT) || param.getParameterType().equals(ParameterType.INOUT)) {
        if(!isEmpty(param.getName())){
          statement.registerOutParameter(param.getName(), param.getParameterValueType().getValue());
          log.debug("Registering 'OUT' parameter with name '" + param.getName() + "'");
        }
        else {
          statement.registerOutParameter(param.getOrder(), param.getParameterValueType().getValue());
          log.debug("Registering 'OUT' parameter with order '" + param.getOrder() + "'");
        }
      }
    }
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public Connection getConnection() {
    return connection;
  }

  public void setConnection(Connection connection) {
    this.connection = connection;
  }

  public CallableStatementCreator getStatementCreator() {
    return statementCreator;
  }

  public void setStatementCreator(CallableStatementCreator statementCreator) {
    this.statementCreator = statementCreator;
  }

  public CallableStatementExecutor getStatementExecutor() {
    return statementExecutor;
  }

  public void setStatementExecutor(CallableStatementExecutor statementExecutor) {
    this.statementExecutor = statementExecutor;
  }

  public List<StoredProcedureParameter> getParameters() {
    return parameters;
  }

  public void setParameters(List<StoredProcedureParameter> parameters) {
    this.parameters = parameters;
  }

  public void addParameter(StoredProcedureParameter parameter) {
    parameters.add(parameter);
  }

  public long getTimeout() {
    return timeout;
  }

  /**
   * Set the timeout in ms.
   * 
   * @param timeout
   */
  public void setTimeout(long timeout) {
    this.timeout = timeout;
  }
}
