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
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import com.adaptris.annotation.DisplayOrder;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.ServiceException;
import com.adaptris.core.util.Args;
import com.adaptris.core.util.ExceptionHelper;
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
 * <td>result-count-metadata-item</td><td>If set to a String metadata item name will specify the metadata item to contain the number of rows returned by your query</td><td>Metadata item name</td>
 * </tr>
 * <tr>
 * <td>update-count-metadata-item</td><td>If set to a String metadata item name will specify the metadata item to contain the number of rows updated by your SQL statement</td><td>Metadata item name</td>
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
  public long translateResult(JdbcResult source, AdaptrisMessage target) throws SQLException, ServiceException {
    long resultSetCount = 0;
    try {
      Args.notNull(getMergeImplementation(), "mergeImplementation");
      DocumentWrapper wrapper = createDocument(source, target);
      Document original = XmlHelper.createDocument(target, wrapper.builderFactoryBuilder);
      Document result = mergeImplementation.merge(original, wrapper.document);
      XmlHelper.writeXmlDocument(result, target, getOutputMessageEncoding(), createTransformer(wrapper));
      resultSetCount = wrapper.resultSetCount;
    }
    catch (SQLException e) {
      throw e;
    }
    catch (Exception e) {
      throw ExceptionHelper.wrapServiceException(e);
    }
    finally {
    }
    return resultSetCount;
  }

  private DocumentWrapper createDocument(JdbcResult rs, AdaptrisMessage msg) throws Exception {
    DocumentWrapper wrapper = createWrapper(msg);
    Document doc = wrapper.document;
    Element results = doc.createElement(getColumnNameStyle().format(ELEMENT_NAME_RESULTS));
    doc.appendChild(results);
    for(JdbcResultSet rSet : rs.getResultSets()) {
      List<Element> elements = createListFromResultSet(wrapper, rSet);
      for (Element element : elements) {
        results.appendChild(element);
      }
      wrapper.resultSetCount += elements.size();
    }
    return wrapper;
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
