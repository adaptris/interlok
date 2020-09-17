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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import org.junit.Test;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.util.KeyValuePair;
import com.adaptris.util.KeyValuePairList;

@SuppressWarnings("deprecation")
public class MapMetadataServiceTest extends MetadataServiceExample {

  private static final String METADATA_KEY = "theMetadataKey";
  private static final String BASIC_NEW_VALUE = "newMetadataValueToBeSet";
  private static final String BASIC_MATCH_VALUE = "theMetadataValueToMatchAgainst";

  private static final String REGEXP_VALUE_TO_MATCH = "ABCEF_VER_PDF";
  private static final String REGEXP = "(.*)_(.*)_(.*)";
  private static final String REGEX_MATCH_GROUP = "{2}";
  private static final String REGEX_NEW_METADATA_VALUE = "VER";

  private static final String UNMATCHED_REGXP = "ABCDEFGHIJKL(.*)";


  private MapMetadataService createService() {
    KeyValuePairList kvps = new KeyValuePairList();
    kvps.addKeyValuePair(new KeyValuePair(BASIC_MATCH_VALUE, BASIC_NEW_VALUE));
    kvps.addKeyValuePair(new KeyValuePair(UNMATCHED_REGXP, BASIC_NEW_VALUE));
    kvps.addKeyValuePair(new KeyValuePair(UNMATCHED_REGXP, "{1}"));
    kvps.addKeyValuePair(new KeyValuePair(REGEXP, REGEX_MATCH_GROUP));
    MapMetadataService service = new MapMetadataService();
    service.setMetadataKeyMap(kvps);
    service.setMetadataKey(METADATA_KEY);
    return service;
  }

  @Test
  public void testSetter() throws Exception {
    MapMetadataService service = createService();
    try {
      service.setMetadataKeyMap(null);
      fail("Success with metadata keys null");
    }
    catch (IllegalArgumentException expected) {
      ;
    }
  }

  @Test
  public void testReplacementNoMatch() throws Exception {
    MapMetadataService service = new MapMetadataService();
    service.setMetadataKey(null);
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage("zzzzzzzz");
    execute(service, msg);
    assertFalse(msg.containsKey(METADATA_KEY));
  }

  @Test
  public void testReplacementNoMap() throws Exception {
    MapMetadataService service = new MapMetadataService();
    service.setMetadataKey(METADATA_KEY);
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage("zzzzzzzz");
    msg.addMetadata(METADATA_KEY, BASIC_MATCH_VALUE);
    execute(service, msg);
    assertEquals(BASIC_MATCH_VALUE, msg.getMetadataValue(METADATA_KEY));
  }

  @Test
  public void testReplacementNoMatchingKey() throws Exception {
    MapMetadataService service = createService();
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage("zzzzzzzz");
    execute(service, msg);
    assertFalse(msg.containsKey(METADATA_KEY));
  }

  @Test
  public void testReplacement() throws Exception {
    MapMetadataService service = createService();
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance()
        .newMessage("zzzzzzzz");
    msg.addMetadata(METADATA_KEY, BASIC_MATCH_VALUE);
    execute(service, msg);
    assertTrue(msg.getMetadataValue(METADATA_KEY).equals(BASIC_NEW_VALUE));
  }

  @Test
  public void testReplacement_Resolved() throws Exception {
    MapMetadataService service = createService();
    KeyValuePairList kvps = new KeyValuePairList();
    kvps.addKeyValuePair(new KeyValuePair(BASIC_MATCH_VALUE, "%message{ResolvedKey}"));
    service.setMetadataKeyMap(kvps);
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage("zzzzzzzz");
    msg.addMetadata(METADATA_KEY, BASIC_MATCH_VALUE);
    msg.addMetadata("ResolvedKey", BASIC_NEW_VALUE);
    execute(service, msg);
    assertTrue(msg.getMetadataValue(METADATA_KEY).equals(BASIC_NEW_VALUE));
  }

  @Test
  public void testRegexGroupReplacement() throws Exception {
    MapMetadataService service = createService();
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance()
        .newMessage("zzzzzzzz");
    msg.addMetadata(METADATA_KEY, REGEXP_VALUE_TO_MATCH);
    service.doService(msg);
    assertTrue(msg.getMetadataValue(METADATA_KEY).equals(REGEX_NEW_METADATA_VALUE));
  }

  @Override
  protected Object retrieveObjectForSampleConfig() {
    return createService();
  }
}
