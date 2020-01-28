package com.adaptris.core.common;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.DefaultMessageFactory;
import com.adaptris.core.MultiPayloadAdaptrisMessage;
import com.adaptris.core.MultiPayloadMessageFactory;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

import java.io.InputStream;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class MultiPayloadStreamInputParameterTest
{
	private static final String ID = "cupcake";
	private static final byte[] PAYLOAD = "Cupcake ipsum dolor sit amet donut topping brownie wafer. Pie dessert tiramisu. Toffee candy canes fruitcake. Pastry cookie jelly-o tiramisu I love carrot cake lollipop cake halvah. Icing tart jelly ice cream. I love muffin chocolate cake sweet roll I love. Apple pie souffle I love I love pie cake. Carrot cake jelly beans cake.".getBytes();

	@Rule
	public TestName testName = new TestName();

	@Test
	public void testExtract() throws Exception
	{
		MultiPayloadStreamInputParameter parameter = new MultiPayloadStreamInputParameter();
		parameter.setPayloadId(ID);
		MultiPayloadAdaptrisMessage message = (MultiPayloadAdaptrisMessage)new MultiPayloadMessageFactory().newMessage(ID, PAYLOAD);
		try (InputStream stream = parameter.extract(message))
		{
			for (byte b : PAYLOAD)
			{
				assertEquals(b, stream.read());
			}
		}
		try (InputStream stream = parameter.extract(ID, message))
		{
			for (byte b : PAYLOAD)
			{
				assertEquals(b, stream.read());
			}
		}
		try (InputStream stream = parameter.extract(null, message))
		{
			for (byte b : PAYLOAD)
			{
				assertEquals(b, stream.read());
			}
		}
		assertEquals(ID, parameter.getPayloadId());
	}

	@Test
	public void testWrongMessageType()
	{
		try
		{
			MultiPayloadStreamInputParameter parameter = new MultiPayloadStreamInputParameter();
			AdaptrisMessage message = DefaultMessageFactory.getDefaultInstance().newMessage(PAYLOAD);
			parameter.extract(message);
			fail();
		}
		catch (Exception e)
		{
			// expected
		}
	}
}
