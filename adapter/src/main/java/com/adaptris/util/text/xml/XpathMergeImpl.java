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

import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;

import com.adaptris.annotation.AdvancedConfig;
import com.adaptris.core.util.DocumentBuilderFactoryBuilder;
import com.adaptris.util.KeyValuePairSet;
import com.adaptris.util.XmlUtils;

public abstract class XpathMergeImpl extends MergeImpl {

  @AdvancedConfig
  private KeyValuePairSet namespaceContext;
  @AdvancedConfig
  private DocumentBuilderFactoryBuilder xmlDocumentFactoryConfig;

  XpathMergeImpl() {
    super();
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

  protected XPath createXPath() {
    return new XPath(SimpleNamespaceContext.create(getNamespaceContext()));
  }

  protected XmlUtils create(Document doc) throws Exception {
    XmlUtils xml = new XmlUtils(SimpleNamespaceContext.create(getNamespaceContext()),
        documentFactoryBuilder().configure(DocumentBuilderFactory.newInstance()));
    xml.setSource(doc);
    return xml;
  }

  public DocumentBuilderFactoryBuilder getXmlDocumentFactoryConfig() {
    return xmlDocumentFactoryConfig;
  }


  public void setXmlDocumentFactoryConfig(DocumentBuilderFactoryBuilder xml) {
    this.xmlDocumentFactoryConfig = xml;
  }

  DocumentBuilderFactoryBuilder documentFactoryBuilder() {
    return DocumentBuilderFactoryBuilder.newInstance(getXmlDocumentFactoryConfig());
  }
}
