package com.adaptris.core.http;

import static org.junit.Assert.assertEquals;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.DefaultMessageFactory;

public class ConfiguredContentTypeProviderTest {

  @Rule
  public TestName testName = new TestName();

  @Before
  public void setUp() throws Exception {}

  @After
  public void tearDown() throws Exception {}

  @Test
  public void testGetContentType() throws Exception {
    ConfiguredContentTypeProvider provider = new ConfiguredContentTypeProvider("text/complicated");

    AdaptrisMessage msg = new DefaultMessageFactory().newMessage("");
    msg.addMetadata(testName.getMethodName(), "text/complicated");

    String contentType = provider.getContentType(msg);
    assertEquals("text/complicated", contentType);
  }


  @Test
  public void testGetContentType_WithCharset() throws Exception {
    ConfiguredContentTypeProvider provider = new ConfiguredContentTypeProvider("text/complicated");

    AdaptrisMessage msg = new DefaultMessageFactory().newMessage("");
    msg.setContentEncoding("UTF-8");
    String contentType = provider.getContentType(msg);
    assertEquals("text/complicated; charset=UTF-8", contentType);
  }

}
