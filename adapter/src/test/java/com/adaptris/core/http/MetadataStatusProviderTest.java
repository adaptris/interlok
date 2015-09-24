package com.adaptris.core.http;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;

import java.net.HttpURLConnection;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.DefaultMessageFactory;
import com.adaptris.core.http.server.MetadataStatusProvider;

public class MetadataStatusProviderTest {

  @Before
  public void setUp() throws Exception {}

  @After
  public void tearDown() throws Exception {

  }

  @Test
  public void testGetStatus_MissingMetadata() {
    MetadataStatusProvider prov = new MetadataStatusProvider("httpStatus");
    AdaptrisMessage msg = new DefaultMessageFactory().newMessage("");
    assertEquals(HttpURLConnection.HTTP_INTERNAL_ERROR, prov.getStatus(msg).getCode());
    assertEquals("Internal Server Error", prov.getStatus(msg).getText());
  }

  @Test
  public void testGetStatus_MissingMetadata_WithText() {
    MetadataStatusProvider prov = new MetadataStatusProvider("httpStatus", "httpStatusText");
    AdaptrisMessage msg = new DefaultMessageFactory().newMessage("");
    msg.addMetadata("httpStatusText", "Really Not OK");
    assertEquals(HttpURLConnection.HTTP_INTERNAL_ERROR, prov.getStatus(msg).getCode());
    assertNotSame("Internal Server Error", prov.getStatus(msg).getText());
    assertEquals("Really Not OK", prov.getStatus(msg).getText());
  }


  @Test
  public void testGetStatus_WithMetadata() {
    MetadataStatusProvider prov = new MetadataStatusProvider("httpStatus");
    AdaptrisMessage msg = new DefaultMessageFactory().newMessage("");
    msg.addMetadata("httpStatus", "200");
    assertEquals(HttpURLConnection.HTTP_OK, prov.getStatus(msg).getCode());
    assertEquals("OK", prov.getStatus(msg).getText());
  }


  @Test
  public void testGetStatus_WithText() {
    MetadataStatusProvider prov = new MetadataStatusProvider("httpStatus", "httpStatusText");
    AdaptrisMessage msg = new DefaultMessageFactory().newMessage("");
    msg.addMetadata("httpStatus", "200");
    msg.addMetadata("httpStatusText", "Really Not OK");
    assertEquals(HttpURLConnection.HTTP_OK, prov.getStatus(msg).getCode());
    assertNotSame("OK", prov.getStatus(msg).getText());
    assertEquals("Really Not OK", prov.getStatus(msg).getText());
  }


}
