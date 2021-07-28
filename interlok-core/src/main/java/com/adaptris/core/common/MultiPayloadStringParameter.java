package com.adaptris.core.common;

import com.adaptris.annotation.InputFieldHint;
import com.adaptris.core.MultiPayloadAdaptrisMessage;
import com.adaptris.interlok.InterlokException;
import com.adaptris.interlok.types.InterlokMessage;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import lombok.Getter;
import lombok.Setter;

import static org.apache.commons.lang3.StringUtils.defaultIfEmpty;

/**
 * This {@code MultiPayloadDataParameter} is used when you want to
 * source data from the {@link MultiPayloadAdaptrisMessage}
 * payload and return the result there.
 *
 * An example might be specifying that the XML content required for the
 * {@link com.adaptris.core.services.path.XPathService} can be found in
 * the payload of an {@link MultiPayloadAdaptrisMessage}.
 *
 * @author andersonam
 * @config multi-payload-string-parameter
 */
@XStreamAlias("multi-payload-string-parameter")
public class MultiPayloadStringParameter extends StringPayloadDataParameter
{
  @Getter
  @Setter
  @InputFieldHint(expression=true)
  private String payloadId;

  /**
   * {@inheritDoc}.
   */
  @Override
  public String extract(InterlokMessage m) throws InterlokException
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
  public String extract(String id, MultiPayloadAdaptrisMessage m)
  {
    return m.getContent(id != null ? id : m.getCurrentPayloadId());
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
