package com.adaptris.core.fs.enhanced;

import java.io.File;
import java.util.List;

public class SizeDescendingTest extends FileSorterCase {

  public SizeDescendingTest(String testName) {
    super(testName);
  }

  public void testSort() throws Exception {
    SizeDescending sorter = new SizeDescending();
    List<File> files = createFiles(10);
    sorter.sort(files);
    log("Sorted", files);

    File firstFile = files.get(0);
    File lastFile = files.get(9);
    assertTrue(firstFile.length() > lastFile.length());
  }
}
