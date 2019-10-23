package com.adaptris.core;

import com.adaptris.core.common.ConstantDataInputParameter;
import com.adaptris.core.common.StringPayloadDataInputParameter;
import org.junit.Before;
import org.junit.Test;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertArrayEquals;

public class MultiPayloadMessageTests extends ServiceCase
{
	private final MultiPayloadMessageFactory messageFactory = new MultiPayloadMessageFactory();

	private static final String ID = "custom-message-id";
	private static final String ENCODING = "UTF-8";
	private static final String CONTENT = "Bacon ipsum dolor amet jowl boudin salami strip steak turkey.";
	private static final byte[] PAYLOAD = "Cupcake ipsum dolor sit amet fruitcake jelly-o tootsie roll.".getBytes(Charset.forName(ENCODING));

	private static final Set<MetadataElement> METADATA = new HashSet<>();

	@Before
	public void setUp()
	{
		messageFactory.setDefaultCharEncoding(ENCODING);
		METADATA.add(new MetadataElement("KEY", "VALUE"));
	}

	@Test
	public void testMessageFactory()
	{
		MultiPayloadAdaptrisMessage message = (MultiPayloadAdaptrisMessage)messageFactory.newMessage();
		assertEquals(messageFactory.getDefaultCharEncoding(), message.getContentEncoding());
	}

	@Test
	public void testMessageFactoryPayload()
	{
		MultiPayloadAdaptrisMessage message = (MultiPayloadAdaptrisMessage)messageFactory.newMessage(PAYLOAD);
		assertEquals(MultiPayloadAdaptrisMessage.DEFAULT_PAYLOAD_ID, message.getCurrentPayloadId());
		assertEquals(1, message.getPayloadCount());
		assertArrayEquals(PAYLOAD, message.getPayload());
		assertEquals(0, message.getMetadata().size());
	}

	@Test
	public void testMessageFactoryPayloadID()
	{
		MultiPayloadAdaptrisMessage message = (MultiPayloadAdaptrisMessage)messageFactory.newMessage(ID, PAYLOAD);
		assertEquals(ID, message.getCurrentPayloadId());
		assertEquals(1, message.getPayloadCount());
		assertArrayEquals(PAYLOAD, message.getPayload());
		assertEquals(0, message.getMetadata().size());
	}

	@Test
	public void testMessageFactoryPayloadMetadata()
	{
		MultiPayloadAdaptrisMessage message = (MultiPayloadAdaptrisMessage)messageFactory.newMessage(PAYLOAD, METADATA);
		assertEquals(MultiPayloadAdaptrisMessage.DEFAULT_PAYLOAD_ID, message.getCurrentPayloadId());
		assertEquals(1, message.getPayloadCount());
		assertArrayEquals(PAYLOAD, message.getPayload());
		assertEquals(1, message.getMetadata().size());
	}

	@Test
	public void testMessageFactoryPayloadMetadataID()
	{
		MultiPayloadAdaptrisMessage message = (MultiPayloadAdaptrisMessage)messageFactory.newMessage(ID, PAYLOAD, METADATA);
		assertEquals(ID, message.getCurrentPayloadId());
		assertEquals(1, message.getPayloadCount());
		assertArrayEquals(PAYLOAD, message.getPayload());
		assertEquals(1, message.getMetadata().size());
	}

	@Test
	public void testMessageFactoryContent()
	{
		MultiPayloadAdaptrisMessage message = (MultiPayloadAdaptrisMessage)messageFactory.newMessage(CONTENT);
		assertEquals(MultiPayloadAdaptrisMessage.DEFAULT_PAYLOAD_ID, message.getCurrentPayloadId());
		assertEquals(1, message.getPayloadCount());
		assertEquals(CONTENT, message.getContent());
		assertEquals(0, message.getMetadata().size());
	}

