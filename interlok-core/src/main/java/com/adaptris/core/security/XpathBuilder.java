package com.adaptris.core.security;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.xml.namespace.NamespaceContext;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.adaptris.annotation.AdapterComponent;
import com.adaptris.annotation.AdvancedConfig;
import com.adaptris.annotation.AutoPopulated;
import com.adaptris.annotation.ComponentProfile;
import com.adaptris.annotation.DisplayOrder;
import com.adaptris.annotation.InputFieldHint;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.CoreException;
import com.adaptris.core.ServiceException;
import com.adaptris.core.util.Args;
import com.adaptris.core.util.DocumentBuilderFactoryBuilder;
import com.adaptris.core.util.XmlHelper;
import com.adaptris.util.KeyValuePairSet;
import com.adaptris.util.text.xml.SimpleNamespaceContext;
import com.adaptris.util.text.xml.XPath;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamImplicit;

import lombok.Getter;
import lombok.Setter;

/**
 * 
 * @author jwickham
 * 
 *         Imp that expects an Xpath to be provided.
 *
 */

@XStreamAlias("xpath-builder")
@AdapterComponent
@ComponentProfile(summary = "xpath builder to extract and insert", tag = "service,security,path", since = "4.8.9")
@DisplayOrder(order = { "xpaths", "namespaceContext", "xmlDocumentFactoryConfig" })
public class XpathBuilder implements PathBuilder {

  private static final String ENCRYPTED_ELEMENTS_WRAPPER = "<encryptedNestedElements>%s</encryptedNestedElements>";

  private static final String NON_XML_EXCEPTION_MESSAGE = "Unable to create XML document";
  private static final String INVALID_XPATH_EXCEPTION_MESSAGE = "Unable to evaluate if Xpath [%s] exists, please ensure the Xpath is valid";
  private static final String XPATH_DOES_NOT_EXIST_EXCEPTION_MESSAGE = "XPath [%s] does not match any nodes";
  private static final String COULD_NOT_WRITE_TO_MSG_EXCEPTION_MESSAGE = "Could not write to msg";

  public XpathBuilder() {
    this.setPaths(new ArrayList<String>());
  }

  private NamespaceContext namespaceCxt;

  @Getter
  @Setter
  @NotNull
  @AutoPopulated
  @XStreamImplicit(itemFieldName = "xpaths")
  @InputFieldHint(expression = true)
  private List<String> paths;
  @AdvancedConfig(rare = true)
  @Valid
  private KeyValuePairSet namespaceContext;
  @AdvancedConfig(rare = true)
  @Valid
  private DocumentBuilderFactoryBuilder xmlDocumentFactoryConfig;

  @Override
  public void prepare() throws CoreException {
    Args.notNull(getPaths(), "xpath");
  }

  @Override
  public Map<String, String> extract(AdaptrisMessage msg) throws ServiceException {
    Node node;
    Document doc = prepareXmlDoc(msg);
    Map<String, String> pathKeyValuePairs = new LinkedHashMap<>();
    for (String path : this.getPaths()) {
      String xPathToExecute = msg == null ? path : msg.resolve(path);
      node = prepareNode(doc, xPathToExecute, msg);
      try {
        XPath xPathHandler = XPath.newXPathInstance(documentFactoryBuilder(), createNamespaceCxt(msg));
        node = xPathHandler.selectSingleNode(doc, xPathToExecute);
      } catch (XPathExpressionException e) {
        throw new ServiceException(String.format(INVALID_XPATH_EXCEPTION_MESSAGE, xPathToExecute));
      }
      if (node != null) {
        NodeList childNodeList = node.getChildNodes();
        if (childNodeList.getLength() > 1) {
          pathKeyValuePairs.put(xPathToExecute, concatAndWrapNestedNodesToString(childNodeList));
          continue;
        }
        pathKeyValuePairs.put(xPathToExecute, node.getTextContent());
      } else {
        throw new ServiceException(String.format(XPATH_DOES_NOT_EXIST_EXCEPTION_MESSAGE, xPathToExecute));
      }
    }

    return pathKeyValuePairs;
  }

