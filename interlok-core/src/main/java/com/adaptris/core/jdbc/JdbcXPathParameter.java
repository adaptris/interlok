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

package com.adaptris.core.jdbc;
import static com.adaptris.util.text.xml.XPath.newXPathInstance;
import static org.apache.commons.lang3.StringUtils.isEmpty;

import javax.validation.constraints.NotNull;
import javax.xml.namespace.NamespaceContext;

import org.hibernate.validator.constraints.NotBlank;
import org.w3c.dom.Document;

import com.adaptris.annotation.AdvancedConfig;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.util.DocumentBuilderFactoryBuilder;
import com.adaptris.core.util.XmlHelper;
import com.adaptris.util.KeyValuePairSet;
import com.adaptris.util.text.xml.SimpleNamespaceContext;
import com.adaptris.util.text.xml.XPath;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Stored Procedure parameter implementation, can be used only for IN Stored Procedure parameters.
 * <p>
 * When this implementation is used for an IN parameter, then an xpath expression will be evaluated from the
 * {@link com.adaptris.core.AdaptrisMessage} payload and used as the parameter value. You will simply set xpath to a valid xpath
 * expression to retrieve data from the {@link com.adaptris.core.AdaptrisMessage} payload.
 * </p>
 * <p>
 * Additionally you will set one or both of "name" and/or "order". "name" will map this parameter to a Stored Procedure parameter
 * using the Stored Procedures method signature. "order" will map this parameter according to the parameter number using the Stored
 * Procedures method signature. Note that the "order" starts from 1 and not 0, so the first parameter would be order 1. You will
 * also need to set the data type of the parameter; you may use any of the string types defined in
 * {@link com.adaptris.jdbc.ParameterValueType}
 * </p>
 * <p>
 * If the {@code DocumentBuilderFactoryBuilder} has been explicitly set to be not namespace aware and the document does in fact
 * contain namespaces, then Saxon can cause merry havoc in the sense that {@code //NonNamespaceXpath} doesn't work if the document
 * has namespaces in it. We have included a shim so that behaviour can be toggled based on what you have configured.
 * </p>
 * 
 * @see XPath#newXPathInstance(DocumentBuilderFactoryBuilder, NamespaceContext)
 * 
 * @config jdbc-xpath-parameter
 * @author Aaron McGrath
 * 
 */
@XStreamAlias("jdbc-xpath-parameter")
public class JdbcXPathParameter extends NullableParameter {

  @NotNull
  @NotBlank
  private String xpath;
  @AdvancedConfig
  private KeyValuePairSet namespaceContext;
  @AdvancedConfig
  private DocumentBuilderFactoryBuilder xmlDocumentFactoryConfig;



  @Override
  public Object applyInputParam(AdaptrisMessage msg) throws JdbcParameterException {
    this.checkXPath();
    NamespaceContext ctx = SimpleNamespaceContext.create(namespaceContext, msg);
    try {
      DocumentBuilderFactoryBuilder builder = documentFactoryBuilder();
      XPath xpathToUse = newXPathInstance(builder, ctx);
      Document doc = XmlHelper.createDocument(msg, builder);
      String textItem = xpathToUse.selectSingleTextItem(doc, msg.resolve(getXpath()));
      return normalize(textItem);
    }
    catch (Exception ex) {
      throw new JdbcParameterException(ex);
    }
  }

  @Override
  public void applyOutputParam(Object dbValue, AdaptrisMessage msg) throws JdbcParameterException {
    throw new JdbcParameterException(this.getClass().getName() + " cannot be applied to Jdbc output parameters.");
  }
  
  private void checkXPath() throws JdbcParameterException {
    if(isEmpty(this.getXpath()))
      throw new JdbcParameterException("XPath has not been set for " + this.getClass().getName());
  }

  public String getXpath() {
    return xpath;
  }

  public void setXpath(String xpath) {
    this.xpath = xpath;
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
    return DocumentBuilderFactoryBuilder.newInstance(getXmlDocumentFactoryConfig());
  }
}
