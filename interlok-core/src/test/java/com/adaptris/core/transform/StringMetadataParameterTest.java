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

package com.adaptris.core.transform;

import java.util.HashMap;
import java.util.Map;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.metadata.RegexMetadataFilter;


public class StringMetadataParameterTest extends TransformParameterCase {
  public StringMetadataParameterTest(String name) {
    super(name);
  }

  public void testConstructor() throws Exception {
    StringMetadataParameter p = new StringMetadataParameter();
    assertNotNull(p.getMetadataFilter());
    p = new StringMetadataParameter(new String[]
    {
      "abc"
    }, new String[]
    {
      "def"
    });
    assertNotNull(p.getMetadataFilter());
    RegexMetadataFilter filter = p.getMetadataFilter();
    assertEquals(1, filter.getIncludePatterns().size());
    assertEquals(1, filter.getExcludePatterns().size());
    assertEquals("abc", filter.getIncludePatterns().get(0));
    assertEquals("def", filter.getExcludePatterns().get(0));

    try {
      p.setMetadataFilter(null);
      fail();
    }
    catch (IllegalArgumentException e) {

    }
    assertNotNull(p.getMetadataFilter());

  }

  public void testCreateParameters() throws Exception {
    StringMetadataParameter p = new StringMetadataParameter();
    AdaptrisMessage msg = createMessage();
    Map map = p.createParameters(msg, null);
    assertNotNull(map);
    assertTrue(map.containsKey(KEY_STRING_METADATA));
    assertEquals(METADATA_VALUE, map.get(KEY_STRING_METADATA));

    Map existing = new HashMap();
    existing.put("key", "value");
    map = p.createParameters(msg, existing);
    assertNotNull(map);
    assertTrue(map.containsKey(KEY_STRING_METADATA));
    assertEquals(METADATA_VALUE, map.get(KEY_STRING_METADATA));
  }

  public void testCreateParameters_UnmatchedRegexp() throws Exception {
    StringMetadataParameter p = new StringMetadataParameter(new String[]
    {
      "abc"
    }, new String[] {});
    AdaptrisMessage msg = createMessage();
    Map existing = new HashMap();
    Map map = p.createParameters(msg, existing);
    // It's unmatched so nothing to include here, should be the same as "existing".
    assertNotNull(map);
    assertTrue(map == existing);
    assertEquals(0, map.size());
  }

}
