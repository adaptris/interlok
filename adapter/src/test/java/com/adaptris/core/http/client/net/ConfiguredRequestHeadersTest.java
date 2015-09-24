package com.adaptris.core.http.client.net;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.net.HttpURLConnection;
import java.net.URL;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.core.Channel;
import com.adaptris.util.KeyValuePair;
import com.adaptris.util.KeyValuePairSet;

public class ConfiguredRequestHeadersTest extends RequestHeadersCase {

  @Before
  public void setUp() throws Exception {}

  @After
  public void tearDown() throws Exception {}

  @Test
  public void testSetHandlers() throws Exception {
    ConfiguredRequestHeaders headers = new ConfiguredRequestHeaders();
    assertNotNull(headers.getHeaders());
    assertEquals(0, headers.getHeaders().size());
    String name = testName.getMethodName();
    headers.getHeaders().add(new KeyValuePair(name, name));
    assertEquals(1, headers.getHeaders().size());
    headers.setHeaders(new KeyValuePairSet());
    assertEquals(0, headers.getHeaders().size());
  }

  @Test
  public void testAddHeaders() throws Exception {

    Channel c = null;
    HttpURLConnection urlC = null;
    try {
      c = HttpHelper.createAndStartChannel();
      URL url = new URL(HttpHelper.createProduceDestination(c).getDestination());
      urlC = (HttpURLConnection) url.openConnection();
      String name = testName.getMethodName();
      ConfiguredRequestHeaders headers = new ConfiguredRequestHeaders();
      headers.getHeaders().add(new KeyValuePair(name, name));
      AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage("");
      msg.addMetadata(name, name);
      urlC = headers.addHeaders(msg, urlC);
      assertTrue(contains(urlC, name, name));
    } finally {
      HttpHelper.stopChannelAndRelease(c);
    }
  }



}
