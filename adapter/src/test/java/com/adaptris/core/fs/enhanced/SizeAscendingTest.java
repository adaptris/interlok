package com.adaptris.core.fs.enhanced;

import java.io.File;
import java.util.List;

public class SizeAscendingTest extends FileSorterCase {

  public SizeAscendingTest(String testName) {
    super(testName);
  }

  public void testSort() throws Exception {
    SizeAscending sorter = new SizeAscending();
    List<File> files = createFiles(10);
    sorter.sort(files);
    log("Sorted", files);

    File firstFile = files.get(0);
    File lastFile = files.get(9);

    assertTrue(lastFile.length() > firstFile.length());
  }
}
