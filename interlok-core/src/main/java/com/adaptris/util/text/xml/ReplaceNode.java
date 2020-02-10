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

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import org.apache.commons.lang3.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import com.adaptris.annotation.DisplayOrder;
import com.adaptris.core.util.Args;
import com.adaptris.core.util.DocumentBuilderFactoryBuilder;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Merge implementation that replaces a node derived from an Xpath.
 * <p>
 * If the {@code DocumentBuilderFactoryBuilder} has been explicitly set to be not namespace aware and the document does in fact
 * contain namespaces, then Saxon can cause merry havoc in the sense that {@code //NonNamespaceXpath} doesn't work if the document
 * has namespaces in it. We have included a shim so that behaviour can be toggled based on what you have configured.
 * </p>
 * 
 * @config xml-replace-node
 * @author lchan
 * @see XPath#newXPathInstance(DocumentBuilderFactoryBuilder, NamespaceContext)
 */
@XStreamAlias("xml-replace-node")
@DisplayOrder(order = {"xpathToNode"})
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
    if (StringUtils.isEmpty(getXpathToNode())) {
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
    xpathToNode = Args.notBlank(xpath, "xpathToNode");
  }
}
