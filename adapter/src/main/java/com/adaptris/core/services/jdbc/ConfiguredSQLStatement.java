package com.adaptris.core.services.jdbc;

import javax.validation.constraints.NotNull;

import com.adaptris.annotation.InputFieldHint;
import com.adaptris.core.AdaptrisMessage;

public class ConfiguredSQLStatement implements JdbcStatementCreator {

  public ConfiguredSQLStatement() {
  }
  
  public ConfiguredSQLStatement(String statement) {
    setStatement(statement);
  }
  
  @NotNull
  @InputFieldHint(style="SQL")
  private String statement;
  
  @Override
  public String createStatement(AdaptrisMessage msg) {
    return statement;
  }

  public String getStatement() {
    return statement;
  }

  public void setStatement(String statement) {
    this.statement = statement;
  }

}
