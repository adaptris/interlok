package com.adaptris.core.resolver;

import com.adaptris.core.ResolveFromPayloadUsingXPath;
import com.adaptris.interlok.resolver.ExternalResolver;
import com.adaptris.interlok.types.InterlokMessage;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class ResolveFromPayloadUsingXPathTest
{
  private static final String REGEX_POOR = "%payload{xpath:/something[0--]invalid}";
  private static final String REGEX_MISS = "%payload{xpath:/text/para/quote/text()}";
  static final String REGEX_BAD = "%payload{jsonpath:$['text']['para']['sent'][3]}";
  static final String REGEX_GOOD = "%payload{xpath:/text/para/sent[3]/text()}";
  static final String DATA = "<text><para><sent>Hipster ipsum dolor amet portland asymmetrical try-hard roof party poke, schlitz blue bottle pop-up 3 wolf moon kogi hammock kitsch austin health goth.</sent><sent>Ethical mlkshk crucifix pug, hexagon XOXO tote bag portland typewriter celiac cornhole lumbersexual 8-bit pop-up.</sent><sent>Cred typewriter seitan, narwhal quinoa master cleanse mlkshk freegan.</sent><sent>Whatever vape paleo, mustache taiyaki XOXO chia ethical viral.</sent></para></text>";
  static final String RESULT = "Cred typewriter seitan, narwhal quinoa master cleanse mlkshk freegan.";

  private ResolveFromPayloadUsingXPath resolver = new ResolveFromPayloadUsingXPath();
  private InterlokMessage message = new InterlokMessage()
  {
    @Override public String getUniqueId() { return null; }
    @Override public void setUniqueId(String uniqueId) { }
    @Override public Map<String, String> getMessageHeaders() { return null; }
    @Override public void setMessageHeaders(Map<String, String> metadata) { }
    @Override public void addMessageHeader(String key, String value) { }
    @Override public void removeMessageHeader(String key) { }
    @Override public String getContentEncoding() { return null; }
    @Override public void setContentEncoding(String payloadEncoding) { }
    @Override public Reader getReader() { return null; }
    @Override public Writer getWriter() { return null; }
    @Override public Writer getWriter(String encoding) { return null; }
    @Override public OutputStream getOutputStream() { return null; }
    @Override public void addObjectHeader(Object key, Object object) { }
    @Override public Map<Object, Object> getObjectHeaders() { return null; }
    @Override public boolean headersContainsKey(String key) { return false; }
    @Override public String resolve(String s, boolean multiline) { return null; }
    @Override public Object resolveObject(String s) { return null; }
    @Override public void clearMessageHeaders() { }
    private String payload = DATA;
    private String encoding = "UTF-8";
    @Override
    public void setContent(String payload, String encoding)
    {
      this.payload = payload;
      this.encoding = encoding;
    }
    @Override
    public String getContent()
    {
      return payload;
    }
    @Override
    public InputStream getInputStream()
    {
      return new ByteArrayInputStream(payload.getBytes(Charset.forName(encoding)));
    }
  };

  @Test
  public void testCanHandle()
  {
    assertTrue(resolver.canHandle(REGEX_GOOD));
    assertFalse(resolver.canHandle(REGEX_BAD));
  }

  @Test
  public void testResolveSuccess()
  {
    assertEquals(RESULT, resolver.resolve(REGEX_GOOD, message));
  }

  @Test
  public void testResolveException()
  {
    try
    {
      resolver.resolve(REGEX_GOOD);
      fail();
    }
    catch (Exception e)
    {
      // expected
    }
  }

  @Test
  public void testResolveRegexNotFound()
  {
    try
    {
      assertEquals("", resolver.resolve(REGEX_MISS, message));
    }
    catch (Exception e)
    {
      // expected
    }
  }

  @Test
  public void testNotXML()
  {
    try
    {
      message.setContent(RESULT, "UTF-8");
      resolver.resolve(REGEX_GOOD, message);
      fail();
    }
    catch (Exception e)
    {
      // expected
    }
  }

  @Test
  public void testResolveRegexInvalid()
  {
    assertEquals(REGEX_BAD, resolver.resolve(REGEX_BAD, message));
    try
    {
      resolver.resolve(REGEX_POOR, message);
      fail();
    }
    catch (Exception e)
    {
      // expected
    }
  }

  @Test
  public void testExternalResolver()
  {
    assertNull(ExternalResolver.resolve(null, null));
    assertEquals(RESULT, ExternalResolver.resolve(REGEX_GOOD, message));
    assertEquals(REGEX_BAD, ExternalResolver.resolve(REGEX_BAD, message));
  }
}
