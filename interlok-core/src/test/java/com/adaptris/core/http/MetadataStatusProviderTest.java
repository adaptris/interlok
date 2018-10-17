/*
 * Copyright 2015 Adaptris Ltd.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/

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
