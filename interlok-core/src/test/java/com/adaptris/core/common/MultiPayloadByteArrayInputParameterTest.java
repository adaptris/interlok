package com.adaptris.core.common;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.DefaultMessageFactory;
import com.adaptris.core.MultiPayloadAdaptrisMessage;
import com.adaptris.core.MultiPayloadMessageFactory;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class MultiPayloadByteArrayInputParameterTest
{
	private static final String ID = "bacon";
	private static final byte[] PAYLOAD = "Bacon ipsum dolor amet short loin porchetta ham turducken chicken tail meatball frankfurter. Rump t-bone flank kielbasa ribeye strip steak landjaeger fatback capicola pastrami cow brisket leberkas jerky. Ham pork chop chislic ground round prosciutto. Sirloin porchetta ribeye, spare ribs strip steak fatback cupim short loin burgdoggen landjaeger. Ground round tri-tip sirloin pig jowl shoulder pork loin cow jerky picanha pastrami. Pork rump bacon strip steak pig bresaola kielbasa ball tip tongue drumstick t-bone. Boudin kevin filet mignon prosciutto tongue short loin spare ribs.".getBytes();

	@Rule
	public TestName testName = new TestName();

	@Test
	public void testExtract() throws Exception
	{
		MultiPayloadByteArrayInputParameter parameter = new MultiPayloadByteArrayInputParameter();
		parameter.setPayloadId(ID);
		MultiPayloadAdaptrisMessage message = (MultiPayloadAdaptrisMessage)new MultiPayloadMessageFactory().newMessage(ID, PAYLOAD);
		assertArrayEquals(PAYLOAD, parameter.extract(message));
		assertArrayEquals(PAYLOAD, parameter.extract(ID, message));
		assertArrayEquals(PAYLOAD, parameter.extract(null, message));
		assertEquals(ID, parameter.getPayloadId());
	}

	@Test
	public void testWrongMessageType()
	{
		try
		{
			MultiPayloadByteArrayInputParameter parameter = new MultiPayloadByteArrayInputParameter();
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
