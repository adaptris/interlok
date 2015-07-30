package com.adaptris.core.jms;

import static org.apache.commons.lang.StringUtils.isEmpty;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.NotBlank;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.adaptris.core.AdaptrisMessage;
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
@XStreamAlias("metadata-correlation-id-source")
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
  public void processCorrelationId(AdaptrisMessage src, Message dest) throws JMSException {
    if (isEmpty(getMetadataKey())) {
      log.warn("metadata key for correlation ID not configured");
    }
    else {
      String correlationId = src.getMetadataValue(getMetadataKey());
      if (isEmpty(correlationId)) {
        log.warn("no value for metadata key [" + getMetadataKey() + "]");
      }
      else {
        dest.setJMSCorrelationID(correlationId);
        log.debug("set correlation ID to [" + correlationId + "]");
      }
    }
  }

  public void processCorrelationId(Message src, AdaptrisMessage dest) throws JMSException {
    if (isEmpty(getMetadataKey())) {
      log.warn("Metadata key for correlation ID not configured");
    }
    else {
      String id = src.getJMSCorrelationID();
      if (isEmpty(id)) {
        log.warn("No Correlation Id available");
      }
      else {
        dest.addMetadata(getMetadataKey(), id);
        log.debug("Set metadata key [" + getMetadataKey() + "] to [" + id + "]");
      }
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
    if (isEmpty(s)) {
      throw new IllegalArgumentException("null/empty metadata key param");
    }
    metadataKey = s;
  }

}
