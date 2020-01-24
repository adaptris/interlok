package com.adaptris.interlok.resolver;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;
import java.io.ByteArrayInputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FromPayloadUsingXPath extends ResolverImp
{
	private static final String RESOLVE_PAYLOAD_REGEXP = "^.*%payload\\{xpath:([\\w!\\$\"#&%'\\*\\+,\\-\\.:=\\(\\)\\[\\]\\/@\\|]+)\\}.*$";
	private static final transient Pattern PAYLOAD_RESOLVER = Pattern.compile(RESOLVE_PAYLOAD_REGEXP);

	@Override
	public String resolve(String value)
	{
		throw new UnresolvableException();
	}

	/**
	 * Attempt to resolve a value externally.
	 *
	 * @param value The string to resolve.
	 * @param target The document to search.
	 *
	 * @return The resolved value.
	 */
	@Override
	public String resolve(String value, String target)
	{
		Matcher m = PAYLOAD_RESOLVER.matcher(value);
		while (m.matches())
		{
			String path = m.group(1);
			String replaceWith = extract(path, target);
			log.info("XPath {} found {}", path, replaceWith);
			value = value.replace("%payload{xpath:" + path + "}", replaceWith);
			m = PAYLOAD_RESOLVER.matcher(value);
		}
		return value;
	}

	private String extract(String path, String target)
	{
		try
		{
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			factory.setNamespaceAware(true);
			Document doc = factory.newDocumentBuilder().parse(new ByteArrayInputStream(target.getBytes()));
			XPathExpression expr = XPathFactory.newInstance().newXPath().compile(path);
			NodeList nodes = (NodeList)expr.evaluate(doc, XPathConstants.NODESET);
			Node n = nodes.item(0);
			if (n.getNodeType() == Node.TEXT_NODE)
			{
				return n.getTextContent();
			}
		}
		catch (Exception e)
		{
			log.error("Could not use XPath {} to extract data from message payload", path, e);
			throw new UnresolvableException("Could not use XPath {} to extract data from message payload");
		}
		log.error("XPath payload resolver can only resolve Text() nodes at this time");
		throw new UnresolvableException("XPath payload resolver can only resolve Text() nodes at this time");
	}

	/**
	 * Can this resolver handle this type of value.
	 *
	 * @param value the value e.g. {@code %payload{xpath:…}}
	 *
	 * @return True if the value will provide matches, false otherwise.
	 */
	@Override
	public boolean canHandle(String value)
	{
		return PAYLOAD_RESOLVER.matcher(value).matches();
	}
}
