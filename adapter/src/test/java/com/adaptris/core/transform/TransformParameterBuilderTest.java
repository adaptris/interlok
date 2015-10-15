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

public class TransformParameterBuilderTest extends TransformParameterCase {
  public TransformParameterBuilderTest(String name) {
    super(name);
  }

  public void testConstructor() throws Exception {
    XmlTransformParameterBuilder p = new XmlTransformParameterBuilder();
    assertNotNull(p.getParameterBuilders());
    assertEquals(0, p.getParameterBuilders().size());
    p = new XmlTransformParameterBuilder(new StringMetadataParameter(), new ObjectMetadataParameter());
    assertNotNull(p.getParameterBuilders());
    assertEquals(2, p.getParameterBuilders().size());
    assertEquals(StringMetadataParameter.class, p.getParameterBuilders().get(0).getClass());
    assertEquals(ObjectMetadataParameter.class, p.getParameterBuilders().get(1).getClass());
    try {
      p.setParameterBuilders(null);
      fail();
    }
    catch (IllegalArgumentException e) {

    }
    assertNotNull(p.getParameterBuilders());

  }

  public void testCreateParameters() throws Exception {
    XmlTransformParameterBuilder p = new XmlTransformParameterBuilder(new StringMetadataParameter(), new ObjectMetadataParameter(
        ".*"));
    AdaptrisMessage msg = createMessage();
    Map map = p.createParameters(msg, null);
    assertNotNull(map);
    assertEquals(4, map.size());

    Map existing = new HashMap();
    existing.put("key", "value");
    map = p.createParameters(msg, existing);
    assertNotNull(map);
    assertEquals(5, map.size());
    assertTrue(map.containsKey(KEY_STRING_METADATA));
    assertEquals(METADATA_VALUE, map.get(KEY_STRING_METADATA));
  }

}
