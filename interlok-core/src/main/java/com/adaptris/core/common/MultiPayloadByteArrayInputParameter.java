package com.adaptris.core.common;

import com.adaptris.core.MultiPayloadAdaptrisMessage;
import com.adaptris.interlok.InterlokException;
import com.adaptris.interlok.types.InterlokMessage;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * This {@code MultiPayloadDataInputParameter} is used when you want to
 * source data from the {@link com.adaptris.core.MultiPayloadAdaptrisMessage}
 * payload.
 *
 * @author andersonam
 * @config multi-payload-byte-array-input-parameter
 */
@XStreamAlias("multi-payload-byte-array-input-parameter")
public class MultiPayloadByteArrayInputParameter implements MultiPayloadDataInputParameter<byte[]>
{
	private String payloadId;

	@Override
	public String getPayloadId()
	{
		return payloadId;
	}

	@Override
	public void setPayloadId(String payloadId)
	{
		this.payloadId = payloadId;
	}

	/**
	 * {@inheritDoc}.
	 */
	@Override
	public byte[] extract(InterlokMessage m) throws InterlokException
	{
		if (m instanceof MultiPayloadAdaptrisMessage)
		{
			return extract(payloadId, (MultiPayloadAdaptrisMessage)m);
		}
		throw new InterlokException("Cannot extract payload from message type " + m.getClass().getName() + " as it does not support multiple payloads.");
	}

	/**
	 * Extract the message payload, for the given ID, as a byte[].
	 *
	 * @param id The payload ID to extract.
	 * @param m The message to extract the payload from.
	 *
	 * @return The message payload.
	 */
	@Override
	public byte[] extract(String id, MultiPayloadAdaptrisMessage m)
	{
		return m.getPayload(id != null ? id : m.getCurrentPayloadId());
	}
}
