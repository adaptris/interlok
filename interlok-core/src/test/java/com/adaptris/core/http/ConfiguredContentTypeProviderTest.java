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

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.DefaultMessageFactory;

public class ConfiguredContentTypeProviderTest {

  
  

  @BeforeEach
  public void setUp() throws Exception {}

  @AfterEach
  public void tearDown() throws Exception {}

  @Test
  public void testGetContentType(TestInfo info) throws Exception {
    ConfiguredContentTypeProvider provider = new ConfiguredContentTypeProvider("text/complicated");

    AdaptrisMessage msg = new DefaultMessageFactory().newMessage("");
    msg.addMetadata(info.getDisplayName(), "text/complicated");

    String contentType = provider.getContentType(msg);
    assertEquals("text/complicated", contentType);
  }

  @Test
  public void testGetContentType_ConfiguredCharset(TestInfo info) throws Exception {
    ConfiguredContentTypeProvider provider = new ConfiguredContentTypeProvider("text/complicated; charset=ISO-8859-1");

    AdaptrisMessage msg = new DefaultMessageFactory().newMessage("");
    msg.addMetadata(info.getDisplayName(), "text/complicated");

    String contentType = provider.getContentType(msg);
    assertEquals("text/complicated; charset=ISO-8859-1", contentType);
  }

  @Test
  public void testGetContentType_WithCharset() throws Exception {
    ConfiguredContentTypeProvider provider = new ConfiguredContentTypeProvider("text/complicated");

    AdaptrisMessage msg = new DefaultMessageFactory().newMessage("");
    msg.setContentEncoding("UTF-8");
    String contentType = provider.getContentType(msg);
    assertEquals("text/complicated; charset=UTF-8", contentType);
  }

  @Test
  public void testGetContentType_ConfiguredWithCharset() throws Exception {
    ConfiguredContentTypeProvider provider = new ConfiguredContentTypeProvider("text/complicated; charset=ISO-8859-1");

    AdaptrisMessage msg = new DefaultMessageFactory().newMessage("");
    msg.setContentEncoding("UTF-8");
    String contentType = provider.getContentType(msg);
    assertEquals("text/complicated; charset=ISO-8859-1", contentType);
  }

  @Test
  public void testGetContentType_ConfiguredWithCrapParams() throws Exception {
    ConfiguredContentTypeProvider provider = new ConfiguredContentTypeProvider("text/complicated; shouldbreak=");

    AdaptrisMessage msg = new DefaultMessageFactory().newMessage("");
    msg.setContentEncoding("UTF-8");
    String contentType = provider.getContentType(msg);
    assertEquals("text/complicated; shouldbreak=; charset=UTF-8", contentType);
  }

}
