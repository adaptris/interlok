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
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Test;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageFactory;

public class UrlDecodeMetadataServiceTest extends MetadataServiceExample {

  public static final String SOURCE_METADATA_KEY = "sourceMetadataKey";
  public static final String ENCODED = "2016-01-01T12%3A00%3A00Z";
  public static final String DECODED = "2016-01-01T12:00:00Z";


  private static AdaptrisMessage createMessage() {
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage();
    msg.addMetadata(SOURCE_METADATA_KEY, ENCODED);
    return msg;
  }

  @Test
  public void testService() throws Exception {
    UrlDecodeMetadataService service = new UrlDecodeMetadataService(SOURCE_METADATA_KEY);
    AdaptrisMessage msg = createMessage();
    execute(service, msg);
    assertTrue(msg.headersContainsKey(SOURCE_METADATA_KEY));
    assertEquals(DECODED, msg.getMetadataValue(SOURCE_METADATA_KEY));
  }

  @Override
  protected Object retrieveObjectForSampleConfig() {
    return new UrlDecodeMetadataService(".*matchingMetadataKeysWhichNeedToDecoded.*");
  }

  @Override
  protected String getExampleCommentHeader(Object o) {
    return super.getExampleCommentHeader(o) + "<!-- This is used to URL Decode metadata values" + "\n-->\n";
  }
}