  @Override
  public void insert(AdaptrisMessage msg, Map<String, String> pathKeyValuePairs) throws ServiceException {
    Node node;
    Document doc = prepareXmlDoc(msg);
    for (Map.Entry<String, String> entry : pathKeyValuePairs.entrySet()) {
      String xpathKey = entry.getKey();
      String xpathValue = entry.getValue();
      node = prepareNode(doc, xpathKey, msg);
      if (node != null) {
        if (isStringNestedNodes(xpathValue)) {
          try {
            Node nestedNodes = XmlHelper.stringToNode(xpathValue);
            Node nestedNodesToImport = doc.importNode(nestedNodes, true);
            node.replaceChild(nestedNodesToImport, node.getFirstChild());
          } catch (Exception e) {
            throw new ServiceException(NON_XML_EXCEPTION_MESSAGE);
          }
        } else {
          node.setTextContent(xpathValue);
        }
      } else {
        throw new ServiceException(String.format(XPATH_DOES_NOT_EXIST_EXCEPTION_MESSAGE, xpathKey));
      }
      doc = StripNestedNodesWrapper(doc);
    }
    try {
      XmlHelper.writeXmlDocument(doc, msg, msg.getContentEncoding());
    } catch (Exception e) {
      throw new ServiceException(COULD_NOT_WRITE_TO_MSG_EXCEPTION_MESSAGE);
    }
  }

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
   * @param namespaceContext
   */
  public void setNamespaceContext(KeyValuePairSet namespaceContext) {
    this.namespaceContext = namespaceContext;
  }

  public DocumentBuilderFactoryBuilder getXmlDocumentFactoryConfig() {
    return xmlDocumentFactoryConfig;
  }

  /**
   * Set the document factory config
   * 
   * @param xmlDocumentFactoryConfig
   */

  public void setXmlDocumentFactoryConfig(DocumentBuilderFactoryBuilder xmlDocumentFactoryConfig) {
    this.xmlDocumentFactoryConfig = xmlDocumentFactoryConfig;
  }

  private NamespaceContext createNamespaceCxt(AdaptrisMessage msg) {
    return namespaceCxt = SimpleNamespaceContext.create(getNamespaceContext(), msg);
  }

  private DocumentBuilderFactoryBuilder documentFactoryBuilder() {
    return DocumentBuilderFactoryBuilder.newInstanceIfNull(getXmlDocumentFactoryConfig());
  }

  private Document prepareXmlDoc(AdaptrisMessage msg) throws ServiceException {
    try {
      if (createNamespaceCxt(msg) != null) {
        documentFactoryBuilder().setNamespaceAware(true);
      }
      return XmlHelper.createDocument(msg, documentFactoryBuilder());
    } catch (ParserConfigurationException | IOException | SAXException e1) {
      throw new ServiceException(NON_XML_EXCEPTION_MESSAGE);
    }
  }

  private Node prepareNode(Document doc, String xPath, AdaptrisMessage msg) throws ServiceException {
    try {
      XPath xPathHandler = XPath.newXPathInstance(documentFactoryBuilder(), createNamespaceCxt(msg));
      return xPathHandler.selectSingleNode(doc, xPath);
    } catch (XPathExpressionException e) {
      throw new ServiceException(String.format(INVALID_XPATH_EXCEPTION_MESSAGE, xPath));
    }
  }

  private String concatAndWrapNestedNodesToString(NodeList nestedNodeList) {
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < nestedNodeList.getLength(); i++) {
      if (nestedNodeList.item(i).getNodeType() == Node.ELEMENT_NODE) {
        sb.append(XmlHelper.nodeToString(nestedNodeList.item(i)));
      }
    }
    return String.format(ENCRYPTED_ELEMENTS_WRAPPER, sb.toString());
  }

  private Document StripNestedNodesWrapper(Document doc) {
    NodeList wrapperNodes = doc.getElementsByTagName("encryptedNestedElements");
    for (int i = 0; i < wrapperNodes.getLength(); i++) {
      Node wrapperNode = wrapperNodes.item(i);
      Node childNodes = wrapperNode.getFirstChild();
      while (childNodes != null) {
        Node nextSibling = childNodes.getNextSibling();
        Node parentNode = wrapperNode.getParentNode();
        parentNode.appendChild(childNodes);
        childNodes = nextSibling;
      }
      wrapperNode.getParentNode().removeChild(wrapperNode);
    }
    return doc;
  }

  private boolean isStringNestedNodes(String xmlString) {
    try {
      XmlHelper.createDocument(xmlString, documentFactoryBuilder());
    } catch (ParserConfigurationException | IOException | SAXException e) {
      return false;
    }
    return true;
  }

}