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

package com.adaptris.core.jdbc;

import static org.apache.commons.lang.StringUtils.isEmpty;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import com.adaptris.annotation.AdapterComponent;
import com.adaptris.annotation.AdvancedConfig;
import com.adaptris.annotation.AutoPopulated;
import com.adaptris.annotation.ComponentProfile;
import com.adaptris.annotation.DisplayOrder;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.CoreException;
import com.adaptris.core.ProduceDestination;
import com.adaptris.core.ProduceException;
import com.adaptris.core.RequestReplyProducerImp;
import com.adaptris.core.services.jdbc.ResultSetTranslator;
import com.adaptris.core.util.JdbcUtil;
import com.adaptris.jdbc.CallableStatementCreator;
import com.adaptris.jdbc.CallableStatementExecutor;
import com.adaptris.jdbc.DefaultStatementCreator;
import com.adaptris.jdbc.ExecuteCallableStatementExecutor;
import com.adaptris.jdbc.JdbcResult;
import com.adaptris.jdbc.ParameterType;
import com.adaptris.jdbc.StoredProcedure;
import com.adaptris.jdbc.StoredProcedureParameter;
import com.adaptris.util.TimeInterval;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * StoredProcedure Producer implementation; executes a stored procedure within your chosen database vendor.
 * 
 * <p>
 * To specify your chosen database vendor, you will set the {@link com.adaptris.jdbc.CallableStatementCreator} to one of the
 * following
 * implementations; {@link com.adaptris.jdbc.MysqlStatementCreator} or {@link com.adaptris.jdbc.OracleStatementCreator}, other types
 * of database can be directly
 * supported by using {@link com.adaptris.jdbc.ConfiguredStatementCreator}. Generally, {@link #setDestination(ProduceDestination)
 * ProduceDestination}
 * will be used to derive the stored procedure to execute.
 * </p>
 * <p>
 * You may also set parameters (In, Out and InOut) for your Stored Procedure. Both the "In" and the "InOut" parameters will retrieve
 * their data from the {@link com.adaptris.core.AdaptrisMessage} and that data will be passed into the stored procedure. Both the
 * "Out" and "InOut"
 * parameters will have the result from the Stored Procedure re-applied back into the {@link com.adaptris.core.AdaptrisMessage}. The
 * method with which
 * these parameters get or apply their values to and from the {@link com.adaptris.core.AdaptrisMessage} will depend on the parameter
 * implementation
 * chosen, see the sub classes of {@link AbstractParameter} To set the parameters for your Stored Procedure configure the following;
 * - {@link InParameters} - {@link OutParameters} - {@link InOutParameters}
 * </p>
 * <p>
 * Each parameter you set, must include one of either the Order or the Name. The name of the parameter is mapped the parameter name
 * of the Stored Procedure, the Order will define the order.Stored Procedures, as well as returning data through Out and InOut
 * parameters, may also return data through a {@link java.sql.ResultSet}. In fact, Stored Procedures may return multiple {@link
 * java.sql.ResultSet}'s.
 * {@link java.sql.ResultSet}'s may also be applied back into the {@link com.adaptris.core.AdaptrisMessage} To define the behaviour
 * of applying
 * {@link java.sql.ResultSet}'s back into the {@link com.adaptris.core.AdaptrisMessage} set the translator for this producer, see
 * {@link ResultSetTranslator}
 * Note; if your Stored Procedure returns multiple result sets, each will be applied back into your {@link
 * com.adaptris.core.AdaptrisMessage} using
 * the {@link ResultSetTranslator} configured.
 * </p>
 * <p>
 * If you plan on using this producer for Microsoft SQL Server, we recommend using a driver that is fully JDBC 3.0 compliant.  The standard
 * Microsoft JDBC driver does not support multiple open result sets, therefore if your stored procedure returns multiple result sets, we recommend using
 * a fully compliant JDBC 3.0 driver such as the JTDS driver.
 * </p>
 * <p>
 * Finally, the default timeout set for the database operation is 0 (i.e. no timeout). You can override this by configuring the
 * "timeout" field with a {@link TimeInterval}
 * </p>
 * 
 * @config jdbc-stored-procedure-producer
 * 
 * @author Aaron McGrath
 * 
 */
