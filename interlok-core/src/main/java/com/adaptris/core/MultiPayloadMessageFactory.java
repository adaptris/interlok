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

import com.adaptris.annotation.DisplayOrder;
import com.adaptris.util.IdGenerator;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.nio.charset.UnsupportedCharsetException;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import static org.apache.commons.lang3.StringUtils.isEmpty;

/**
 * The multi-payload message factory which returns an implementations of <code>MultiPayloadAdaptrisMessage</code>.
 *
 * @author aanderson
 * @config multi-payload-message-factory
 * @see AdaptrisMessageFactory
 * @see MultiPayloadAdaptrisMessage
 * @since 3.9.x
 */
@XStreamAlias("multi-payload-message-factory")
@DisplayOrder(order = { "defaultCharEncoding" })
public class MultiPayloadMessageFactory extends AdaptrisMessageFactory
{
	private static final Logger log = LoggerFactory.getLogger(MultiPayloadMessageFactory.class);

	private String defaultCharEncoding;

	public MultiPayloadMessageFactory()
	{
		super();
	}

	/**
	 * {@inheritDoc}.
	 */
	@Override
	public AdaptrisMessage newMessage(byte[] payload)
	{
		return newMessage(MultiPayloadAdaptrisMessage.DEFAULT_PAYLOAD_ID, payload, null);
	}

	public AdaptrisMessage newMessage(String payloadId, byte[] payload)
	{
		return newMessage(payloadId, payload, null);
	}

	/**
	 * {@inheritDoc}.
	 */
	@Override
	public AdaptrisMessage newMessage(byte[] payload, Set metadata)
	{
		return newMessage(MultiPayloadAdaptrisMessage.DEFAULT_PAYLOAD_ID, payload, metadata);
	}

	/**
	 * Create a new multi-payload message, with the given ID, payload, and metadata.
	 *
	 * @param payloadId The payload ID to use.
	 * @param payload The payload.
	 * @param metadata Any metadata.
	 * @return The new multi-payload message.
	 */
	public AdaptrisMessage newMessage(String payloadId, byte[] payload, Set metadata)
	{
		AdaptrisMessage result = new MultiPayloadAdaptrisMessageImp(payloadId, uniqueIdGenerator(), this, payload);
		result.setMetadata(metadata);
		return result;
	}

	/**
	 * {@inheritDoc}.
	 */
	@Override
	public AdaptrisMessage newMessage(String payload, Set metadata)
	{
		return newMessage(MultiPayloadAdaptrisMessage.DEFAULT_PAYLOAD_ID, payload, null, metadata);
	}

	/**
	 * {@inheritDoc}.
	 */
	@Override
	public AdaptrisMessage newMessage(String payload)
	{
		return newMessage(MultiPayloadAdaptrisMessage.DEFAULT_PAYLOAD_ID, payload, null, null);
	}

	/**
	 * {@inheritDoc}.
	 */
	@Override
	public AdaptrisMessage newMessage(String payload, String charEncoding, Set metadata)
	{
		return newMessage(MultiPayloadAdaptrisMessage.DEFAULT_PAYLOAD_ID, payload, charEncoding, metadata);
	}

	/**
	 * {@inheritDoc}.
	 */
	@Override
	public AdaptrisMessage newMessage(String payload, String charEncoding)
	{
		return newMessage(MultiPayloadAdaptrisMessage.DEFAULT_PAYLOAD_ID, payload, charEncoding, null);
	}

	/**
	 * Create a new multi-payload message, with the given ID, payload, and metadata.
	 *
	 * @param payloadId The payload ID to use.
	 * @param content The payload content.
	 * @param metadata Any metadata.
	 * @return The new multi-payload message.
	 */
	public AdaptrisMessage newMessage(String payloadId, String content, String charEncoding, Set metadata)
	{
		Charset charset = Charset.defaultCharset();
		try
		{
			charset = Charset.forName(charEncoding);
		}
		catch (UnsupportedCharsetException e)
		{
			log.warn("Character set [" + charEncoding + "] is not available; using [" + charset.displayName() + "] instead.");
		}
		AdaptrisMessage result = new MultiPayloadAdaptrisMessageImp(payloadId, uniqueIdGenerator(), this, content, charset);
		result.setMetadata(metadata);
		return result;
	}

	/**
	 * {@inheritDoc}.
	 */
	@Override
	public AdaptrisMessage newMessage(AdaptrisMessage source, Collection<String> metadataKeysToPreserve) throws CloneNotSupportedException
	{
		return newMessage(MultiPayloadAdaptrisMessage.DEFAULT_PAYLOAD_ID, source, metadataKeysToPreserve);
	}

	public AdaptrisMessage newMessage(String payloadId, AdaptrisMessage source, Collection<String> metadataKeysToPreserve) throws CloneNotSupportedException
	{
		MultiPayloadAdaptrisMessage result = (MultiPayloadAdaptrisMessage)newMessage();
		if (!payloadId.equals(MultiPayloadAdaptrisMessage.DEFAULT_PAYLOAD_ID))
		{
			/* delete the default payload if it's not being used */
			result.deletePayload(MultiPayloadAdaptrisMessage.DEFAULT_PAYLOAD_ID);
		}
		result.setUniqueId(source.getUniqueId());
		if (metadataKeysToPreserve == null)
		{
			result.setMetadata(source.getMetadata());
		}
		else
		{
			for (Iterator i = metadataKeysToPreserve.iterator(); i.hasNext(); )
			{
				String metadataKey = (String)i.next();
				if (source.headersContainsKey(metadataKey))
				{
					result.addMetadata(metadataKey, source.getMetadataValue(metadataKey));
				}
			}
		}
		MessageLifecycleEvent mle = result.getMessageLifecycleEvent();
		List<MleMarker> markers = source.getMessageLifecycleEvent().getMleMarkers();
		for (int i = 0; i < markers.size(); i++)
		{
			MleMarker marker = (MleMarker)(markers.get(i)).clone();
			mle.addMleMarker(marker);
		}
		result.getObjectHeaders().putAll(source.getObjectHeaders());
		return result;
	}

	/**
	 * {@inheritDoc}.
	 */
	@Override
	public AdaptrisMessage newMessage()
	{
		AdaptrisMessage m = new MultiPayloadAdaptrisMessageImp(MultiPayloadAdaptrisMessage.DEFAULT_PAYLOAD_ID, uniqueIdGenerator(), this);
		if (!isEmpty(getDefaultCharEncoding()))
		{
			m.setContentEncoding(getDefaultCharEncoding());
		}
		return m;
	}

	/**
	 * @return the defaultCharEncoding
	 */
	@Override
	public String getDefaultCharEncoding()
	{
		return defaultCharEncoding;
	}

	/**
	 * Set the default character encoding to be applied to the message upon
	 * creation.
	 * <p>
	 * If not explicitly configured, then the platform default character encoding
	 * will be used.
	 * </p>
	 *
	 * @param s the defaultCharEncoding to set
	 * @see AdaptrisMessage#setCharEncoding(String)
	 */
	@Override
	public void setDefaultCharEncoding(String s)
	{
		defaultCharEncoding = s;
	}
}
