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

import static org.junit.Assert.assertNotEquals;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageFactory;

@SuppressWarnings("deprecation")
public class TrimMetadataServiceTest extends MetadataServiceExample {

  private static final String SOURCE_METADATA_KEY = "sourceMetadataKey";
  private static final String PADDED = "  ABCDEFG   ";
  private static final String TRIMMED = PADDED.trim();
  public TrimMetadataServiceTest(java.lang.String testName) {
    super(testName);
  }

  @Override
  protected void setUp() throws Exception {
  }

  private static AdaptrisMessage createMessage() {
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage();
    msg.addMetadata(SOURCE_METADATA_KEY, PADDED);
    return msg;
  }

  public void testService_NoKey() throws Exception {
    TrimMetadataService service = new TrimMetadataService("");
    AdaptrisMessage msg = createMessage();
    execute(service, msg);
    assertTrue(msg.containsKey(SOURCE_METADATA_KEY));
    assertNotEquals(TRIMMED, msg.getMetadataValue(SOURCE_METADATA_KEY));
    assertEquals(PADDED, msg.getMetadataValue(SOURCE_METADATA_KEY));
  }

  public void testService() throws Exception {
    TrimMetadataService service = new TrimMetadataService(SOURCE_METADATA_KEY);
    AdaptrisMessage msg = createMessage();
    execute(service, msg);
    assertTrue(msg.containsKey(SOURCE_METADATA_KEY));
    assertEquals(TRIMMED, msg.getMetadataValue(SOURCE_METADATA_KEY));
  }

  public void testServiceWithEmptyString() throws Exception {
    TrimMetadataService service = new TrimMetadataService(SOURCE_METADATA_KEY);
    AdaptrisMessage msg = createMessage();
    msg.addMetadata(SOURCE_METADATA_KEY, "");
    execute(service, msg);
    assertTrue(msg.containsKey(SOURCE_METADATA_KEY));
    assertEquals("", msg.getMetadataValue(SOURCE_METADATA_KEY));
  }

  @Override
  protected Object retrieveObjectForSampleConfig() {
    return new TrimMetadataService(".*matchingMetadataKeysWhichNeedToBeTrimmed.*");
  }

  @Override
  protected String getExampleCommentHeader(Object o) {
    return super.getExampleCommentHeader(o) + "<!-- This is used to trim metadata values of whitespace" + "\n-->\n";
  }
}
