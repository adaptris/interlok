package com.adaptris.core;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.adaptris.annotation.AdapterComponent;
import com.adaptris.annotation.AdvancedConfig;
import com.adaptris.annotation.ComponentProfile;
import com.adaptris.interlok.config.DataInputParameter;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Add a new payload to the message.
 *
 * @author aanderson
 * @since 3.9.x
 */
@XStreamAlias("add-payload-service")
@AdapterComponent
@ComponentProfile(summary = "Add a new payload to a multi-payload message", tag = "multi-payload,multi,payload,add")
public class AddPayloadService extends ServiceImp {
  private transient Logger log = LoggerFactory.getLogger(AddPayloadService.class.getName());

  @Valid
  @NotNull
  private String newPayloadId;

  @Valid
  @NotNull
  private DataInputParameter<String> newPayload;

  @Valid
  @AdvancedConfig
  private String newPayloadEncoding;

  /**
   * Set the ID of the payload to add.
   *
   * @param newPayloadId
   *          The new payload ID.
   */
  public void setNewPayloadId(String newPayloadId) {
    this.newPayloadId = newPayloadId;
  }

  /**
   * Get the ID of the payload to add.
   *
   * @return The new payload ID.
   */
  public String getNewPayloadId() {
    return newPayloadId;
  }

  /**
   * Set the source of the new payload.
   *
   * @param newPayload
   *          The new payload.
   */
  public void setNewPayload(DataInputParameter<String> newPayload) {
    this.newPayload = newPayload;
  }

  /**
   * Get the source of the new payload.
   *
   * @return The new payload.
   */
  public DataInputParameter<String> getNewPayload() {
    return newPayload;
  }

  /**
   * Set the encoding for the new payload, if it's a String.
   *
   * @param newPayloadEncoding
   *          The payload encoding.
   */
  public void setNewPayloadEncoding(String newPayloadEncoding) {
    this.newPayloadEncoding = newPayloadEncoding;
  }

  /**
   * Get the encoding for the new payload.
   *
   * @return The payload encoding.
   */
  public String getNewPayloadEncoding() {
    return newPayloadEncoding;
  }

  /**
   * Add a new payload to the message. {@inheritDoc}.
   *
   * @param msg
   *          The message to which the payload should be added.
   */
  @Override
  public void doService(AdaptrisMessage msg) throws ServiceException {
    log.debug("Attempting to add payload " + newPayloadId + " to message");
    if (!(msg instanceof MultiPayloadAdaptrisMessage)) {
      throw new ServiceException("Message [" + msg.getUniqueId() + "] is not a multi-payload message");
    }
    MultiPayloadAdaptrisMessage message = (MultiPayloadAdaptrisMessage) msg;
    String payload;
    try {
      payload = getNewPayload().extract(message);
    } catch (Exception e) {
      log.error("Could not extract new payload from source", e);
      throw new ServiceException(e);
    }
    if (getNewPayloadEncoding() == null) {
      setNewPayloadEncoding(message.getContentEncoding());
    }
    message.addContent(getNewPayloadId(), payload, getNewPayloadEncoding());
    log.debug("Added message payload [" + getNewPayloadId() + "]");
  }

  /**
   * {@inheritDoc}.
   */
  @Override
  protected void initService() {
    /* unused */
  }

  /**
   * {@inheritDoc}.
   */
  @Override
  protected void closeService() {
    /* unused */
  }

  /**
   * {@inheritDoc}.
   */
  @Override
  public void prepare() {
    /* unused */
  }
}
