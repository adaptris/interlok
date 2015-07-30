/*
 * $RCSfile: MetadataElementTest.java,v $
 * $Revision: 1.6 $
 * $Date: 2006/05/23 06:35:00 $
 * $Author: hfraser $
 */
package com.adaptris.core;

import junit.framework.TestCase;

public class MetadataElementTest extends TestCase {

  private MetadataElement me1;
  private MetadataElement me2;
  private MetadataElement me3;
  private MetadataElement me4;

  public MetadataElementTest(java.lang.String testName) {
    super(testName);
  }

  @Override
  protected void setUp() {
    me1 = new MetadataElement("key1", "val1");
    me2 = new MetadataElement("key1", "val1");
    me3 = new MetadataElement("key1", "val2");
    me4 = new MetadataElement("key2", "val2");
  }

  public void testSetKey() {
    try {
      me1.setKey(null);
      fail("should raise an IllegalArgumentException");
    }
    catch (IllegalArgumentException e) { /* ok */ }

    try {
      me1.setKey("");
      fail("should raise an IllegalArgumentException");
    }
    catch (IllegalArgumentException e) { /* ok */ }
  }


  public void testGetKey() {
    assertTrue("key1".equals(me1.getKey()));
  }


  public void testSetValue() {
    try {
      me1.setValue(null);
      fail("should raise an IllegalArgumentException");
    }
    catch (IllegalArgumentException e) { /* ok */ }

    try {
      me1.setValue("");
//      fail("should raise an IllegalArgumentException");
    }
    catch (IllegalArgumentException e) {
      fail("empty is allowed");
    }
  }

  public void testClose() throws Exception {
    MetadataElement cloned = (MetadataElement) me1.clone();
    assertEquals(me1, cloned);
  }
  public void testGetValue() {
    assertTrue("val1".equals(me1.getValue()));
  }

  public void testEquals() {
    assertTrue(me1.equals(me2));
    assertTrue(me1.equals(me3));
    assertTrue(!me1.equals(me4));
    assertFalse(me1.equals(new Object()));
  }

  public void testHashCode() {
    assertTrue(me1.hashCode() == me2.hashCode());
    assertTrue(me1.hashCode() == me3.hashCode());
    assertTrue(!(me1.hashCode() == me4.hashCode()));
  }

  public void testConstructors() {
    new MetadataElement();
    new MetadataElement("key", "val");

    try {
      new MetadataElement(null, null);
      fail("should raise an IllegalArgumentException");
    }
    catch (IllegalArgumentException e) { /* ok */ }

    try {
      new MetadataElement("", "");
      fail("should raise an IllegalArgumentException");
    }
    catch (IllegalArgumentException e) { /* ok */ }
  }
}
