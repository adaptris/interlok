package com.adaptris.core.http;

import static org.junit.Assert.assertEquals;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.DefaultMessageFactory;
import com.adaptris.core.http.client.ConfiguredRequestMethodProvider;
import com.adaptris.core.http.client.RequestMethodProvider.RequestMethod;

public class ConfiguredRequestProviderTest {

  @Before
  public void setUp() throws Exception {}

  @After
  public void tearDown() throws Exception {

  }

  @Test
  public void testGetMethod() {
    ConfiguredRequestMethodProvider prov = new ConfiguredRequestMethodProvider(RequestMethod.GET);
    AdaptrisMessage msg = new DefaultMessageFactory().newMessage("");
    assertEquals(RequestMethod.GET, prov.getMethod(msg));
  }



}
