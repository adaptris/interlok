package com.adaptris.core.fs.enhanced;

import java.io.File;
import java.util.List;

/**
 * Interface that allows FsConsumer style implementations to sort the list of files before processing.
 *
 * @author lchan
 * 
 */
public interface FileSorter {

  /**
   * Sort the list of files that need to be processed.
   *
   * @param unsorted an unsorted list of files.
   */
  void sort(List<File> unsorted);

}
