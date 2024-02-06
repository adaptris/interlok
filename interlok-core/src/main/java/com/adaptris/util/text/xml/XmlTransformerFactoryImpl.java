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

import static com.adaptris.util.URLHelper.connect;
import static org.apache.commons.lang3.StringUtils.isEmpty;

import java.io.InputStream;
import java.io.StringWriter;
import java.nio.charset.Charset;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.xml.transform.ErrorListener;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.EntityResolver;

import com.adaptris.annotation.AdvancedConfig;
import com.adaptris.annotation.AutoPopulated;
import com.adaptris.annotation.InputFieldDefault;
import com.adaptris.core.ServiceException;
import com.adaptris.core.util.Args;
import com.adaptris.core.util.DocumentBuilderFactoryBuilder;
import com.adaptris.util.KeyValuePair;
import com.adaptris.util.KeyValuePairSet;

public abstract class XmlTransformerFactoryImpl implements XmlTransformerFactory {

  private transient Logger log = LoggerFactory.getLogger(this.getClass());

  @AdvancedConfig(rare = true)
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
    xmlDocumentFactoryConfig = xml;
  }

  DocumentBuilderFactoryBuilder documentFactoryBuilder() {
    return DocumentBuilderFactoryBuilder.newInstanceIfNull(getXmlDocumentFactoryConfig());
  }

  @Override
  public Transformer createTransformerFromUrl(String xsl) throws Exception {
    return createTransformerFromUrl(xsl, null);
  }

  @Override
  public Transformer createTransformerFromUrl(String xsl, EntityResolver entityResolver) throws Exception {
    xsl = backslashToSlash(xsl);
    try (InputStream inputStream = connect(xsl)) {
      StringWriter writer = new StringWriter();
      IOUtils.copy(inputStream, writer, Charset.defaultCharset());
      xsl = writer.toString();
    } catch (Exception e) {
      throw new ServiceException("unable to extract the xsl content.", e);
    }
    return createTransformerFromRawXsl(xsl, entityResolver);
  }

  @Override
  public Transformer createTransformerFromRawXsl(String xsl) throws Exception {
    return createTransformerFromRawXsl(xsl, null);
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
    transformerFactoryAttributes = Args.notNull(attr, "TransformerFactoryAttributes");
  }

  public KeyValuePairSet getTransformerFactoryFeatures() {
    return transformerFactoryFeatures;
  }

  public void setTransformerFactoryFeatures(KeyValuePairSet features) {
    transformerFactoryFeatures = Args.notNull(features, "TransformerFactoryFeatures");
  }

  /**
   *
   * @deprecated since 3.6.5, XSLT 3.0 has eliminated all "recoverable errors" from the specification. If you are using a previous version
   *             of saxon or xalan then this will still have an effect.
   */
  @Deprecated
  public Boolean getFailOnRecoverableError() {
    return failOnRecoverableError;
  }

  /**
   * Whether or not to fail on a recoverable error.
   *
   * @param b true to fail on a recoverable error which is the default, false otherwise.
   * @deprecated since 3.6.5, XSLT 3.0 has eliminated all "recoverable errors" from the specification. If you are using a previous version
   *             of saxon or xalan then this will still have an effect.
   */
  @Deprecated
  public void setFailOnRecoverableError(Boolean b) {
    failOnRecoverableError = b;
  }

  private boolean failOnRecoverableError() {
    return BooleanUtils.toBooleanDefaultIfNull(getFailOnRecoverableError(), true);
  }

  private class DefaultErrorListener implements ErrorListener {

    private boolean failOnError;

    DefaultErrorListener(boolean b) {
      failOnError = b;
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
      if (failOnError) {
        throw e;
      }
    }

    @Override
    public void fatalError(TransformerException e) throws TransformerException {
      log.error("Fatal : [{}]", e.getMessageAndLocation());
      // The method should throw an exception if it is unable to process the error, or if it wishes execution to terminate
      // immediately. The processor will not necessarily honor this request.
      throw e;
    }
  }

  private static String backslashToSlash(String url) {
    if (!isEmpty(url)) {
      return url.replaceAll("\\\\", "/");
    }
    return url;
  }

}
