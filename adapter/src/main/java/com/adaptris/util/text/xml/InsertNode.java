package com.adaptris.util.text.xml;

import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.NotBlank;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import com.adaptris.util.XmlUtils;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Merge implementation that appends the result to a parent node derived from an Xpath.
 * 
 * @config xml-insert-node
 * 
 * @author lchan
 * 
 */
@XStreamAlias("xml-insert-node")
public class InsertNode extends XpathMergeImpl {
  @NotNull
  @NotBlank
  private String xpathToParentNode;

  public InsertNode() {

  }

  public InsertNode(String xpath) {
    this();
    setXpathToParentNode(xpath);
  }

  @Override
  public Document merge(Document original, Document newDoc) throws Exception {
    if (getXpathToParentNode() == null) {
      throw new Exception("No parent node configured");
    }
    Document resultDoc = original;
    XmlUtils xml = create(resultDoc);
    Node parent = resolve(xml, getXpathToParentNode());
    if (parent.getOwnerDocument() == null) {
      throw new Exception("Invalid xpath-to-parent-node [" + getXpathToParentNode() + "]");
    }
    xml.appendNode(newDoc.getDocumentElement(), parent);
    return resultDoc;
  }

  private Node resolve(XmlUtils xml, String xpath) throws Exception {
    Node parent = xml.getSingleNode(xpath);
    if (parent == null) {
      log.trace("Failed to resolve " + xpath + ", creating node");
      try {
        // This can be quite an obscure stack trace.
        parent = xml.createNode(xpath);
      }
      catch (Exception e) {
        throw new Exception("Failed to create node [" + xpath + "]", e);
      }
    }
    return parent;
  }

  public String getXpathToParentNode() {
    return xpathToParentNode;
  }

  /**
   * Set the xpath to discover the parent node where the result will be
   * inserted.
   *
   * @param xpath
   */
  public void setXpathToParentNode(String xpath) {
    xpathToParentNode = xpath;
  }
}
