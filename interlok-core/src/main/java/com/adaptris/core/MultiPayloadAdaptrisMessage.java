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
	 * Switch from one payload to another, given by the ID.
	 *
	 * @param id The ID of the payload to switch to.
	 */
	void switchPayload(String id);

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
