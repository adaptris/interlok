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

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;

import org.w3c.dom.Document;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;

import com.adaptris.annotation.AdvancedConfig;
import com.adaptris.core.util.DocumentBuilderFactoryBuilder;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * <p>
 * The XsltTransformerFactory is responsible for creating the {@link Transformer}.
 * </p>
 * <p>
 * The {@link Transformer} is used to actually perform a document transformation.
 * </p>
 * 
 * @config xslt-transformer-factory
 * 
 * @author amcgrath
 */

@XStreamAlias("xslt-transformer-factory")
public class XsltTransformerFactory implements XmlTransformerFactory {
  @AdvancedConfig
  private DocumentBuilderFactoryBuilder xmlDocumentFactoryConfig;

  public Transformer createTransformer(String url) throws Exception {
    return this.createTransformer(url, null);
  }

  public Transformer createTransformer(String url, EntityResolver entityResolver) throws Exception {
    DocumentBuilderFactory dfactory = documentFactoryBuilder().configure(DocumentBuilderFactory.newInstance());
    dfactory.setCoalescing(true);
    dfactory.setNamespaceAware(true);

    DocumentBuilder docBuilder = dfactory.newDocumentBuilder();
    if (entityResolver != null) {
      docBuilder.setEntityResolver(entityResolver);
    }
    Document xmlDoc = docBuilder.parse(new InputSource(url));

    return TransformerFactory.newInstance().newTransformer(new DOMSource(xmlDoc, url));
  }


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

}
