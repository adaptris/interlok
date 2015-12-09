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
import com.adaptris.annotation.InputFieldHint;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.CoreException;
import com.adaptris.core.ServiceException;
import com.adaptris.core.jdbc.JdbcService;
import com.adaptris.core.util.DocumentBuilderFactoryBuilder;
import com.adaptris.core.util.JdbcUtil;
import com.adaptris.core.util.LifecycleHelper;
import com.adaptris.core.util.XmlHelper;
import com.adaptris.jdbc.JdbcResult;
import com.adaptris.jdbc.JdbcResultBuilder;
import com.adaptris.util.KeyValuePairSet;
import com.adaptris.util.XmlUtils;
import com.adaptris.util.text.xml.SimpleNamespaceContext;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamImplicit;

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
@ComponentProfile(summary = "Query a database and store the results in the message", tag = "service,jdbc")
public class JdbcDataQueryService extends JdbcService {

  static final String KEY_XML_UTILS = "XmlUtils_" + JdbcDataQueryService.class.getCanonicalName();
  static final String KEY_NAMESPACE_CTX = "NamespaceCtx_" + JdbcDataQueryService.class.getCanonicalName();
  static final String KEY_DOCBUILDER_FAC = "DocBuilderFactoryBuilder_" + JdbcDataQueryService.class.getCanonicalName();

  @NotNull
  @InputFieldHint(style = "SQL")
  private String statement = null;
  @NotNull
  @AutoPopulated
  @Valid
  @XStreamImplicit
  private StatementParameterList statementParameters;
  @NotNull
  @AutoPopulated
  @Valid
  private ResultSetTranslator resultSetTranslator;
  @AdvancedConfig
  private KeyValuePairSet namespaceContext;
  @AdvancedConfig
  private DocumentBuilderFactoryBuilder xmlDocumentFactoryConfig;

  @NotNull
  @AutoPopulated
  @Valid
  @AdvancedConfig
  private ParameterApplicator parameterApplicator;

  private transient DatabaseActor actor;

  public JdbcDataQueryService() {
    setStatementParameters(new StatementParameterList());
    setResultSetTranslator(new XmlPayloadTranslator());
    setParameterApplicator(new SequentialParameterApplicator());
    actor = new DatabaseActor();
  }

  @Override
  protected void initJdbcService() throws CoreException {
    LifecycleHelper.init(resultSetTranslator);
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
    ResultSet rs = null;
    Connection conn = null;
    try {
      Connection c = getConnection(msg);
      if (!c.equals(actor.getSqlConnection())) {
        actor.reInitialise(c);
      }
      conn = actor.getSqlConnection();
      initXmlHelper(msg);
      PreparedStatement preparedStatement = actor.getQueryStatement();
      preparedStatement.clearParameters();
      log.trace("Executing statement " + getStatement());
      
      this.getParameterApplicator().applyStatementParameters(msg, preparedStatement, getStatementParameters(), statement);
      rs = preparedStatement.executeQuery();

      JdbcResult result = new JdbcResultBuilder().setHasResultSet(true).setResultSet(rs).build();

      resultSetTranslator.translate(result, msg);
      destroyXmlHelper(msg);
      commit(conn, msg);
    }
    catch (Exception e) {
      rollback(conn, msg);
      rethrowServiceException(e);
    }
    finally {
      JdbcUtil.closeQuietly(rs);
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
    msg.getObjectMetadata().put(KEY_DOCBUILDER_FAC, builder);
    if (containsXpath(getStatementParameters())) {
      XmlUtils xml = XmlHelper.createXmlUtils(msg, namespaceCtx, builder);
      msg.getObjectMetadata().put(KEY_XML_UTILS, xml);
    }
    if (namespaceCtx != null) {
      msg.getObjectMetadata().put(KEY_NAMESPACE_CTX, namespaceCtx);
    }
  }

  private void destroyXmlHelper(AdaptrisMessage msg) {
    msg.getObjectMetadata().remove(KEY_XML_UTILS);
    msg.getObjectMetadata().remove(KEY_NAMESPACE_CTX);
  }

  private static boolean containsXpath(List<StatementParameter> list) {
    boolean result = false;
    for (StatementParameter sp : list) {
      if (StatementParameter.QueryType.xpath.equals(sp.getQueryType())) {
        result = true;
        break;
      }
    }
    return result;
  }
  
  private String prepareStringStatement() {
    return this.getParameterApplicator().prepareParametersToStatement(getStatement());
  }

  /**
   * 
   * @return Returns the statement.
   */
  public String getStatement() {
    return statement;
  }

  /**
   * Set the SQL Query statement
   * 
   * @param statement The statement to set.
   */
  public void setStatement(String statement) {
    this.statement = statement;
  }

  /**
   * @return Returns the statementParameters.
   */
  public StatementParameterList getStatementParameters() {
    return statementParameters;
  }

  /**
   * @param list The statementParameters to set.
   */
  public void setStatementParameters(StatementParameterList list) {
    statementParameters = list;
  }

  /**
   * Add a StatementParameter to this service.
   * 
   * @see StatementParameter
   * @param query the StatementParameter
   */
  public void addStatementParameter(StatementParameter query) {
    statementParameters.add(query);
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

  public ParameterApplicator getParameterApplicator() {
    return parameterApplicator;
  }

  public void setParameterApplicator(ParameterApplicator parameterApplicator) {
    this.parameterApplicator = parameterApplicator;
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

  private class DatabaseActor {
    private PreparedStatement queryStatement = null;
    private Connection sqlConnection;

    DatabaseActor() {

    }

    void reInitialise(Connection c) throws SQLException {
      destroy();
      sqlConnection = c;
      queryStatement = prepareStatement(sqlConnection, prepareStringStatement());
    }

    void destroy() {
      JdbcUtil.closeQuietly(queryStatement);
      JdbcUtil.closeQuietly(sqlConnection);
      sqlConnection = null;
    }

    PreparedStatement getQueryStatement() {
      return queryStatement;
    }

    Connection getSqlConnection() {
      return sqlConnection;
    }
  }

}
