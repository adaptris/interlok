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

import java.util.Arrays;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.core.MetadataElement;
import com.adaptris.util.KeyValuePair;
import com.adaptris.util.KeyValuePairCollection;

public class CopyMetadataServiceTest extends MetadataServiceExample {

  private static final String NEW_KEY_1 = "Metadata_Key_To_Copy_To_1";
  private static final String NEW_KEY_2 = "Metadata_Key_To_Copy_To_2";
  private static final String NEW_KEY_3 = "Metadata_Key_To_Copy_To_3";
  private static final String NEW_KEY_4 = "Metadata_Key_To_Copy_To_4";
  private static final String METADATA_VALUE = "Some_Metadata_Value";
  private static final String KEY_TO_BE_COPIED = "Metadata_Key_To_Copy_From";

  public CopyMetadataServiceTest(String arg0) {
    super(arg0);
  }

  private CopyMetadataService createService() {
    CopyMetadataService service = new CopyMetadataService();
    service.setMetadataKeys(new KeyValuePairCollection(Arrays.asList(new KeyValuePair[] {
      new KeyValuePair(KEY_TO_BE_COPIED, NEW_KEY_1)
    })));
    return service;
  }

  private AdaptrisMessage createMessage() {
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage("zzzzzzzz");
    msg.addMetadata(KEY_TO_BE_COPIED, METADATA_VALUE);
    return msg;
  }

  public void testSetter() throws Exception {
    CopyMetadataService service = createService();
    try {
      service.setMetadataKeys(null);
      fail("Success with metadata keys null");
    }
    catch (IllegalArgumentException expected) {
      ;
    }
  }

  public void testService() throws Exception {
    CopyMetadataService service = createService();
    AdaptrisMessage msg = createMessage();
    execute(service, msg);

    assertTrue(msg.getMetadataValue(KEY_TO_BE_COPIED).equals(METADATA_VALUE));
    assertTrue(msg.getMetadataValue(NEW_KEY_1).equals(METADATA_VALUE));
  }

  public void testNewKeyOverwritten() throws Exception {
    CopyMetadataService service = createService();
    AdaptrisMessage msg = createMessage();
    msg.addMetadata(NEW_KEY_1, "zzz");
    execute(service, msg);

    assertTrue(msg.getMetadataValue(KEY_TO_BE_COPIED).equals(METADATA_VALUE));
    assertTrue(msg.getMetadataValue(NEW_KEY_1).equals(METADATA_VALUE)); // overwrites
  }

  public void testNoOriginalKey() throws Exception {
    CopyMetadataService service = createService();
    AdaptrisMessage msg = createMessage();
    msg.removeMetadata(new MetadataElement(KEY_TO_BE_COPIED, METADATA_VALUE));
    execute(service, msg);
    assertTrue(msg.getMetadata().size() == 0);
  }

  public void testBug2101_SameSourceKeyMultipleDestinationKeys() throws Exception {
    CopyMetadataService service = createService();
    service.getMetadataKeys().add(new KeyValuePair(KEY_TO_BE_COPIED, NEW_KEY_2));
    service.getMetadataKeys().add(new KeyValuePair(KEY_TO_BE_COPIED, NEW_KEY_3));
    service.getMetadataKeys().add(new KeyValuePair(KEY_TO_BE_COPIED, NEW_KEY_4));
    AdaptrisMessage msg = createMessage();
    execute(service, msg);
    assertTrue(msg.getMetadataValue(KEY_TO_BE_COPIED).equals(METADATA_VALUE));
    assertTrue(NEW_KEY_1 + " must exist as metadata", msg.containsKey(NEW_KEY_1));
    assertTrue(NEW_KEY_2 + " must exist as metadata", msg.containsKey(NEW_KEY_2));
    assertTrue(NEW_KEY_3 + " must exist as metadata", msg.containsKey(NEW_KEY_3));
    assertTrue(NEW_KEY_4 + " must exist as metadata", msg.containsKey(NEW_KEY_4));
    assertEquals(METADATA_VALUE, msg.getMetadataValue(NEW_KEY_1));
    assertEquals(METADATA_VALUE, msg.getMetadataValue(NEW_KEY_2));
    assertEquals(METADATA_VALUE, msg.getMetadataValue(NEW_KEY_3));
    assertEquals(METADATA_VALUE, msg.getMetadataValue(NEW_KEY_4));
  }

  @Override
  protected Object retrieveObjectForSampleConfig() {
    CopyMetadataService result = createService();
    result.getMetadataKeys().add(new KeyValuePair(KEY_TO_BE_COPIED, NEW_KEY_2));
    result.getMetadataKeys().add(new KeyValuePair("Another_Key_To_Be_Copied", "Metadata Key To Copy To"));
    return result;
  }

}
