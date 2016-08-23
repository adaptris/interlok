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

import static org.apache.commons.lang.StringUtils.isEmpty;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.xml.namespace.NamespaceContext;

import com.adaptris.annotation.AdapterComponent;
import com.adaptris.annotation.AdvancedConfig;
import com.adaptris.annotation.AutoPopulated;
import com.adaptris.annotation.ComponentProfile;
import com.adaptris.annotation.DisplayOrder;
import com.adaptris.annotation.InputFieldDefault;
import com.adaptris.annotation.InputFieldHint;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.CoreException;
import com.adaptris.core.ServiceException;
import com.adaptris.core.jdbc.DatabaseConnection;
import com.adaptris.core.util.DocumentBuilderFactoryBuilder;
import com.adaptris.core.util.ExceptionHelper;
import com.adaptris.core.util.JdbcUtil;
import com.adaptris.core.util.LifecycleHelper;
import com.adaptris.core.util.XmlHelper;
import com.adaptris.jdbc.JdbcResult;
import com.adaptris.jdbc.JdbcResultBuilder;
import com.adaptris.util.KeyValuePairSet;
import com.adaptris.util.text.xml.SimpleNamespaceContext;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Perform a triggered JDBC query and the results of this query that can be stored in the AdaptrisMessage.
 * 
 * @config jdbc-data-query-service
 * 
 * 
 * @author sellidge
 * @author $Author: sellidge $
 */
@XStreamAlias("jdbc-data-query-service")
@AdapterComponent
@ComponentProfile(summary = "Query a database and store the results in the message", tag = "service,jdbc",
    recommended = {DatabaseConnection.class})
@DisplayOrder(order = {"connection", "statementCreator", "statementParameters", "resultSetTranslator", "parameterApplicator",
 "namespaceContext", "xmlDocumentFactoryConfig", "ignoreExecuteQueryErrors"})
public class JdbcDataQueryService extends JdbcServiceWithParameters {

  static final String KEY_XML_UTILS = "XmlUtils_" + JdbcDataQueryService.class.getCanonicalName();
  static final String KEY_NAMESPACE_CTX = "NamespaceCtx_" + JdbcDataQueryService.class.getCanonicalName();
  static final String KEY_DOCBUILDER_FAC = "DocBuilderFactoryBuilder_" + JdbcDataQueryService.class.getCanonicalName();

  @InputFieldHint(style="SQL")
  @Deprecated
  private String statement;  
  @Valid
  private JdbcStatementCreator statementCreator;
  @NotNull
  @AutoPopulated
  @Valid
  private ResultSetTranslator resultSetTranslator;
  @AdvancedConfig
  @Valid
  private KeyValuePairSet namespaceContext;
  @AdvancedConfig
  @Valid
  private DocumentBuilderFactoryBuilder xmlDocumentFactoryConfig;

  @Valid
  @AdvancedConfig
  @InputFieldDefault(value = "false")
  private Boolean ignoreExecuteQueryErrors;

  private transient DatabaseActor actor;

  public JdbcDataQueryService() {
    setStatementParameters(new StatementParameterList());
    setResultSetTranslator(new XmlPayloadTranslator());
    setParameterApplicator(new SequentialParameterApplicator());
    actor = new DatabaseActor();
  }
  
  public JdbcDataQueryService(JdbcStatementCreator creator) {
    this();
    setStatementCreator(creator);
  }


  @Override
  protected void initJdbcService() throws CoreException {
    LifecycleHelper.init(resultSetTranslator);
    if (isEmpty(getStatement()) && getStatementCreator() == null) {
      throw new CoreException("No JDBC Statement or creator defined");
    }
    if(getStatementCreator() == null) {
      log.warn("'statement' is deprecated; use a statement-creator instead");
      setStatementCreator(new ConfiguredSQLStatement(statement));
    }
  }

  @Override
  protected void prepareService() throws CoreException {
    getResultSetTranslator().prepare();
  }

  @Override
  public void startService() throws CoreException {
    LifecycleHelper.start(resultSetTranslator);
  }

