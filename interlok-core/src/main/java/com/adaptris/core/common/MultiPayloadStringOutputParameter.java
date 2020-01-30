package com.adaptris.core.common;

import com.adaptris.annotation.InputFieldHint;
import com.adaptris.core.MultiPayloadAdaptrisMessage;
import com.adaptris.interlok.InterlokException;
import com.adaptris.interlok.config.DataOutputParameter;
import com.adaptris.interlok.types.InterlokMessage;
import com.thoughtworks.xstream.annotations.XStreamAlias;

import static org.apache.commons.lang3.StringUtils.defaultIfEmpty;

/**
 * This {@code MultiPayloadDataOutputParameter} is used when you want to
 * insert data into the {@link MultiPayloadAdaptrisMessage} payload.
 *
 * @author andersonam
 * @config multi-payload-string-output-parameter
 */
@XStreamAlias("multi-payload-string-output-parameter")
public class MultiPayloadStringOutputParameter extends StringPayloadDataOutputParameter implements DataOutputParameter<String>
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
  public void insert(String data, InterlokMessage m) throws InterlokException
  {
    if (m instanceof MultiPayloadAdaptrisMessage)
    {
      insert(data, m.resolve(getPayloadId()), (MultiPayloadAdaptrisMessage)m);
    }
    else
    {
      throw new InterlokException("Cannot insert payload into message type " + m.getClass().getName() + " as it does not support multiple payloads.");
    }
  }

  /**
   * Insert the data into the multi-payload message for the given payload ID.
   *
   * @param data
   *          The data to insert.
   * @param id
   *          The payload ID.
   * @param m
   *          The multi-payload message.
   */
  public void insert(String data, String id, MultiPayloadAdaptrisMessage m)
  {
    m.setContent(id != null ? id : m.getCurrentPayloadId(), data, defaultIfEmpty(getContentEncoding(), m.getContentEncoding()));
  }
}
