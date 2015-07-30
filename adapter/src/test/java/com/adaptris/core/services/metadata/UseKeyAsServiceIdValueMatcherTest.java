package com.adaptris.core.services.metadata;

import com.adaptris.core.BaseCase;

public class UseKeyAsServiceIdValueMatcherTest extends BaseCase {

  public UseKeyAsServiceIdValueMatcherTest(String name) {
    super(name);
  }

  public void testMatcher() throws Exception {
    UseKeyAsServiceIdValueMatcher matcher = new UseKeyAsServiceIdValueMatcher();
    assertEquals("key", matcher.getNextServiceId("key", null));
  }
}
