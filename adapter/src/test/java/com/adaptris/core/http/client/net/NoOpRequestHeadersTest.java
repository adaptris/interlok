package com.adaptris.core.http.client.net;

import java.net.HttpURLConnection;
import java.net.URL;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.core.Channel;

public class NoOpRequestHeadersTest extends RequestHeadersCase {
  @Before
  public void setUp() throws Exception {}

  @After
  public void tearDown() throws Exception {}

  @Test
  public void testAddHeaders() throws Exception {

    Channel c = null;
    HttpURLConnection urlC = null;
    try {
      c = HttpHelper.createAndStartChannel();
      URL url = new URL(HttpHelper.createProduceDestination(c).getDestination());
      urlC = (HttpURLConnection) url.openConnection();
      String name = testName.getMethodName();
      NoRequestHeaders headers = new NoRequestHeaders();
      AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage("");
      urlC = headers.addHeaders(msg, urlC);
    } finally {
      HttpHelper.stopChannelAndRelease(c);
    }
  }

}
