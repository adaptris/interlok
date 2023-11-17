package com.adaptris.core.resolver;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.core.SaferXMLResolver;
import com.adaptris.core.UnresolvedMetadataException;
import com.adaptris.interlok.resolver.UnresolvableException;

public class SaferXMLResolverTest
{
	private static final String KEY = "eq";
	private static final String EQUATION = "x - 5 < 10";
	private static final String XML_SOURCE = "<equation>%asCDATA{%message{" + KEY + "}}</equation>";
	private static final String XML_RESOLVED = "<equation><![CDATA[" + EQUATION + "]]></equation>";
	private static final String SPECIAL_CASE = "<![CDATA[WTF is this?]]>";
	private static final String SPECIAL_RESULT = "<equation><![CDATA[<![CDATA[WTF is this?<![CDATA[]]]]><![CDATA[>]]>]]></equation>";

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
		assertEquals(XML_RESOLVED, result);
	}

	@Test
	public void testResolveMessageContent()
	{
		AdaptrisMessage message = AdaptrisMessageFactory.getDefaultInstance().newMessage(XML_SOURCE);
		message.addMetadata(KEY, EQUATION);
		SaferXMLResolver resolver = new SaferXMLResolver();
		String result = resolver.resolve(null, message);
		assertEquals(XML_RESOLVED, result);
	}

	@Test
	public void testResolveNoValue()
	{
	  Assertions.assertThrows(UnresolvedMetadataException.class, () -> {
		AdaptrisMessage message = AdaptrisMessageFactory.getDefaultInstance().newMessage();
		SaferXMLResolver resolver = new SaferXMLResolver();
		resolver.resolve(XML_SOURCE, message);
	  });
	}

	@Test
	public void testNoMessage()
	{
	  Assertions.assertThrows(UnresolvableException.class, () -> {
		new SaferXMLResolver().resolve(XML_SOURCE);
	  });
	}

	@Test
	public void testNullMessage()
	{
	  Assertions.assertThrows(UnresolvableException.class, () -> {
		new SaferXMLResolver().resolve(XML_SOURCE, null);
	  });
	}

	@Test
	public void testNullExpression()
	{
	  Assertions.assertThrows(UnresolvableException.class, () -> {
		new SaferXMLResolver().resolve(null, null);
	  });
	}

	@Test
	public void testSpecialCase()
	{
		AdaptrisMessage message = AdaptrisMessageFactory.getDefaultInstance().newMessage();
		message.addMetadata(KEY, SPECIAL_CASE);
		SaferXMLResolver resolver = new SaferXMLResolver();
		String result = resolver.resolve(XML_SOURCE, message);
		assertEquals(SPECIAL_RESULT, result);
	}
}
