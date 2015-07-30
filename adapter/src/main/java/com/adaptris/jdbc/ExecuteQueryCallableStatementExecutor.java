package com.adaptris.jdbc;

import java.sql.CallableStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Implementation of CallableStatementExecutor will use the {@link CallableStatement#executeQuery()} method.
 * <p>
 * Depending on the database vendor, using execute(), executeUpdate() or executeQuery() will deliver your results in a different
 * way.
 * </p>
 * 
 * @config execute-query-callable-statement-executor
 * 
 * @author Aaron McGrath
 */
@XStreamAlias("execute-query-callable-statement-executor")
public class ExecuteQueryCallableStatementExecutor implements CallableStatementExecutor {

  @Override
  public JdbcResult executeCallableStatement(CallableStatement statement) throws SQLException {
    ResultSet resultSet = statement.executeQuery();
    
    JdbcResult result = new JdbcResultBuilder()
    .setHasResultSet(true)
    .setResultSet(resultSet)
    .build();
    
    return result;
  }

}
