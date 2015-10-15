/*
 * Copyright 2015 Adaptris Ltd.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/

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
