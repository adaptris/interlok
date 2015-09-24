package com.adaptris.core.http;

import static org.junit.Assert.assertEquals;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.DefaultMessageFactory;
import com.adaptris.core.http.client.MetadataRequestMethodProvider;
import com.adaptris.core.http.client.RequestMethodProvider.RequestMethod;

public class MetadataRequestMethodProviderTest {

  @Before
  public void setUp() throws Exception {}

  @After
  public void tearDown() throws Exception {

  }

  @Test
  public void testDefaultMethod() {
    MetadataRequestMethodProvider prov = new MetadataRequestMethodProvider("httpMethod");
    AdaptrisMessage msg = new DefaultMessageFactory().newMessage("");
    assertEquals(RequestMethod.POST, prov.getMethod(msg));
  }


  @Test
  public void testGetMethod_WithMetadata() {
    MetadataRequestMethodProvider prov = new MetadataRequestMethodProvider("httpMethod");
    AdaptrisMessage msg = new DefaultMessageFactory().newMessage("");
    msg.addMetadata("httpMethod", "GET");

    assertEquals(RequestMethod.GET, prov.getMethod(msg));
  }


}
