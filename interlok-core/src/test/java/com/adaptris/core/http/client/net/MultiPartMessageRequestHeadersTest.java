package com.adaptris.core.http.client.net;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.net.HttpURLConnection;
import java.net.URL;

import org.junit.Test;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.core.Channel;

public class MultiPartMessageRequestHeadersTest extends RequestHeadersCase {

  private static final String MULTI_PART_NO_HEADER = "\n"
      + "------=_Part_1_1038479175.1649809692675\n"
      + "Content-Type: text/plain\n"
      + "Content-Disposition: form-data; name=\"file\"\n"
      + "Content-ID: file\n"
      + "\n"
      + "Some text\n"
      + "------=_Part_1_1038479175.1649809692675--\n"
      + "";

  private static final String MULTI_PART = "Message-ID: check-service-test-message\n"
      + "Mime-Version: 1.0\n"
      + "Message-ID: message-id\n"
      + "Content-Type: multipart/form-data; \n"
      + "    boundary=\"----=_Part_1_1038479175.1649809692675\"\n"
      + "Content-Length: 191\n"
      + MULTI_PART_NO_HEADER;

  @Test
  public void testAddHeaders() throws Exception {
    Channel c = null;
    HttpURLConnection urlC = null;
    try {
      c = HttpHelper.createAndStartChannel();
      URL url = new URL(HttpHelper.createProduceDestination(c));
      urlC = (HttpURLConnection) url.openConnection();
      MultiPartMessageRequestHeaders headers = new MultiPartMessageRequestHeaders();
      AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(MULTI_PART);
      urlC = headers.addHeaders(msg, urlC);
      assertTrue(urlC.getRequestProperty("Content-Type").replaceAll("\\r", "")
          .equals("multipart/form-data; \n    boundary=\"----=_Part_1_1038479175.1649809692675\""));
      assertNull(urlC.getRequestProperty("Message-ID"));
    } finally {
      HttpHelper.stopChannelAndRelease(c);
    }
  }

  @Test
  public void testAddHeaders_Flatten() throws Exception {
    Channel c = null;
    HttpURLConnection urlC = null;
    try {
      c = HttpHelper.createAndStartChannel();
      URL url = new URL(HttpHelper.createProduceDestination(c));
      urlC = (HttpURLConnection) url.openConnection();
      MultiPartMessageRequestHeaders headers = new MultiPartMessageRequestHeaders();
      headers.setUnfold(true);
      AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(MULTI_PART);
      urlC = headers.addHeaders(msg, urlC);
      assertTrue(contains(urlC, "Content-Type", "multipart/form-data; boundary=\"----=_Part_1_1038479175.1649809692675\""));
      assertNull(urlC.getRequestProperty("Message-ID"));
    } finally {
      HttpHelper.stopChannelAndRelease(c);
    }
  }

  @Test
  public void testAddHeadersWithMessageId() throws Exception {
    Channel c = null;
    HttpURLConnection urlC = null;
    try {
      c = HttpHelper.createAndStartChannel();
      URL url = new URL(HttpHelper.createProduceDestination(c));
      urlC = (HttpURLConnection) url.openConnection();
      MultiPartMessageRequestHeaders headers = new MultiPartMessageRequestHeaders();
      headers.setExcludeMessageId(false);
      AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(MULTI_PART);
      urlC = headers.addHeaders(msg, urlC);
      assertTrue(urlC.getRequestProperty("Content-Type").replaceAll("\\r", "")
          .equals("multipart/form-data; \n    boundary=\"----=_Part_1_1038479175.1649809692675\""));
      assertEquals("message-id", urlC.getRequestProperty("Message-ID"));
    } finally {
      HttpHelper.stopChannelAndRelease(c);
    }
  }

}