@XStreamAlias("jdbc-stored-procedure-producer")
@AdapterComponent
@ComponentProfile(summary = "Execute a stored procedure via JDBC", tag = "producer,jdbc", recommended = {DatabaseConnection.class})
@DisplayOrder(order = {"statementCreator", "statementExecutor", "inParameters", "outParameters", "inOutParameters",
    "resultSetTranslator", "timeout"})
public class JdbcStoredProcedureProducer extends RequestReplyProducerImp {

  private static final long DEFAULT_TIMEOUT_MS = 0;
  
  @NotNull
  @AutoPopulated
  @Valid
  private InParameters inParameters;
  @NotNull
  @AutoPopulated
  @Valid
  private OutParameters outParameters;
  @NotNull
  @AutoPopulated
  @Valid
  private InOutParameters inOutParameters;
  @NotNull
  @AutoPopulated
  @Valid
  @AdvancedConfig
  private CallableStatementCreator statementCreator;
  @NotNull
  @AutoPopulated
  @Valid
  @AdvancedConfig
  private CallableStatementExecutor statementExecutor;
  @NotNull
  @Valid
  private ResultSetTranslator resultSetTranslator;
  @AdvancedConfig
  private TimeInterval timeout;

  public JdbcStoredProcedureProducer() {
    setInParameters(new InParameters());
    setOutParameters(new OutParameters());
    setInOutParameters(new InOutParameters());
    setStatementCreator(new DefaultStatementCreator());
    setStatementExecutor(new ExecuteCallableStatementExecutor());
  }

  @Override
  protected AdaptrisMessage doRequest(AdaptrisMessage msg, ProduceDestination destination, long timeout) throws ProduceException {
    Connection connection = null;
    JdbcResult results = null;
    try {
      connection = getConnection(msg);

      StoredProcedure storedProcedure = new StoredProcedure();
      storedProcedure.setConnection(connection);
      storedProcedure.setName(destination.getDestination(msg));
      storedProcedure.setStatementCreator(getStatementCreator());
      storedProcedure.setParameters(parseInParameters(msg));
      storedProcedure.setStatementExecutor(getStatementExecutor());
      storedProcedure.setTimeout(this.defaultTimeout());
      storedProcedure.setAdaptrisMessage(msg);
      storedProcedure.setResultSetTranslator(getResultSetTranslator());

      results = storedProcedure.execute();

      parseOutParameters(msg, results.getParameters());

      commit(connection, msg);
    }
    catch (Exception e) {
      rollback(connection, msg);
      throw new ProduceException(e);
    }
    finally {
      JdbcUtil.closeQuietly(results);
      JdbcUtil.closeQuietly(connection);
    }
    return msg;
  }
  
  @Override
  public void produce(AdaptrisMessage msg, ProduceDestination destination) throws ProduceException {
    this.doRequest(msg, destination, this.defaultTimeout());
  }

  /**
   * Get the {@link Connection} either from the {@link com.adaptris.core.AdaptrisMessage} object or from configuration.
   *
   * @param msg the adaptrisMessage object
   * @return the connection either from the adaptris message or from configuration.
   */
  private Connection getConnection(AdaptrisMessage msg) throws SQLException {
    if (msg.getObjectHeaders().containsKey(JdbcConstants.OBJ_METADATA_DATABASE_CONNECTION_KEY)) {
      return (Connection) msg.getObjectHeaders().get(JdbcConstants.OBJ_METADATA_DATABASE_CONNECTION_KEY);
    }
    return retrieveConnection(DatabaseConnection.class).connect();
  }

  /**
   * Rollback to the stored savepoint.
   * <p>
   * If a database connection exists in the AdaptrisMessage object metadata then you don't want to rollback, you want to let the
   * parent (presumably a {@link com.adaptris.core.services.jdbc.JdbcServiceList}) to do it for you.
   * </p>
   *
   * @param sqlConnection the database connection.
   * @param msg the AdaptrisMessage
   */
  private void rollback(Connection sqlConnection, AdaptrisMessage msg) {
    if (msg.getObjectHeaders().containsKey(JdbcConstants.OBJ_METADATA_DATABASE_CONNECTION_KEY)) {
      return;
    }
    JdbcUtil.rollback(sqlConnection);
  }

