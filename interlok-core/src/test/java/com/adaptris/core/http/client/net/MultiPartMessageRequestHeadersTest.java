package com.adaptris.core.http.client.net;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.HttpURLConnection;
import java.net.URL;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.core.Channel;
import com.adaptris.core.stubs.DefectiveMessageFactory;

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

  private static final String MULTI_PART = "Mime-Version: 1.0\n"
      + "Message-ID: message-id\n"
      + "Content-Type: multipart/form-data; \n"
      + "    boundary=\"----=_Part_1_1038479175.1649809692675\"\n"
      + "Content-Length: 191\n"
      + MULTI_PART_NO_HEADER;

  @Test
  public void testAddHeaders() throws Exception {
    Channel c = null;
    try {
      c = HttpHelper.createAndStartChannel();
      URL url = new URL(HttpHelper.createProduceDestination(c));
      HttpURLConnection urlC = (HttpURLConnection) url.openConnection();
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
    try {
      c = HttpHelper.createAndStartChannel();
      URL url = new URL(HttpHelper.createProduceDestination(c));
      HttpURLConnection urlC = (HttpURLConnection) url.openConnection();
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
    try {
      c = HttpHelper.createAndStartChannel();
      URL url = new URL(HttpHelper.createProduceDestination(c));
      HttpURLConnection urlC = (HttpURLConnection) url.openConnection();
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

  @Test
  public void testAddHeadersInvalidMessage() throws Exception {
    Channel c = null;
    try {
      c = HttpHelper.createAndStartChannel();
      URL url = new URL(HttpHelper.createProduceDestination(c));
      final HttpURLConnection urlC = (HttpURLConnection) url.openConnection();
      MultiPartMessageRequestHeaders headers = new MultiPartMessageRequestHeaders();
      AdaptrisMessage msg = new DefectiveMessageFactory().newMessage();

      Assertions.assertThrows(RuntimeException.class, () -> {
        headers.addHeaders(msg, urlC);
      });
    } finally {
      HttpHelper.stopChannelAndRelease(c);
    }
  }

}
