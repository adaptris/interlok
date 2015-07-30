/*
 * $Id: NodeBuilder.java,v 1.2 2004/03/22 13:43:58 lchan Exp $
 */
package com.adaptris.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.Text;

/** Class which a simple method of creating new
  * w3c DOM Nodes.
  * @author  Stuart Ellidge
  */

class NodeBuilder {
  private String name = null;
  private String value = null;

  private ArrayList<NodeBuilder> children = null;
  private HashMap<String, String> attributes = null;

  /** Constructor initialises aboject and sets the element name
   *  @param elementName the name of the element.
   */
  public NodeBuilder(String elementName) {
    name = elementName;
    children = new ArrayList<NodeBuilder>();
    attributes = new HashMap<String, String>();
  }

  /** Set the String value of the Node to be created
   *  @param nodeValue the value of the node
   */
  public void setValue(String nodeValue) {
    value = nodeValue;
  }

  /** Method to add a child Node
   *  @param nb the cuild.
   */
  public void addChild(NodeBuilder nb) {
    children.add(nb);
  }

  /** Method to set an attribute against the Node
   *  @param elementName the element Name,
   *  @param elementValue the element value.
   */
  public void setAttribute(String elementName, String elementValue) {
    attributes.put(elementName, elementValue);
  }

  /** method which returns a Node of type org.w3c.dom.Element from the
    * values set against the tree of NodeBuilders that is a child of the
    * supplier Document Node.
    * @param node the DOM Document that this Node will belong to
    * @throws Exception if the supplied Node is not of type
    * org.w3c.dom.Document
    * @return Node the Node this NodeBuilder represents.
    */
  public Node getNode(Node node) throws Exception {
    Document doc = null;
    try {
      doc = (Document) node;
    }
    catch (ClassCastException cce) {
      throw new Exception("Node provided must be a Document Node");
    }

    Element e = doc.createElement(name);

    Iterator attrIterator = attributes.keySet().iterator();

    while (attrIterator.hasNext()) {
      String attrName = attrIterator.next().toString();
      e.setAttribute(attrName, attributes.get(attrName).toString());
    }

    for (int i = 0; i < children.size(); i++) {
      NodeBuilder child = children.get(i);
      e.appendChild(child.getNode(doc));
    }

    if (value != null) {
      Text t = doc.createTextNode(value);
      e.appendChild(t);
    }

    return e;
  }
}
