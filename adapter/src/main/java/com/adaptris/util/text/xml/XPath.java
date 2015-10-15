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

package com.adaptris.util.text.xml;

import javax.xml.namespace.NamespaceContext;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * @author Stuart Ellidge
 * 
 */
// * Class which provides convenience methods for executing XPath queries.
// * <b>!!!IMPORTANT!!! The underlying Xalan apis create a new entry in the DTM
// * (Document Table Model) for each document processed. Therefore you MUST use a
// * new instance of this class for each document processed otherwise you will
// * create a memory leak </b>
public class XPath {

  private NamespaceContext context = null;

  public XPath() {

  }

  public XPath(NamespaceContext ctx) {
    context = ctx;
  }

  private javax.xml.xpath.XPath createXpath() {
    javax.xml.xpath.XPath result = XPathFactory.newInstance().newXPath();
    if (context != null) {
      result.setNamespaceContext(context);
    }
    return result;
  }

  /**
   * returns the string value contained in an element returned by an XPath
   *
   * @param context the node to apply the XPath to
   * @param xpath the xpath to apply
   * @return the string extracted
   * @throws XPathExpressionException on error
   */
  public String selectSingleTextItem(Node context, String xpath)
      throws XPathExpressionException {
    return (String) createXpath().evaluate(xpath, context, XPathConstants.STRING);
  }

  /**
   * returns an array of string values taken from a list of elements returned by
   * an xpath
   *
   * @param context the node to apply the XPath to
   * @param xpath the xpath to apply
   * @return the strings extracted
   * @throws XPathExpressionException on error
   */
  public String[] selectMultipleTextItems(Node context, String xpath)
      throws XPathExpressionException {
    NodeList list = selectNodeList(context, xpath);
    String[] retArray = new String[list.getLength()];

    for (int i = 0; i < list.getLength(); i++) {
      Node node = list.item(i);
      if (node != null) {
        if (node.getNodeType() == Node.ATTRIBUTE_NODE) {
          retArray[i] = node.getNodeValue();
        }
        else if (node.getNodeType() == Node.TEXT_NODE) {
          retArray[i] = node.getNodeValue();
        }
        else {
          node.normalize();
          Node text = node.getFirstChild();
          retArray[i] = text.getNodeValue();
        }
      }
    }
    return retArray;
  }

  /**
   * selects a list of Nodes from the context node using the supplied xpath
   *
   * @param context the root node to query
   * @param xpath the xpath to apply
   * @return NodeList of returned Nodes
   * @throws XPathExpressionException on error.
   */
  public NodeList selectNodeList(Node context, String xpath)
      throws XPathExpressionException {
    return (NodeList) createXpath().evaluate(xpath, context, XPathConstants.NODESET);
  }

  /**
   * Selects a single Node based on the supplied Xpath
   *
   * @param context the root node to query
   * @param xpath the xpath to apply
   * @return the Node extracted
   * @throws XPathExpressionException on error.
   */
  public Node selectSingleNode(Node context, String xpath)
      throws XPathExpressionException {
    return (Node) createXpath().evaluate(xpath, context, XPathConstants.NODE);
  }
}
