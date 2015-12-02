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

import static com.adaptris.core.util.XmlHelper.createDocument;

import java.sql.Connection;
import java.sql.PreparedStatement;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.xml.namespace.NamespaceContext;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.adaptris.annotation.AdvancedConfig;
import com.adaptris.annotation.AutoPopulated;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.CoreException;
import com.adaptris.core.ServiceException;
import com.adaptris.core.util.DocumentBuilderFactoryBuilder;
import com.adaptris.core.util.JdbcUtil;
import com.adaptris.util.KeyValuePairSet;
import com.adaptris.util.text.xml.SimpleNamespaceContext;
import com.adaptris.util.text.xml.XPath;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamImplicit;

/**
 * <p>
 * Capture Data from a AdaptrisMessage and store it in a JDBC-compliant database.
 * </p>
 * 
 * @config jdbc-data-capture-service
 * 
 * 
 * @author sellidge
 */
@XStreamAlias("jdbc-data-capture-service")
public class JdbcDataCaptureService extends JdbcDataCaptureServiceImpl {
  private String iterationXpath = null;
  private Boolean iterates = null;

  @NotNull
  @AutoPopulated
  @Valid
  @XStreamImplicit
  private StatementParameterList statementParameters = null;
  @AdvancedConfig
  private DocumentBuilderFactoryBuilder xmlDocumentFactoryConfig;
  @AdvancedConfig
  private KeyValuePairSet namespaceContext;

  /**
   * <p>
   * Creates a new instance.
   * </p>
   */
  public JdbcDataCaptureService() {
    super();
    setStatementParameters(new StatementParameterList());
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
   * Get the configured StatementParameter list.
   *
   * @return the list.
   */
  public StatementParameterList getStatementParameters() {
    return statementParameters;
  }

  /**
   * Set the configured StatementParameter list.
   *
   * @param l the list.
   */
  public void setStatementParameters(StatementParameterList l) {
    statementParameters = l;
  }

  /**
   * <p>
   * An Xpath that can be iterated on.
   * </p>
   * <p>
   * An Iteration Xpath defines some repeating element or value in the payload.
   * </p>
   *
   * @see #setIterates(Boolean)
   * @param xpath the xpath
   */
  public void setIterationXpath(String xpath) {
    iterationXpath = xpath;
  }

  /**
   * Get the configured iteration xpath.
   *
   * @return the xpath.
   */
  public String getIterationXpath() {
    return iterationXpath;
  }

  /**
   * <p>
   * Set the service to iterate on the given xpath.
   * </p>
   * <p>
   * If set to true, then it is expected that there is a configured iteration
   * xpath available for use. If this is not the case, then results are
   * undefined, and depends on the underlying Xpath implementation
   * </p>
   *
   * @see #setIterationXpath(String)
   * @param iterates the flag.
   */
  public void setIterates(Boolean iterates) {
    this.iterates = iterates;
  }

  /**
   * Get the configured iteration flag.
   *
   * @return the flag.
   */
  public Boolean getIterates() {
    return iterates;
  }

  private boolean iterates() {
    return getIterates() != null ? getIterates().booleanValue() : false;
  }

  protected void initJdbcService() throws CoreException {
    super.initJdbcService();
  }

  /**
   * @see com.adaptris.core.Service#doService
   *      (com.adaptris.core.AdaptrisMessage)
   */
  @Override
  public void doService(AdaptrisMessage msg) throws ServiceException {
    log.trace("Beginning doService in " + getUniqueId() != null ? getUniqueId() : this.getClass().getSimpleName());
    Connection conn = null;
    NamespaceContext namespaceCtx = SimpleNamespaceContext.create(getNamespaceContext(), msg);
    try {
      DocumentBuilderFactoryBuilder builder = documentFactoryBuilder();
      if (namespaceCtx != null) {
        builder.setNamespaceAware(true);
      }
      XPath xpath = new XPath(namespaceCtx);
      configureActor(msg);
      conn = actor.getSqlConnection();
      Document doc = builder.configure(DocumentBuilderFactory.newInstance()).newDocumentBuilder().newDocument();
      try {
        doc = createDocument(msg, builder);
      }
      catch (Exception e) {
        // do nothing - it is acceptable to get a non-xml document
        // all XPath queries will return null
        log.debug("Ignoring Exception [" + e.getMessage() + "]");
      }

      // initially set the NodeList to be the whole document
      // this ensures it will have one node only and will therefore
      // enable the remaining structure to cope with the optionally
      // iterative process structure
      NodeList nodes = xpath.selectNodeList(doc, "/");

      if (iterates()) {
        nodes = xpath.selectNodeList(doc, getIterationXpath());
      }

      // This is a kludge to ensure that we don't get a null pointer exception
      // in the event of a null NodeList and yet still manage to reach the
      // executeUpdate call.
      if (nodes == null) {
        nodes = new NodeList() {
          @Override
          public int getLength() {
            return 1;
          }

          @Override
          public Node item(int i) {
            return null;
          }
        };
      }

      log.trace("Iterating " + nodes.getLength() + " times for statement " + getStatement());
      PreparedStatement insert = actor.getInsertStatement();
      for (int i = 0; i < nodes.getLength(); i++) {
        log.trace("---Begin execution of iteration {}", i);
        insert.clearParameters();
        Node n = nodes.item(i);

        StatementParameterList cloneParameterList = new StatementParameterList();
        // set the statement arguments
        for (int args = 1; args <= statementParameters.size(); args++) {
          StatementParameter sp = statementParameters.get(args - 1);
          String queryResult = null;
          // Due to iteratesXpath, we don't use getqueryValue from
          // statementParameter.
          if (StatementParameter.QueryType.xpath.equals(sp.getQueryType())) {
            queryResult = xpath.selectSingleTextItem(n, sp.getQueryString());
            cloneParameterList.add(new StatementParameter(queryResult, sp.getQueryClass(), StatementParameter.QueryType.constant,
                sp.getConvertNull(), sp.getName()));
          }
          else
            cloneParameterList.add(new StatementParameter(sp.getQueryString(), sp.getQueryClass(), sp.getQueryType(), sp.getConvertNull(), sp.getName()));
        }
        
        this.getParameterApplicator().applyStatementParameters(msg, insert, cloneParameterList, getStatement());

        insert.executeUpdate();
        log.trace("---End execution of iteration {}", i);
      }

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
    return getXmlDocumentFactoryConfig() != null ? getXmlDocumentFactoryConfig()
        : DocumentBuilderFactoryBuilder.newInstance().withNamespaceAware(true);
  }

}
