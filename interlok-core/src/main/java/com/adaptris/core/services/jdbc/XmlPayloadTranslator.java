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

import java.sql.SQLException;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.lang3.BooleanUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.adaptris.annotation.DisplayOrder;
import com.adaptris.annotation.InputFieldDefault;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.ServiceException;
import com.adaptris.core.util.DocumentBuilderFactoryBuilder;
import com.adaptris.core.util.ExceptionHelper;
import com.adaptris.core.util.XmlHelper;
import com.adaptris.jdbc.JdbcResult;
import com.adaptris.jdbc.JdbcResultSet;
import com.adaptris.util.XmlUtils;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Translate the ResultSet contents into an XML Payload.
 * <p>
 * The original message can be included in the output message in order to allow consolidation of XML data and SQL data in a
 * subsequent XSLT step.
 *
 * The format of the returned message is as follows:
 *
 * <pre>
 * {@code
 * <Results>
 *   <OriginalMessage>
 *   ...
 *   </OriginalMessage>
 *   <Row>
 *     <column1>...</column1>
 *     <column2>...</column2>
 *     ...
 *   </Row>
 *   <Row> ... </Row>
 * </Results>
 * }
 * </pre>
 * </p>
 * <p>
 * Note that column1, column2, etc. is replaced by the actual column name as returned in the query. As such, the column name must be
 * a valid XML element name. If the actual name (in the database table definition) is not valid, the query should specify an alias
 * name in the query. E.g: <code>SELECT "col 1" AS "col1" FROM mytable;</code>
 * </p>
 * <p>
 * If you want to see how many rows were processed you can set one/both of the following;
 * <table>
 * <tr>
 * <th>Item</th>
 * <th>Description</th>
 * <th>Value</th>
 * </tr>
 * <tr>
 * <td>result-count-metadata-item</td><td>If set to a String metadata item name will specify the metadata item to contain the number of rows returned by your query</td><td>Metadata item name</td>
 * </tr>
 * <tr>
 * <td>update-count-metadata-item</td><td>If set to a String metadata item name will specify the metadata item to contain the number of rows updated by your SQL statement</td><td>Metadata item name</td>
 * </tr>
 * </table>
 * <p>
 *
 * @config jdbc-xml-payload-translator
 *
 * @author lchan
 *
 */
@XStreamAlias("jdbc-xml-payload-translator")
@DisplayOrder(order = {"preserveOriginalMessage", "columnNameStyle", "columnTranslators", "mergeImplementation",
    "outputMessageEncoding", "stripIllegalXmlChars", "xmlColumnPrefix", "xmlColumnRegexp", "cdataColumnRegexp"})
public class XmlPayloadTranslator extends XmlPayloadTranslatorImpl {
  private static final String ORIGINAL_MESSAGE_ELEMENT = "OriginalMessage";
  @InputFieldDefault(value = "false")
  private Boolean preserveOriginalMessage;

  public XmlPayloadTranslator() {
    super();
  }

  @Override
  public long translateResult(JdbcResult source, AdaptrisMessage target) throws SQLException, ServiceException {
    long resultSetCount = 0;
    try {
      DocumentWrapper d = toDocument(source, target);
      XmlHelper.writeXmlDocument(d.document, target, getOutputMessageEncoding());
      resultSetCount = d.resultSetCount;
    }
    catch (SQLException e) {
      throw e;
    }
    catch (Exception e) {
      throw ExceptionHelper.wrapServiceException(e);
    }
    return resultSetCount;
  }

  private DocumentWrapper toDocument(JdbcResult rs, AdaptrisMessage msg)
      throws ParserConfigurationException, SQLException {
    XmlUtils xu = createXmlUtils(msg);
    DocumentBuilderFactoryBuilder factoryBuilder = documentFactoryBuilder(msg);
    DocumentBuilderFactory factory = factoryBuilder.configure(DocumentBuilderFactory.newInstance());
    DocumentBuilder builder = factoryBuilder.configure(factory.newDocumentBuilder());
    Document doc = builder.newDocument();
    DocumentWrapper result = new DocumentWrapper(doc, 0);
    ColumnStyle elementNameStyle = getColumnNameStyle();

    Element results = doc.createElement(elementNameStyle.format(ELEMENT_NAME_RESULTS));
    doc.appendChild(results);

    if (isPreserveOriginalMessage()) {
      Element originalMessage = doc.createElement(elementNameStyle.format(ORIGINAL_MESSAGE_ELEMENT));

      if (xu.isDocumentValid()) {
        Node n = xu.getSingleNode("/").getFirstChild();
        originalMessage.appendChild(doc.importNode(n, true));
      }
      else {
        // Not XML, so let's add it in as a CDATA node.
        originalMessage.appendChild(createTextNode(doc, msg.getContent(), true));
      }
      results.appendChild(originalMessage);
    }
    for(JdbcResultSet rSet : rs.getResultSets()) {
      List<Element> elements = createListFromResultSet(builder, doc, rSet);
      for (Element element : elements) {
        results.appendChild(element);
      }
      result.resultSetCount += elements.size();
    }
    return result;
  }

  private DocumentBuilderFactoryBuilder documentFactoryBuilder(AdaptrisMessage msg) {
    DocumentBuilderFactoryBuilder factoryBuilder =
        (DocumentBuilderFactoryBuilder) msg.getObjectHeaders().get(JdbcDataQueryService.KEY_DOCBUILDER_FAC);
    return DocumentBuilderFactoryBuilder.newInstance(factoryBuilder);
  }

  private boolean isPreserveOriginalMessage() {
    return BooleanUtils.toBooleanDefaultIfNull(getPreserveOriginalMessage(), false);
  }

  /**
   * @return whether to incorporate the original message in the output message
   *         body
   */
  public Boolean getPreserveOriginalMessage() {
    return preserveOriginalMessage;
  }

  /**
   * Sets whether to incorporate the original message in the output message body
   *
   * @param b true or false, default is false.
   */
  public void setPreserveOriginalMessage(Boolean b) {
    preserveOriginalMessage = b;
  }

}