  /**
   * Commit the connection
   * <p>
   * If a database connection exists in the AdaptrisMessage object metadata then you don't want to rollback, you want to let the
   * parent (presumably a {@link com.adaptris.core.services.jdbc.JdbcServiceList}) to do it for you.
   * </p>
   *
   * @param sqlConnection the SQL Connection
   * @param msg the AdaptrisMessage currently being processed.
   * @throws SQLException if the commit fails.
   */
  private void commit(Connection sqlConnection, AdaptrisMessage msg) throws SQLException {
    if (msg.getObjectHeaders().containsKey(JdbcConstants.OBJ_METADATA_DATABASE_CONNECTION_KEY)) {
      return;
    }
    JdbcUtil.commit(sqlConnection);
  }

  private void parseOutParameters(AdaptrisMessage msg, List<StoredProcedureParameter> parameters) throws JdbcParameterException {
    for(StoredProcedureParameter param : parameters) {
      if(param.getParameterType() == ParameterType.OUT) {
        if(!isEmpty(param.getName())) {
          getOutParameters().getByName(param.getName()).applyOutputParam(param.getOutValue(), msg);
        }
        else {
          getOutParameters().getByOrder(param.getOrder()).applyOutputParam(param.getOutValue(), msg);
        }
      }

      if(param.getParameterType() == ParameterType.INOUT) {
        if(!isEmpty(param.getName())) {
          getInOutParameters().getByName(param.getName()).applyOutputParam(param.getOutValue(), msg);
        }
        else {
          getInOutParameters().getByOrder(param.getOrder()).applyOutputParam(param.getOutValue(), msg);
        }
      }
    }

  }

  private List<StoredProcedureParameter> parseInParameters(AdaptrisMessage msg) throws JdbcParameterException {
    ArrayList<StoredProcedureParameter> params = new ArrayList<StoredProcedureParameter>();

    for (InParameter p : getInParameters().getParameters()) {
      params.add(new StoredProcedureParameter(p.getName(), p.getOrder(), p.getType(), ParameterType.IN, p.applyInputParam(msg)));
    }

    for (InOutParameter p : getInOutParameters().getParameters()) {
      params.add(new StoredProcedureParameter(p.getName(), p.getOrder(), p.getType(), ParameterType.INOUT, p.applyInputParam(msg)));
    }

    for (OutParameter p : getOutParameters().getParameters()) {
      params.add(new StoredProcedureParameter(p.getName(), p.getOrder(), p.getType(), ParameterType.OUT, null));
    }

    return params;
  }

  @Override
  public void init() throws CoreException {
  }

  @Override
  public void start() throws CoreException {
  }

  @Override
  public void stop() {
  }

  @Override
  public void close() {
  }

  @Override
  public void prepare() throws CoreException {
  }

  public InParameters getInParameters() {
    return inParameters;
  }

  public void setInParameters(InParameters inParameters) {
    this.inParameters = inParameters;
  }

  public OutParameters getOutParameters() {
    return outParameters;
  }

  public void setOutParameters(OutParameters outParameters) {
    this.outParameters = outParameters;
  }

  public InOutParameters getInOutParameters() {
    return inOutParameters;
  }

  public void setInOutParameters(InOutParameters inOutParameters) {
    this.inOutParameters = inOutParameters;
  }

  public CallableStatementCreator getStatementCreator() {
    return statementCreator;
  }

  public void setStatementCreator(CallableStatementCreator statementCreator) {
    this.statementCreator = statementCreator;
  }

  public CallableStatementExecutor getStatementExecutor() {
    return statementExecutor;
  }

  public void setStatementExecutor(CallableStatementExecutor statementExecutor) {
    this.statementExecutor = statementExecutor;
  }

  public ResultSetTranslator getResultSetTranslator() {
    return resultSetTranslator;
  }

  public void setResultSetTranslator(ResultSetTranslator resultSetTranslator) {
    this.resultSetTranslator = resultSetTranslator;
  }

  @Override
  protected long defaultTimeout() {
    if(this.getTimeout() != null)
      return this.getTimeout().toMilliseconds();
    else
      return DEFAULT_TIMEOUT_MS;
  }

  public TimeInterval getTimeout() {
    return timeout;
  }

  public void setTimeout(TimeInterval timeout) {
    this.timeout = timeout;
  }

}
