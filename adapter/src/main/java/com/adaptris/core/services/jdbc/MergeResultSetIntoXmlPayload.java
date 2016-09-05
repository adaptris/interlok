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

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.adaptris.annotation.DisplayOrder;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.ServiceException;
import com.adaptris.core.util.DocumentBuilderFactoryBuilder;
import com.adaptris.core.util.XmlHelper;
import com.adaptris.jdbc.JdbcResult;
import com.adaptris.jdbc.JdbcResultSet;
import com.adaptris.util.text.xml.DocumentMerge;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Merge the ResultSet contents into an existing XML Payload.
 * 
 * <p>
 * If you want to see how many rows were processed you can set one/both of the following;
 * <table>
 * <tr>
 * <th>Item</th>
 * <th>Description</th>
 * <th>Value</th>
 * </tr>
 * <tr>
 * <td>result-count-metadata-item</td><td>If set to TRUE will specify the metadata item to contain the number of rows returned by your query</td><td>Boolean</td>
 * </tr>
 * <tr>
 * <td>update-count-metadata-item</td><td>If set to TRUE will specify the metadata item to contain the number of rows updated by your SQL statement</td><td>Boolean</td>
 * </tr>
 * </table>
 * <p>
 * 
 * @config jdbc-merge-into-xml-payload
 * 
 * 
 * @author lchan
 * 
 */
@XStreamAlias("jdbc-merge-into-xml-payload")
@DisplayOrder(order = {"columnNameStyle", "columnTranslators", "mergeImplementation", "outputMessageEncoding",
    "stripIllegalXmlChars", "xmlColumnPrefix",
    "xmlColumnRegexp", "cdataColumnRegexp"})
public class MergeResultSetIntoXmlPayload extends XmlPayloadTranslatorImpl {
  @NotNull
  @Valid
  private DocumentMerge mergeImplementation;

  public MergeResultSetIntoXmlPayload() {
    super();
  }

  public MergeResultSetIntoXmlPayload(DocumentMerge m) {
    this();
    setMergeImplementation(m);
  }

  @Override
  public void translateResult(JdbcResult source, AdaptrisMessage target) throws SQLException, ServiceException {
    if (mergeImplementation == null) {
      throw new ServiceException("No Document Merge implementation configured.");
    }
    try {
      DocumentBuilderFactoryBuilder builder =
          (DocumentBuilderFactoryBuilder) target.getObjectHeaders().get(JdbcDataQueryService.KEY_DOCBUILDER_FAC);
      Document resultSet = createDocument(source, builder);
      Document original = XmlHelper.createDocument(target, builder);
      Document result = mergeImplementation.merge(original, resultSet);
      writeXmlDocument(result, target);
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

  private Document createDocument(JdbcResult rs, DocumentBuilderFactoryBuilder builderFactory) throws Exception {
    DocumentBuilderFactory factory = builderFactory.configure(DocumentBuilderFactory.newInstance());
    DocumentBuilder builder = factory.newDocumentBuilder();
    Document doc = builder.newDocument();

    Element results = doc.createElement(getColumnNameStyle().format(ELEMENT_NAME_RESULTS));
    doc.appendChild(results);
    for(JdbcResultSet rSet : rs.getResultSets()) {
      List<Element> elements = createListFromResultSet(builder, doc, rSet);
      for (Element element : elements) {
        results.appendChild(element);
      }
    }
    return doc;
  }

  public DocumentMerge getMergeImplementation() {
    return mergeImplementation;
  }

  /**
   * Set the merge implementation for the resulting document.
   *
   * @param merge
   */
  public void setMergeImplementation(DocumentMerge merge) {
    mergeImplementation = merge;
  }

}
