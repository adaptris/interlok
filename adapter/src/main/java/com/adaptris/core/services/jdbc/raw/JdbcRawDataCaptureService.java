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

import static org.apache.commons.lang.StringUtils.isEmpty;

import java.sql.Connection;
import java.sql.PreparedStatement;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import com.adaptris.annotation.AutoPopulated;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.ServiceException;
import com.adaptris.core.services.jdbc.JdbcDataCaptureServiceImpl;
import com.adaptris.core.services.jdbc.JdbcStatementParameter;
import com.adaptris.core.services.jdbc.StatementParameter;
import com.adaptris.core.services.jdbc.StatementParameterList;
import com.adaptris.core.util.JdbcUtil;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamImplicit;

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
 * @license STANDARD
 */
@XStreamAlias("jdbc-raw-data-capture-service")
public class JdbcRawDataCaptureService extends JdbcDataCaptureServiceImpl {
  @XStreamImplicit
  @NotNull
  @AutoPopulated
  @Valid
  private StatementParameterList statementParameters = null;

  /**
   * <p>
   * Creates a new instance.
   * </p>
   */
  public JdbcRawDataCaptureService() {
    super();
    setStatementParameters(new StatementParameterList());
  }

  /**
   * Add a StatementParameter to this service.
   *
   * @param query the StatementParameter
   */
  public void addStatementParameter(StatementParameter query) {
    statementParameters.add(query);
  }

  /**
   * Get the configured parameters
   *
   * @return the list.
   */
  public StatementParameterList getStatementParameters() {
    return statementParameters;
  }

  /**
   * Set the configured parameters list.
   *
   * @param statementParameterList the list.
   * @see JdbcStatementParameter
   */
  public void setStatementParameters(StatementParameterList statementParameterList) {
    statementParameters = statementParameterList;
  }

  @Override
  public void doService(AdaptrisMessage msg) throws ServiceException {
    log.trace("Beginning doService in " + (!isEmpty(getUniqueId()) ? getUniqueId() : this.getClass().getSimpleName()));
    Connection conn = null;

    try {
      configureActor(msg);
      conn = actor.getSqlConnection();
      PreparedStatement insert = actor.getInsertStatement();
      
      this.getParameterApplicator().applyStatementParameters(msg, insert, this.getStatementParameters(), getStatement());      
      
      insert.executeUpdate();
      // Will only store the generated keys from the last query
      saveKeys(msg);
      commit(conn, msg);
    }
    catch (Exception e) {
      rollback(conn, msg);
      rethrowServiceException(e);
    } finally {
      JdbcUtil.closeQuietly(conn);
    }
    return;
  }
}
