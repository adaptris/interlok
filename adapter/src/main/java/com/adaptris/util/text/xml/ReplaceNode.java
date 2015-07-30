package com.adaptris.util.text.xml;

import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.NotBlank;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Merge implementation that replaces a node derived from an Xpath.
 * 
 * @config xml-replace-node
 * 
 * @author lchan
 * 
 */
@XStreamAlias("xml-replace-node")
public class ReplaceNode extends XpathMergeImpl {
  @NotNull
  @NotBlank
  private String xpathToNode;

  public ReplaceNode() {
  }

  public ReplaceNode(String xpath) {
    this();
    setXpathToNode(xpath);
  }

  @Override
  public Document merge(Document original, Document newDoc) throws Exception {
    if (getXpathToNode() == null) {
      throw new Exception("No xpath node configured");
    }
    Document resultDoc = original;
    Node node = createXPath().selectSingleNode(resultDoc, getXpathToNode());
    if (node == null) {
      throw new Exception("Failed to resolve " + getXpathToNode());
    }
    Node parent = node.getParentNode();
    if (parent == null) {
      throw new Exception("Parent of " + getXpathToNode() + " is null");
    }
    Node replacement = resultDoc.importNode(newDoc.getDocumentElement(), true);
    parent.replaceChild(replacement, node);
    return resultDoc;
  }

  public String getXpathToNode() {
    return xpathToNode;
  }

  /**
   * Set the xpath to discover the node to be replaced.
   *
   * @param xpath
   */
  public void setXpathToNode(String xpath) {
    xpathToNode = xpath;
  }
}
