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

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import javax.validation.Valid;

import com.adaptris.annotation.AdapterComponent;
import com.adaptris.annotation.ComponentProfile;
import com.adaptris.core.AdaptrisConnection;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.CoreException;
import com.adaptris.core.Service;
import com.adaptris.core.ServiceException;
import com.adaptris.core.ServiceList;
import com.adaptris.core.jdbc.DatabaseConnection;
import com.adaptris.core.jdbc.JdbcConstants;
import com.adaptris.core.jdbc.JdbcService;
import com.adaptris.core.util.JdbcUtil;
import com.adaptris.core.util.LifecycleHelper;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Implementation of {@link com.adaptris.core.ServiceCollection} that creates a {@link java.sql.Connection} instance at the start of
 * the execution of
 * the service list and stores it in object metadata.
 * 
 * <p>
 * Other than the creation of the {@link java.sql.Connection} at the start of execution, all other behaviour is the same as
 * {@link ServiceList} which this class extends.
 * </p>
 * <p>
 * The rationale behind this service collection implementation is to allow {@link JdbcService} implementations to share the same
 * underlying {@link java.sql.Connection}. Embedded {@link JdbcService} implementations do not need to have a
 * {@link DatabaseConnection} configured as they can derive the correct DatabaseConnection from object metadata. This also allows
 * the services to participate in a simple transaction if the connection is not set to auto-commit. The event of an exception, the
 * connection is rolledback and then committed. If all services are considered successful then the transaction is committed (note
 * that continue-on-fail=true equates to success).
 * </p>
 * <p>
 * The standard use case for this will be multiple {@link JdbcDataCaptureService} or {@link
 * com.adaptris.core.services.jdbc.raw.JdbcRawDataCaptureService} services that
 * need to be executed but the captured data should only committed to the database at the end. In the event of an exception, all the
 * changes should be discarded ensuring that the database is consistent.
 * </p>
 * <p>
 * Note that only some JdbcService implementations support this behaviour namely concrete {@link AbstractJdbcSequenceNumberService}
 * instances, {@link JdbcDataCaptureService}, {@link com.adaptris.core.services.jdbc.raw.JdbcRawDataCaptureService} and {@link
 * JdbcDataQueryService}.
 * </p>
 * 
 * @config jdbc-service-list
 * 
 * 
 * @see JdbcConstants#OBJ_METADATA_DATABASE_CONNECTION_KEY
 * 
 * @author lchan
 */
@XStreamAlias("jdbc-service-list")
@AdapterComponent
@ComponentProfile(summary = "A collection of services which has an additional database connection", tag = "service,jdbc")
public class JdbcServiceList extends ServiceList {

  @Valid
  private AdaptrisConnection databaseConnection;

  public JdbcServiceList() {
    super();
  }

  public JdbcServiceList(Collection<Service> serviceList) {
    super(serviceList);
  }

  public JdbcServiceList(Service... services) {
    super(new ArrayList<Service>(Arrays.asList(services)));
  }

  @Override
  protected void applyServices(AdaptrisMessage msg) throws ServiceException {
    try {
      if (getDatabaseConnection() != null) {
        Connection conn = getDatabaseConnection().retrieveConnection(DatabaseConnection.class).connect();
        msg.getObjectMetadata().put(JdbcConstants.OBJ_METADATA_DATABASE_CONNECTION_KEY, conn);
      }
      super.applyServices(msg);
      // We may not have a valid connection here if we're using pooled connections.  If not, then no point in committing on a new connection.
      Connection conn = (Connection) msg.getObjectMetadata().get(JdbcConstants.OBJ_METADATA_DATABASE_CONNECTION_KEY);
      if((conn != null) && (!conn.isClosed()))
        JdbcUtil.commit(conn);
    }
    catch (Exception e) {
      rollback(msg, e);
      rethrowServiceException(e);
    }
    finally {
      Connection conn = (Connection) msg.getObjectMetadata().get(JdbcConstants.OBJ_METADATA_DATABASE_CONNECTION_KEY);
      if(conn != null)
        JdbcUtil.closeQuietly(conn);
    }
  }

  @Override
  protected void doInit() throws CoreException {
    LifecycleHelper.init(databaseConnection);
    super.doInit();
    if(getDatabaseConnection() != null) {
      for (Service service : this) {
        if(service instanceof JdbcService) {
          ((JdbcService) service).setConnection(getDatabaseConnection());
        }
      }
    }
  }

  @Override
  protected void doStart() throws CoreException {
    LifecycleHelper.start(databaseConnection);
    super.doStart();
  }

  @Override
  protected void doStop() {
    super.doStop();
    LifecycleHelper.stop(databaseConnection);
  }

  @Override
  protected void doClose() {
    super.doClose();
    LifecycleHelper.close(databaseConnection);
  }

  private void rollback(AdaptrisMessage msg, Exception exc) throws ServiceException {
    try {
      log.warn("Exception encountered; attempting rollback due to: " + exc.getMessage());
      Connection sqlConnection = (Connection) msg.getObjectMetadata().get(JdbcConstants.OBJ_METADATA_DATABASE_CONNECTION_KEY);
      if (inDebugMode()) {
        log.trace("Rolling back SQLConnection=" + sqlConnection);
      }
        // Theres no savepoint, we should just roll everything back.
      JdbcUtil.rollback(sqlConnection);
    }
    catch (Exception e) {
      rethrowServiceException(e);
    }
  }

  private boolean inDebugMode() {
    return getDatabaseConnection() != null && getDatabaseConnection().retrieveConnection(DatabaseConnection.class).debugMode();
  }

  private void rethrowServiceException(Throwable e) throws ServiceException {
    if (e instanceof ServiceException) {
      throw (ServiceException) e;
    }
    throw new ServiceException(e);
  }

  @Override
  public void prepare() throws CoreException {
    super.prepare();
    if (databaseConnection != null) {
      databaseConnection.retrieveConnection(DatabaseConnection.class).prepare();
    }
  }

  /**
   * Get the connection that will be used the underlying {@link JdbcService} instances.
   *
   * @return the connection.
   */
  public AdaptrisConnection getDatabaseConnection() {
    if(databaseConnection == null)
      return null;
    return databaseConnection.retrieveConnection(DatabaseConnection.class);
  }

  /**
   * Set the connection that will be used by all {@link JdbcService} instances in this service list.
   * <p>
   * Traditionally, you would use a {@link DatabaseConnection} instance here, but it is an {@link com.adaptris.core.AdaptrisConnection} so that you
   * can use a {@link com.adaptris.core.SharedConnection} where appropriate.
   * </p>
   * 
   * @param c
   */
  public void setDatabaseConnection(AdaptrisConnection c) {
    databaseConnection = c;
  }

}
