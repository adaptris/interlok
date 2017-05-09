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

package com.adaptris.core.services.jdbc.raw;

import java.sql.Connection;
import java.sql.PreparedStatement;

import com.adaptris.annotation.AdapterComponent;
import com.adaptris.annotation.ComponentProfile;
import com.adaptris.annotation.DisplayOrder;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.ServiceException;
import com.adaptris.core.services.jdbc.JdbcDataCaptureServiceImpl;
import com.adaptris.core.util.ExceptionHelper;
import com.adaptris.core.util.JdbcUtil;
import com.adaptris.core.util.LoggingHelper;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Capture Data from a AdaptrisMessage and store it in a JDBC-compliant database.
 * <p>
 * {@link com.adaptris.core.services.jdbc.JdbcDataCaptureService} generally expects text or XML data to be available; this is what
 * is captured and stored in the database. This particular flavour does not make any assumptions about the nature of the payload,
 * and simply allows you to capture metadata and/or the entire payload and insert into into a database.
 * </p>
 * 
 * @config jdbc-raw-data-capture-service
 * 
 * 
 */
@XStreamAlias("jdbc-raw-data-capture-service")
@AdapterComponent
@ComponentProfile(summary = "Capture data from the message and store it in a database", tag = "service,jdbc")
@DisplayOrder(order = {"connection", "statement", "statementParameters", "parameterApplicator", "saveReturnedKeys",
    "saveReturnedKeysColumn", "saveReturnedKeysTable"})
public class JdbcRawDataCaptureService extends JdbcDataCaptureServiceImpl {

  public JdbcRawDataCaptureService() {
    super();
  }

  public JdbcRawDataCaptureService(String statement) {
    this();
    setStatement(statement);
  }

  @Override
  public void doService(AdaptrisMessage msg) throws ServiceException {
    log.trace("Beginning doService in {}", LoggingHelper.friendlyName(this));
    Connection conn = null;

    try {
      configureActor(msg);
      conn = actor.getSqlConnection();
      PreparedStatement insert = actor.getInsertStatement(msg);
      
      this.getParameterApplicator().applyStatementParameters(msg, insert, this.getStatementParameters(), getStatement());      
      
      insert.executeUpdate();
      // Will only store the generated keys from the last query
      saveKeys(msg, insert);
      commit(conn, msg);
    }
    catch (Exception e) {
      rollback(conn, msg);
      throw ExceptionHelper.wrapServiceException(e);
    } finally {
      JdbcUtil.closeQuietly(conn);
    }
    return;
  }
}
