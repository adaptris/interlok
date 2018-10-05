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

package com.adaptris.core.services.exception;

import static com.adaptris.core.CoreConstants.OBJ_METADATA_EXCEPTION;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import com.adaptris.annotation.AdapterComponent;
import com.adaptris.annotation.AdvancedConfig;
import com.adaptris.annotation.ComponentProfile;
import com.adaptris.annotation.DisplayOrder;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.CoreException;
import com.adaptris.core.ServiceException;
import com.adaptris.core.ServiceImp;
import com.adaptris.core.util.Args;
import com.adaptris.core.util.DocumentBuilderFactoryBuilder;
import com.adaptris.core.util.ExceptionHelper;
import com.adaptris.util.text.xml.DocumentMerge;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Service that takes an exception in object metadata and serializes that into the AdaptrisMessage payload.
 * 
 * <p>
 * In some scenarios (e.g. handling request reply scenarios), rather than directly handling the message exception in the adapter, it
 * may be required to report back to the back-end application that an exception has occured along with the problem document.
 * </p>
 * 
 * @config exception-report-service
 * 
 * @see ExceptionSerializer
 * @author lchan
 * @see com.adaptris.core.CoreConstants#OBJ_METADATA_EXCEPTION
 */
@XStreamAlias("exception-report-service")
@AdapterComponent
@ComponentProfile(summary = "Generate a report based on the current exception", tag = "service,error-handling")
@DisplayOrder(order = {"exceptionSerializer"})
public class ExceptionReportService extends ServiceImp {

  @Valid
  @Deprecated
  @AdvancedConfig
  private DocumentMerge documentMerge;
  @Valid
  @Deprecated
  @AdvancedConfig
  private ExceptionReportGenerator exceptionGenerator;
  @Deprecated
  @AdvancedConfig
  private String xmlEncoding;
  @AdvancedConfig
  @Deprecated
  private DocumentBuilderFactoryBuilder xmlDocumentFactoryConfig;

  @Valid
  @NotNull
  private ExceptionSerializer exceptionSerializer;

  public ExceptionReportService() {
  }

  public ExceptionReportService(ExceptionSerializer e) {
    this();
    setExceptionSerializer(e);
  }

  public void doService(AdaptrisMessage msg) throws ServiceException {
    try {
      if (msg.getObjectHeaders().containsKey(OBJ_METADATA_EXCEPTION)) {
        Exception e = (Exception) msg.getObjectHeaders().get(OBJ_METADATA_EXCEPTION);
        getExceptionSerializer().serialize(e, msg);
      }
      else {
        log.debug("No Exception in object metadata, nothing to do.");
      }
    }
    catch (Exception e) {
      throw ExceptionHelper.wrapServiceException(e);
    }
  }

  @Override
  protected void initService() throws CoreException {
  }

  @Override
  protected void closeService() {
  }

  @Override
  public void prepare() throws CoreException {
    if (getExceptionSerializer() == null && getExceptionGenerator() != null) {
      log.warn("exception-generator, document-merge, xml-encoding all deprecated; use a exception-serializer instead");
      setExceptionSerializer(new ExceptionAsXml().withXmlEncoding(getXmlEncoding()).withDocumentMerge(getDocumentMerge())
          .withExceptionGenerator(getExceptionGenerator()).withDocumentFactoryConfig(getXmlDocumentFactoryConfig()));
    }
    try {
      Args.notNull(getExceptionSerializer(), "exceptionSerializer");
    }
    catch (IllegalArgumentException e) {
      throw ExceptionHelper.wrapCoreException(e);
    }
  }

  public ExceptionSerializer getExceptionSerializer() {
    return exceptionSerializer;
  }

  public void setExceptionSerializer(ExceptionSerializer exceptionSerializer) {
    this.exceptionSerializer = exceptionSerializer;
  }

  @Deprecated
  public DocumentMerge getDocumentMerge() {
    return documentMerge;
  }

  /**
   * Specify how to merge the exception into the AdaptrisMessage.
   *
   * @param m the merge implementation
   * @deprecated since 3.6.4 use a {@link ExceptionSerializer} instead.
   */
  @Deprecated
  public void setDocumentMerge(DocumentMerge m) {
    documentMerge = m;
  }

  @Deprecated
  public ExceptionReportGenerator getExceptionGenerator() {
    return exceptionGenerator;
  }

  /**
   * Specify how to create the XML document from the exception.
   *
   * @param generator the generator.
   * @deprecated since 3.6.4 use a {@link ExceptionSerializer} instead.
   */
  @Deprecated
  public void setExceptionGenerator(ExceptionReportGenerator generator) {
    exceptionGenerator = generator;
  }

  @Deprecated
  public String getXmlEncoding() {
    return xmlEncoding;
  }

  /**
   * Set the encoding for the resulting XML document.
   *
   * @param encoding the encoding, default is UTF-8
   * @deprecated since 3.6.4 use a {@link ExceptionSerializer} instead.
   */
  @Deprecated
  public void setXmlEncoding(String encoding) {
    xmlEncoding = encoding;
  }

  @Deprecated
  public DocumentBuilderFactoryBuilder getXmlDocumentFactoryConfig() {
    return xmlDocumentFactoryConfig;
  }


  /**
   * 
   * @deprecated since 3.6.4 use a {@link ExceptionSerializer} instead.
   */
  @Deprecated
  public void setXmlDocumentFactoryConfig(DocumentBuilderFactoryBuilder xml) {
    this.xmlDocumentFactoryConfig = xml;
  }

}
