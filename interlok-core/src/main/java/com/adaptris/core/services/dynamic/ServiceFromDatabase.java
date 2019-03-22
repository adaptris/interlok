/*******************************************************************************
 * Copyright 2019 Adaptris Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package com.adaptris.core.services.dynamic;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import org.hibernate.validator.constraints.NotBlank;
import com.adaptris.annotation.ComponentProfile;
import com.adaptris.annotation.DisplayOrder;
import com.adaptris.annotation.InputFieldHint;
import com.adaptris.core.AdaptrisConnection;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.ServiceException;
import com.adaptris.core.jdbc.DatabaseConnection;
import com.adaptris.core.services.jdbc.FirstRowMetadataTranslator;
import com.adaptris.core.services.jdbc.JdbcDataQueryService;
import com.adaptris.core.util.Args;
import com.adaptris.core.util.JdbcUtil;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Extract the service to execute from a database
 * 
 * <p>
 * This executes the configured query, takes the first column of the first ResultSet and uses that
 * as the source for the dynamic service.
 * </p>
 * <p>
 * Since it supports the expression syntax; this is perfectly acceptable; It is up to you to protect
 * against SQL injection attacks.
 * 
 * <pre>
 * {@code SELECT dynamicService FROM services 
 *        WHERE src='%message{source}' 
 *              AND dest='%message{destination}' 
 *              AND msgType='%message{messageType}'}
 * </pre>
 * </p>
 * <p>
 * The alternative to this would be to use {@link JdbcDataQueryService} with a
 * {@link FirstRowMetadataTranslator} and subsequently a {@link ServiceFromDataInputParameter} (from
 * metadata). That might be more performant as you would benefit from prepared statement caching and
 * mitigate against SQL injection style attacks.
 * </p>
 * 
 * @config dynamic-service-from-database
 * @see DynamicServiceExecutor
 * 
 */
@XStreamAlias("dynamic-service-from-database")
@ComponentProfile(summary = "Extract the service to execute from a database",
    recommended = {DatabaseConnection.class}, since = "3.8.4")
@DisplayOrder(order = {"key", "connection"})
public class ServiceFromDatabase extends ExtractorWithConnection {

  @NotBlank
  @InputFieldHint(expression = true)
  private String query;
  @NotNull
  @Valid
  private AdaptrisConnection connection;

  public ServiceFromDatabase() {

  }

  @Override
  public InputStream getInputStream(AdaptrisMessage m) throws Exception {
    Args.notBlank(getQuery(), "query");
    Connection jdbcCon = getConnection().retrieveConnection(DatabaseConnection.class).connect();
    // needs multi-line mode.
    String jdbcQuery = m.resolve(getQuery(), true);
    ResultSet resultSet = null;
    Statement statement = null;
    InputStream result = null;
    try {
      statement = jdbcCon.createStatement();
      resultSet = statement.executeQuery(jdbcQuery);
      if (resultSet.next()) {
        result = new ServiceInputStream(resultSet, statement);
      } else {
        throw new ServiceException("No results from [" + jdbcQuery + "]");
      }
    } catch (Exception e) {
      JdbcUtil.closeQuietly(resultSet, statement);
      throw e;
    }
    return result;
  }

  public String getQuery() {
    return query;
  }

  public void setQuery(String query) {
    this.query = Args.notBlank(query, "query");
  }

  public ServiceFromDatabase withQuery(String q) {
    setQuery(q);
    return this;
  }

  private class ServiceInputStream extends FilterInputStream {
    private ResultSet result;
    private Statement statement;

    ServiceInputStream(ResultSet rs, Statement stmt) throws Exception {
      super(rs.getAsciiStream(1));
      result = rs;
      statement = stmt;
    }

    @Override
    public void close() throws IOException {
      super.close();
      JdbcUtil.closeQuietly(result, statement);
    }
  }
}
