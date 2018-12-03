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

package com.adaptris.util;

import static org.apache.commons.lang.StringUtils.defaultIfEmpty;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;

import javax.xml.namespace.NamespaceContext;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.URIResolver;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.Text;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;

import com.adaptris.util.text.xml.Resolver;
import com.adaptris.util.text.xml.Validator;
import com.adaptris.util.text.xml.XPath;

/**
 * Class which provides convenience methods for various aspects of XML usage.
 * 
 * @author Stuart Ellidge
 */
public class XmlUtils {

  private static final String DEFAULT_XML_CHARSET = "ISO-8859-1";
  private XPath xpath = null;
  private Validator validator = null;
  private NamespaceContext namespaceCtx;

  private Document currentDoc = null;

  private boolean validate = false;
  private boolean isValid = false;
  private String parseMessage = "";

  private EntityResolver entityResolver = null;
  private DocumentBuilderFactory docBuilderFactory;


  public XmlUtils() {
    this(new Resolver());
  }

  public XmlUtils(EntityResolver er) {
    this(er, (NamespaceContext) null, (DocumentBuilderFactory) null);
  }

  @Deprecated
  public XmlUtils(NamespaceContext ctx) {
    this(new Resolver(), ctx, null);
  }


  public XmlUtils(NamespaceContext ctx, DocumentBuilderFactory f) {
    this(new Resolver(), ctx, f);
  }

  /**
   * @deprecated URIResolver does nothing so use {@link #XmlUtils(EntityResolver)} instead.
   */
  @Deprecated
  public XmlUtils(EntityResolver er, URIResolver ur) {
    this(er);
  }

  /**
   * @deprecated URIResolver does nothing so use {@link #XmlUtils(EntityResolver, NamespaceContext, DocumentBuilderFactory)}
   *             instead.
   */
  @Deprecated
  public XmlUtils(EntityResolver er, URIResolver ur, NamespaceContext ctx) {
    this(er, ctx, DocumentBuilderFactory.newInstance());
  }

  /**
   * @deprecated URIResolver does nothing so use {@link #XmlUtils(EntityResolver, NamespaceContext, DocumentBuilderFactory)}
   *             instead.
   */
  @Deprecated
  public XmlUtils(EntityResolver er, URIResolver ur, NamespaceContext ctx, DocumentBuilderFactory dbf) {
    this(er, ctx, dbf);
  }

  public XmlUtils(EntityResolver er, NamespaceContext ctx, DocumentBuilderFactory dbf) {
    entityResolver = er;
    namespaceCtx = ctx;
    xpath = XPath.newXPathInstance(dbf, ctx);
    if (dbf == null) {
      docBuilderFactory = DocumentBuilderFactory.newInstance();
      if (ctx != null) {
        docBuilderFactory.setNamespaceAware(true);
      }
    }
    else {
      docBuilderFactory = dbf;
    }
  }

  /**
   * Method added to specify the Schema to Validate against. If not set,
   * document will not be validated.
   *
   * @param uri the URI to validate against.
   * @throws Exception on error.
   */
  public void setSchema(String uri) throws Exception {
    validator = new Validator(uri, entityResolver);
    validate = true;
  }

  /**
   * Method which sets the source XML document for this class. Source document
   * must be specified before any additional methods can be successfully called.
   * <p/>This method not only receives the document, but it is immediately
   * compiled into a DOM Object.
   *
   * @param xml the InputSource
   */
  public void setSource(InputSource xml) {
    isValid = false;

    try {
      if (validate) {
        try {
          parseMessage = "";
          currentDoc = validator.parse(xml);
        }
        catch (Exception e) {
          parseMessage = "Failed to validate message: " + e.getMessage();
          throw e;
        }
      }
      else {
        currentDoc = documentBuilder().parse(xml);
      }
      isValid = true;
    }
    catch (Exception e) {
      currentDoc = null;
    }
  }

  private DocumentBuilder documentBuilder() throws ParserConfigurationException {
    DocumentBuilder builder = docBuilderFactory.newDocumentBuilder();
    if (entityResolver != null) {
      builder.setEntityResolver(entityResolver);
    }
    return builder;
  }
  /**
   * Method which sets the source XML document for this class. Source document
   * must be specified before any additional methods can be successfully called.
   * <p/>This method not only receives the document, but it is immediately
   * compiled into a DOM Object.
   *
   * @param xml the Reader
   */
  public void setSource(Reader xml) {
    setSource(new InputSource(xml));
  }

  /**
   * Method which sets the source XML document for this class. Source document
   * must be specified before any additional methods can be successfully called.
   * <p/>This method not only receives the document, but it is immediately
   * compiled into a DOM Object.
   *
   * @param xml the inputstream for the source
   */
  public void setSource(InputStream xml) {
    setSource(new InputSource(xml));
  }

