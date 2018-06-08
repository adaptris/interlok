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

import javax.validation.constraints.NotNull;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.validator.constraints.NotBlank;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import com.adaptris.annotation.DisplayOrder;
import com.adaptris.core.util.Args;
import com.adaptris.core.util.DocumentBuilderFactoryBuilder;
import com.adaptris.util.XmlUtils;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Merge implementation that appends the result to a parent node derived from an Xpath.
 * <p>
 * If the {@code DocumentBuilderFactoryBuilder} has been explicitly set to be not namespace aware and the document does in fact
 * contain namespaces, then Saxon can cause merry havoc in the sense that {@code //NonNamespaceXpath} doesn't work if the document
 * has namespaces in it. We have included a shim so that behaviour can be toggled based on what you have configured.
 * </p>
 * 
 * @see XPath#newXPathInstance(DocumentBuilderFactoryBuilder, NamespaceContext)
 * @config xml-insert-node
 * 
 * @author lchan
 * 
 */
@XStreamAlias("xml-insert-node")
@DisplayOrder(order = {"xpathToParentNode"})
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
    if (StringUtils.isEmpty(getXpathToParentNode())) {
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
    xpathToParentNode = Args.notBlank(xpath, "xpathToParentNode");
  }
}
