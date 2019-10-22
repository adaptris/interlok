/*
 * Copyright 2017 Adaptris Ltd.
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
package com.adaptris.core.jdbc;

import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;
import javax.validation.constraints.NotBlank;
import org.apache.commons.io.IOUtils;
import com.adaptris.core.CoreException;
import com.adaptris.core.util.Args;
import com.adaptris.core.util.ExceptionHelper;
import com.adaptris.core.util.JdbcUtil;

public abstract class JdbcPooledConnectionImpl extends DatabaseConnection {
  @NotBlank
  private String connectUrl;
  protected transient PooledDataSource connectionPool;

  @Override
  protected Connection makeConnection() throws SQLException {
    return testConnection(connectionPool.getConnection());
  }

  @Override
  protected void initialiseDatabaseConnection() throws CoreException {
    try {
      validatePool();
      connect().close();
    }
    catch (Exception e) {
      throw ExceptionHelper.wrapCoreException(e);
    }
  }

  @Override
  protected void startDatabaseConnection() throws CoreException {
  }

  @Override
  protected void stopDatabaseConnection() {
  }

  @Override
  @SuppressWarnings("deprecation")
  protected void closeDatabaseConnection() {
    IOUtils.closeQuietly(connectionPool);
    connectionPool = null;
  }

  protected abstract PooledDataSource createPool() throws Exception;

  private synchronized void validatePool() throws Exception {
    if (connectionPool == null) {
      connectionPool = createPool();
    }
  }

  @Override
  public DataSource asDataSource() throws SQLException {
    try {
      validatePool();
    } catch (Exception ex) {
      throw new SQLException(ex);
    }
    return connectionPool;
  }

  @Override
  protected String getConnectionName() {
    return getConnectUrl();
  }

  /**
   * <p>
   * Run the test statement against the database.
   * </p>
   * 
   * @throws SQLException if the statement could not be performed.
   */
  private Connection testConnection(Connection sqlConnection) throws SQLException {
    if (!alwaysValidateConnection()) {
      return sqlConnection;
    }
    return JdbcUtil.testConnection(sqlConnection, getTestStatement(), debugMode());
  }

  public String getConnectUrl() {
    return connectUrl;
  }

  public void setConnectUrl(String connectUrl) {
    this.connectUrl = Args.notBlank(connectUrl, "connectUrl");
  }
}
