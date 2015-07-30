/*
 * $RCSfile: AdaptrisMessageFactoryTest.java,v $
 * $Revision: 1.12 $
 * $Date: 2009/03/20 10:44:34 $
 * $Author: lchan $
 */
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
