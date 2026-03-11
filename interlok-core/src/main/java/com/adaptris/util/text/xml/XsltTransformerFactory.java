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

import java.util.Optional;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;

import com.adaptris.annotation.AdvancedConfig;
import com.adaptris.annotation.DisplayOrder;
import com.thoughtworks.xstream.annotations.XStreamAlias;

import lombok.Getter;
import lombok.Setter;
import net.sf.saxon.Configuration;
import net.sf.saxon.TransformerFactoryImpl;

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
@DisplayOrder(order = { "transformerFactoryImpl", "extensionRegistrarImplementation", "failOnRecoverableError" })
public class XsltTransformerFactory extends XmlTransformerFactoryImpl {

  private transient final Logger log = LoggerFactory.getLogger(this.getClass());

  @Getter
  @Setter
  @AdvancedConfig
  private String transformerFactoryImpl;

  @Getter
  @Setter
  @AdvancedConfig
  private String extensionRegistrarImplementation;

  public XsltTransformerFactory() {
    super();
  }

  public XsltTransformerFactory(String impl) {
    this();
    setTransformerFactoryImpl(impl);
  }

  /**
   * Override {@link XmlTransformerFactoryImpl#createTransformerFromUrl(String, EntityResolver)} so when using a URL we build the XML
   * document directly from the URL instead of the InputSream of the URL file content. Doing this allows the transformer to have the file
   * location context and therefore the import statement in the XSL can use relative path.
   */
  @Override
  public Transformer createTransformerFromUrl(String url, EntityResolver entityResolver) throws Exception {
    DocumentBuilder docBuilder = documentFactoryBuilder().newDocumentBuilder(DocumentBuilderFactory.newInstance());
    if (entityResolver != null) {
      docBuilder.setEntityResolver(entityResolver);
    }
    Document xmlDoc = docBuilder.parse(new InputSource(url));
    return configure(newInstance()).newTransformer(new DOMSource(xmlDoc, url));
  }

  @Override
  protected TransformerFactory newInstance() {
    TransformerFactory tf = StringUtils.isEmpty(getTransformerFactoryImpl())
        ? TransformerFactory.newInstance()
        : TransformerFactory.newInstance(getTransformerFactoryImpl(), null);

    getSaxonConfiguration(tf).ifPresent(config -> {
      log.debug("Registering Saxon extensions with config: {}", config);
      getExtensionRegistrar().register(config);
    });

    return tf;
  }

  protected Optional<Configuration> getSaxonConfiguration(TransformerFactory tf) {
    log.debug("Getting SaxonConfiguration if applicable");

    if (tf == null) {
      return Optional.empty();
    }

    // Saxon-HE and Saxon-EE factories both derive from TransformerFactoryImpl.
    if (tf instanceof TransformerFactoryImpl) {
      log.debug("Retrieving Saxon Configuration from {}", tf.getClass().getName());
      return Optional.of(((TransformerFactoryImpl) tf).getConfiguration());
    }
    return Optional.empty();
  }

  public SaxonExtensionRegistrar getExtensionRegistrar() {
    return StringUtils.isNotEmpty(getExtensionRegistrarImplementation())
        ? loadExtensionRegistrar(getExtensionRegistrarImplementation())
        : new NoExtensions();
  }

  private SaxonExtensionRegistrar loadExtensionRegistrar(String className) {
    try {
      Class<?> clazz = Class.forName(className);
      if (SaxonExtensionRegistrar.class.isAssignableFrom(clazz)) {
        log.debug("Found SaxonExtensionRegistrar implementation class: {}", className);
        return (SaxonExtensionRegistrar) clazz.getDeclaredConstructor().newInstance();
      } else {
        log.warn("Class {} does not implement SaxonExtensionRegistrar", className);
      }
    } catch (Exception e) {
      log.warn("Failed to load SaxonExtensionRegistrar implementation: {}", className, e);
    }
    return new NoExtensions();
  }
}
