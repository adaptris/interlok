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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import java.util.HashMap;
import java.util.Map;
import org.junit.Test;
import org.w3c.dom.Document;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.ServiceException;


public class ObjectMetadataParameterTest extends TransformParameterCase {

  @Test
  public void testConstructor() throws Exception {
    ObjectMetadataParameter p = new ObjectMetadataParameter();
    assertNull(p.getObjectMetadataKeyRegexp());
    p = new ObjectMetadataParameter(".*");
    assertEquals(".*", p.getObjectMetadataKeyRegexp());
    try {
      p.setObjectMetadataKeyRegexp(null);
      fail();
    } catch (IllegalArgumentException e) {
      
    }
    assertEquals(".*", p.getObjectMetadataKeyRegexp());
  }

  @Test
  public void testCreateParameters() throws Exception {
    ObjectMetadataParameter p = new ObjectMetadataParameter(".*");
    AdaptrisMessage msg = createMessage();
    Map map = p.createParameters(msg, null);
    assertNotNull(map);
    assertTrue(map.containsKey(KEY_OBJECT_METADATA));
    assertTrue(map.get(KEY_OBJECT_METADATA) instanceof Document);

    Map existing = new HashMap();
    existing.put("key", "value");
    map = p.createParameters(msg, existing);
    assertNotNull(map);
    assertTrue(map.containsKey(KEY_OBJECT_METADATA));
    assertTrue(map.get(KEY_OBJECT_METADATA) instanceof Document);
    assertTrue(map.containsKey("key"));
  }

  @Test
  public void testCreateParameters_NoRegexp() throws Exception {
    ObjectMetadataParameter p = new ObjectMetadataParameter();
    AdaptrisMessage msg = createMessage();
    try {
      Map map = p.createParameters(msg, null);
      fail();
    }
    catch (ServiceException e) {

    }
  }

  @Test
  public void testCreateParameters_UnmatchedRegexp() throws Exception {
    ObjectMetadataParameter p = new ObjectMetadataParameter(".*Unmatched.*");
    AdaptrisMessage msg = createMessage();
    Map map = p.createParameters(msg, null);
    assertNotNull(map);
    assertEquals(0, map.size());
  }

}