  /**
   * Method which sets the source XML document for this class. Source document
   * must be specified before any additional methods can be successfully called.
   * <p/>
   *
   * @param node a Node object of type Document
   */
  public void setSource(Node node) {
    isValid = false;
    currentDoc = null;

    try {
      currentDoc = (Document) node;
      parseMessage = "Using supplied Document node - not validated";
      isValid = true;
    }
    catch (ClassCastException cce) {
      parseMessage = "Supplied Node not of type org.w3c.dom.Document";
    }
  }

  /**
   * Method which returns a String representation of either a TEXT_NODE or an ATTRIBUTE_NODE, extracted from the provided Node.
   * <p/>
   * Null will be returned if the provided XPath fails to retrieve a Node of the above types.
   * <p/>
   * The provided String is compiled into an XPath and stored for future use.
   *
   * @return the text string returned by the Xpath
   * @param xp the Xpath query
   * @param documentNode the Node to operate on.
   */
  public String getSingleTextItem(String xp, Node documentNode) {
    try {
      return xpath.selectSingleTextItem(documentNode, xp);
    }
    catch (Exception e) {
      return null;
    }
  }

  /**
   * Method which returns a String representation of either a TEXT_NODE or an ATTRIBUTE_NODE, extracted from the Node created by
   * "setSource".
   * <p/>
   * Null will be returned if the provided XPath fails to retrieve a Node of the above types.
   * <p/>
   * The provided String is compiled into an XPath and stored for future use.
   *
   * @return the string representing the text item returned by the query
   * @param xp the Xpath
   */
  public String getSingleTextItem(String xp) {
    return getSingleTextItem(xp, currentDoc);
  }

  /**
   * Method which returns an array of String representations of either a TEXT_NODE or an ATTRIBUTE_NODE, extracted from the provided
   * Node.
   * <p/>
   * Null will be returned if the provided XPath fails to retrieve any Nodes of the above types.
   * <p/>
   * The provided String is compiled into an XPath and stored for future use.
   *
   * @return an array of String objects that were returned by the query.
   * @param xp the xpath
   * @param documentNode the starting node.
   */
  public String[] getMultipleTextItems(String xp, Node documentNode) {
    try {
      return xpath.selectMultipleTextItems(documentNode, xp);
    }
    catch (Exception e) {
      return null;
    }
  }

  /**
   * Method which returns an array of String representations of either a TEXT_NODE or an ATTRIBUTE_NODE, extracted from the Node
   * created by "setSource".
   * <p/>
   * Null will be returned if the provided XPath fails to retrieve any Nodes of the above types.
   * <p/>
   * The provided String is compiled into an XPath and stored for future use.
   *
   * @return an array of String objects returned by the query.
   * @param xp the Xpath query.
   */
  public String[] getMultipleTextItems(String xp) {
    return getMultipleTextItems(xp, currentDoc);
  }

  /**
   * Method which returns a NodeList extracted from the provided Node based on the provided xpath String. An example of the
   * functionality of this would be to extract multiple documents from within an envelope structure and then to use the extracted
   * Nodes to perform more granular queries on the extracted data.
   *
   * @return the NodeList returned by the query.
   * @param xp the Xpath query
   * @param documentNode the Node to search on.
   */
  public org.w3c.dom.NodeList getNodeList(String xp, Node documentNode) {
    try {
      return xpath.selectNodeList(documentNode, xp);
    }
    catch (Exception e) {
      return null;
    }
  }

  /**
   * Method which returns a NodeList extracted from the Node created by the setSource method based on the provided xpath String. An
   * example of the functionality of this would be to extract multiple documents from within an envelope structure and then to use
   * the extracted Nodes to perform more granular queries on the extracted data.
   *
   * @return the NodeList returned by the query.
   * @param xp the Xpath query
   */
  public org.w3c.dom.NodeList getNodeList(String xp) {
    return getNodeList(xp, currentDoc);
  }

  /**
   * Method which returns a Node by applying the provided, this Node can either be modified directly using the DOM api or by using
   * the convenience methods in this class.
   *
   * @param xp the XPath which will return the Node
   * @return the Node.
   */
  public Node getSingleNode(String xp) {
    return getSingleNode(xp, currentDoc);
  }

  /**
   * Method which returns a Node by applying the provided relative to the specified root Node. This Nodecan either be modified
   * directly using the DOM api or by using the convenience methods in this class.
   *
   * @param xp the XPath which will return the Node
   * @param n the root node to apply the XPath to
   * @return The Node.
   */
  public Node getSingleNode(String xp, Node n) {
    try {
      return xpath.selectSingleNode(n, xp);
    }
    catch (Exception e) {
      return null;
    }
  }

