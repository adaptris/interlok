package com.adaptris.core.resolver;

import com.adaptris.interlok.resolver.ExternalResolver;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class FromPayloadUsingXPathTest
{
  private static final String REGEX_POOR = "%payload{xpath:/something[0--]invalid}";
  private static final String REGEX_MISS = "%payload{xpath:/text/para/quote/text()}";
  static final String REGEX_BAD = "%payload{jsonpath:$['text']['para']['sent'][3]}";
  static final String REGEX_GOOD = "%payload{xpath:/text/para/sent[3]/text()}";
  static final String DATA = "<text><para><sent>Hipster ipsum dolor amet portland asymmetrical try-hard roof party poke, schlitz blue bottle pop-up 3 wolf moon kogi hammock kitsch austin health goth.</sent><sent>Ethical mlkshk crucifix pug, hexagon XOXO tote bag portland typewriter celiac cornhole lumbersexual 8-bit pop-up.</sent><sent>Cred typewriter seitan, narwhal quinoa master cleanse mlkshk freegan.</sent><sent>Whatever vape paleo, mustache taiyaki XOXO chia ethical viral.</sent></para></text>";
  static final String RESULT = "Cred typewriter seitan, narwhal quinoa master cleanse mlkshk freegan.";

  private FromPayloadUsingXPath resolver = new FromPayloadUsingXPath();

  @Test
  public void testCanHandle()
  {
    assertTrue(resolver.canHandle(REGEX_GOOD));
    assertFalse(resolver.canHandle(REGEX_BAD));
  }

  @Test
  public void testResolveSuccess()
  {
    assertEquals(RESULT, resolver.resolve(REGEX_GOOD, DATA));
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
      assertEquals("", resolver.resolve(REGEX_MISS, DATA));
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
      resolver.resolve(REGEX_GOOD, RESULT);
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
    assertEquals(REGEX_BAD, resolver.resolve(REGEX_BAD, DATA));
    try
    {
      resolver.resolve(REGEX_POOR, DATA);
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
    assertEquals(FromPayloadUsingXPathTest.RESULT, ExternalResolver.resolve(FromPayloadUsingXPathTest.REGEX_GOOD, FromPayloadUsingXPathTest.DATA));
    assertEquals(FromPayloadUsingXPathTest.REGEX_BAD, ExternalResolver.resolve(FromPayloadUsingXPathTest.REGEX_BAD, FromPayloadUsingXPathTest.DATA));
  }
}
