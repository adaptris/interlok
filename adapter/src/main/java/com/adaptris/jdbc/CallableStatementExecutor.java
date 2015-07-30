package com.adaptris.jdbc;

import java.sql.CallableStatement;
import java.sql.SQLException;

public interface CallableStatementExecutor {
  
  JdbcResult executeCallableStatement(CallableStatement s) throws SQLException;

}
