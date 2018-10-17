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

package com.adaptris.core;

import static com.adaptris.core.util.XmlHelper.createDocument;
import static com.adaptris.util.text.xml.XPath.newXPathInstance;
import static org.apache.commons.lang.StringUtils.isEmpty;

import javax.validation.Valid;
import javax.xml.namespace.NamespaceContext;

import org.hibernate.validator.constraints.NotBlank;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

import com.adaptris.annotation.AdvancedConfig;
import com.adaptris.annotation.DisplayOrder;
import com.adaptris.core.util.Args;
import com.adaptris.core.util.DocumentBuilderFactoryBuilder;
import com.adaptris.util.KeyValuePairSet;
import com.adaptris.util.text.xml.SimpleNamespaceContext;
import com.adaptris.util.text.xml.XPath;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * <p>
 * Implementation of <code>TradingRelationshipCreator</code> which populates the <code>TradingRelationship</code> with values
 * returned from configurable xpaths.
 * </p>
 *
 * <p>
 * If the {@code DocumentBuilderFactoryBuilder} has been explicitly set to be not namespace aware and the document does in fact
 * contain namespaces, then Saxon can cause merry havoc in the sense that {@code //NonNamespaceXpath} doesn't work if the document
 * has namespaces in it. We have included a shim so that behaviour can be toggled based on what you have configured.
 * </p>
 * 
 * @see XPath#newXPathInstance(DocumentBuilderFactoryBuilder, NamespaceContext)
 * @config xpath-trading-relationship-creator
 */
@XStreamAlias("xpath-trading-relationship-creator")
@DisplayOrder(order = {"sourceXpath", "destinationXpath", "typeXpath", "namespaceContext"})
public class XpathTradingRelationshipCreator implements
    TradingRelationshipCreator {

  private transient Logger log = LoggerFactory.getLogger(this.getClass().getName());

  @NotBlank
  private String sourceXpath;
  @NotBlank
  private String destinationXpath;
  @NotBlank
  private String typeXpath;

  @AdvancedConfig
  private KeyValuePairSet namespaceContext;
  @AdvancedConfig
  @Valid
  private DocumentBuilderFactoryBuilder xmlDocumentFactoryConfig;
  /**
   * <p>
   * Creates a new instance. Default keys are empty <code>String</code>s.
   * </p>
   */
  public XpathTradingRelationshipCreator() {
    sourceXpath = "";
    destinationXpath = "";
    typeXpath = "";
  }

  public XpathTradingRelationshipCreator(String srcXp, String destXp, String typeXp) {
    this();
    setSourceXpath(srcXp);
    setDestinationXpath(destXp);
    setTypeXpath(typeXp);
  }

  /**
   * @see TradingRelationshipCreator #create(AdaptrisMessage)
   */
  public TradingRelationship create(AdaptrisMessage msg) throws CoreException {
    TradingRelationship result = null;
    try {
      NamespaceContext namespaceCtx = SimpleNamespaceContext.create(getNamespaceContext(), msg);
      DocumentBuilderFactoryBuilder builder = documentFactoryBuilder(namespaceCtx);     
      XPath xpath = newXPathInstance(builder, namespaceCtx);
      Document doc = createDocument(msg, builder);
      String source = resolveXpath(sourceXpath, xpath, doc);
      String destination = resolveXpath(destinationXpath, xpath, doc);
      String type = resolveXpath(typeXpath, xpath, doc);

      result = new TradingRelationship(source, destination, type);
      log.debug("created " + result);
    }
    catch (Exception e) {
      throw new CoreException(e);
    }
    return result;
  }

  private String resolveXpath(String xpath, XPath xp, Document d) throws Exception {
    String result = null;
    if (isEmpty(xpath)) {
      throw new Exception("Empty XPath");
    }
    result = xp.selectSingleTextItem(d, xpath);
    if (isEmpty(result)) {
      throw new Exception("key [" + xpath + "] returned null or empty");
    }
    return result;
  }

  // getters & setters...

  /**
   * <p>
   * Returns the metadata key used to obtain the destination.
   * </p>
   *
   * @return the metadata key used to obtain the destination
   */
  public String getDestinationXpath() {
    return destinationXpath;
  }

  /**
   * <p>
   * Sets the metadata key used to obtain the destination. May not be null or
   * empty.
   * </p>
   *
   * @param s the metadata key used to obtain the destination
   */
  public void setDestinationXpath(String s) {
    destinationXpath = Args.notBlank(s, "destinationXpath");
  }

  /**
   * <p>
   * Returns the metadata key used to obtain the source.
   * </p>
   *
   * @return the metadata key used to obtain the source
   */
  public String getSourceXpath() {
    return sourceXpath;
  }

  /**
   * <p>
   * Sets the metadata key used to obtain the source. May not be null or empty.
   * </p>
   *
   * @param s the metadata key used to obtain the source
   */
  public void setSourceXpath(String s) {
    sourceXpath = Args.notBlank(s, "sourceXpath");
  }

  /**
   * <p>
   * Returns the metadata key used to obtain the type.
   * </p>
   *
   * @return the metadata key used to obtain the type
   */
  public String getTypeXpath() {
    return typeXpath;
  }

  /**
   * <p>
   * Sets the metadata key used to obtain the type. May not be null or empty.
   * </p>
   *
   * @param s the metadata key used to obtain the type
   */
  public void setTypeXpath(String s) {
    typeXpath = Args.notBlank(s, "typeXpath");
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


  public DocumentBuilderFactoryBuilder getXmlDocumentFactoryConfig() {
    return xmlDocumentFactoryConfig;
  }


  public void setXmlDocumentFactoryConfig(DocumentBuilderFactoryBuilder xml) {
    this.xmlDocumentFactoryConfig = xml;
  }

  private DocumentBuilderFactoryBuilder documentFactoryBuilder(NamespaceContext nc) {
    return DocumentBuilderFactoryBuilder.newInstance(getXmlDocumentFactoryConfig(), nc);
  }
}
