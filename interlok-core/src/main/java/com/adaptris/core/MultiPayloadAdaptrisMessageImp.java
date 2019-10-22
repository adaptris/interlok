/*
 * Copyright 2019 Adaptris Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.adaptris.core;

import com.adaptris.util.IdGenerator;
import org.apache.commons.lang3.StringUtils;

import javax.validation.constraints.NotNull;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static org.apache.commons.lang3.StringUtils.isEmpty;

/**
 * The standard implementation of multi-payload messages; {@link MultiPayloadAdaptrisMessage} implementation created by
 * {@link MultiPayloadMessageFactory}.
 *
 * @author aanderson
 * @see MultiPayloadAdaptrisMessage
 * @see MultiPayloadMessageFactory
 * @see AdaptrisMessageImp
 * @since 3.9.x
 */
public class MultiPayloadAdaptrisMessageImp extends AdaptrisMessageImp implements MultiPayloadAdaptrisMessage
{
	private Map<String, Payload> payloads = new HashMap<>();

	@NotNull
	private String currentPayloadId = DEFAULT_PAYLOAD_ID;

	protected MultiPayloadAdaptrisMessageImp(IdGenerator guid, AdaptrisMessageFactory messageFactory)
	{
		this(DEFAULT_PAYLOAD_ID, guid, messageFactory);
	}

	protected MultiPayloadAdaptrisMessageImp(@NotNull String payloadId, IdGenerator guid, AdaptrisMessageFactory messageFactory)
	{
		this(payloadId, guid, messageFactory, new byte[0]);
	}

	protected MultiPayloadAdaptrisMessageImp(@NotNull String payloadId, IdGenerator guid, AdaptrisMessageFactory messageFactory, byte[] payload)
	{
		super(guid, messageFactory);
		addPayload(payloadId, payload);
	}

	protected MultiPayloadAdaptrisMessageImp(@NotNull String payloadId, IdGenerator guid, AdaptrisMessageFactory messageFactory, String content, Charset encoding)
	{
		super(guid, messageFactory);
		addContent(payloadId, content, encoding.toString());
	}

	/**
	 * {@inheritDoc}.
	 */
	@Override
	public void switchPayload(@NotNull String payloadId)
	{
		currentPayloadId = payloadId;
	}

	/**
	 * {@inheritDoc}.
	 */
	@Override
	public boolean hasPayloadId(@NotNull String payloadId)
	{
		return payloads.containsKey(payloadId);
	}

	/**
	 * {@inheritDoc}.
	 */
	@Override
	public void setCurrentPayloadId(@NotNull String payloadId)
	{
		Payload payload = payloads.remove(currentPayloadId);
		currentPayloadId = payloadId;
		payloads.put(currentPayloadId, payload);
	}

	/**
	 * {@inheritDoc}.
	 */
	@Override
	public String getCurrentPayloadId()
	{
		return currentPayloadId;
	}

	/**
	 * @see AdaptrisMessage#equivalentForTracking (com.adaptris.core.AdaptrisMessage).
	 */
	@Override
	public boolean equivalentForTracking(AdaptrisMessage other)
	{
		boolean result = false;
		if (StringUtils.equals(getUniqueId(), other.getUniqueId()))
		{
			if (Arrays.equals(getPayload(), other.getPayload()))
			{
				if (StringUtils.equals(getContentEncoding(), other.getContentEncoding()))
				{
					if (getMetadata().equals(other.getMetadata()))
					{
						result = true;
					}
				}
			}
		}
		return result;
	}

	/**
	 * Set the current payload data.
	 *
	 * @param bytes The payload data.
	 * @see AdaptrisMessage#setPayload(byte[])
	 */
	@Override
	public void setPayload(byte[] bytes)
	{
		addPayload(currentPayloadId, bytes);
	}

	/**
	 * {@inheritDoc}.
	 */
	@Override
	public void addPayload(@NotNull String payloadId, byte[] bytes)
	{
		byte[] payload;
		if (bytes == null)
		{
			payload = new byte[0];
		}
		else
		{
			payload = bytes;
		}
		payloads.put(payloadId, new Payload(payload));
	}

	/**
	 * {@inheritDoc}.
	 */
	@Override
	public void deletePayload(@NotNull String payloadId)
	{
		payloads.remove(payloadId);
	}

	/**
	 * Get the current payload data.
	 *
	 * @return The payload data.
	 * @see AdaptrisMessage#getPayload()
	 */
	@Override
	public byte[] getPayload()
	{
		return getPayload(currentPayloadId);
	}

	/**
	 * {@inheritDoc}.
	 */
	@Override
	public byte[] getPayload(@NotNull String payloadId)
	{
		byte[] result = null;
		byte[] payload = payloads.get(payloadId).data;
		if (payload != null)
		{
			result = new byte[payload.length];
			System.arraycopy(payload, 0, result, 0, payload.length);
		}
		return result;
	}

	/**
	 * Get the current payload size.
	 *
	 * @return The payload size.
	 * @see AdaptrisMessage#getSize()
	 */
	@Override
	public long getSize()
	{
		return getSize(currentPayloadId);
	}

