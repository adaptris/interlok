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

package com.adaptris.core.http.client.net;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.HttpURLConnection;
import java.net.URL;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.core.Channel;
import com.adaptris.util.KeyValuePair;
import com.adaptris.util.KeyValuePairSet;

public class ConfiguredRequestHeadersTest extends RequestHeadersCase {

  @BeforeEach
  public void setUp() throws Exception {}

  @AfterEach
  public void tearDown() throws Exception {}

  @Test
  public void testSetHandlers(TestInfo info) throws Exception {
    ConfiguredRequestHeaders headers = new ConfiguredRequestHeaders();
    assertNotNull(headers.getHeaders());
    assertEquals(0, headers.getHeaders().size());
    String name = info.getDisplayName();
    headers.getHeaders().add(new KeyValuePair(name, name));
    assertEquals(1, headers.getHeaders().size());
    headers.setHeaders(new KeyValuePairSet());
    assertEquals(0, headers.getHeaders().size());
  }

  @Test
  public void testAddHeaders(TestInfo info) throws Exception {

    Channel c = null;
    HttpURLConnection urlC = null;
    try {
      c = HttpHelper.createAndStartChannel();
      URL url = new URL(HttpHelper.createProduceDestination(c));
      urlC = (HttpURLConnection) url.openConnection();
      String name = info.getDisplayName();
      ConfiguredRequestHeaders headers = new ConfiguredRequestHeaders();
      headers.getHeaders().add(new KeyValuePair(name, name));
      AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage("");
      msg.addMetadata(name, name);
      urlC = headers.addHeaders(msg, urlC);
      assertTrue(contains(urlC, name, name));
    } finally {
      HttpHelper.stopChannelAndRelease(c);
    }
  }



}
