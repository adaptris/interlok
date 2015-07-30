package com.adaptris.jdbc;

import junit.framework.TestCase;

public class ConfiguredStatementCreatorTest extends TestCase{
  
  public void testConfiguredStatementCreator() {
    String statement = "{ CALL procedureName(?, ?, ?); }";
    
    ConfiguredStatementCreator statementCreator = new ConfiguredStatementCreator();
    statementCreator.setStatement(statement);
    
    assertEquals(statement, statementCreator.createCall(null, 0));
  }
  
  public void testConfiguredStatementCreatorWithProcName() {
    String expected = "{ CALL procedureName(?, ?, ?); }";
    String statement = "{ CALL $(?, ?, ?); }";
    
    ConfiguredStatementCreator statementCreator = new ConfiguredStatementCreator();
    statementCreator.setStatement(statement);
    
    assertEquals(expected, statementCreator.createCall("procedureName", 0));
  }
  
  public void testConfiguredStatementCreatorWithProcNameExtraWhiteSpace() {
    String expected = "{ CALL procedureName ( ? , ? , ? ); }";
    String statement = "{ CALL $ ( ? , ? , ? ); }";
    
    ConfiguredStatementCreator statementCreator = new ConfiguredStatementCreator();
    statementCreator.setStatement(statement);
    
    assertEquals(expected, statementCreator.createCall("procedureName", 0));
  }

}
