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