  /**
   * Re-initialises the XmlUtils object. Clears down parsed XML message and
   * compiled XPaths
   *
   * @throws Exception on any error.
   */
  public void reset() throws Exception {
    currentDoc = null;
    xpath = XPath.newXPathInstance(docBuilderFactory, namespaceCtx);
    validator = null;
    validate = false;
  }

  /**
   * Method which indicates whether the file was valid
   *
   * @return true if the document is valid
   */
  public boolean isDocumentValid() {
    return isValid;
  }

  /**
   * Method which returns the success / fail message generated by the document
   * parser.
   *
   * @return the error message.
   */
  public String getParseMessage() {
    return parseMessage;
  }

  /**
   * Method which will create a Node path based on the supplied XPath
   *
   * @param xp the xpath.
   * @throws Exception on error.
   * @return the Node that was constructed from the xpath.
   */
  public Node createNode(String xp) throws Exception {
    int i = xp.lastIndexOf('/');
    String nodeName = xp.substring(i + 1);
    String subPath = xp.substring(0, i);

    Node parent = xpath.selectSingleNode(currentDoc, subPath);
    if (parent == null) {
      parent = createNode(subPath);
    }
    Element newNode = currentDoc.createElement(nodeName);
    appendNode(newNode, parent);
    return newNode;
  }

  /**
   * Method which modifies the value of the Node returned by the XPath query
   * specified.
   *
   * @param xp the XPath which will return the Node to be updated
   * @param value the new value to set the node to
   * @throws Exception on error.
   */
  public void setNodeValue(String xp, String value) throws Exception {
    setNodeValue(xp, value, currentDoc);
  }

  /**
   * Method which modifies the value of the Node returned by the XPath query
   * specified, relative to the provided parent node.
   *
   * @param xp the XPath which will return the Node to be updated
   * @param v the new value to set the node to
   * @param root the root node to apply the XPath to
   * @throws Exception on error.
   */
  public void setNodeValue(String xp, String v, Node root) throws Exception {
    Node n = xpath.selectSingleNode(root, xp);

    if (n == null) {
      n = createNode(xp);
    }
    try {
      setNodeValue(v, n);
    }
    catch (NullPointerException ne) {
      // Node has no children!
      Document d = n.getOwnerDocument();
      Text t = d.createTextNode(v);
      n.appendChild(t);
    }

  }

  /**
   * Method which updates the Text value of a specified Node
   *
   * @param value the new Text value
   * @param n the node to be modified
   * @throws Exception on error.
   */
  public void setNodeValue(String value, Node n) throws Exception {
    n.normalize();
    n.getFirstChild().setNodeValue(value);
  }

  /**
   * Method which sets an attribute on the specified Node, which must be of type
   * org.w3c.dom.Element. If the attribute already exists, it will be updated
   * with the new value.
   *
   * @param xp to retrieve the node to be modified
   * @param name the attribute name
   * @param value the new value
   * @throws Exception on error.
   */
  public void setAttribute(String xp, String name, String value)
      throws Exception {
    setAttribute(name, value, getSingleNode(xp));
  }

  /**
   * Method which sets an attribute on the specified Node, which must be of type
   * org.w3c.dom.Element. If the attribute already exists, it will be updated
   * with the new value.
   *
   * @param name the attribute name
   * @param value the new value
   * @param n the node to be modified
   * @throws Exception on error.
   */
  public void setAttribute(String name, String value, Node n) throws Exception {
    Element e = castOrFail(n, Element.class, "Only Element Nodes can have attributes added");
    e.setAttribute(name, value);
  }

  /**
   * Method which deletes an attribute from the specified Node, which must be of
   * type org.w3c.dom.Element.
   *
   * @param name the attribute name
   * @param n the node to be modified
   * @throws Exception on error.
   */
  public void deleteAttribute(String name, Node n) throws Exception {
    Element e = castOrFail(n, Element.class, "Only Element Nodes have attributes");
    e.removeAttribute(name);
  }

  /**
   * Convenience method which enables a new Node to be added to a parent at a specified position, by specifying the Node to insert
   * before. Here is a sample of how to use:
   * 
   * <pre>
  *  {@code 
   *
   *   // Example of how to insert a Node as the 3rd child of a parent
   *   Node p  = xmlUtils.getSingleNode("/mydoc/parent");
   *   Node c   = xmlUtils.getSingleNode("/mydoc/parent/child[4]");
   *   Node n = // Node creation code here;
   *   xmlUtils.insertNodeBefore(newNode, child, parent);
   *
   * }
   * </pre>
   * 
   * @param newNode the Node to be added
   * @param existingNode the Node to insert before
   * @param parent the parent Node
   * @throws Exception on error.
   */
  public void insertNodeBefore(Node newNode, Node existingNode, Node parent)
      throws Exception {
    Document parentDoc = parent.getOwnerDocument();
    Document newDoc = newNode.getOwnerDocument();
    Node nodeToAdd = newNode;
    if (!parentDoc.equals(newDoc)) {
      nodeToAdd = parentDoc.importNode(newNode, true);
    }

    parent.insertBefore(nodeToAdd, existingNode);
  }

