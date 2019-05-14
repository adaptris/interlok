package com.adaptris.core.http.client.net;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.Test;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageFactory;

public class ResponseHeaderAsObjectMetadataTest {

  @Test
  public void testAddMetadata() {
    List<String> allow = Arrays.asList("GET", "HEAD", "OPTIONS");
    Map<String, List<String>> headers = new HashMap<>();
    headers.put("Allow", allow);
    headers.put("Content-Type", Arrays.asList("text/xml"));

    ResponseHeadersAsObjectMetadata handler = new ResponseHeadersAsObjectMetadata("");
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage();
    handler.addMetadata(headers, msg);
    assertNotNull(msg.getObjectHeaders().get("Allow"));
    assertNotNull(((URLConnectionHeaderField) msg.getObjectHeaders().get("Allow")).getKey());
    assertEquals("Allow",
        ((URLConnectionHeaderField) msg.getObjectHeaders().get("Allow")).getKey());
    assertTrue(((URLConnectionHeaderField) msg.getObjectHeaders().get("Allow")).getValues()
        .contains("HEAD"));
  }

  @Test
  public void testConstructor() {
    ResponseHeadersAsObjectMetadata handler = new ResponseHeadersAsObjectMetadata("");
    assertEquals("", handler.getMetadataPrefix());
  }

}