	@Test
	public void testMessageFactoryContentEncoding()
	{
		MultiPayloadAdaptrisMessage message = (MultiPayloadAdaptrisMessage)messageFactory.newMessage(CONTENT, ENCODING);
		assertEquals(MultiPayloadAdaptrisMessage.DEFAULT_PAYLOAD_ID, message.getCurrentPayloadId());
		assertEquals(1, message.getPayloadCount());
		assertEquals(CONTENT, message.getContent());
		assertEquals(ENCODING, message.getContentEncoding());
		assertEquals(0, message.getMetadata().size());
	}

	@Test
	public void testMessageFactoryContentEncodingMetadata()
	{
		MultiPayloadAdaptrisMessage message = (MultiPayloadAdaptrisMessage)messageFactory.newMessage(CONTENT, ENCODING, METADATA);
		assertEquals(MultiPayloadAdaptrisMessage.DEFAULT_PAYLOAD_ID, message.getCurrentPayloadId());
		assertEquals(1, message.getPayloadCount());
		assertEquals(CONTENT, message.getContent());
		assertEquals(ENCODING, message.getContentEncoding());
		assertEquals(1, message.getMetadata().size());
	}

	@Test
	public void testMessageFactoryContentgMetadata()
	{
		MultiPayloadAdaptrisMessage message = (MultiPayloadAdaptrisMessage)messageFactory.newMessage(CONTENT, METADATA);
		assertEquals(MultiPayloadAdaptrisMessage.DEFAULT_PAYLOAD_ID, message.getCurrentPayloadId());
		assertEquals(1, message.getPayloadCount());
		assertEquals(CONTENT, message.getContent());
		assertEquals(Charset.defaultCharset().toString(), message.getContentEncoding());
		assertEquals(1, message.getMetadata().size());
	}

	@Test
	public void testMessageFactoryContentEncodingMetadataID()
	{
		MultiPayloadAdaptrisMessage message = (MultiPayloadAdaptrisMessage)messageFactory.newMessage(ID, CONTENT, ENCODING, METADATA);
		assertEquals(ID, message.getCurrentPayloadId());
		assertEquals(1, message.getPayloadCount());
		assertEquals(CONTENT, message.getContent());
		assertEquals(ENCODING, message.getContentEncoding());
		assertEquals(1, message.getMetadata().size());
	}

	@Test
	public void testMessageFactoryCloneMessage() throws Exception
	{
		AdaptrisMessage singleMessage = DefaultMessageFactory.getDefaultInstance().newMessage(PAYLOAD, METADATA);
		MultiPayloadAdaptrisMessage multiMessage = (MultiPayloadAdaptrisMessage)messageFactory.newMessage(singleMessage, null);
		assertEquals(MultiPayloadAdaptrisMessage.DEFAULT_PAYLOAD_ID, multiMessage.getCurrentPayloadId());
		assertEquals(1, multiMessage.getPayloadCount());
		assertArrayEquals(PAYLOAD, multiMessage.getPayload());
		assertEquals(1, multiMessage.getMetadata().size());
	}

	@Test
	public void testMessageFactoryCloneMessageMetadata() throws Exception
	{
		AdaptrisMessage singleMessage = DefaultMessageFactory.getDefaultInstance().newMessage(PAYLOAD, METADATA);
		singleMessage.getMessageLifecycleEvent().addMleMarker(new MleMarker());
		List<String> keys = new ArrayList<>();
		keys.add("KEY");
		keys.add("UNKNOWN");
		messageFactory.setDefaultCharEncoding(null);
		MultiPayloadAdaptrisMessage multiMessage = (MultiPayloadAdaptrisMessage)messageFactory.newMessage(singleMessage, keys);
		assertEquals(MultiPayloadAdaptrisMessage.DEFAULT_PAYLOAD_ID, multiMessage.getCurrentPayloadId());
		assertEquals(1, multiMessage.getPayloadCount());
		assertArrayEquals(PAYLOAD, multiMessage.getPayload());
		assertEquals(1, multiMessage.getMetadata().size());
	}

	@Override
	protected Object retrieveObjectForSampleConfig()
	{
		AddPayloadService service = new AddPayloadService();
		service.setNewPayloadId(ID);
		service.setNewPayload(new ConstantDataInputParameter(CONTENT));
		service.setNewPayloadEncoding("UTF-8");
		return service;
	}
}
