package com.adaptris.core;

import org.junit.Test;

import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;

import static org.junit.Assert.assertArrayEquals;

public class SwitchPayloadTest extends ServiceCase
{
	private final MultiPayloadMessageFactory messageFactory = new MultiPayloadMessageFactory();

	private static final String ENCODING = "UTF-8";

	private static final String CONTENT = "Bacon ipsum dolor amet bresaola ball tip flank, doner pork chop ham hock rump kielbasa pork loin beef burgdoggen short ribs tongue.";
	private static final byte[] PAYLOAD = "Cupcake ipsum dolor sit amet dragée carrot cake halvah jujubes.".getBytes(Charset.forName(ENCODING));

	private static final String[] ID = { "bacon", "cupcake" };

	@Test
	public void testService() throws Exception
	{
		Service service = getService(ID[0]);
		MultiPayloadAdaptrisMessage message = getMessage();
		assertEquals(ID[1], message.getCurrentPayloadId());
		assertEquals(new String(PAYLOAD), message.getContent());
		assertEquals(PAYLOAD.length, message.getSize());
		service.doService(message);
		assertEquals(2, message.getPayloadCount());
		assertEquals(ID[0], message.getCurrentPayloadId());
		assertEquals(CONTENT, message.getContent());
		assertArrayEquals(CONTENT.getBytes(ENCODING), message.getPayload());
	}

	@Test
	public void testWrongMessageType()
	{
		try
		{
			Service service = getService(ID[0]);
			AdaptrisMessage message = DefaultMessageFactory.getDefaultInstance().newMessage();
			service.doService(message);
			fail();
		}
		catch (ServiceException e)
		{
			/* expected */
		}
	}

	private MultiPayloadAdaptrisMessage getMessage()
	{
		MultiPayloadAdaptrisMessage message = (MultiPayloadAdaptrisMessage)messageFactory.newMessage(ID[0], CONTENT, ENCODING);
		message.addPayload(ID[1], PAYLOAD);
		return message;
	}

	private SwitchPayloadService getService(String newPayloadId)
	{
		SwitchPayloadService service = new SwitchPayloadService();
		service.setNewPayloadId(newPayloadId);
		return service;
	}

	@Override
	protected Object retrieveObjectForSampleConfig()
	{
		return getService(ID[0]);
	}
}
