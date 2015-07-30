package com.adaptris.core.fs.enhanced;

import java.io.File;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Sort the list of files by their last modified attribute in reverse order.
 * 
 * @config fs-sort-last-modified-descending
 * 
 * @author lchan
 * 
 */
@XStreamAlias("fs-sort-last-modified-descending")
public class LastModifiedDescending implements FileSorter, Comparator<File> {


  @Override
  public void sort(List<File> unsorted) {
    Collections.sort(unsorted, this);
  }

  @Override
  public int compare(File o1, File o2) {
    return Long.valueOf(o2.lastModified()).compareTo(Long.valueOf(o1.lastModified()));
  }

}