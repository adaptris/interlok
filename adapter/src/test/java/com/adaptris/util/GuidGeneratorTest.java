package com.adaptris.util;

import static org.junit.Assert.assertNotNull;

import org.junit.Test;

public class GuidGeneratorTest {

  @Test
  public void testGetUUID() throws Exception {
    GuidGenerator guid = new GuidGenerator();
    assertNotNull(guid.getUUID());
  }

  @Test
  public void testCreateId() throws Exception {
    GuidGenerator guid = new GuidGenerator();
    assertNotNull(guid.create(new Object()));
  }

  @Test
  public void testCreateIdWithNull() throws Exception {
    GuidGenerator guid = new GuidGenerator();
    assertNotNull(guid.create(null));
  }

}
