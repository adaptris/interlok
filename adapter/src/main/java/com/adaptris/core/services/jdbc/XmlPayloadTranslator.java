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

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.ServiceException;
import com.adaptris.core.util.DocumentBuilderFactoryBuilder;
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
 * 
 * @config jdbc-xml-payload-translator
 * 
 * @author lchan
 * 
 */
@XStreamAlias("jdbc-xml-payload-translator")
public class XmlPayloadTranslator extends XmlPayloadTranslatorImpl {
  private static final String ORIGINAL_MESSAGE_ELEMENT = "OriginalMessage";
  private Boolean preserveOriginalMessage;

  public XmlPayloadTranslator() {
    super();
  }

  @Override
  public void translate(JdbcResult source, AdaptrisMessage target) throws SQLException, ServiceException {
    try {
      Document d = toDocument(source, target);
      writeXmlDocument(d, target);
    }
    catch (SQLException e) {
      throw e;
    }
    catch (Exception e) {
      throw new ServiceException("Failed to process message", e);
    }
    finally {
    }
  }

  private Document toDocument(JdbcResult rs, AdaptrisMessage msg) throws Exception {
    XmlUtils xu = createXmlUtils(msg);
    DocumentBuilderFactoryBuilder factoryBuilder = documentFactoryBuilder(msg);
    DocumentBuilderFactory factory = factoryBuilder.configure(DocumentBuilderFactory.newInstance());
    DocumentBuilder builder = factory.newDocumentBuilder();
    Document doc = builder.newDocument();
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
    }
    return doc;
  }

  private DocumentBuilderFactoryBuilder documentFactoryBuilder(AdaptrisMessage msg) {
    DocumentBuilderFactoryBuilder factoryBuilder =
        (DocumentBuilderFactoryBuilder) msg.getObjectHeaders().get(JdbcDataQueryService.KEY_DOCBUILDER_FAC);
    return factoryBuilder != null ? factoryBuilder : DocumentBuilderFactoryBuilder.newInstance();
  }

  private boolean isPreserveOriginalMessage() {
    return preserveOriginalMessage == null ? false : preserveOriginalMessage.booleanValue();
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
