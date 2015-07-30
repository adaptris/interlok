package com.adaptris.core.transform;

import java.util.HashMap;
import java.util.Map;

import com.adaptris.core.AdaptrisMessage;

public class IgnoreMetadataParameterTest extends TransformParameterCase {
  public IgnoreMetadataParameterTest(String name) {
    super(name);
  }

  public void testIgnoreMetadataParameter() throws Exception {
    IgnoreMetadataParameter p = new IgnoreMetadataParameter();
    Map existingParams = new HashMap();
    AdaptrisMessage msg = createMessage();
    assertNull(p.createParameters(msg, existingParams));
  }
}
