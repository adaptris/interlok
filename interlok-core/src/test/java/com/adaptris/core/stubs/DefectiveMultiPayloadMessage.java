package com.adaptris.core.stubs;

import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.core.MultiPayloadAdaptrisMessage;
import com.adaptris.util.IdGenerator;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Writer;
import java.util.Set;

public class DefectiveMultiPayloadMessage extends DefectiveAdaptrisMessage implements MultiPayloadAdaptrisMessage
{
	public DefectiveMultiPayloadMessage(IdGenerator guid, AdaptrisMessageFactory amf) throws RuntimeException
	{
		super(guid, amf);
		setPayload(new byte[0]);
	}

	/**
	 * Switch from one payload to another, given by the ID.
	 *
	 * @param id The ID of the payload to switch to.
	 */
	@Override
	public void switchPayload(String id)
	{
		/* do nothing */
	}

	/**
	 * Indicates whether the given payload ID exists in the message.
	 *
	 * @param id The payload ID to check.
	 * @return True if there's a payload with the given ID, false otherwise.
	 */
	@Override
	public boolean hasPayloadId(String id)
	{
		return MultiPayloadAdaptrisMessage.DEFAULT_PAYLOAD_ID.equals(id);
	}

	/**
	 * Get the ID of the current payload.
	 *
	 * @return The payload ID.
	 */
	@Override
	public String getCurrentPayloadId()
	{
		return MultiPayloadAdaptrisMessage.DEFAULT_PAYLOAD_ID;
	}

	/**
	 * Get the payload IDs used within this message.
	 *
	 * @return The payload IDs.
	 */
	@Override
	public Set<String> getPayloadIDs()
	{
		return Set.of(MultiPayloadAdaptrisMessage.DEFAULT_PAYLOAD_ID);
	}

	/**
	 * Update the ID of the current payload. This does not change the current
	 * working payload, rather updates the ID of the current working payload.
	 *
	 * @param id The new payload ID.
	 */
	@Override
	public void setCurrentPayloadId(String id)
	{
		/* do nothing */
	}

	/**
	 * Add a new payload to the message, with the given ID and data.
	 *
	 * @param id      The new payload ID.
	 * @param payload
	 */
	@Override
	public void addPayload(String id, byte[] payload)
	{
		/* do nothing */
	}

	/**
	 * Delete an existing payload, with the given payload ID.
	 *
	 * @param id The payload ID.
	 */
	@Override
	public void deletePayload(String id)
	{
		/* do nothing */
	}

	/**
	 * Get the payload data for the given ID.
	 *
	 * @param id The payload ID.
	 * @return The payload data.
	 */
	@Override
	public byte[] getPayload(String id)
	{
		return super.getPayload();
	}

	/**
	 * Get the size of the payload for the given ID.
	 *
	 * @param id The payload ID.
	 * @return The payload size.
	 */
	@Override
	public long getSize(String id)
	{
		return super.getSize();
	}

	/**
	 * Return the number of payloads contained within the message.
	 *
	 * @return The number of payloads.
	 */
	@Override
	public int getPayloadCount()
	{
		return 1;
	}

	/**
	 * Add a new payload to the message, with the given ID and content.
	 *
	 * @param id      The new payload ID.
	 * @param content
	 */
	@Override
	public void addContent(String id, String content)
	{
		this.addContent(id, content, null);
	}

	/**
	 * Add a new payload to the message, with the given ID and content and encoding.
	 *
	 * @param id       The new payload ID.
	 * @param content  The payload content.
	 * @param encoding
	 */
	@Override
	public void addContent(String id, String content, String encoding)
	{
		super.setContent(content, encoding);
	}

	/**
	 * Add a new payload to the message, with the given ID and content and encoding.
	 *
	 * @param id       The new payload ID.
	 * @param content  The payload content.
	 * @param encoding
	 */
	@Override
	public void setContent(String id, String content, String encoding)
	{
		super.setContent(content, encoding);
	}

	/**
	 * Get the payload content for the given ID.
	 *
	 * @param id The payload ID.
	 * @return The payload content.
	 */
	@Override
	public String getContent(String id)
	{
		return super.getContent();
	}

	/**
	 * Set the payload content encoding for the given payload ID.
	 *
	 * @param id       The payload ID.
	 * @param encoding
	 */
	@Override
	public void setContentEncoding(String id, String encoding)
	{
		super.setContentEncoding(encoding);
	}

	/**
	 * Get the content encoding for the given payload ID.
	 *
	 * @param id The payload ID.
	 * @return The content encoding.
	 */
	@Override
	public String getContentEncoding(String id)
	{
		return super.getContentEncoding();
	}

	/**
	 * Get the input stream for the given payload ID.
	 *
	 * @param id The payload ID.
	 * @return The payload input stream.
	 */
	@Override
	public InputStream getInputStream(String id) throws IOException
	{
		return super.getInputStream();
	}

	/**
	 * Get the output stream for the given payload ID.
	 *
	 * @param id The payload ID.
	 * @return The payload output stream.
	 */
	@Override
	public OutputStream getOutputStream(String id) throws IOException
	{
		return super.getOutputStream();
	}

	/**
	 * Return a writer ready for writing the payload for the given payload ID.
	 *
	 * @param id       The payload ID.
	 * @param encoding The payload encoding.
	 * @return an Writer that can be used to write the payload using the existing encoding.
	 * @throws IOException if the Writer could not be created.
	 */
	@Override
	public Writer getWriter(String id, String encoding) throws IOException
	{
		return super.getWriter();
	}
}
