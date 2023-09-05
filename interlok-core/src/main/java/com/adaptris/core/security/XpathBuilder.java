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

  private static final String NON_XML_EXCEPTION_MESSAGE = "Unable to create XML document";
  private static final String INVALID_XPATH_EXCEPTION_MESSAGE = "Unable to evaluate if Xpath exists, please ensure the Xpath is valid";
  private static final String XPATH_DOES_NOT_EXIST_EXCEPTION_MESSAGE = "XPath [%s] does not match any nodes";

  public XpathBuilder() {
    this.setPaths(new ArrayList<String>());
  }

  @Getter
  @Setter
  @NotNull
  @AutoPopulated
  @XStreamImplicit(itemFieldName = "xpaths")
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
    Args.notNull(getNamespaceContext(), "namespaceContext");
    }
  
  @Override
  public Map<String, String> extract(AdaptrisMessage msg) throws ServiceException {
    NamespaceContext namespaceContext = SimpleNamespaceContext.create(getNamespaceContext(), msg);
    NodeList nodeList = null;
    Document doc;
    try {
      doc = XmlHelper.createDocument(msg, documentFactoryBuilder(namespaceContext, msg));
    } catch (ParserConfigurationException | IOException | SAXException e1) {
      throw new ServiceException(NON_XML_EXCEPTION_MESSAGE);
    }
    Map<String, String> pathKeyValuePairs = new LinkedHashMap<>();
    for (String path : this.getPaths()) {
      try {
        XPath xPathHandler = XPath.newXPathInstance(documentFactoryBuilder(namespaceContext, msg), namespaceContext);
        nodeList = xPathHandler.selectNodeList(doc, path);
      } catch (XPathExpressionException e) {
        throw new ServiceException(INVALID_XPATH_EXCEPTION_MESSAGE);
      }
      if (nodeList.getLength() > 0) {
        Node node = nodeList.item(0);
        pathKeyValuePairs.put(path, node.getTextContent());
      } else {
        throw new ServiceException(String.format(XPATH_DOES_NOT_EXIST_EXCEPTION_MESSAGE, path));
      }
    }

    return pathKeyValuePairs;
  }

  @Override
  public void insert(AdaptrisMessage msg, Map<String, String> pathKeyValuePairs) throws ServiceException {
    NamespaceContext namespaceContext = SimpleNamespaceContext.create(getNamespaceContext(), msg);
    NodeList nodeList = null;
    Document doc;
    try {
      doc = XmlHelper.createDocument(msg, documentFactoryBuilder(namespaceContext, msg));
      System.out.println(doc);
    } catch (ParserConfigurationException | IOException | SAXException e1) {
      throw new ServiceException(NON_XML_EXCEPTION_MESSAGE);
    }
    for (Map.Entry<String, String> entry : pathKeyValuePairs.entrySet()) {
      String xpathKey = entry.getKey();
      String xpathValue = entry.getValue();
      try {
        XPath xPathHandler = XPath.newXPathInstance(documentFactoryBuilder(namespaceContext, msg), namespaceContext);
        nodeList = xPathHandler.selectNodeList(doc, xpathKey);
      } catch (XPathExpressionException e) {
        throw new ServiceException(INVALID_XPATH_EXCEPTION_MESSAGE);
      }
      if (nodeList.getLength() > 0) {
        Node node = nodeList.item(0);
        node.setTextContent(xpathValue);
        System.out.println(node.getTextContent());
      } else {
        throw new ServiceException(String.format(XPATH_DOES_NOT_EXIST_EXCEPTION_MESSAGE, xpathKey));
      }
    }
    
    try {
      XmlHelper.writeXmlDocument(doc, msg, msg.getContentEncoding());
    } catch (Exception e) {
      throw new ServiceException("could not write to msg");
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

  private DocumentBuilderFactoryBuilder documentFactoryBuilder(NamespaceContext namespaceCtx, AdaptrisMessage msg) {
    return DocumentBuilderFactoryBuilder.newInstanceIfNull(getXmlDocumentFactoryConfig(), namespaceCtx);
  }

}