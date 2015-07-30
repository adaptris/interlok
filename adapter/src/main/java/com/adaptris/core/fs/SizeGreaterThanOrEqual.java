/*
 * $RCSfile: SizeGreaterThanOrEqual.java,v $
 * $Revision: 1.1 $
 * $Date: 2009/05/20 08:46:56 $
 * $Author: lchan $
 */
package com.adaptris.core.fs;

import java.io.File;
import java.io.FileFilter;

/**
 * {@link FileFilter} accepts files based on whether the size of the file is greater or equal to the specified value.
 * 
 * @author lchan
 * @author $Author: lchan $
 */
public class SizeGreaterThanOrEqual extends SizeBasedFileFilter {

  /**
   * Create the filefilter using the specified size.
   *
   * @param size the size of the file in bytes.
   */
  public SizeGreaterThanOrEqual(String size) {
    super(size);
  }

  /**
   * Create the filefilter using the specified size.
   *
   * @param fileSize the size of the file in bytes.
   */
  public SizeGreaterThanOrEqual(long fileSize) {
    super(fileSize);
  }

  /**
   * @see java.io.FileFilter#accept(java.io.File)
   */
  @Override
  public boolean accept(File pathname) {
    return pathname.length() >= getFilesize();
  }
}
