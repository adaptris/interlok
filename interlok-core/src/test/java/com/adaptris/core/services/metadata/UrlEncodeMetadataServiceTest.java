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

import static com.adaptris.core.services.metadata.UrlDecodeMetadataServiceTest.DECODED;
import static com.adaptris.core.services.metadata.UrlDecodeMetadataServiceTest.ENCODED;
import static com.adaptris.core.services.metadata.UrlDecodeMetadataServiceTest.SOURCE_METADATA_KEY;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Test;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageFactory;

public class UrlEncodeMetadataServiceTest extends MetadataServiceExample {

  private static AdaptrisMessage createMessage() {
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage();
    msg.addMetadata(SOURCE_METADATA_KEY, DECODED);
    return msg;
  }

  @Override
  public boolean isAnnotatedForJunit4() {
    return true;
  }

  @Test
  public void testService() throws Exception {
    UrlEncodeMetadataService service = new UrlEncodeMetadataService(SOURCE_METADATA_KEY);
    AdaptrisMessage msg = createMessage();
    execute(service, msg);
    assertTrue(msg.headersContainsKey(SOURCE_METADATA_KEY));
    assertEquals(ENCODED, msg.getMetadataValue(SOURCE_METADATA_KEY));
  }

  @Override
  protected Object retrieveObjectForSampleConfig() {
    return new UrlEncodeMetadataService(".*matchingMetadataKeysWhichNeedToEncoded.*");
  }

  @Override
  protected String getExampleCommentHeader(Object o) {
    return super.getExampleCommentHeader(o) + "<!-- This is used to URL Encoded metadata values" + "\n-->\n";
  }
}
