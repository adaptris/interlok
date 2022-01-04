package com.adaptris.core.resolver;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.core.SaferXMLResolver;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class SaferXMLResolverTest
{
	private static final String KEY = "eq";
	private static final String EQUATION = "x - 5 < 10";
	private static final String XML_SOURCE = "<equation>%resolveXml{%message{" + KEY + "}}</equation>";
	private static final String XML_GOOD = "<equation><![CDATA[" + EQUATION + "]]></equation>";
	private static final String XML_BAD = "<equation>" + EQUATION + "</equation>";

	@Test
	public void testCanResolve()
	{
		assertTrue(new SaferXMLResolver().canHandle(XML_SOURCE));
	}

	@Test
	public void testResolve()
	{
		AdaptrisMessage message = AdaptrisMessageFactory.getDefaultInstance().newMessage();
		message.addMetadata(KEY, EQUATION);

		SaferXMLResolver resolver = new SaferXMLResolver();

		String result = resolver.resolve(XML_SOURCE, message);

		assertEquals(XML_GOOD, result);
	}
}
