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

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;

import org.apache.commons.lang3.StringUtils;
import org.w3c.dom.Document;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;

import com.adaptris.annotation.AdvancedConfig;
import com.adaptris.annotation.DisplayOrder;
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
@DisplayOrder(order = {"transformerFactoryImpl", "failOnRecoverableError"})
public class XsltTransformerFactory extends XmlTransformerFactoryImpl {

  @AdvancedConfig
  private String transformerFactoryImpl;

  public XsltTransformerFactory() {
    super();
  }

  public XsltTransformerFactory(String impl) {
    this();
    setTransformerFactoryImpl(impl);
  }

  public Transformer createTransformer(String url) throws Exception {
    return this.createTransformer(url, null);
  }

  public Transformer createTransformer(String url, EntityResolver entityResolver) throws Exception {
    DocumentBuilder docBuilder = documentFactoryBuilder().newDocumentBuilder(DocumentBuilderFactory.newInstance());
    if (entityResolver != null) {
      docBuilder.setEntityResolver(entityResolver);
    }
    Document xmlDoc = docBuilder.parse(new InputSource(url));
    return configure(newInstance()).newTransformer(new DOMSource(xmlDoc, url));
  }

  /**
   * @return the transformerFactoryImpl
   */
  public String getTransformerFactoryImpl() {
    return transformerFactoryImpl;
  }

  /**
   * Specify the transformer factory that will be used.
   * <p>
   * If you have both saxon and xalan (for instance) available on the classpath; and you want to explicitly use the xalan
   * implementation then you could put {@code org.apache.xalan.processor.TransformerFactoryImpl} here to force it to use Xalan or
   * {@code net.sf.saxon.TransformerFactoryImpl} to force it to use Saxon.
   * <p>
   * 
   * @param s the transformerFactoryImpl to set, if not specified the JVM default is used {@link TransformerFactory#newInstance()}.
   */
  public void setTransformerFactoryImpl(String s) {
    this.transformerFactoryImpl = s;
  }

  private TransformerFactory newInstance() {
    return StringUtils.isEmpty(getTransformerFactoryImpl()) ? TransformerFactory.newInstance()
        : TransformerFactory.newInstance(getTransformerFactoryImpl(), null);
  }
}
