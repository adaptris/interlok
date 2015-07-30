package com.adaptris.core.fs;

import java.io.File;
import java.io.FileFilter;

/**
 * {@link FileFilter} accepts files based on whether the size of the file is less than or equal to the specified value..
 * 
 * @author lchan
 * @author $Author: lchan $
 */
public class SizeLessThanOrEqual extends SizeBasedFileFilter {

  /**
   * Create the filefilter using the specified size.
   *
   * @param size the size of the file in bytes.
   */
  public SizeLessThanOrEqual(String size) {
    super(size);
  }

  /**
   * Create the filefilter using the specified size.
   *
   * @param fileSize the size of the file in bytes.
   */
  public SizeLessThanOrEqual(long fileSize) {
    super(fileSize);
  }

  /**
   * @see java.io.FileFilter#accept(java.io.File)
   */
  @Override
  public boolean accept(File pathname) {
    return pathname.length() <= getFilesize();
  }
}
