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

import java.io.InputStream;
import java.io.OutputStream;

/**
 * Interface for Adaptris messages that support multiple payloads, referenced by an ID/key.
 *
 * @author aanderson
 * @see AdaptrisMessage
 * @since 3.9.x
 */
public interface MultiPayloadAdaptrisMessage extends AdaptrisMessage
{
	/**
	 * The ID to use for the default payload.
	 */
	String DEFAULT_PAYLOAD_ID = "default-payload";

	/**
	 * Switch from one payload to another, given by the ID.
	 *
	 * @param id The ID of the payload to switch to.
	 */
	void switchPayload(String id);

	/**
	 * Indicates whether the given payload ID exists in the message.
	 *
	 * @param id The payload ID to check.
	 * @return True if there's a payload with the given ID, false otherwise.
	 */
	boolean hasPayloadId(String id);

	/**
	 * Get the ID of the current payload.
	 *
	 * @return The payload ID.
	 */
	String getCurrentPayloadId();

	/**
	 * Update the ID of the current payload.
	 *
	 * @param id The new payload ID.
	 */
	void setCurrentPayloadId(String id);

	/**
	 * Add a new payload to the message, with the given ID and data.
	 *
	 * @param id      The new payload ID.
	 * @param payload The payload data.
	 */
	void addPayload(String id, byte[] payload);

	/**
	 * Delete an existing payload, with the given payload ID.
	 *
	 * @param id The payload ID.
	 */
	void deletePayload(String id);

	/**
	 * Get the payload data for the given ID.
	 *
	 * @param id The payload ID.
	 * @return The payload data.
	 */
	byte[] getPayload(String id);

	/**
	 * Get the size of the payload for the given ID.
	 *
	 * @param id The payload ID.
	 * @return The payload size.
	 */
	long getSize(String id);

	/**
	 * Return the number of payloads contained within the message.
	 *
	 * @return The number of payloads.
	 */
	int getPayloadCount();

	/**
	 * Add a new payload to the message, with the given ID and content.
	 *
	 * @param id      The new payload ID.
	 * @param content The payload content.
	 */
	void addContent(String id, String content);

	/**
	 * Add a new payload to the message, with the given ID and content and encoding.
	 *
	 * @param id       The new payload ID.
	 * @param content  The payload content.
	 * @param encoding The content encoding.
	 */
	void addContent(String id, String content, String encoding);

	/**
	 * Get the payload content for the given ID.
	 *
	 * @param id The payload ID.
	 * @return The payload content.
	 */
	String getContent(String id);

	/**
	 * Set the payload content encoding for the given payload ID.
	 *
	 * @param id       The payload ID.
	 * @param encoding The content encoding.
	 */
	void setContentEncoding(String id, String encoding);

	/**
	 * Get the content encoding for the given payload ID.
	 *
	 * @param id The payload ID.
	 * @return The content encoding.
	 */
	String getContentEncoding(String id);

	/**
	 * Get the input stream for the given payload ID.
	 *
	 * @param id The payload ID.
	 * @return The payload input stream.
	 */
	InputStream getInputStream(String id);

	/**
	 * Get the output stream for the given payload ID.
	 *
	 * @param id The payload ID.
	 * @return The payload output stream.
	 */
	OutputStream getOutputStream(String id);
}
