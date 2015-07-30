package com.adaptris.core.fs.enhanced;

import java.io.File;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Sort the list of files alphabetically in reverse order
 * 
 * @config fs-sort-alphabetic-descending
 * @author lchan
 * 
 */
@XStreamAlias("fs-sort-alphabetic-descending")
public class AlphabeticDescending implements FileSorter, Comparator<File> {


  @Override
  public void sort(List<File> unsorted) {
    Collections.sort(unsorted, this);
  }

  @Override
  public int compare(File o1, File o2) {
    return o2.compareTo(o1);
  }

}
