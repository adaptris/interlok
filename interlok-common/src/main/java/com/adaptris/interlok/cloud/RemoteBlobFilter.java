package com.adaptris.interlok.cloud;

/**
 * Similar to {@link FileFilter} allowing you filter a remote blob
 * 
 */
@FunctionalInterface
public interface RemoteBlobFilter {

  /**
   * Whether the given file is accepted by this filter.
   */
  boolean accept(RemoteBlob blob);
}
