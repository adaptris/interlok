package com.adaptris.core;

import com.adaptris.annotation.AdapterComponent;
import com.adaptris.annotation.ComponentProfile;
import com.adaptris.annotation.InputFieldHint;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

/**
 * Switch the message payload from one payload to another.
 *
 * <pre>{@code
 * <switch-payload-service>
 *   <unique-id>switch-payload-unique-id</unique-id>
 *   <new-payload-id>payload-1</new-payload-id>
 * </switch-payload-service>
 * }</pre>
 *
 * @author aanderson
 * @since 3.9.x
 */
@JacksonXmlRootElement(localName = "switch-payload-service")
@XStreamAlias("switch-payload-service")
@AdapterComponent
@ComponentProfile(summary = "Switch the active payload of a multi-payload message", tag = "multi-payload,multi,payload,switch")
public class SwitchPayloadService extends ServiceImp {
  private transient Logger log = LoggerFactory.getLogger(SwitchPayloadService.class.getName());

  @Valid
  @NotNull
  @InputFieldHint(expression=true)
  private String newPayloadId;

  /**
   * Set the ID of the payload to switch to.
   *
   * @param newPayloadId
   *          The new payload ID.
   */
  public void setNewPayloadId(String newPayloadId) {
    this.newPayloadId = newPayloadId;
  }

  /**
   * Get the ID of the payload to switch to.
   *
   * @return The new payload ID.
   */
  public String getNewPayloadId() {
    return newPayloadId;
  }

  /**
   * Switch the message payload from one to another. {@inheritDoc}.
   *
   * @param msg
   *          The message whose payload to switch.
   */
  @Override
  public void doService(AdaptrisMessage msg) throws ServiceException {
    String id = msg.resolve(getNewPayloadId());
    log.debug("Attempting to switch message payload to [" + id + "]");
    if (!(msg instanceof MultiPayloadAdaptrisMessage)) {
      throw new ServiceException("Message [" + msg.getUniqueId() + "] is not a multi-payload message");
    }
    MultiPayloadAdaptrisMessage message = (MultiPayloadAdaptrisMessage) msg;
    log.debug("Switching message payload from [" + message.getCurrentPayloadId() + "]");
    message.switchPayload(id);
    log.debug("Switched message payload to [" + id + "]");
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
