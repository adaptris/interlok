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

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.xml.transform.ErrorListener;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;

import org.apache.commons.lang.BooleanUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.adaptris.annotation.AdvancedConfig;
import com.adaptris.annotation.AutoPopulated;
import com.adaptris.annotation.InputFieldDefault;
import com.adaptris.core.util.Args;
import com.adaptris.core.util.DocumentBuilderFactoryBuilder;
import com.adaptris.util.KeyValuePair;
import com.adaptris.util.KeyValuePairSet;

public abstract class XmlTransformerFactoryImpl implements XmlTransformerFactory {

  private transient Logger log = LoggerFactory.getLogger(this.getClass());

  @AdvancedConfig
  @Valid
  private DocumentBuilderFactoryBuilder xmlDocumentFactoryConfig;
  @NotNull
  @AdvancedConfig
  @AutoPopulated
  private KeyValuePairSet transformerFactoryAttributes;
  @NotNull
  @AdvancedConfig
  @AutoPopulated
  private KeyValuePairSet transformerFactoryFeatures;
  @AdvancedConfig
  @InputFieldDefault(value = "true")
  private Boolean failOnRecoverableError;

  public XmlTransformerFactoryImpl() {
    setTransformerFactoryAttributes(new KeyValuePairSet());
    setTransformerFactoryFeatures(new KeyValuePairSet());
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

  @Override
  public XmlTransformer configure(XmlTransformer xmlTransformer) throws Exception {
    xmlTransformer.registerBuilder(documentFactoryBuilder());
    return xmlTransformer;
  }


  TransformerFactory configure(TransformerFactory tf) throws TransformerConfigurationException {
    for (KeyValuePair kp : getTransformerFactoryAttributes()) {
      tf.setAttribute(kp.getKey(), kp.getValue());
    }
    for (KeyValuePair kp : getTransformerFactoryFeatures()) {
      tf.setFeature(kp.getKey(), BooleanUtils.toBoolean(kp.getValue()));
    }
    tf.setErrorListener(new DefaultErrorListener(failOnRecoverableError()));
    return tf;
  }


  public KeyValuePairSet getTransformerFactoryAttributes() {
    return transformerFactoryAttributes;
  }


  public void setTransformerFactoryAttributes(KeyValuePairSet attr) {
    this.transformerFactoryAttributes = Args.notNull(attr, "TransformerFactoryAttributes");
  }


  public KeyValuePairSet getTransformerFactoryFeatures() {
    return transformerFactoryFeatures;
  }

  public void setTransformerFactoryFeatures(KeyValuePairSet features) {
    this.transformerFactoryFeatures = Args.notNull(features, "TransformerFactoryFeatures");
  }



  /**
   * 
   * @deprecated since 3.6.5, XSLT 3.0 has eliminated all "recoverable errors" from the specification. If you are using a previous
   *             version of saxon or xalan then this will still have an effect.
   */
  public Boolean getFailOnRecoverableError() {
    return failOnRecoverableError;
  }

  /**
   * Whether or not to fail on a recoverable error.
   * 
   * @param b true to fail on a recoverable error which is the default, false otherwise.
   * @deprecated since 3.6.5, XSLT 3.0 has eliminated all "recoverable errors" from the specification. If you are using a previous
   *             version of saxon or xalan then this will still have an effect.
   */
  public void setFailOnRecoverableError(Boolean b) {
    this.failOnRecoverableError = b;
  }

  private boolean failOnRecoverableError() {
    return getFailOnRecoverableError() != null ? getFailOnRecoverableError().booleanValue() : true;
  }

  private class DefaultErrorListener implements ErrorListener {

    private boolean failOnError;

    DefaultErrorListener(boolean b) {
      this.failOnError = b;
    }
    @Override
    public void warning(TransformerException e) throws TransformerException {
      log.warn("[{}]", e.getMessageAndLocation());
    }

    @Override
    public void error(TransformerException e) throws TransformerException {
      // It's a recoverable exception; let it try and carry on, but let's log the error message
      // This will be the error message from <xsl:message terminate="yes">Msg</xsl:message>
      log.error("[{}]", e.getMessageAndLocation());
      // We throw an error, but Saxon may still eat it.
      if (failOnError)
        throw e;
    }

    @Override
    public void fatalError(TransformerException e) throws TransformerException {
      log.error("Fatal : [{}]", e.getMessageAndLocation());
      // The method should throw an exception if it is unable to process the error, or if it wishes execution to terminate
      // immediately. The processor will not necessarily honor this request.
      throw e;
    }
  }


}
