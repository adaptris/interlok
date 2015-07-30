package com.adaptris.core.jdbc;

import junit.framework.TestCase;

import com.adaptris.util.text.NullPassThroughConverter;
import com.adaptris.util.text.NullsNotSupportedConverter;

public abstract class NullableParameterCase extends TestCase {

  public void setUp() throws Exception {

  }

  public void testNullConverter() throws Exception {
    NullableParameter param = createParameter();
    assertNull(param.getNullConverter());
    assertEquals(NullPassThroughConverter.class, param.nullConverter().getClass());

    param.setNullConverter(new NullsNotSupportedConverter());
    assertEquals(NullsNotSupportedConverter.class, param.getNullConverter().getClass());
    assertEquals(NullsNotSupportedConverter.class, param.nullConverter().getClass());

    param.setNullConverter(null);
    assertNull(param.getNullConverter());
    assertEquals(NullPassThroughConverter.class, param.nullConverter().getClass());

  }

  protected abstract NullableParameter createParameter();
}
