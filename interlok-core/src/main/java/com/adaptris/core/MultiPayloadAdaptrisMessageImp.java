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
		addContent(payloadId, content, encoding != null ? encoding.toString() : null);
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
	public boolean equivalentForTracking(AdaptrisMessage o)
	{
		if (!(o instanceof MultiPayloadAdaptrisMessage))
		{
			return false;
		}
		MultiPayloadAdaptrisMessage other = (MultiPayloadAdaptrisMessage)o;
		if (!StringUtils.equals(getUniqueId(), other.getUniqueId()))
		{
			return false;
		}
		if (!getMetadata().equals(other.getMetadata()))
		{
			return false;
		}
		if (getPayloadCount() != other.getPayloadCount())
		{
			return false;
		}
		for (String id : payloads.keySet())
		{
			if (!other.hasPayloadId(id))
			{
				return false;
			}
			else if (!Arrays.equals(getPayload(id), other.getPayload(id)))
			{
				return false;
			}
			else if (!StringUtils.equals(getContentEncoding(id), other.getContentEncoding(id)))
			{
				return false;
			}
		}
		return true;
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
		byte[] pb;
		if (bytes == null)
		{
			pb = new byte[0];
		}
		else
		{
			pb = bytes;
		}
		Payload payload;
		if (payloads.containsKey(payloadId))
		{
			payload = payloads.get(payloadId);
			payload.data = pb;
		}
		else
		{
			payload = new Payload(pb);
		}
		payloads.put(payloadId, payload);
		currentPayloadId = payloadId;
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
		return payloads.get(payloadId).data;
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
		return getPayload(payloadId).length;
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
		}
		else
		{
			payload = new byte[0];
		}
		setContentEncoding(payloadId, charEnc);
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
		if (isEmpty(getContentEncoding()))
		{
			return new String(payload);
		}
		else
		{
			return new String(payload, Charset.forName(getContentEncoding()));
		}
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
		Payload payload;
		if (payloads.containsKey(payloadId))
		{
			payload = payloads.get(payloadId);
			payload.encoding = contentEncoding;
		}
		else
		{
			payload = new Payload(contentEncoding, new byte[0]);
		}
		payloads.put(payloadId, payload);
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
			result.payloads = new HashMap<>();
			for (String payloadId : payloads.keySet())
			{
				Payload payload = payloads.get(payloadId);
				result.addPayload(payloadId, payload.data.clone());
				result.setContentEncoding(payloadId, payload.encoding);
			}
		}
		catch (Exception e)
		{
			throw new RuntimeException(e);
		}
		result.switchPayload(currentPayloadId);
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
		return new ByteArrayInputStream(getPayload(payloadId));
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
			addPayload(payloadId, ((ByteArrayOutputStream)super.out).toByteArray());
		}
	}

	private class Payload
	{
		public String encoding;
		@NotNull public byte[] data;

		public Payload(String encoding, @NotNull byte[] data)
		{
			this.encoding = encoding;
			this.data = data;
		}

		public Payload(@NotNull byte[] data)
		{
			this.data = data;
		}
	}
}
