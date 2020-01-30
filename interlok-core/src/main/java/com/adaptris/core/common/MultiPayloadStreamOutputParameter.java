package com.adaptris.core.common;

import com.adaptris.annotation.InputFieldHint;
import com.adaptris.core.MultiPayloadAdaptrisMessage;
import com.adaptris.core.util.ExceptionHelper;
import com.adaptris.interlok.InterlokException;
import com.adaptris.interlok.config.DataOutputParameter;
import com.adaptris.interlok.types.InterlokMessage;
import com.thoughtworks.xstream.annotations.XStreamAlias;

import static com.adaptris.util.stream.StreamUtil.copyAndClose;
import static org.apache.commons.lang3.StringUtils.defaultIfEmpty;
import static org.apache.commons.lang3.StringUtils.isEmpty;

/**
 * This {@code MultiPayloadDataOutputParameter} is used when you want to
 * insert data into the {@link MultiPayloadAdaptrisMessage} payload.
 *
 * @author andersonam
 * @config multi-payload-stream-output-parameter
 */
@XStreamAlias("multi-payload-stream-output-parameter")
public class MultiPayloadStreamOutputParameter extends PayloadStreamOutputParameter implements DataOutputParameter<InputStreamWithEncoding>
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
  public void insert(InputStreamWithEncoding data, InterlokMessage m) throws InterlokException
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
  public void insert(InputStreamWithEncoding data, String id, MultiPayloadAdaptrisMessage m) throws InterlokException
  {
    if (id == null)
    {
      id = m.getCurrentPayloadId();
    }
    try
    {
      String encoding = defaultIfEmpty(getContentEncoding(), data.encoding);
      if (isEmpty(encoding))
      {
        copyAndClose(data.inputStream, m.getOutputStream(id));
      }
      else
      {
        copyAndClose(data.inputStream, m.getWriter(id, encoding));
        m.setContentEncoding(id, encoding);
      }
    }
    catch (Exception e)
    {
      throw ExceptionHelper.wrapCoreException(e);
    }
  }
}
