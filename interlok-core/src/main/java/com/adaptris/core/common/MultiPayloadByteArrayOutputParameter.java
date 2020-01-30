package com.adaptris.core.common;

import com.adaptris.annotation.InputFieldHint;
import com.adaptris.core.MultiPayloadAdaptrisMessage;
import com.adaptris.interlok.InterlokException;
import com.adaptris.interlok.config.DataOutputParameter;
import com.adaptris.interlok.types.InterlokMessage;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * This {@code MultiPayloadDataOutputParameter} is used when you want to
 * insert data into the {@link MultiPayloadAdaptrisMessage} payload.
 *
 * @author andersonam
 * @config multi-payload-byte-array-output-parameter
 */
@XStreamAlias("multi-payload-byte-array-output-parameter")
public class MultiPayloadByteArrayOutputParameter implements DataOutputParameter<byte[]>
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
  public void insert(byte[] data, InterlokMessage m) throws InterlokException
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
  public void insert(byte[] data, String id, MultiPayloadAdaptrisMessage m)
  {
    m.addPayload(id != null ? id : m.getCurrentPayloadId(), data);
  }
}
