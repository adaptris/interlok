package com.adaptris.core.services.jdbc;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import com.adaptris.core.AdaptrisMessage;

public interface JdbcStatementParameter {
  
  /**
   * Apply this statement parameter to the {@link PreparedStatement}.
   *
   * @param parameterIndex the index in the {@link PreparedStatement}
   * @param statement the {@link PreparedStatement}
   * @param msg the AdaptrisMessage
   * @throws SQLException on exception
   */
   void apply(int parameterIndex, PreparedStatement statement, AdaptrisMessage msg) throws Exception;

}
