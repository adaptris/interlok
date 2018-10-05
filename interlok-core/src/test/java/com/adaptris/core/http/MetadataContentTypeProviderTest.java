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
import static org.junit.Assert.fail;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.CoreException;
import com.adaptris.core.DefaultMessageFactory;

public class MetadataContentTypeProviderTest {

  @Rule
  public TestName testName = new TestName();

  @Before
  public void setUp() throws Exception {}

  @After
  public void tearDown() throws Exception {}

  @Test
  public void testGetContentType() throws Exception {
    MetadataContentTypeProvider provider = new MetadataContentTypeProvider(testName.getMethodName());

    AdaptrisMessage msg = new DefaultMessageFactory().newMessage("");
    msg.addMetadata(testName.getMethodName(), "text/complicated");

    String contentType = provider.getContentType(msg);
    assertEquals("text/complicated", contentType);
  }


  @Test
  public void testGetContentType_WithCharset() throws Exception {
    MetadataContentTypeProvider provider = new MetadataContentTypeProvider(testName.getMethodName());

    AdaptrisMessage msg = new DefaultMessageFactory().newMessage("");
    msg.setContentEncoding("UTF-8");
    msg.addMetadata(testName.getMethodName(), "text/complicated");

    String contentType = provider.getContentType(msg);
    assertEquals("text/complicated; charset=UTF-8", contentType);
  }


  @Test
  public void testGetContentType_MetadataKeyNonExistent() throws Exception {
    MetadataContentTypeProvider provider = new MetadataContentTypeProvider(testName.getMethodName());

    AdaptrisMessage msg = new DefaultMessageFactory().newMessage("");
    String contentType = provider.getContentType(msg);
    assertEquals("text/plain", contentType);
  }

  @Test
  public void testGetContentType_NullMetadataKey() throws Exception {
    MetadataContentTypeProvider provider = new MetadataContentTypeProvider();

    AdaptrisMessage msg = new DefaultMessageFactory().newMessage("");
    msg.addMetadata(testName.getMethodName(), "text/complicated");
    try {
      provider.getContentType(msg);
      fail();
    } catch (CoreException expected) {

    }
  }


}
