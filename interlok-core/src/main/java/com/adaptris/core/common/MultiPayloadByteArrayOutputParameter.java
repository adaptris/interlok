package com.adaptris.core.common;

import com.adaptris.core.MultiPayloadAdaptrisMessage;
import com.adaptris.interlok.InterlokException;
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
public class MultiPayloadByteArrayOutputParameter implements MultiPayloadDataOutputParameter<byte[]>
{
	private String payloadId;

	/**
	 * {@inheritDoc}.
	 */
	@Override
	public String getPayloadId()
	{
		return payloadId;
	}

	/**
	 * {@inheritDoc}.
	 */
	@Override
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
			insert(data, getPayloadId(), (MultiPayloadAdaptrisMessage)m);
		}
		else
		{
			throw new InterlokException("Cannot insert payload into message type " + m.getClass().getName() + " as it does not support multiple payloads.");
		}
	}

	/**
	 * {@inheritDoc}.
	 */
	@Override
	public void insert(byte[] data, String id, MultiPayloadAdaptrisMessage m)
	{
		m.addPayload(id != null ? id : m.getCurrentPayloadId(), data);
	}
}
