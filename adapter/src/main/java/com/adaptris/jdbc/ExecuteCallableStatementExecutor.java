package com.adaptris.jdbc;

import java.sql.CallableStatement;
import java.sql.SQLException;

import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Implementation of CallableStatementExecutor will use the {@link CallableStatement#execute()} method.
 * <p>
 * Depending on the database vendor, using execute(), executeUpdate() or executeQuery() will deliver your results in a different
 * way.
 * </p>
 * 
 * @config execute-callable-statement-executor
 * 
 * @author Aaron McGrath
 */
@XStreamAlias("execute-callable-statement-executor")
public class ExecuteCallableStatementExecutor implements CallableStatementExecutor {

  @Override
  public JdbcResult executeCallableStatement(CallableStatement statement) throws SQLException{
    boolean hasResultSet = statement.execute();
    
    JdbcResult result = new JdbcResultBuilder()
    .setHasResultSet(hasResultSet)
    .setResultSet(statement)
    .build();

    return result;
  }

}
