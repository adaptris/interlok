package com.adaptris.core.http.client.net;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.core.Channel;
import com.adaptris.core.http.client.RequestHeaderProvider;
import com.adaptris.core.metadata.RegexMetadataFilter;

public class CompositeRequestHeadersTest extends RequestHeadersCase {

  @Before
  public void setUp() throws Exception {}

  @After
  public void tearDown() throws Exception {}

  @Test
  public void testSetHandlers() throws Exception {
    CompositeRequestHeaders headers = new CompositeRequestHeaders();
    assertNotNull(headers.getProviders());
    assertEquals(0, headers.getProviders().size());
    headers.addProvider(new NoRequestHeaders());
    assertEquals(1, headers.getProviders().size());
    headers.setProviders(new ArrayList<RequestHeaderProvider<HttpURLConnection>>());
    assertEquals(0, headers.getProviders().size());
  }

  @Test
  public void testAddHeaders() throws Exception {
    Channel c = null;
    HttpURLConnection urlC = null;
    try {
      c = HttpHelper.createAndStartChannel();
      URL url = new URL(HttpHelper.createProduceDestination(c).getDestination());
      urlC = (HttpURLConnection) url.openConnection();
      CompositeRequestHeaders headers = new CompositeRequestHeaders();
      MetadataRequestHeaders meta = new MetadataRequestHeaders();
      meta.setFilter(new RegexMetadataFilter());
      AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage("");
      String name = testName.getMethodName();
      headers.addProvider(meta);
      msg.addMetadata(name, name);
      urlC = headers.addHeaders(msg, urlC);
      assertTrue(contains(urlC, name, name));
    } finally {
      HttpHelper.stopChannelAndRelease(c);
    }
  }



}
