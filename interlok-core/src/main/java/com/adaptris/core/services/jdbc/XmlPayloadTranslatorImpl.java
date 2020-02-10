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

import java.io.StringReader;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import javax.xml.namespace.NamespaceContext;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import org.apache.commons.lang3.BooleanUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import com.adaptris.annotation.AdvancedConfig;
import com.adaptris.annotation.InputFieldDefault;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.CoreException;
import com.adaptris.core.util.DocumentBuilderFactoryBuilder;
import com.adaptris.core.util.XmlHelper;
import com.adaptris.jdbc.JdbcResultRow;
import com.adaptris.jdbc.JdbcResultSet;
import com.adaptris.util.XmlUtils;

/**
 * Base class for translating ResultSet contents into an XML Payload.
 * 
 * @author lchan
 * 
 */
public abstract class XmlPayloadTranslatorImpl extends ResultSetTranslatorImp {

  protected static final String ELEMENT_NAME_COLUMN = "column-";
  protected static final String ELEMENT_NAME_ROW = "Row";
  protected static final String ELEMENT_NAME_RESULTS = "Results";

  @AdvancedConfig(rare = true)
  private String xmlColumnPrefix = null;
  @AdvancedConfig(rare = true)
  private String xmlColumnRegexp = null;
  @AdvancedConfig
  private String cdataColumnRegexp = null;
  @InputFieldDefault(value = "false")
  private Boolean stripIllegalXmlChars = null;
  private String outputMessageEncoding = null;
  private transient Pattern cdataColumnRegexpPattern = null;

  private transient Pattern xmlColumnRegexpPattern = null;

  public XmlPayloadTranslatorImpl() {
    super();
  }

  @Override
  public void init() throws CoreException {
    super.init();
    if (xmlColumnRegexp != null) {
      xmlColumnRegexpPattern = Pattern.compile(xmlColumnRegexp);
    }
    if (cdataColumnRegexp != null) {
      cdataColumnRegexpPattern = Pattern.compile(cdataColumnRegexp);
    }
  }

  @Override
  public void close() {
    super.close();
    xmlColumnRegexpPattern = null;
    cdataColumnRegexpPattern = null;
  }

  protected List<Element> createListFromResultSet(DocumentWrapper wrapper, JdbcResultSet rs)
      throws SQLException {
    List<Element> results = new ArrayList<Element>();

    List<String> elementNames = new ArrayList<>();
    boolean firstRecord = true;
    Document doc = wrapper.document;
    for (JdbcResultRow row : rs.getRows()) {
      Element elementRow = doc.createElement(getColumnNameStyle().format(ELEMENT_NAME_ROW));
      // let's go through and build up the element names we need once.
      if (firstRecord) {
        firstRecord = false;
        elementNames = createElementNames(row);
      }
      for (int i = 0; i < row.getFieldCount(); i++) {
        String columnName = row.getFieldName(i);
        String value = toString(row, i);

        Element node = doc.createElement(elementNames.get(i));
        if (isXmlColumn(columnName)) {
          try {
            Document xmlColumn = null;
            xmlColumn = wrapper.builder.parse(createInputSource(value));
            node.appendChild(doc.importNode(xmlColumn.getFirstChild(), true));
          }
          catch (Exception e) {
            if (isDisplayColumnErrors()) {
              log.warn("Failed to parse column {} as an XML Document, treating as text.", columnName);
              log.trace("Failed to parse column {} as an XML Document", columnName, e);
            }
            node.appendChild(createTextNode(doc, value, isCdataColumn(columnName, wrapper)));
          }
        }
        else {
          node.appendChild(createTextNode(doc, value, isCdataColumn(columnName, wrapper)));
        }
        elementRow.appendChild(node);
      }
      results.add(elementRow);
    }
    return results;
  }

  private List<String> createElementNames(JdbcResultRow row) throws SQLException {
    List<String> result = new ArrayList<>();
    for (int i = 0; i < row.getFieldCount(); i++) {
      String columnName = row.getFieldName(i);
      // Numbering starts at 0, so make it SQL-alike and start our count at 1.
      String elementName = getColumnNameStyle().format(XmlHelper.safeElementName(columnName, ELEMENT_NAME_COLUMN + (i + 1)));
      log.trace("Creating element [{}] from column [{}]", elementName, columnName);
      result.add(elementName);
    }
    return result;
  }

  protected static XmlUtils createXmlUtils(AdaptrisMessage msg) {
    XmlUtils xu = null;
    try {
      NamespaceContext ctx = (NamespaceContext) msg.getObjectHeaders().get(JdbcDataQueryService.KEY_NAMESPACE_CTX);
      DocumentBuilderFactoryBuilder builder =
          (DocumentBuilderFactoryBuilder) msg.getObjectHeaders().get(JdbcDataQueryService.KEY_DOCBUILDER_FAC);
      xu = XmlHelper.createXmlUtils(msg, ctx, builder);
    } catch (CoreException e) {
      xu = new XmlUtils();
    }
    return xu;
  }

  protected boolean isXmlColumn(String name) {
    boolean result = false;
    if (getXmlColumnPrefix() != null) {
      if (name.startsWith(xmlColumnPrefix)) {
        result = true;
      }
    }
    if (!result && xmlColumnRegexpPattern != null) {
      result = xmlColumnRegexpPattern.matcher(name).matches();
    }
    return result;
  }

