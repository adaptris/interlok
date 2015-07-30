package com.adaptris.core.fs.enhanced;

import java.io.File;
import java.util.List;

import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Do no sorting at all.
 * 
 * @config fs-sort-none
 * 
 * @author lchan
 * 
 */
@XStreamAlias("fs-sort-none")
public class NoSorting implements FileSorter {

  @Override
  public void sort(List<File> unsorted) {
    // Do Nothing
  }

}
