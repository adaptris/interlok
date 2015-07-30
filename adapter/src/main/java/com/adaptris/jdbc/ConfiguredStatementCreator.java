package com.adaptris.jdbc;

import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * ConfiguredStatementCreator.
 * 
 * <p>
 * With this implementation of the CallableStatementCreator, you can specify the entire statement string. An example of a full
 * statement; { CALL procedureName(?, ?, ?); }
 * </p>
 * <p>
 * You have the choice of hard-coding the procedure name as in the above example, or simply inserting the dollar ($) symbol to have
 * the procedure name injected for you; { CALL $(?, ?, ?); }
 * </p>
 * 
 * @config configured-statement-creator
 * 
 */
@XStreamAlias("configured-statement-creator")
public class ConfiguredStatementCreator implements CallableStatementCreator {

  public String statement;
  
  @Override
  public String createCall(String procedureName, int parameterCount) {
    if(statement.indexOf("$") >= 0)
      return statement.replace("$", procedureName);
    
    return statement;
  }

  public String getStatement() {
    return statement;
  }

  public void setStatement(String statement) {
    this.statement = statement;
  }

}
