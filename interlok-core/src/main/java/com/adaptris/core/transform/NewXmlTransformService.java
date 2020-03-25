/*
 * Copyright 2020 Adaptris Ltd.
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

package com.adaptris.core.transform;

import com.adaptris.annotation.AdapterComponent;
import com.adaptris.annotation.ComponentProfile;
import com.adaptris.annotation.DisplayOrder;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.MultiPayloadAdaptrisMessage;
import com.adaptris.core.ServiceException;
import com.adaptris.util.text.xml.XmlTransformer;
import com.adaptris.util.text.xml.XmlTransformerFactory;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import org.apache.commons.lang3.StringUtils;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.xml.transform.Transformer;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;

import static org.apache.commons.lang3.StringUtils.isEmpty;

/**
 * Implementation of <code>Service</code> which provides transformation
 * of XML payloads contained within the new multi-payload message.
 *
 * You are required to configure the XML transformer factory; see the
 * javadoc and implementations of {@link XmlTransformerFactory} for
 * details on the supported transformer factories.
 *
 * Configuration including allow over-ride behaviour matches previous
 * implementation.
 *
 * @author aanderson
 * @config new-xml-transform-service
 * 
 */
@XStreamAlias("new-xml-transform-service")
@AdapterComponent
@ComponentProfile(summary = "Execute an XSLT transform", tag = "service,transform,xml,multi,payload,multi-payload")
@DisplayOrder(order = { "sourcePayloadId", "targetPayloadId", "url", "outputMessageEncoding", "cacheTransforms", "allowOverride", "metadataKey", "transformParameter", "xmlTransformerFactory" })
public class NewXmlTransformService extends XmlTransformService {

  @NotNull
  @Valid
  private String sourcePayloadId;

  @NotNull
  @Valid
  private String outputPayloadId;

  /**
   * Creates a new instance. Defaults to caching transforms and not
   * allowing over-rides. Default metadata key is
   * <code>transformurl</code>.
   */
  public NewXmlTransformService() {
    super();
  }

  /**
   * @see com.adaptris.core.Service#doService (com.adaptris.core.AdaptrisMessage)
   */
  @Override
  public void doService(AdaptrisMessage msg) throws ServiceException {
    if (msg instanceof MultiPayloadAdaptrisMessage) {
      this.doTransform((MultiPayloadAdaptrisMessage)msg, obtainUrlToUse(msg));
    } else {
      super.doService(msg);
    }
  }

  private void doTransform(MultiPayloadAdaptrisMessage msg, String urlToUse) throws ServiceException {
    XmlTransformer xmlTransformerImpl = new XmlTransformer();
    Transformer transformer;
    try {
      if (cacheTransforms()) {
        transformer = cacheAndGetTransformer(urlToUse, getXmlTransformerFactory());
      } else {
        transformer = getXmlTransformerFactory().createTransformer(urlToUse);
      }
      getXmlTransformerFactory().configure(xmlTransformerImpl);
    } catch (Exception ex) {
      throw new ServiceException(ex);
    }
    // INTERLOK-2022 Let the XML parser do its thing, rather than using a reader/writer.

    /*
     * for new-xml-transform-service: allow data-input-parameters for
     * payload streams and possibly in place of urlToUse (and maybe
     * even parameters)
     */

    try (InputStream input = msg.getInputStream(sourcePayloadId); OutputStream output = msg.getOutputStream(outputPayloadId)) {
      Map<Object, Object> parameters = getParameterBuilder().createParameters(msg, null);
      xmlTransformerImpl.transform(transformer, input, output, urlToUse, parameters);
      if (!StringUtils.isBlank(getOutputMessageEncoding())) {
        msg.setContentEncoding(outputPayloadId, getOutputMessageEncoding());
      }
    } catch (Exception e) {
      throw new ServiceException("Failed to transform message", e);
    }
  }

  public String getSourcePayloadId() {
    return sourcePayloadId;
  }

  public void setSourcePayloadId(String sourcePayloadId) {
    if (isEmpty(sourcePayloadId)) {
      throw new IllegalArgumentException("Source payload ID is required");
    }
    this.sourcePayloadId = sourcePayloadId;
  }

  public String getOutputPayloadId() {
    return outputPayloadId;
  }

  public void setOutputPayloadId(String outputPayloadId) {
    if (isEmpty(outputPayloadId)) {
      throw new IllegalArgumentException("Output payload ID is required");
    }
    this.outputPayloadId = outputPayloadId;
  }
}
