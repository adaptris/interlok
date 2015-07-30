package com.adaptris.jdbc;

import java.sql.CallableStatement;
import java.sql.SQLException;

import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Implementation of CallableStatementExecutor will use the {@link CallableStatement#executeUpdate()} method.
 * 
 * <p>
 * Depending on the database vendor, using execute(), executeUpdate() or executeQuery() will deliver your results in a different
 * way. This implementation will not create a ResultSet.
 * 
 * </p>
 * 
 * @config execute-update-callable-statement-executor
 * 
 * @author Aaron McGrath
 */
@XStreamAlias("execute-update-callable-statement-executor")
public class ExecuteUpdateCallableStatementExecutor implements CallableStatementExecutor {

  @Override
  public JdbcResult executeCallableStatement(CallableStatement statement) throws SQLException {
    int updatedCount = statement.executeUpdate();
    
    JdbcResult result = new JdbcResultBuilder()
      .setResultSet(statement)
      .setRowsUpdatedCount(updatedCount)
      .build();
    
    return result;
  }

}
