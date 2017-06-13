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
public class ExecuteUpdateCallableStatementExecutor extends CallableStatementExecutorImpl {

  @Override
  public JdbcResult executeCallableStatement(CallableStatement statement) throws SQLException {
    int updatedCount = statement.executeUpdate();

    JdbcResult result = new JdbcResultBuilder().setResultSet(statement, ignoreMoreResultsException())
        .setRowsUpdatedCount(updatedCount).build();

    return result;
  }

}
