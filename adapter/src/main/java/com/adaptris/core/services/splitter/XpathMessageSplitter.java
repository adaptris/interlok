package com.adaptris.core.services.splitter;

import static com.adaptris.core.util.XmlHelper.createDocument;
import static org.apache.commons.lang.StringUtils.isEmpty;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

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

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.CoreException;
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
public class XpathMessageSplitter extends MessageSplitterImp {

  @NotNull
  @NotBlank
  private String xpath = null;
  private String encoding = null;
  private KeyValuePairSet namespaceContext;

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
  public List<AdaptrisMessage> splitMessage(AdaptrisMessage msg) throws CoreException {
    List<AdaptrisMessage> result = new ArrayList<AdaptrisMessage>();
    try {
      NamespaceContext namespaceCtx = SimpleNamespaceContext.create(getNamespaceContext(), msg);
      DocumentBuilder builder = createDocumentBuilder(namespaceCtx);
      XmlUtils xml = new XmlUtils(namespaceCtx);
      NodeList list = resolveXpath(msg, namespaceCtx);
      String encodingToUse = evaluateEncoding(msg);
      for (int i = 0; i < list.getLength(); i++) {
        Document splitXmlDoc = builder.newDocument();
        Node e = list.item(i);
        Node dup = splitXmlDoc.importNode(e, true);
        splitXmlDoc.appendChild(dup);
        AdaptrisMessage splitMsg = selectFactory(msg).newMessage("", encodingToUse);
        try (Writer writer = splitMsg.getWriter()) {
          xml.writeDocument(splitXmlDoc, writer, encodingToUse);
          copyMetadata(msg, splitMsg);
          result.add(splitMsg);
        }
      }
    }
    catch (Exception e) {
      throw new CoreException(e);
    }
    finally {

    }
    return result;
  }

  // Consider making this namespace aware; we could follow what XpathMetadataQuery does.
  private NodeList resolveXpath(AdaptrisMessage msg, NamespaceContext namespaceCtx) throws ParserConfigurationException,
      IOException, SAXException,
      XPathExpressionException {
    Document d = createDocument(msg, namespaceCtx);
    XPath xp = new XPath(namespaceCtx);
    return xp.selectNodeList(d, getXpath());
  }

  private DocumentBuilder createDocumentBuilder(NamespaceContext namespaceCtx) throws ParserConfigurationException {
    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    factory.setValidating(namespaceCtx != null ? true : false);
    return factory.newDocumentBuilder();
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
}