	/**
	 * {@inheritDoc}.
	 */
	public int getPayloadCount()
	{
		return payloads.size();
	}

	/**
	 * {@inheritDoc}.
	 */
	@Override
	public long getSize(@NotNull String payloadId)
	{
		byte[] payload = payloads.get(payloadId).data;
		return payload != null ? payload.length : 0;
	}

	/**
	 * Set the current payload content.
	 *
	 * @param payloadString The payload content.
	 * @param charEnc       The content encoding.
	 * @see AdaptrisMessage#setContent(String, String)
	 */
	@Override
	public void setContent(String payloadString, String charEnc)
	{
		addContent(currentPayloadId, payloadString, charEnc);
	}

	/**
	 * {@inheritDoc}.
	 */
	@Override
	public void addContent(@NotNull String payloadId, String payloadString)
	{
		addContent(payloadId, payloadString, null);
	}

	/**
	 * {@inheritDoc}.
	 */
	@Override
	public void addContent(@NotNull String payloadId, String payloadString, String charEnc)
	{
		byte[] payload;
		if (payloadString != null)
		{
			Charset charset = Charset.forName(StringUtils.defaultIfBlank(charEnc, Charset.defaultCharset().name()));
			payload = payloadString.getBytes(charset);
			setContentEncoding(payloadId, charEnc);
		}
		else
		{
			payload = new byte[0];
			setContentEncoding(payloadId, charEnc);
		}
		addPayload(payloadId, payload);
	}

	/**
	 * Get the current payload content.
	 *
	 * @return The payload content.
	 * @see AdaptrisMessage#getContent()
	 */
	@Override
	public String getContent()
	{
		return getContent(currentPayloadId);
	}

	/**
	 * {@inheritDoc}.
	 */
	@Override
	public String getContent(@NotNull String payloadId)
	{
		byte[] payload = getPayload(payloadId);
		if (payload != null)
		{
			if (isEmpty(getContentEncoding()))
			{
				return new String(payload);
			}
			else
			{
				return new String(payload, Charset.forName(getContentEncoding()));
			}
		}
		return null;
	}

	/**
	 * {@inheritDoc}.
	 */
	@Override
	public void setContentEncoding(String enc)
	{
		setContentEncoding(currentPayloadId, enc);
	}

	/**
	 * {@inheritDoc}.
	 */
	@Override
	public void setContentEncoding(@NotNull String payloadId, String enc)
	{
		String contentEncoding = enc != null ? Charset.forName(enc).name() : null;
		payloads.get(payloadId).encoding = contentEncoding;
	}

	/**
	 * {@inheritDoc}.
	 */
	@Override
	public String getContentEncoding()
	{
		return getContentEncoding(currentPayloadId);
	}

	/**
	 * {@inheritDoc}.
	 */
	@Override
	public String getContentEncoding(@NotNull String payloadId)
	{
		return payloads.get(payloadId).encoding;
	}

	/**
	 * @see Object#clone()
	 */
	@Override
	public Object clone() throws CloneNotSupportedException
	{
		MultiPayloadAdaptrisMessageImp result = (MultiPayloadAdaptrisMessageImp)super.clone();
		// clone the payloads.
		try
		{
			for (String payloadId : payloads.keySet())
			{
				byte[] payload = payloads.get(payloadId).data;
				byte[] newPayload = new byte[payload.length];
				System.arraycopy(payload, 0, newPayload, 0, payload.length);
				result.addPayload(payloadId, newPayload);
			}
		}
		catch (Exception e)
		{
			throw new RuntimeException(e);
		}
		return result;
	}

	/**
	 * @see AdaptrisMessage#getInputStream()
	 */
	@Override
	public InputStream getInputStream()
	{
		return getInputStream(currentPayloadId);
	}

	/**
	 * {@inheritDoc}.
	 */
	@Override
	public InputStream getInputStream(@NotNull String payloadId)
	{
		byte[] payload = getPayload(payloadId);
		return payload != null ? new ByteArrayInputStream(payload) : new ByteArrayInputStream(new byte[0]);
	}

	/**
	 * @see AdaptrisMessage#getOutputStream()
	 */
	@Override
	public OutputStream getOutputStream()
	{
		return getOutputStream(currentPayloadId);
	}

	/**
	 * {@inheritDoc}.
	 */
	@Override
	public OutputStream getOutputStream(@NotNull String payloadId)
	{
		return new ByteFilterStream(payloadId, new ByteArrayOutputStream());
	}

	private class ByteFilterStream extends FilterOutputStream
	{
		private final String payloadId;

		ByteFilterStream(@NotNull String payloadId, OutputStream out)
		{
			super(out);
			this.payloadId = payloadId;
		}

		@Override
		public void close() throws IOException
		{
			super.close();
			byte[] payload = ((ByteArrayOutputStream)super.out).toByteArray();
			addPayload(payloadId, payload);
		}
	}

	private class Payload
	{
		public String encoding;
		public byte[] data;

		public Payload(String encoding, byte[] data)
		{
			this.encoding = encoding;
			this.data = data;
		}

		public Payload(byte[] data)
		{
			this.data = data;
		}
	}
}