  /**
   * Convenience method which appends a new Node to the children of a parent
   *
   * @param newNode the Node to be added
   * @param parent the parent Node
   * @throws Exception on error.
   */
  public void appendNode(Node newNode, Node parent) throws Exception {
    Document parentDoc = parent.getOwnerDocument();
    Document newDoc = newNode.getOwnerDocument();

    if (!parentDoc.equals(newDoc)) {
      newNode = parentDoc.importNode(newNode, true);
    }

    parent.appendChild(newNode);
  }

  /**
   * Convenience method which removes a Node from the children of a parent
   *
   * @param toBeRemoved the Node to be Removed
   * @param parent the parent Node
   * @throws Exception on error.
   */
  public void removeNode(Node toBeRemoved, Node parent) throws Exception {
    parent.removeChild(toBeRemoved);
  }

  /**
   * Method which writes the document set by setSource to the specified
   * OutputStream using ISO-8859-1 encoding
   *
   * @param output the Outputstream
   * @throws Exception on error.
   */
  public void writeDocument(OutputStream output) throws Exception {
    writeDocument(output, DEFAULT_XML_CHARSET);
  }

  /**
   * Method which writes the document set by setSource to the specified Writer
   * using ISO-8859-1 encoding
   *
   * @param output the Outputstream
   * @throws Exception on error.
   */
  public void writeDocument(Writer output) throws Exception {
    writeDocument(output, DEFAULT_XML_CHARSET);
  }

  /**
   * Method which writes a Document Node to the specified OutputStream using the
   * specified encoding
   *
   * @param encoding the encoding type.
   * @param output the Outputstream
   * @throws Exception on error.
   */
  public void writeDocument(OutputStream output, String encoding)
      throws Exception {
    writeDocument(currentDoc, output, encoding);
  }

  /**
   * Method which writes a Document Node to the specified Writer using the
   * specified encoding
   *
   * @param encoding the encoding type.
   * @param output the Outputstream
   * @throws Exception on error.
   */
  public void writeDocument(Writer output, String encoding) throws Exception {
    writeDocument(currentDoc, output, encoding);
  }

  /**
   * Method which writes a Document Node to the specified Writer using
   * ISO-8859-1 encoding
   *
   * @param node the Node to write.
   * @param output the Outputstream to use.
   * @throws Exception on error.
   */
  public void writeDocument(Node node, OutputStream output) throws Exception {
    writeDocument(node, output, DEFAULT_XML_CHARSET);
  }

  /**
   * Method which writes a Document Node to the specified OutputStream using the
   * specified encoding
   *
   * @param node the node to write.
   * @param writer the Writer
   * @throws Exception on error.
   */
  public void writeDocument(Node node, Writer writer) throws Exception {
    writeDocument(node, writer, DEFAULT_XML_CHARSET);
  }

  /**
   * Method which writes a Document Node to the specified Writer using the
   * specified encoding
   *
   * @param node the Node to write.
   * @param output the Outputstream to use.
   * @param encoding the Encoding.
   * @throws Exception on error.
   */
  public void writeDocument(Node node, OutputStream output, String encoding) throws Exception {
    Document docToWrite = castOrFail(node, Document.class, "writeDocument method can only work with Document Nodes");
    serialize(new DOMSource(docToWrite), new StreamResult(output), encoding);
  }

  /**
   * Write the node to the specified writer.
   *
   * @param node a Document object
   * @param writer the writer to use.
   * @param encoding the encoding format.
   * @throws Exception on error.
   */
  public void writeDocument(Node node, Writer writer, String encoding)
      throws Exception {
    Document docToWrite = castOrFail(node, Document.class, "writeDocument method can only work with Document Nodes");
    serialize(new DOMSource(docToWrite), new StreamResult(writer), encoding);
  }

  private void serialize(DOMSource doc, StreamResult result, String encoding)
      throws TransformerFactoryConfigurationError, TransformerException {
    String enc = defaultIfEmpty(encoding, DEFAULT_XML_CHARSET);
    Transformer serializer = TransformerFactory.newInstance().newTransformer();
    serializer.setOutputProperty(OutputKeys.ENCODING, enc);
    serializer.setOutputProperty(OutputKeys.INDENT, "yes");
    serializer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
    serializer.transform(doc, result);
  }

  public Document getCurrentDoc() {
    return currentDoc;
  }

  public <T> T castOrFail(Object o, Class<T> type, String exceptionMsg) throws Exception {
    if (type.isAssignableFrom(o.getClass())) {
      return (T) o;
    }
    throw new Exception(exceptionMsg);
  }

}
