/*
 * Copyright 2015 Adaptris Ltd.
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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static com.adaptris.core.metadata.MetadataResolver.resolveKey;
import static org.apache.commons.lang3.StringUtils.isEmpty;

/**
 * {@link AdaptrisMessage} implementation created by {@link DefaultMessageFactory}
 *
 * @author aanderson
 * @author $Author: aanderson $
 */
public class MultiPayloadAdaptrisMessageImp extends AdaptrisMessageImp
{
	static final String DEFAULT_PAYLOAD = "default-payload";

	private Map<String, Payload> payloads = new HashMap<>();
	private String currentPayload = DEFAULT_PAYLOAD;

	protected MultiPayloadAdaptrisMessageImp(IdGenerator guid, AdaptrisMessageFactory fac) throws RuntimeException
	{
		super(guid, fac);
		setPayload(currentPayload, new byte[0]);
	}

	public void setCurrentPayload(String payloadKey)
	{
		currentPayload = payloadKey;
	}

	public String getCurrentPayload()
	{
		return currentPayload;
	}

	/**
	 * @see AdaptrisMessage#equivalentForTracking (com.adaptris.core.AdaptrisMessage)
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
	 * @see AdaptrisMessage#setPayload(byte[])
	 */
	@Override
	public void setPayload(byte[] bytes)
	{
		setPayload(currentPayload, bytes);
	}

	public void setPayload(String payloadKey, byte[] bytes)
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
		payloads.put(payloadKey, new Payload(payload));
	}

	/**
	 * @see AdaptrisMessage#getPayload()
	 */
	@Override
	public byte[] getPayload()
	{
		return getPayload(currentPayload);
	}

	public byte[] getPayload(String payloadKey)
	{
		byte[] result = null;
		byte[] payload = payloads.get(payloadKey).data;
		if (payload != null)
		{
			result = new byte[payload.length];
			System.arraycopy(payload, 0, result, 0, payload.length);
		}
		return result;
	}

	/**
	 * @see AdaptrisMessage#getSize()
	 */
	@Override
	public long getSize()
	{
		return getSize(currentPayload);
	}

	public long getSize(String payloadKey)
	{
		byte[] payload = payloads.get(payloadKey).data;
		return payload != null ? payload.length : 0;
	}

	/**
	 * @see AdaptrisMessage#setContent(String, String)
	 */
	@Override
	public void setContent(String payloadString, String charEnc)
	{
		setContent(currentPayload, payloadString, charEnc);
	}

	public void setContent(String payloadKey, String payloadString, String charEnc)
	{
		byte[] payload;
		if (payloadString != null)
		{
			Charset charset = Charset.forName(StringUtils.defaultIfBlank(charEnc, Charset.defaultCharset().name()));
			payload = payloadString.getBytes(charset);
			setContentEncoding(payloadKey, charEnc);
		}
		else
		{
			payload = new byte[0];
			setContentEncoding(payloadKey, charEnc);
		}
		setPayload(payloadKey, payload);
	}

	/**
	 * @see AdaptrisMessage#getContent()
	 */
	@Override
	public String getContent()
	{
		return getContent(currentPayload);
	}

	public String getContent(String payloadKey)
	{
		byte[] payload = getPayload(payloadKey);
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

	@Override
	public void setContentEncoding(String enc)
	{
		setContentEncoding(currentPayload, enc);
	}

	public void setContentEncoding(String payloadKey, String enc)
	{
		String contentEncoding = enc != null ? Charset.forName(enc).name() : null;
		payloads.get(payloadKey).encoding = contentEncoding;
	}

	@Override
	public String getContentEncoding()
	{
		return getContentEncoding(currentPayload);
	}

	public String getContentEncoding(String payloadKey)
	{
		return payloads.get(payloadKey).encoding;
	}

	/**
	 * @see Object#clone()
	 */
	@Override
	public Object clone() throws CloneNotSupportedException
	{
		MultiPayloadAdaptrisMessageImp result = (MultiPayloadAdaptrisMessageImp)super.clone();
		// clone the payload.
		try
		{
			for (String payloadKey : payloads.keySet())
			{
				byte[] payload = payloads.get(payloadKey).data;
				byte[] newPayload = new byte[payload.length];
				System.arraycopy(payload, 0, newPayload, 0, payload.length);
				result.setPayload(payloadKey, newPayload);
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
	public InputStream getInputStream() throws IOException
	{
		return getInputStream(currentPayload);
	}

	public InputStream getInputStream(String payloadKey) throws IOException
	{
		byte[] payload = getPayload(payloadKey);
		return payload != null ? new ByteArrayInputStream(payload) : new ByteArrayInputStream(new byte[0]);
	}

	/**
	 * @see AdaptrisMessage#getOutputStream()
	 */
	@Override
	public OutputStream getOutputStream()
	{
		return getOutputStream(currentPayload);
	}

	public OutputStream getOutputStream(String payloadKey)
	{
		return new ByteFilterStream(payloadKey, new ByteArrayOutputStream());
	}

	private class ByteFilterStream extends FilterOutputStream
	{
		private String payloadKey;

		ByteFilterStream(String payloadKey, OutputStream out)
		{
			super(out);
			this.payloadKey = payloadKey;
		}

		@Override
		public void close() throws IOException
		{
			super.close();
			byte[] payload = ((ByteArrayOutputStream)super.out).toByteArray();
			setPayload(payloadKey, payload);
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
