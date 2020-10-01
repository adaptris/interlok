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
import org.apache.commons.lang3.text.WordUtils;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.metadata.RegexMetadataFilter;

@SuppressWarnings("deprecation")
public class MetadataKeyToCapitalCaseTest extends MetadataKeyCaseChanger {

  private static final String KEY_START_KEY = "KEY";
  private static final String KEY_MATCH = "^" + KEY_START_KEY + "$";

  private static final String KEY_RESULT_KEY = WordUtils.capitalizeFully(KEY_START_KEY);
  private static final String KEY_UNMATCHED = "yetAnotherKey";
  private static final String HELLO_WORLD = "Hello World";
  private static final String METADATA_VALUE = "VALUE";


  @Override
  protected void addMetadata(AdaptrisMessage msg) {
    msg.addMetadata(KEY_START_KEY, METADATA_VALUE);
    msg.addMetadata(KEY_UNMATCHED, HELLO_WORLD);
  }

  @Override
  protected MetadataKeyToCapitalCase createService() {
    MetadataKeyToCapitalCase service = new MetadataKeyToCapitalCase();
    RegexMetadataFilter filter = new RegexMetadataFilter();
    filter.addIncludePattern(KEY_MATCH);
    service.setKeysToModify(filter);
    return service;
  }

  @Override
  protected void doAssertions(AdaptrisMessage msg) {
    assertFalse(msg.containsKey(KEY_START_KEY));
    assertTrue(msg.containsKey(KEY_RESULT_KEY));
    assertEquals(METADATA_VALUE, msg.getMetadataValue(KEY_RESULT_KEY));
    assertTrue(msg.containsKey(KEY_UNMATCHED));
    assertEquals(HELLO_WORLD, msg.getMetadataValue(KEY_UNMATCHED));
  }
}
