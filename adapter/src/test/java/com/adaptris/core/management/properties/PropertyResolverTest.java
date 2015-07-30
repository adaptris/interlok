package com.adaptris.core.management.properties;

import static com.adaptris.core.management.SystemPropertiesUtilTest.encode;
import static org.junit.Assert.assertEquals;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class PropertyResolverTest {

  private static final String DEFAULT_VALUE = "Back At The Chicken Shack 1960";

  @Before
  public void setUp() throws Exception {
  }

  @After
  public void tearDown() throws Exception {
  }

  @Test
  public void testNoDecoding() throws Exception {
    PropertyResolver resolver = PropertyResolver.getDefaultInstance();
    resolver.init();
    assertEquals(DEFAULT_VALUE, resolver.resolve(DEFAULT_VALUE));
  }


  @Test
  public void testDecoding() throws Exception {
    PropertyResolver resolver = PropertyResolver.getDefaultInstance();
    resolver.init();
    assertEquals(DEFAULT_VALUE, resolver.resolve(encode(DEFAULT_VALUE)));
  }


}
