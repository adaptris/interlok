/*
 * $RCSfile: OlderThanTest.java,v $
 * $Revision: 1.3 $
 * $Date: 2009/03/27 13:42:12 $
 * $Author: lchan $
 */
package com.adaptris.core.fs;

import java.io.File;

import com.adaptris.core.BaseCase;

public class OlderThanTest extends BaseCase {

  private File file;

  public OlderThanTest(java.lang.String testName) {
    super(testName);
  }

  @Override
  protected void setUp() throws Exception {
    file = new File(PROPERTIES.getProperty("fs.LastModifiedFilter"));
  }

// It appears that CVS doesn't always preserve the last-modified
// of the file actually in CVS...
//  public void testFilterDefault() throws Exception {
//    OlderThan filter = new OlderThan();
//    filter.setAdditionalDebug(true);
//    assertTrue(filter.accept(file));
//  }

  public void testOlderThan30Seconds() throws Exception {
    OlderThan filter = new OlderThan("-PT30S");
    filter.setAdditionalDebug(true);
    assertTrue(filter.accept(file));
  }

  public void testOlderThan1Minute() throws Exception {
    OlderThan filter = new OlderThan("-PT1M");
    filter.setAdditionalDebug(false);
    assertTrue(filter.accept(file));
  }


  public void testOlderThan15Years() throws Exception {
    OlderThan filter = new OlderThan("-P15Y");
    filter.setAdditionalDebug(true);
    assertFalse(filter.accept(file));
  }

  public void testOlderThanChris() throws Exception {
    OlderThan filter = new OlderThan("-P40Y6M4DT12H30M5S");
    filter.setAdditionalDebug(true);
    assertFalse(filter.accept(file));
  }

  public void testOlderThanHoward() throws Exception {
    OlderThan filter = new OlderThan("-P39Y6MT12H30M5.5S");
    filter.setAdditionalDebug(true);
    assertFalse(filter.accept(file));
  }

  public void testBadDuration() throws Exception {
    OlderThan filter = new OlderThan("-PXXX");
    assertFalse(filter.accept(file));
  }

}