  @Override
  protected void closeJdbcService() {
    LifecycleHelper.close(resultSetTranslator);
  }

  @Override
  public void stopService() {
    LifecycleHelper.stop(resultSetTranslator);
    actor.destroy();
  }

  /**
   * The main service method, which sees the specified query executed and the results returned in an XML message.
   * 
   * @see com.adaptris.core.Service#doService(com.adaptris.core.AdaptrisMessage)
   */
  @Override
  public void doService(AdaptrisMessage msg) throws ServiceException {
    log.trace("Beginning doService");
    JdbcResult result = null;
    Connection conn = null;
    try {
      Connection c = getConnection(msg);
      if (!c.equals(actor.getSqlConnection())) {
        actor.reInitialise(c);
      }
      conn = actor.getSqlConnection();
      initXmlHelper(msg);
      String statement = getStatementCreator().createStatement(msg);
      PreparedStatement preparedStatement = actor.getQueryStatement(statement); 
      preparedStatement.clearParameters();
      log.trace("Executing statement [{}]", statement);
      
      this.getParameterApplicator().applyStatementParameters(msg, preparedStatement, getStatementParameters(), statement);
      try {
        ResultSet rs = preparedStatement.executeQuery();
        result = new JdbcResultBuilder().setHasResultSet(true).setResultSet(rs).build();
      } catch (SQLException e) {
        if (ignoreExecuteQueryErrors()) {
          log.debug("Ignore ExecuteQuery Errors enabled); using empty ResultSet");
          result = new JdbcResultBuilder().setHasResultSet(false).build();
        } else {
          throw e;
        }
      }
      resultSetTranslator.translate(result, msg);
      destroyXmlHelper(msg);
      commit(conn, msg);
    }
    catch (Exception e) {
      rollback(conn, msg);
      throw ExceptionHelper.wrapServiceException(e);
    }
    finally {
      JdbcUtil.closeQuietly(result);
      JdbcUtil.closeQuietly(conn);
    }
  }

  @SuppressWarnings("unchecked")
  private void initXmlHelper(AdaptrisMessage msg) throws CoreException {
    NamespaceContext namespaceCtx = SimpleNamespaceContext.create(getNamespaceContext(), msg);
    DocumentBuilderFactoryBuilder builder = documentFactoryBuilder();
    if (namespaceCtx != null) {
      builder = builder.withNamespaceAware(true);
    }
    msg.getObjectHeaders().put(KEY_DOCBUILDER_FAC, builder);
    if (containsXpath(getStatementParameters())) {
      msg.getObjectHeaders().put(KEY_XML_UTILS, XmlHelper.createXmlUtils(msg, namespaceCtx, builder));
    }
    if (namespaceCtx != null) {
      msg.getObjectHeaders().put(KEY_NAMESPACE_CTX, namespaceCtx);
    }
  }

  private void destroyXmlHelper(AdaptrisMessage msg) {
    msg.getObjectHeaders().remove(KEY_XML_UTILS);
    msg.getObjectHeaders().remove(KEY_NAMESPACE_CTX);
  }

  private static boolean containsXpath(List<JdbcStatementParameter> list) {
    boolean result = false;
    for (JdbcStatementParameter sp : list) {
      if (sp instanceof StatementParameter) {
        if (StatementParameter.QueryType.xpath.equals(((StatementParameter) sp).getQueryType())) {
          result = true;
          break;
        }
      }
    }
    return result;
  }
  
  private String prepareStringStatement(String statement) {
    return this.getParameterApplicator().prepareParametersToStatement(statement);
  }

  /**
   * 
   * @return Returns the statement.
   * @deprecated since 3.4.0 use a {@link JdbcStatementCreator} instead.
   */
  @Deprecated
  public String getStatement() {
    return statement;
  }

  /**
   * Set the SQL Query statement
   * 
   * @param statement The statement to set.
   * @deprecated since 3.4.0 use a {@link JdbcStatementCreator} instead.
   */
  @Deprecated
  public void setStatement(String statement) {
    this.statement = statement;
  }


