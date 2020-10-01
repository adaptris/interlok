package com.adaptris.core;

import java.io.InputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import com.adaptris.core.util.DocumentBuilderFactoryBuilder;
import com.adaptris.interlok.resolver.ResolverImp;
import com.adaptris.interlok.resolver.UnresolvableException;
import com.adaptris.interlok.types.InterlokMessage;
import com.adaptris.util.KeyValuePair;
import com.adaptris.util.KeyValuePairSet;
import com.adaptris.util.text.xml.SimpleNamespaceContext;
import com.adaptris.util.text.xml.XPath;

public class ResolveFromPayloadUsingXPath extends ResolverImp
{
  private static final String RESOLVE_PAYLOAD_REGEXP = "^.*%payload\\{xpath:([\\w!\\$\"#&%'\\*\\+,\\-\\.:=\\(\\)\\[\\]\\/@\\|]+)\\}.*$";
  private static final transient Pattern PAYLOAD_RESOLVER = Pattern.compile(RESOLVE_PAYLOAD_REGEXP, Pattern.DOTALL);

  private final DocumentBuilderFactoryBuilder factoryBuilder;
  private final NamespaceContext namespaceContext;

  public ResolveFromPayloadUsingXPath()
  {
    factoryBuilder = DocumentBuilderFactoryBuilder.newRestrictedInstance();
    KeyValuePairSet result = new KeyValuePairSet();
    result.add(new KeyValuePair("xsd", XMLConstants.W3C_XML_SCHEMA_NS_URI));
    result.add(new KeyValuePair("xs", XMLConstants.W3C_XML_SCHEMA_NS_URI));
    result.add(new KeyValuePair(XMLConstants.XML_NS_PREFIX, XMLConstants.XML_NS_URI));
    result.add(new KeyValuePair(XMLConstants.XMLNS_ATTRIBUTE, XMLConstants.XMLNS_ATTRIBUTE_NS_URI));
    namespaceContext = SimpleNamespaceContext.create(result);
  }

  /**
   * {@inheritDoc}.
   */
  @Override
  public String resolve(String value)
  {
    throw new UnresolvableException();
  }

  /**
   * Attempt to resolve a value externally.
   *
   * @param value The string to resolve.
   * @param target The message to search.
   *
   * @return The resolved value.
   */
  @Override
  public String resolve(String value, InterlokMessage target)
  {
    Document document;
    try (InputStream inputStream = target.getInputStream())
    {
      DocumentBuilderFactory factory =
          DocumentBuilderFactoryBuilder.newRestrictedInstance().build();
      DocumentBuilder builder = factoryBuilder.newDocumentBuilder(factory);
      document = builder.parse(inputStream); // lgtm [java/xxe]
    }
    catch (Exception e)
    {
      log.error("Could not parse XML document for resolve", e);
      throw new UnresolvableException(e);
    }

    Matcher m = PAYLOAD_RESOLVER.matcher(value);
    while (m.matches())
    {
      String path = m.group(1);
      String replaceWith = extract(path, document);
      log.trace("XPath {} found {}", path, replaceWith);
      value = value.replace("%payload{xpath:" + path + "}", replaceWith);
      m = PAYLOAD_RESOLVER.matcher(value);
    }
    return value;
  }

  private String extract(String path, Document document)
  {
    try
    {
      XPath xPath = XPath.newXPathInstance(factoryBuilder, namespaceContext);
      return xPath.selectSingleTextItem(document, path);
    }
    catch (Exception e)
    {
      log.error("Could not use XPath {} to extract data from message payload", path, e);
      throw new UnresolvableException("Could not use XPath {} to extract data from message payload");
    }
  }

  /**
   * Can this resolver handle this type of value.
   *
   * @param value the value e.g. {@code %payload{xpath:...}}
   *
   * @return True if the value will provide matches, false otherwise.
   */
  @Override
  public boolean canHandle(String value)
  {
    return PAYLOAD_RESOLVER.matcher(value).matches();
  }
}
