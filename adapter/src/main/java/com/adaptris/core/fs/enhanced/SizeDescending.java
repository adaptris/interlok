package com.adaptris.core.fs.enhanced;

import java.io.File;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Sort the list of files by their size in reverse order
 * 
 * @config fs-sort-size-descending
 * 
 * @author lchan
 * 
 */
@XStreamAlias("fs-sort-size-descending")
public class SizeDescending implements FileSorter, Comparator<File> {


  @Override
  public void sort(List<File> unsorted) {
    Collections.sort(unsorted, this);
  }

  @Override
  public int compare(File o1, File o2) {
    return Long.valueOf(o2.length()).compareTo(Long.valueOf(o1.length()));
  }

}