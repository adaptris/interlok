package com.adaptris.core.http.client.net;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.Test;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageFactory;

public class ResponseHeaderAsMetadataTest {

  @Test
  public void testAddMetadata() {
    List<String> allow = Arrays.asList("GET", "HEAD", "OPTIONS");
    Map<String, List<String>> headers = new HashMap<>();
    headers.put("Allow", allow);
    headers.put("Content-Type", Arrays.asList("text/xml"));

    ResponseHeadersAsMetadata handler = new ResponseHeadersAsMetadata("", "|");
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage();
    handler.addMetadata(headers, msg);
    assertEquals("text/xml", msg.getMetadataValue("Content-Type"));
    assertEquals("GET|HEAD|OPTIONS", msg.getMetadataValue("Allow"));
  }

  @Test
  public void testConstructor_String() {
    ResponseHeadersAsMetadata handler = new ResponseHeadersAsMetadata("");
    assertEquals("", handler.getMetadataPrefix());
    assertNull(handler.getMetadataSeparator());
    assertEquals("\t", handler.metadataSeparator());
  }

  @Test
  public void testConstructor_StringString() {
    ResponseHeadersAsMetadata handler = new ResponseHeadersAsMetadata("", "|");
    assertEquals("", handler.getMetadataPrefix());
    assertEquals("|", handler.getMetadataSeparator());
    assertEquals("|", handler.metadataSeparator());
  }

}
