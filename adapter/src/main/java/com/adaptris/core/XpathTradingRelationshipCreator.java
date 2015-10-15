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
import static org.apache.commons.lang.StringUtils.isEmpty;

import javax.validation.constraints.NotNull;
import javax.xml.namespace.NamespaceContext;

import org.hibernate.validator.constraints.NotBlank;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

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
 * @config xpath-trading-relationship-creator
 */
@XStreamAlias("xpath-trading-relationship-creator")
public class XpathTradingRelationshipCreator implements
    TradingRelationshipCreator {

  private transient Logger log = LoggerFactory.getLogger(this.getClass().getName());

  @NotNull
  @NotBlank
  private String sourceXpath;
  @NotNull
  @NotBlank
  private String destinationXpath;
  @NotNull
  @NotBlank
  private String typeXpath;

  private KeyValuePairSet namespaceContext;

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
      XPath xpath = new XPath(namespaceCtx);
      Document doc = createDocument(msg, namespaceCtx);
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

  /** @see java.lang.Object#toString() */
  @Override
  public String toString() {
    StringBuffer result = new StringBuffer(this.getClass().getName());
    result.append(" source xpath [");
    result.append(getSourceXpath());
    result.append("] destination xpath [");
    result.append(getDestinationXpath());
    result.append("] type xpath [");
    result.append(getTypeXpath());
    result.append("]");

    return result.toString();
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
    if (s == null || "".equals(s)) {
      throw new IllegalArgumentException("null or empty param");
    }
    destinationXpath = s;
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
    if (s == null || "".equals(s)) {
      throw new IllegalArgumentException("null or empty param");
    }
    sourceXpath = s;
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
    if (s == null || "".equals(s)) {
      throw new IllegalArgumentException("null or empty param");
    }
    typeXpath = s;
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
}
