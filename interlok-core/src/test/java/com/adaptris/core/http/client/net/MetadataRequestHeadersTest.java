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
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.net.HttpURLConnection;
import java.net.URL;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.core.Channel;
import com.adaptris.core.metadata.NoOpMetadataFilter;
import com.adaptris.core.metadata.RegexMetadataFilter;

public class MetadataRequestHeadersTest extends RequestHeadersCase {

  @Test
  public void testFilter() throws Exception {
    MetadataRequestHeaders headers = new MetadataRequestHeaders();
    assertNull(headers.getFilter());
    headers.setFilter(new NoOpMetadataFilter());
    assertEquals(NoOpMetadataFilter.class, headers.getFilter().getClass());
    try {
      headers.setFilter(null);
      fail();
    } catch (IllegalArgumentException expected) {

    }
    assertEquals(NoOpMetadataFilter.class, headers.getFilter().getClass());
  }

  @Test
  public void testAddHeaders(TestInfo info) throws Exception {
    Channel c = null;
    HttpURLConnection urlC = null;
    try {
      c = HttpHelper.createAndStartChannel();
      URL url = new URL(HttpHelper.createProduceDestination(c));
      urlC = (HttpURLConnection) url.openConnection();
      MetadataRequestHeaders headers = new MetadataRequestHeaders();
      headers.setFilter(new RegexMetadataFilter());
      AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage("");
      String name = info.getDisplayName();
      msg.addMetadata(name, name);
      urlC = headers.addHeaders(msg, urlC);
      assertTrue(contains(urlC, name, name));
    } finally {
      HttpHelper.stopChannelAndRelease(c);
    }
  }

  @Test
  public void testAddHeaders_Flatten(TestInfo info) throws Exception {
    Channel c = null;
    HttpURLConnection urlC = null;
    try {
      c = HttpHelper.createAndStartChannel();
      URL url = new URL(HttpHelper.createProduceDestination(c));
      urlC = (HttpURLConnection) url.openConnection();
      MetadataRequestHeaders headers = new MetadataRequestHeaders();
      headers.setUnfold(true);
      headers.setFilter(new RegexMetadataFilter());
      AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage("");
      String name = info.getDisplayName();
      msg.addMetadata(name, name + "\r\n     " + name);
      urlC = headers.addHeaders(msg, urlC);
      assertTrue(contains(urlC, name, name + " " + name));
    }
    finally {
      HttpHelper.stopChannelAndRelease(c);
    }
  }

}
