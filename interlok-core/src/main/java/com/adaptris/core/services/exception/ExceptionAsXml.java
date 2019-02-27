/*
 * Copyright 2017 Adaptris Ltd.
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
package com.adaptris.core.services.exception;

import javax.validation.Valid;
import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import com.adaptris.annotation.AdvancedConfig;
import com.adaptris.annotation.DisplayOrder;
import com.adaptris.annotation.InputFieldDefault;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.CoreConstants;
import com.adaptris.core.CoreException;
import com.adaptris.core.Workflow;
import com.adaptris.core.util.DocumentBuilderFactoryBuilder;
import com.adaptris.core.util.ExceptionHelper;
import com.adaptris.core.util.XmlHelper;
import com.adaptris.util.text.xml.DocumentMerge;
import com.adaptris.util.text.xml.ReplaceOriginal;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Use with {@link ExceptionReportService} to write the exception as part of an xml document.
 * 
 * 
 * @config exception-as-xml
 */
@XStreamAlias("exception-as-xml")
@DisplayOrder(order =
{
    "xmlEncoding", "exceptionGenerator", "ignoreXmlParseExceptions", "documentMerge"
})
public class ExceptionAsXml implements ExceptionSerializer {
  protected transient Logger log = LoggerFactory.getLogger(this.getClass());

  @Valid
  @InputFieldDefault(value = "ReplaceOriginal")
  private DocumentMerge documentMerge;
  @Valid
  @InputFieldDefault(value = "SimpleExceptionReport")
  private ExceptionReportGenerator exceptionGenerator;
  @InputFieldDefault(value = "UTF-8")
  private String xmlEncoding;
  @AdvancedConfig
  @Valid
  private DocumentBuilderFactoryBuilder xmlDocumentFactoryConfig;

  @AdvancedConfig
  @InputFieldDefault(value = "false")
  private Boolean ignoreXmlParseExceptions;

  public ExceptionAsXml() {
  }

  public ExceptionAsXml withDocumentMerge(DocumentMerge docMerge) {
    setDocumentMerge(docMerge);
    return this;
  }

  public ExceptionAsXml withExceptionGenerator(ExceptionReportGenerator e) {
    setExceptionGenerator(e);
    return this;
  }

  public ExceptionAsXml withXmlEncoding(String e) {
    setXmlEncoding(e);
    return this;
  }

  public ExceptionAsXml withDocumentFactoryConfig(DocumentBuilderFactoryBuilder e) {
    setXmlDocumentFactoryConfig(e);
    return this;
  }

  public ExceptionAsXml withIgnoreXmlParseExceptions(Boolean b) {
    setIgnoreXmlParseExceptions(b);
    return this;
  }
  
  @Override
  public void serialize(Exception exception, AdaptrisMessage msg) throws CoreException {
    try {
      Document newDoc =
          exceptionGenerator().create(exception, msg.getMetadataValue(Workflow.WORKFLOW_ID_KEY),
              (String) msg.getObjectHeaders().get(CoreConstants.OBJ_METADATA_EXCEPTION_CAUSE));
      documentFactoryBuilder().build().newDocumentBuilder().newDocument();
      Document result = documentMerge().merge(XmlHelper.createDocument(msg, documentFactoryBuilder(), ignoreXmlParseExceptions()),
          newDoc);
      String encoding = XmlHelper.getXmlEncoding(msg, getXmlEncoding());
      XmlHelper.writeXmlDocument(result, msg, encoding);
    }
    catch (Exception e) {
      throw ExceptionHelper.wrapServiceException(e);
    }
  }

  public DocumentMerge getDocumentMerge() {
    return documentMerge;
  }

  /**
   * Specify how to merge the exception into the AdaptrisMessage.
   *
   * @param m the merge implementation, if not specified defaults to {@link ReplaceOriginal}
   */
  public void setDocumentMerge(DocumentMerge m) {
    documentMerge = m;
  }

  private DocumentMerge documentMerge() {
    return ObjectUtils.defaultIfNull(getDocumentMerge(), new ReplaceOriginal());
  }

  public ExceptionReportGenerator getExceptionGenerator() {
    return exceptionGenerator;
  }

  /**
   * Specify how to create the XML document from the exception.
   *
   * @param generator the generator, if not specified defaults to {@link SimpleExceptionReport}.
   */
  public void setExceptionGenerator(ExceptionReportGenerator generator) {
    exceptionGenerator = generator;
  }

  private ExceptionReportGenerator exceptionGenerator() {
    return ObjectUtils.defaultIfNull(getExceptionGenerator(), new SimpleExceptionReport());
  }

  public String getXmlEncoding() {
    return xmlEncoding;
  }

  /**
   * Set the encoding for the resulting XML document.
   *
   * @param encoding the encoding, default is UTF-8
   */
  public void setXmlEncoding(String encoding) {
    xmlEncoding = encoding;
  }

  public DocumentBuilderFactoryBuilder getXmlDocumentFactoryConfig() {
    return xmlDocumentFactoryConfig;
  }

  public void setXmlDocumentFactoryConfig(DocumentBuilderFactoryBuilder xml) {
    this.xmlDocumentFactoryConfig = xml;
  }


  public Boolean getIgnoreXmlParseExceptions() {
    return ignoreXmlParseExceptions;
  }

  /**
   * Whether or not to ignore exceptions parsing the {@code AdaptrisMessage}.
   * <p>
   * In some situations you might have an empty payload (such as when the workflow is fired by an HTTP GET request); but you want to
   * report the exception as XML using {@link ReplaceOriginal} as the {@link DocumentMerge} implementation. If that is the case,
   * then you should set this value to be true. It defaults to false to preserve backwards compatibility.
   * </p>
   * 
   * @param b true to ignore parse exceptions (default is false if not specified.
   */
  public void setIgnoreXmlParseExceptions(Boolean b) {
    this.ignoreXmlParseExceptions = b;
  }
  
  private boolean ignoreXmlParseExceptions() {
    return BooleanUtils.toBooleanDefaultIfNull(getIgnoreXmlParseExceptions(), false);
  }
  
  private DocumentBuilderFactoryBuilder documentFactoryBuilder() {
    return DocumentBuilderFactoryBuilder.newInstance(getXmlDocumentFactoryConfig());
  }


}
