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

package com.adaptris.core.services.splitter;

import static com.adaptris.core.util.XmlHelper.createDocument;
import java.io.IOException;
import javax.validation.constraints.NotBlank;
import javax.xml.namespace.NamespaceContext;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;
import com.adaptris.annotation.AdvancedConfig;
import com.adaptris.annotation.DisplayOrder;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.CoreException;
import com.adaptris.core.util.DocumentBuilderFactoryBuilder;
import com.adaptris.core.util.ExceptionHelper;
import com.adaptris.util.KeyValuePairSet;
import com.adaptris.util.text.xml.SimpleNamespaceContext;
import com.adaptris.util.text.xml.XPath;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * <p>
 * Implementation of {@link MessageSplitter} which creates multiple instances of the same XML document based on an XPath.
 * 
 * <p>
 * The split messages will always contain the entire document, the XPath evaluation simply provides information on how many
 * documents to produce and should always return an integer.
 * </p>
 * <p>
 * Given the following input document<br/>
 * 
 * <pre>
 * {@code 
 * <envelope>
 *   <document>one</document>
 *   <document>two</document>
 *   <document>three</document>
 * </envelope>
 * }
 * </pre>
 * </p>
 * You could use the xpath <code>count(/envelope/document)</code> to split into 3 documents; each of which contains the whole XML
 * document.
 * </p>
 * <p>
 * If the {@code DocumentBuilderFactoryBuilder} has been explicitly set to be not namespace aware and the document does in fact
 * contain namespaces, then Saxon can cause merry havoc in the sense that {@code //NonNamespaceXpath} doesn't work if the document
 * has namespaces in it. We have included a shim so that behaviour can be toggled based on what you have configured.
 * </p>
 * 
 * @see XPath#newXPathInstance(DocumentBuilderFactoryBuilder, NamespaceContext)
 * 
 * @config xpath-document-copier
 */
@XStreamAlias("xpath-document-copier")
@DisplayOrder(order = {"xpath", "copyMetadata", "copyObjectMetadata", "namespaceContext", "xmlDocumentFactoryConfig"})
public class XpathDocumentCopier extends MessageCopier {

  @NotBlank
  private String xpath = null;
  @AdvancedConfig
  private KeyValuePairSet namespaceContext;
  @AdvancedConfig
  private DocumentBuilderFactoryBuilder xmlDocumentFactoryConfig;

  public XpathDocumentCopier() {
  }

  public XpathDocumentCopier(String xpath) {
    setXpath(xpath);
  }

  @Override
  public com.adaptris.core.util.CloseableIterable<AdaptrisMessage> splitMessage(AdaptrisMessage msg) throws CoreException {
    try {
      String xpathResult = resolveXpath(msg);
      int size = toInteger(xpathResult);
      return new MessageCopierIterator(msg, size, (e, i) -> {
        return e;
      });
    }
    catch (Exception e) {
      throw ExceptionHelper.wrapCoreException(e);
    }
  }

  private String resolveXpath(AdaptrisMessage msg) throws ParserConfigurationException, IOException, SAXException,
      XPathExpressionException {
    NamespaceContext ctx = SimpleNamespaceContext.create(getNamespaceContext(), msg);
    DocumentBuilderFactoryBuilder builder = documentFactoryBuilder();
    if (ctx != null) {
      builder.setNamespaceAware(true);
    }
    Document d = createDocument(msg, builder);
    XPath xp = XPath.newXPathInstance(builder, ctx);
    return xp.selectSingleTextItem(d, getXpath());
  }


  /**
   * Set the XPath to use to extract the individual messages
   *
   * @param xp the XPath
   */
  public void setXpath(String xp) {
    xpath = xp;
  }

  /**
   * Get the XPath to use to extract the individual messages.
   * 
   * @return the XPath as a String
   */
  public String getXpath() {
    return xpath;
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
    return DocumentBuilderFactoryBuilder.newInstanceIfNull(getXmlDocumentFactoryConfig());
  }
}
