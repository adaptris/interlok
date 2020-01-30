package com.adaptris.core.common;

import com.adaptris.annotation.InputFieldHint;
import com.adaptris.core.MultiPayloadAdaptrisMessage;
import com.adaptris.interlok.InterlokException;
import com.adaptris.interlok.config.DataInputParameter;
import com.adaptris.interlok.types.InterlokMessage;
import com.thoughtworks.xstream.annotations.XStreamAlias;

import java.io.InputStream;

/**
 * This {@code MultiPayloadDataInputParameter} is used when you want to
 * source data from the {@link com.adaptris.core.MultiPayloadAdaptrisMessage}
 * payload.
 *
 * @author andersonam
 * @config multi-payload-stream-input-parameter
 */
@XStreamAlias("multi-payload-stream-input-parameter")
public class MultiPayloadStreamInputParameter extends PayloadStreamInputParameter implements DataInputParameter<InputStream>
{
  @InputFieldHint(expression=true)
  private String payloadId;

  /**
   * Get the ID of the payload to extract.
   *
   * @return  The payload ID.
   */
  public String getPayloadId()
  {
    return payloadId;
  }

  /**
   * Set the ID of the payload to extract.
   *
   * @param payloadId
   *          The payload ID.
   */
  public void setPayloadId(String payloadId)
  {
    this.payloadId = payloadId;
  }

  /**
   * {@inheritDoc}.
   */
  @Override
  public InputStream extract(InterlokMessage m) throws InterlokException
  {
    if (m instanceof MultiPayloadAdaptrisMessage)
    {
      return extract(m.resolve(getPayloadId()), (MultiPayloadAdaptrisMessage)m);
    }
    throw new InterlokException("Cannot extract payload from message type " + m.getClass().getName() + " as it does not support multiple payloads.");
  }

  /**
   * Extract the payload with the given ID from the multi-payload message.
   *
   * @param id
   *          The payload ID.
   * @param m
   *          The multi-payload message.
   *
   * @return  The extracted payload.
   */
  public InputStream extract(String id, MultiPayloadAdaptrisMessage m)
  {
    return m.getInputStream(id != null ? id : m.getCurrentPayloadId());
  }
}
