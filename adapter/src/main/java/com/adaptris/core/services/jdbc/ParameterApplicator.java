package com.adaptris.core.services.jdbc;

import java.sql.PreparedStatement;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.ServiceException;

/**
 * Interface defining how parameters are applied to SQL statements.
 * 
 * 
 */
public interface ParameterApplicator {
  
  void applyStatementParameters(AdaptrisMessage message, PreparedStatement statement, StatementParameterCollection parameters, String originalSql) throws ServiceException;
  
  String prepareParametersToStatement(String originalStatement);

}
