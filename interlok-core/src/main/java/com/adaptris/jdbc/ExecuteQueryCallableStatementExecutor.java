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
import java.sql.ResultSet;
import java.sql.SQLException;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
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
@JacksonXmlRootElement(localName = "execute-query-callable-statement-executor")
@XStreamAlias("execute-query-callable-statement-executor")
public class ExecuteQueryCallableStatementExecutor implements CallableStatementExecutor {

@Override
@SuppressWarnings({"lgtm [java/database-resource-leak]"})
public JdbcResult executeCallableStatement(CallableStatement statement) throws SQLException {
ResultSet resultSet = statement.executeQuery(); // lgtm

JdbcResult result = new JdbcResultBuilder().trackStatement(statement)
.setHasResultSet(true)
.setResultSet(resultSet)
.build();

return result;
}

}
