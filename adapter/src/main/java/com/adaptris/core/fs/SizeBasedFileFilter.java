/*
 * $RCSfile: SizeBasedFileFilter.java,v $
 * $Revision: 1.1 $
 * $Date: 2009/05/20 08:46:56 $
 * $Author: lchan $
 */
package com.adaptris.core.fs;

import java.io.FileFilter;

/**
 * A {@link FileFilter} that works based on the size of a file.
 * 
 * @author lchan
 * @author $Author: lchan $
 */
abstract class SizeBasedFileFilter implements FileFilter {

  private long filesize;

  /**
   * Default constructor
   */
  private SizeBasedFileFilter() {
    filesize = 0;
  }

  /**
   * Create the filefilter using the specified size.
   *
   * @param size the size of the file in bytes.
   */
  public SizeBasedFileFilter(String size) {
    this(Long.valueOf(size));
  }

  /**
   * Create the filefilter using the specified size.
   *
   * @param size the size of the file in bytes.
   */
  public SizeBasedFileFilter(long size) {
    this();
    filesize = size;
  }

  protected long getFilesize() {
    return filesize;
  }
}