  /**
   * 
   * @return the output translator implementation.
   */
  public ResultSetTranslator getResultSetTranslator() {
    return resultSetTranslator;
  }

  /**
   * Set the implementation that will be used to parse the result set.
   * 
   * @param outputTranslator the implementation to use.
   */
  public void setResultSetTranslator(ResultSetTranslator outputTranslator) {
    resultSetTranslator = outputTranslator;
  }

  /**
   * @return the namespaceContext
   */
  public KeyValuePairSet getNamespaceContext() {
    return namespaceContext;
  }

  /**
   * Set the namespace context for resolving namespaces.
   * <ul>
   * <li>The key is the namespace prefix</li>
   * <li>The value is the namespace uri</li>
   * </ul>
   * 
   * @param kvps the namespace context
   * @see SimpleNamespaceContext#create(KeyValuePairSet)
   */
  public void setNamespaceContext(KeyValuePairSet kvps) {
    this.namespaceContext = kvps;
  }

  public DocumentBuilderFactoryBuilder getXmlDocumentFactoryConfig() {
    return xmlDocumentFactoryConfig;
  }


  public void setXmlDocumentFactoryConfig(DocumentBuilderFactoryBuilder xml) {
    this.xmlDocumentFactoryConfig = xml;
  }

  DocumentBuilderFactoryBuilder documentFactoryBuilder() {
    return getXmlDocumentFactoryConfig() != null ? getXmlDocumentFactoryConfig() : DocumentBuilderFactoryBuilder.newInstance();
  }

  public JdbcStatementCreator getStatementCreator() {
    return statementCreator;
  }

  /**
   * Set the SQL Query statement creator. If set, a StatementCreator will override the configured statement, if any.
   * 
   * @param statementCreator The statement creator to set.
   */
  public void setStatementCreator(JdbcStatementCreator statementCreator) {
    this.statementCreator = statementCreator;
  }


  /**
   * @return the ignoreExecuteQueryErrors
   */
  public Boolean getIgnoreExecuteQueryErrors() {
    return ignoreExecuteQueryErrors;
  }

  /**
   * Whether or not to ignore the SQL Exception when executing {@link PreparedStatement#executeQuery()}.
   * 
   * <p>
   * If set to true this means is that the {@link PreparedStatement#executeQuery()} is treated as successful, but with
   * an empty result set.
   * </p>
   * 
   * @param b the ignoreExecuteQueryErrors to set
   */
  public void setIgnoreExecuteQueryErrors(Boolean b) {
    this.ignoreExecuteQueryErrors = b;
  }

  boolean ignoreExecuteQueryErrors() {
    return getIgnoreExecuteQueryErrors() != null ? getIgnoreExecuteQueryErrors().booleanValue() : false;
  }


  private class DatabaseActor {
    private String queryString = "";
    private PreparedStatement queryStatement = null;
    private Connection sqlConnection;

    DatabaseActor() {

    }

    void reInitialise(Connection c) throws SQLException {
      destroy();
      sqlConnection = c;
      queryStatement = null;
    }

    void destroy() {
      JdbcUtil.closeQuietly(queryStatement);
      JdbcUtil.closeQuietly(sqlConnection);
      sqlConnection = null;
    }

    /**
     * If the statement string has changed or if we haven't prepared the statement yet, prepare the 
     * statement. Otherwise just return the prepared statement.
     * 
     * @param statement
     * @return
     * @throws SQLException
     */
    PreparedStatement getQueryStatement(String statement) throws SQLException {
      // This will prepare the statement *every time* because queryString never equals the statement.
      // Can we just rely on the fact that the JDBC driver will optimize that out of the way?
      if (queryStatement == null || !queryString.equals(statement)) {
        queryStatement = prepareStatement(sqlConnection, prepareStringStatement(statement));
      }
      return queryStatement;
    }

    Connection getSqlConnection() {
      return sqlConnection;
    }
  }


}
