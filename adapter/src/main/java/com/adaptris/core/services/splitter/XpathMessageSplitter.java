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
import static org.apache.commons.lang.StringUtils.isEmpty;

import java.io.IOException;
import java.io.Writer;
import java.util.Iterator;

import javax.validation.constraints.NotNull;
import javax.xml.namespace.NamespaceContext;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.hibernate.validator.constraints.NotBlank;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.adaptris.annotation.AdvancedConfig;
import com.adaptris.annotation.DisplayOrder;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.CoreException;
import com.adaptris.core.util.DocumentBuilderFactoryBuilder;
import com.adaptris.util.KeyValuePairSet;
import com.adaptris.util.XmlUtils;
import com.adaptris.util.text.xml.SimpleNamespaceContext;
import com.adaptris.util.text.xml.XPath;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * <p>
 * Implementation of {@link MessageSplitter} which splits an XML document based on an XPath.
 * <p>
 * TheMessage must be an XML document and split is specified by an XPath which returns a repeating subset of the document.
 * </p>
 * <p>
 * Given the following input document:
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
 * then the following XPath: <code>/envelope/document</code> will create 3 documents each of which will only contain the
 * <code><document></code> element.
 * </p>
 * 
 * @config xpath-message-splitter
 * 
 * @author sellidge
 */
@XStreamAlias("xpath-message-splitter")
@DisplayOrder(order = {"xpath", "encoding", "copyMetadata", "copyObjectMetadata", "namespaceContext", "xmlDocumentFactoryConfig"})
public class XpathMessageSplitter extends MessageSplitterImp {

  @NotNull
  @NotBlank
  private String xpath = null;
  @AdvancedConfig
  private String encoding = null;
  @AdvancedConfig
  private KeyValuePairSet namespaceContext;
  @AdvancedConfig
  private DocumentBuilderFactoryBuilder xmlDocumentFactoryConfig;

  public XpathMessageSplitter() {
    this(null, null);
  }

  public XpathMessageSplitter(String xpath) {
    this(xpath, null);
  }

  public XpathMessageSplitter(String xpath, String encoding) {
    setXpath(xpath);
    setEncoding(encoding);
  }

  @Override
  public CloseableIterable<AdaptrisMessage> splitMessage(AdaptrisMessage msg) throws CoreException {
    try {
      return new XmlSplitGenerator(msg);
    }
    catch (Exception e) {
      throw new CoreException(e);
    }
    finally {

    }
  }

  // Consider making this namespace aware; we could follow what XpathMetadataQuery does.
  private NodeList resolveXpath(AdaptrisMessage msg, NamespaceContext namespaceCtx, DocumentBuilderFactoryBuilder builder)
      throws ParserConfigurationException,
      IOException, SAXException,
      XPathExpressionException {
    Document d = createDocument(msg, builder);
    XPath xp = new XPath(namespaceCtx);
    return xp.selectNodeList(d, getXpath());
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
   * Sets the encoding to use on the output XML docs.
   *
   * @param charSet the encoding, defaults to ISO-8859-1
   */
  public void setEncoding(String charSet) {
    encoding = charSet;
  }

  /**
   * Gets the encoding used by this splitter
   *
   * @return the encoding.
   */
  public String getEncoding() {
    return encoding;
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

  private String evaluateEncoding(AdaptrisMessage msg) {
    String encoding = "UTF-8";
    if (!isEmpty(getEncoding())) {
      encoding = getEncoding();
    }
    else if (!isEmpty(msg.getContentEncoding())) {
      encoding = msg.getContentEncoding();
    }
    return encoding;
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

  private class XmlSplitGenerator implements CloseableIterable<AdaptrisMessage>, Iterator<AdaptrisMessage> {
    private final AdaptrisMessage originalMessage;
    private DocumentBuilder docBuilder;
    private NodeList nodeList;
    private String encoding;
    private int nodeListIndex;
    private XmlUtils xml;

    private AdaptrisMessage nextMessage;
    private int numberOfMessages;

    public XmlSplitGenerator(AdaptrisMessage msg) throws Exception {
      this.originalMessage = msg;
      initialiseSplitGenerator();
    }

    private void initialiseSplitGenerator() throws Exception {
      NamespaceContext namespaceCtx = SimpleNamespaceContext.create(getNamespaceContext(), originalMessage);
      DocumentBuilderFactoryBuilder factoryBuilder = documentFactoryBuilder();
      if (namespaceCtx != null) {
        factoryBuilder = documentFactoryBuilder().withNamespaceAware(true);
      }
      DocumentBuilderFactory docBuilderFactory = factoryBuilder.configure(DocumentBuilderFactory.newInstance());
      xml = new XmlUtils(namespaceCtx, docBuilderFactory);
      docBuilder = factoryBuilder.configure(docBuilderFactory.newDocumentBuilder());
      nodeList = resolveXpath(originalMessage, namespaceCtx, factoryBuilder);
      encoding = evaluateEncoding(originalMessage);
      nodeListIndex = 0;
    }

    @Override
    public Iterator<AdaptrisMessage> iterator() {
      return this;
    }

    @Override
    public boolean hasNext() {
      if (nextMessage == null) {
        try {
          nextMessage = constructAdaptrisMessage();
        }
        catch (Exception e) {
          logR.warn("Could not construct next AdaptrisMessage", e);
          throw new RuntimeException("Could not construct next AdaptrisMessage", e);
        }
      }

      return nextMessage != null;
    }

    @Override
    public AdaptrisMessage next() {
      AdaptrisMessage ret = nextMessage;
      nextMessage = null;
      return ret;
    }

    private AdaptrisMessage constructAdaptrisMessage() throws Exception {
      if (nodeListIndex < nodeList.getLength()) {
        Node e = nodeList.item(nodeListIndex);
        Document splitXmlDoc = docBuilder.newDocument();
        Node dup = splitXmlDoc.importNode(e, true);
        splitXmlDoc.appendChild(dup);
        
        AdaptrisMessage splitMsg = selectFactory(originalMessage).newMessage("", encoding);
        try (Writer writer = splitMsg.getWriter()) {
          xml.writeDocument(splitXmlDoc, writer, encoding);
        }
        copyMetadata(originalMessage, splitMsg);
        numberOfMessages++;
        nodeListIndex++;
        return splitMsg;
      }
      return null;
    }

    @Override
    public void close() throws IOException {
      logR.trace("Split gave {} messages", numberOfMessages);
    }

    @Override
    public void remove() {
      throw new UnsupportedOperationException();
    }

  };

}
