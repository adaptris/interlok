/*
 * $RCSfile: FileCleanupTest.java,v $
 * $Revision: 1.1 $
 * $Date: 2009/06/05 17:55:10 $
 * $Author: lchan $
 */
package com.adaptris.zz.cleanup;

import org.junit.Test;

import com.adaptris.core.lms.FilePurge;

// It appears to be the case that FileCleaningTracker
// doesn't appear to work within junit...
// WE don't want loads of temporary files hanging around
// This should be the last case that's run.
public class FileCleanupTest {

  @Test
  public void testCleanupTemporaryFiles() {
    System.gc();
    FilePurge.getInstance().purge();
  }

}