  protected boolean isCdataColumn(String name, DocumentWrapper wrapper) {
    boolean result = false;
    if (cdataColumnRegexpPattern != null) {
      result = cdataColumnRegexpPattern.matcher(name).matches();

    }
    if (result) {
      wrapper.hasCDATA = true;
    }
    return result;
  }

  protected Node createTextNode(Document doc, String value, boolean isCDATA) {
    String munged = stripIllegalXmlChars() ? XmlHelper.stripIllegalXmlCharacters(value) : value;
    if (isCDATA) {
      return doc.createCDATASection(munged);
    }
    return doc.createTextNode(munged);
  }

  protected static InputSource createInputSource(String value) {
    return new InputSource(new StringReader(value));
  }

  /**
   * @return the prefix used to identify columns that should be returned as XML as opposed to text
   */
  public String getXmlColumnPrefix() {
    return xmlColumnPrefix;
  }

  /**
   * Sets the prefix that is used to identify columns that should be treated as XML rather than text.
   * 
   * @param s the prefix; note that this will be a case sensitive match, so if the behaviour of the database / JDBC driver is to
   *          make all columns / labels uppercase, then make sure you use the right case.
   */
  public void setXmlColumnPrefix(String s) {
    xmlColumnPrefix = s;
  }

  public String getXmlColumnRegexp() {
    return xmlColumnRegexp;
  }

  /**
   * Set a regular expression that will be used to check if a column should be treated as XML rather than text.
   * 
   * @param s the regular expression to match that indicates the column is XML, {@link #setXmlColumnPrefix(String)} is always
   *          checked first.
   * @see #setXmlColumnPrefix(String)
   */
  public void setXmlColumnRegexp(String s) {
    xmlColumnRegexp = s;
  }

  public String getOutputMessageEncoding() {
    return outputMessageEncoding;
  }

  /**
   * Set the encoding for the resulting XML document.
   * <p>
   * If not specified the following rules will be applied:
   * </p>
   * <ol>
   * <li>If the {@link com.adaptris.core.AdaptrisMessage#getCharEncoding()} is non-null then that will be used.</li>
   * <li>UTF-8</li>
   * </ol>
   * <p>
   * As a result; the character encoding on the message is always set using {@link com.adaptris.core.AdaptrisMessage#setCharEncoding(String)}.
   * </p>
   * 
   * @param encoding the character
   */
  public void setOutputMessageEncoding(String encoding) {
    outputMessageEncoding = encoding;
  }

  public String getCdataColumnRegexp() {
    return cdataColumnRegexp;
  }

  /**
   * Set the regular expression that will be used to check if a column should be wrapped in a CDATA element.
   * 
   * @param s the regular expression to match that indicates the column should be wrapped in a CDATA element.
   */
  public void setCdataColumnRegexp(String s) {
    this.cdataColumnRegexp = s;
  }

  public Boolean getStripIllegalXmlChars() {
    return stripIllegalXmlChars;
  }

  /**
   * Specify whether or not to strip illegal XML characters from all the data before converting to XML.
   * <p>
   * The following regular expression is used to strip out all invalid XML 1.0 characters :
   * <code>"[^\u0009\r\n\u0020-\uD7FF\uE000-\uFFFD\ud800\udc00-\udbff\udfff]"</code>. Note that not stripping Illegal XML characters
   * can still mean that you get output that looks like XML, but you will not be able to subsequently process it as a DOM object.
   * </p>
   * 
   * @param s true to enable stripping, default is null (false)
   */
  public void setStripIllegalXmlChars(Boolean s) {
    this.stripIllegalXmlChars = s;
  }

  protected boolean stripIllegalXmlChars() {
    return BooleanUtils.toBooleanDefaultIfNull(getStripIllegalXmlChars(), false);
  }

  protected static Transformer createTransformer(DocumentWrapper wrapper) throws Exception {
    if (wrapper.hasCDATA) {
      // Saxon has opinions such that CDATA elements aren't always written
      // as CDATA, they are escaped as appropriate; might be to do with OutputKeys.CDATA_SECTION_ELEMENTS
      // Life is a bit to short for "correctness"...
      // fallback transformer to force CDATA sections to have CDATA...
      return TransformerFactory.newInstance(XmlHelper.FALLBACK_TRANSFORMER_FACTORY_IMPL, Thread.currentThread().getContextClassLoader())
          .newTransformer();
    }
    return TransformerFactory.newInstance().newTransformer();
  }

  protected DocumentWrapper createWrapper(AdaptrisMessage msg) throws Exception {
    return new DocumentWrapper(DocumentBuilderFactoryBuilder
        .newInstanceIfNull((DocumentBuilderFactoryBuilder) msg.getObjectHeaders().get(JdbcDataQueryService.KEY_DOCBUILDER_FAC)), 0);
  }

  protected class DocumentWrapper {
    Document document;
    long resultSetCount;
    boolean hasCDATA = false;
    DocumentBuilder builder = null;
    DocumentBuilderFactoryBuilder builderFactoryBuilder;

    DocumentWrapper(DocumentBuilderFactoryBuilder bfb, long count) throws Exception {
      this.builderFactoryBuilder = bfb;
      builder = builderFactoryBuilder.newDocumentBuilder(DocumentBuilderFactory.newInstance());
      resultSetCount = count;
      document = builder.newDocument();
      // document.setXmlStandalone(true);
    }
  }
}
