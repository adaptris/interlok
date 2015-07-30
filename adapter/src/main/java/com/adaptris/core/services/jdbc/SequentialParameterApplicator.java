package com.adaptris.core.services.jdbc;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.ServiceException;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * {@link ParameterApplicator} implementation that applies parameters sequentially.
 * 
 * <p>
 * This applies {@link StatementParameter} instances in the order that they are declared in adapter configuration and is the default
 * {@link ParameterApplicator} implementation
 * </p>
 * 
 * 
 */
@XStreamAlias("sequential-parameter-applicator")
public class SequentialParameterApplicator implements ParameterApplicator {

  @Override
  public void applyStatementParameters(AdaptrisMessage message, PreparedStatement statement, StatementParameterCollection parameters, String originalSql) throws ServiceException {
    try {
      for (int i = 1; i <= parameters.size(); i++) {
        StatementParameter statementParameter = parameters.get(i - 1);
        statementParameter.apply(i, statement, message);
      }      
    } catch(SQLException ex) {
      throw new ServiceException(ex);
    }
  }

  @Override
  public String prepareParametersToStatement(String originalStatement) {
    return originalStatement;
  }

}
