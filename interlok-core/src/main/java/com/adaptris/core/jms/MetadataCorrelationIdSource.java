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

package com.adaptris.core.jms;

import static org.apache.commons.lang3.StringUtils.isEmpty;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.adaptris.annotation.DisplayOrder;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.util.Args;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * <p>
 * Implementation of <code>CorrelationIdSource</code> which uses the value stored against a configureable metadata key as the
 * <code>JMSCorrelationId
 * </code>.
 * </p>
 * 
 * @config metadata-correlation-id-source
 */
@JacksonXmlRootElement(localName = "metadata-correlation-id-source")
@XStreamAlias("metadata-correlation-id-source")
@DisplayOrder(order = {"metadataKey"})
public class MetadataCorrelationIdSource implements CorrelationIdSource {

  private transient Logger log = LoggerFactory.getLogger(this.getClass());

  @NotNull
  @NotBlank
  private String metadataKey;

  public MetadataCorrelationIdSource() {
  }

  public MetadataCorrelationIdSource(String key) {
    this();
    setMetadataKey(key);
  }

  /**
   * <p>
   * If no metadata key is configured or if no value is stored against the
   * configured key a message is logged to this effect and no Exception is
   * thrown.
   * </p>
   *
   * @see com.adaptris.core.jms.CorrelationIdSource#processCorrelationId
   *      (com.adaptris.core.AdaptrisMessage, javax.jms.Message)
   */
  @Override
  public void processCorrelationId(AdaptrisMessage src, Message dest) throws JMSException {
    String correlationId = src.getMetadataValue(getMetadataKey());
    if (isEmpty(correlationId)) {
      log.warn("no value for metadata key [{}]", getMetadataKey());
    }
    else {
      dest.setJMSCorrelationID(correlationId);
      log.debug("set correlation ID to [{}]", correlationId);
    }
  }

  @Override
  public void processCorrelationId(Message src, AdaptrisMessage dest) throws JMSException {
    try {
      Args.notBlank(getMetadataKey(), "metadata-key");
      String id = src.getJMSCorrelationID();
      if (isEmpty(id)) {
        log.warn("No Correlation Id available");
      } else {
        dest.addMetadata(getMetadataKey(), id);
        log.debug("Set metadata key [{}] to [{}]", getMetadataKey(), id);
      }
    } catch (IllegalArgumentException e) {
      // do nothing as it's probably because getMtadataKey() was null.
    }
  }

  /**
   * <p>
   * Returns the metadata key that should be used to obtain the
   * JMSCorrelationId.
   * </p>
   *
   * @return metadataKey the metadata key that should be used to obtain the
   *         JMSCorrelationId
   */
  public String getMetadataKey() {
    return metadataKey;
  }

  /**
   * <p>
   * Sets the metadata key that should be used to obtain the JMSCorrelationId.
   * </p>
   *
   * @param s the metadata key that should be used to obtain the
   *          JMSCorrelationId
   */
  public void setMetadataKey(String s) {
    metadataKey = Args.notBlank(s, "metadata-key");
  }

}
