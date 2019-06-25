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

package com.adaptris.core.services.metadata;

import org.junit.Test;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.core.ServiceException;

public class ExtractMetadataServiceTest extends MetadataServiceExample {


  @Test
  public void testDoService() throws Exception {
    ExtractMetadataService service =
        new ExtractMetadataService().withRegularExpression("/path/(.*)/(.*)").withMetadataKeys("p1", "p2").withSourceKey("key");
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage();
    msg.addMessageHeader("key", "/path/value1/value2");
    execute(service, msg);
    assertTrue(msg.headersContainsKey("p1"));
    assertEquals("value1", msg.getMetadataValue("p1"));
    assertTrue(msg.headersContainsKey("p2"));
    assertEquals("value2", msg.getMetadataValue("p2"));
  }


  @Test
  public void testDoService_NoMatch() throws Exception {
    ExtractMetadataService service =
        new ExtractMetadataService().withRegularExpression("/path/(.*)/(.*)").withMetadataKeys("p1", "p2").withSourceKey("key");
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage();
    msg.addMessageHeader("key", "/alternative/value1/value2");
    execute(service, msg);
    assertFalse(msg.headersContainsKey("p1"));
    assertFalse(msg.headersContainsKey("p2"));
  }

  @Test
  public void testDoService_Exception() throws Exception {
    ExtractMetadataService service =
        new ExtractMetadataService().withRegularExpression("/path/(.*)/(.*)").withSourceKey("key");
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage();
    msg.addMessageHeader("key", "/path/value1/value2");
    try {
      execute(service, msg);
      fail();
    } catch (ServiceException expected) {

    }
  }

  @Override
  protected ExtractMetadataService retrieveObjectForSampleConfig() {
    return new ExtractMetadataService().withSourceKey("MetadataKey")
        .withRegularExpression("regular-expression/(.*)/with/groups/(.*)").withMetadataKeys("matchGroup1", "matchGroup2");
  }
}
