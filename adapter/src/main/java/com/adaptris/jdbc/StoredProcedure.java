package com.adaptris.jdbc;

import static org.apache.commons.lang.StringUtils.isEmpty;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.adaptris.core.CoreException;
import com.adaptris.core.util.JdbcUtil;

public class StoredProcedure {

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
    CallableStatement statement = null;
    try {
      statement = getConnection().prepareCall(getStatementCreator().createCall(getName(), getParameters().size()));
      statement.setQueryTimeout((int) (this.getTimeout() / 1000));// seconds
      applyInParameters(statement);

      JdbcResult results = statementExecutor.executeCallableStatement(statement);

      applyOutParameters(statement);
      results.setParameters(getParameters());

      return results;

    } catch (SQLException e) {
      throw new CoreException(e);
    } finally {
      JdbcUtil.closeQuietly(statement);
    }
  }

  private void applyOutParameters(CallableStatement statement) throws SQLException {
    for(StoredProcedureParameter param : getParameters()) {
      if(param.getParameterType().equals(ParameterType.OUT) || param.getParameterType().equals(ParameterType.INOUT)) {
        if(!isEmpty(param.getName())) {
          param.setOutValue(statement.getObject(param.getName()));
        }
        else {
          param.setOutValue(statement.getObject(param.getOrder()));
        }
      }
    }
  }

  private void applyInParameters(CallableStatement statement) throws SQLException {
    for(StoredProcedureParameter param : getParameters()) {
      if(param.getParameterType().equals(ParameterType.IN) || param.getParameterType().equals(ParameterType.INOUT)) {
        if(!isEmpty(param.getName())) {
          statement.setObject(param.getName(), param.getInValue(), param.getParameterValueType().getValue());
        }
        else {
          statement.setObject(param.getOrder(), param.getInValue(), param.getParameterValueType().getValue());
        }
      }
        
      if(param.getParameterType().equals(ParameterType.OUT) || param.getParameterType().equals(ParameterType.INOUT)) {
        if(!isEmpty(param.getName()))
          statement.registerOutParameter(param.getName(), param.getParameterValueType().getValue());
        else
          statement.registerOutParameter(param.getOrder(), param.getParameterValueType().getValue());
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

  public void setTimeout(long timeout) {
    this.timeout = timeout;
  }
}
