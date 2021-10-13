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

import static com.adaptris.core.util.XmlHelper.createXmlUtils;
import static org.apache.commons.lang3.StringUtils.isBlank;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.xml.namespace.NamespaceContext;

import com.adaptris.validation.constraints.ConfigDeprecated;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.adaptris.annotation.AdvancedConfig;
import com.adaptris.annotation.DisplayOrder;
import com.adaptris.core.util.Args;
import com.adaptris.core.util.DocumentBuilderFactoryBuilder;
import com.adaptris.util.KeyValuePairSet;
import com.adaptris.util.XmlUtils;
import com.adaptris.util.text.xml.SimpleNamespaceContext;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * <p>
 * Basic implementation of <code>ProduceDestination</code> that uses an XPath to interrogate the Message.
 * </p>
 * <p>
 * Only the first match for the supplied xpath is used. If the xpath does not return any results, then the default destination is
 * returned.
 * </p>
 * 
 * @config xpath-produce-destination
 */
@XStreamAlias("xpath-produce-destination")
@DisplayOrder(order = {"xpath", "defaultDestination", "namespaceContext", "xmlDocumentFactoryConfig"})
@Deprecated(forRemoval = true)
@ConfigDeprecated(removalVersion = "4.0.0", groups = Deprecated.class)
public class XpathProduceDestination implements MessageDrivenDestination {

  @NotNull
  private String xpath;
  @NotNull
  private String defaultDestination;

  @AdvancedConfig(rare = true)
  @Valid
  private KeyValuePairSet namespaceContext;
  @AdvancedConfig(rare = true)
  @Valid
  private DocumentBuilderFactoryBuilder xmlDocumentFactoryConfig;

  private transient Logger logR = LoggerFactory.getLogger(this.getClass().getName());

  /**
   * Default Constructor.
   */
  public XpathProduceDestination() {
    this.setXpath("");
    this.setDefaultDestination("");
  }

  public XpathProduceDestination(String xpath) {
    this();
    setXpath(xpath);
  }

  public XpathProduceDestination(String xpath, String defaultDest) {
    this();
    setXpath(xpath);
    setDefaultDestination(defaultDest);
  }

  /**
   *
   * @see java.lang.Object#equals(java.lang.Object)
   */
  @Override
  public boolean equals(Object obj) {
    if (obj == null) return false;
    if (obj == this) return true;
    if (obj instanceof XpathProduceDestination) {
      XpathProduceDestination item = (XpathProduceDestination) obj;
      return new EqualsBuilder().append(item.getXpath(), this.getXpath())
          .append(item.getDefaultDestination(), this.getDefaultDestination())
          .append(item.getXmlDocumentFactoryConfig(), this.getXmlDocumentFactoryConfig())
          .append(item.getNamespaceContext(), this.getNamespaceContext()).isEquals();
    }
    return false;
  }

  /**
   *
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode() {
    return new HashCodeBuilder().append(getXpath()).append(getDefaultDestination()).append(getXmlDocumentFactoryConfig())
        .append(getNamespaceContext())
        .toHashCode();
  }

  /**
   * @see ProduceDestination#getDestination(com.adaptris.core.AdaptrisMessage)
   */
  @Override
  public String getDestination(AdaptrisMessage msg) {
    String result = defaultDestination;
    try {
      NamespaceContext ctx = SimpleNamespaceContext.create(getNamespaceContext(), msg);
      DocumentBuilderFactoryBuilder builder = documentFactoryBuilder();
      if (ctx != null) {
        builder.setNamespaceAware(true);
      }
      XmlUtils xml = createXmlUtils(msg, ctx, builder);
      String s = xml.getSingleTextItem(xpath);
      if (isBlank(s)) {
        logR.warn("[{}] returned no results", xpath);
      }
      else {
        result = s;
      }
    }
    catch (Exception e) {
      logR.warn("Exception caught attempting to parse message, using default destination");
    }
    logR.trace("Returning [{}]", result);
    return result;
  }

  /**
   * <p>
   * Returns the name of the destination.
   * </p>
   *
   * @return the name of the destination
   */
  public String getXpath() {
    return xpath;
  }

  /**
   * Set the XPath that will be used to resolve the correct destination.
   *
   * @param s the xpath
   */
  public void setXpath(String s) {
    xpath = Args.notNull(s, "xpath");
  }

  /**
   * @return the defaultDestination
   */
  public String getDefaultDestination() {
    return defaultDestination;
  }

  /**
   * The default destination to use if the configured xpath does not resolve to
   * any elements.
   *
   * @param s the defaultDestination to set
   */
  public void setDefaultDestination(String s) {
    this.defaultDestination = Args.notNull(s, "defaultDestination");
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

  DocumentBuilderFactoryBuilder documentFactoryBuilder() {
    return DocumentBuilderFactoryBuilder.newInstanceIfNull(getXmlDocumentFactoryConfig());
  }

}
