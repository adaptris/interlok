package com.adaptris.core.fs.enhanced;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class NoSortingTest extends FileSorterCase {

  public NoSortingTest(String testName) {
    super(testName);
  }

  public void testSort() throws Exception {
    NoSorting sorter = new NoSorting();
    List<File> files = createFiles(10);
    ArrayList unsorted = new ArrayList<File>(files);
    sorter.sort(files);
    log("Sorted", files);
    assertEquals(unsorted, files);
  }
}
