package com.adaptris.core;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;

public class AdaptrisMessageFactoryTest {

  @Test
  public void testGetDefaultInstance() {
    AdaptrisMessageFactory m1 = AdaptrisMessageFactory.getDefaultInstance();
    assertNotNull(m1);
    assertNotNull(m1.newMessage());
    AdaptrisMessageFactory m2 = AdaptrisMessageFactory.getDefaultInstance();
    assertNotNull(m2);
    assertEquals(m1, m2);
  }

}
