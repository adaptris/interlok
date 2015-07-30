package com.adaptris.core.fs.enhanced;

import java.io.File;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Sort the list of files by their size.
 * 
 * @config fs-sort-size
 * 
 * @author lchan
 * 
 */
@XStreamAlias("fs-sort-size")
public class SizeAscending implements FileSorter, Comparator<File> {


  @Override
  public void sort(List<File> unsorted) {
    Collections.sort(unsorted, this);
  }

  @Override
  public int compare(File o1, File o2) {
    return Long.valueOf(o1.length()).compareTo(Long.valueOf(o2.length()));
  }

}