/*
 * Copyright 2015 Adaptris Ltd.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.adaptris.util.text.xml;

import com.adaptris.annotation.AdvancedConfig;
import com.adaptris.core.util.DocumentBuilderFactoryBuilder;

public abstract class XmlTransformerFactoryImpl implements XmlTransformerFactory {
  @AdvancedConfig
  private DocumentBuilderFactoryBuilder xmlDocumentFactoryConfig;

  public DocumentBuilderFactoryBuilder getXmlDocumentFactoryConfig() {
    return xmlDocumentFactoryConfig;
  }


  public void setXmlDocumentFactoryConfig(DocumentBuilderFactoryBuilder xml) {
    this.xmlDocumentFactoryConfig = xml;
  }

  DocumentBuilderFactoryBuilder documentFactoryBuilder() {
    return getXmlDocumentFactoryConfig() != null ? getXmlDocumentFactoryConfig()
        : DocumentBuilderFactoryBuilder.newInstance().withNamespaceAware(true);
  }

  @Override
  public XmlTransformer configure(XmlTransformer xmlTransformer) throws Exception {
    xmlTransformer.registerBuilder(documentFactoryBuilder());
    return xmlTransformer;
  }

}
