package com.adaptris.core.http;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.DefaultMessageFactory;
import com.adaptris.core.http.HttpStatusProvider.HttpStatus;

public class ConfiguredStatusProviderTest {

  @Before
  public void setUp() throws Exception {}

  @After
  public void tearDown() throws Exception {

  }

  @Test
  public void testGetStatus() {
    ConfiguredStatusProvider prov = new ConfiguredStatusProvider(HttpStatus.OK_200);
    AdaptrisMessage msg = new DefaultMessageFactory().newMessage("");
    assertEquals(200, prov.getStatus(msg).getCode());
    assertEquals("OK", prov.getStatus(msg).getText());
  }

  @Test
  public void testGetStatus_WithText() {
    ConfiguredStatusProvider prov = new ConfiguredStatusProvider(HttpStatus.OK_200);
    prov.setText("Really Not OK");
    AdaptrisMessage msg = new DefaultMessageFactory().newMessage("");
    assertEquals(200, prov.getStatus(msg).getCode());
    assertNotSame("OK", prov.getStatus(msg).getText());
    assertEquals("Really Not OK", prov.getStatus(msg).getText());
  }


}
