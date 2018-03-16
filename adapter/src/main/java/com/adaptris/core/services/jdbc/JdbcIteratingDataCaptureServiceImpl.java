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
import java.sql.SQLException;

import javax.validation.Valid;
import javax.xml.namespace.NamespaceContext;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.adaptris.annotation.AdvancedConfig;
import com.adaptris.annotation.InputFieldDefault;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.CoreException;
import com.adaptris.core.ServiceException;
import com.adaptris.core.services.jdbc.StatementParameterImpl.QueryType;
import com.adaptris.core.util.DocumentBuilderFactoryBuilder;
import com.adaptris.core.util.ExceptionHelper;
import com.adaptris.core.util.JdbcUtil;
import com.adaptris.core.util.LoggingHelper;
import com.adaptris.core.util.XmlHelper;
import com.adaptris.util.KeyValuePairSet;
import com.adaptris.util.text.xml.SimpleNamespaceContext;
import com.adaptris.util.text.xml.XPath;

/**
 * @author sellidge
 */
public abstract class JdbcIteratingDataCaptureServiceImpl extends JdbcDataCaptureServiceImpl {
  @AdvancedConfig
  private String iterationXpath = null;
  @AdvancedConfig
  @InputFieldDefault(value = "false")
  private Boolean iterates = null;
  @AdvancedConfig
  @Valid
  private DocumentBuilderFactoryBuilder xmlDocumentFactoryConfig;
  @AdvancedConfig
  @Valid
  private KeyValuePairSet namespaceContext;

  public JdbcIteratingDataCaptureServiceImpl() {
    super();
  }

  protected void initJdbcService() throws CoreException {
    super.initJdbcService();
  }


  @Override
  public void doService(AdaptrisMessage msg) throws ServiceException {
    log.trace("Beginning doService in {}", LoggingHelper.friendlyName(this));
    Connection conn = null;
    NamespaceContext namespaceCtx = SimpleNamespaceContext.create(getNamespaceContext(), msg);
    try {
      XPath xpath = new XPath(namespaceCtx);
      conn = configureActor(msg).getSqlConnection();
      Document doc = createDocument(namespaceCtx, msg);
      NodeList nodes = nodesToProcess(doc, xpath);
      log.debug("Iterating {} times for statement [{}]", nodes.getLength(), getStatement());
      PreparedStatement insert = actor.getInsertStatement(msg);
      insert.clearParameters();
      for (int i = 0; i < nodes.getLength(); i++) {
        log.trace("---Start Iteration {}", i);
        Node n = nodes.item(i);
        this.getParameterApplicator().applyStatementParameters(msg, insert, createActualParams(xpath, n), getStatement());
        executeUpdate(insert);
        log.trace("---End Iteration {}", i);

      }
      finishUpdate(insert);
      // Will only store the generated keys from the last query
      saveKeys(msg, insert);
      commit(conn, msg);
    }
    catch (Exception e) {
      rollback(conn, msg);
      throw ExceptionHelper.wrapServiceException(e);
    }
    finally {
      JdbcUtil.closeQuietly(conn);
    }
    return;
  }

  protected abstract void executeUpdate(PreparedStatement insert) throws SQLException;

  protected abstract void finishUpdate(PreparedStatement insert) throws SQLException;

  private NodeList nodesToProcess(Document doc, XPath xpath) throws XPathExpressionException {
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
    return nodes;
  }

  private Document createDocument(NamespaceContext ctx, AdaptrisMessage msg) throws ParserConfigurationException {
    DocumentBuilderFactoryBuilder builder = documentFactoryBuilder().withNamespaceAware(ctx);
    Document doc = builder.newDocumentBuilder(DocumentBuilderFactory.newInstance()).newDocument();
    try {
      doc = XmlHelper.createDocument(msg, builder);
    }
    catch (Exception e) {
      // do nothing - it is acceptable to get a non-xml document
      // all XPath queries will return null
      log.trace("Treating as NON-XML Document, Ignoring Exception [" + e.getMessage() + "]");
    }
    return doc;
  }

  private StatementParameterList createActualParams(XPath xpath, Node n) throws XPathExpressionException {
    StatementParameterList result = new StatementParameterList();
    StatementParameterList original = getStatementParameters();
    for (int args = 1; args <= original.size(); args++) {
      JdbcStatementParameter param = original.get(args - 1);
      // Due to iteratesXpath, we don't use getqueryValue from
      // statementParameter.
      if (isXpathParam(param)) {
        StatementParameterImpl actualParam = (StatementParameterImpl) param.makeCopy();
        Node xpathNode = xpath.selectSingleNode(n, actualParam.getQueryString());
        // queryResult = xpath.selectSingleTextItem(n, actualParam.getQueryString());
        actualParam.setQueryString(xpathNode != null ? xpathNode.getTextContent() : null);
        actualParam.setQueryType(QueryType.constant);
        result.add(actualParam);
      }
      else {
        result.add(param.makeCopy());
      }
    }
    return result;
  }

  private boolean isXpathParam(JdbcStatementParameter param) {
    if (param instanceof StatementParameterImpl) {
      if (StatementParameterImpl.QueryType.xpath.equals(((StatementParameterImpl) param).getQueryType())) {
        return true;
      }
    }
    return false;
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

  private DocumentBuilderFactoryBuilder documentFactoryBuilder() {
    return DocumentBuilderFactoryBuilder.newInstance(getXmlDocumentFactoryConfig());
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
   * If set to true, then it is expected that there is a configured iteration xpath available for use. If this is not the case, then
   * results are undefined, and depends on the underlying Xpath implementation
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
}
