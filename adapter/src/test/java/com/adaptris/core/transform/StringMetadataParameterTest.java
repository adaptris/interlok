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
