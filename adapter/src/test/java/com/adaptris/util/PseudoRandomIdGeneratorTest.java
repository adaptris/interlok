package com.adaptris.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class PseudoRandomIdGeneratorTest {

  @Test
  public void testCreateId() throws Exception {
    IdGenerator guid = new PseudoRandomIdGenerator();
    assertNotNull(guid.create(guid));
  }

  @Test
  public void testCreateIdWithNull() throws Exception {
    IdGenerator guid = new PseudoRandomIdGenerator();
    assertNotNull(guid.create(null));
  }

  @Test
  public void testPrefix() throws Exception {
    PseudoRandomIdGenerator guid = new PseudoRandomIdGenerator("prefix");
    assertEquals("prefix", guid.getPrefix());
    assertNotNull(guid.create(guid));
    assertTrue(guid.create(guid).startsWith("prefix"));
    assertEquals(18, guid.create(guid).length());
  }
}
