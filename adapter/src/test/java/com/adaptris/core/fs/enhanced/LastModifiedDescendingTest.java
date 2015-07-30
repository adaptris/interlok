package com.adaptris.core.fs.enhanced;

import java.io.File;
import java.util.List;

public class LastModifiedDescendingTest extends FileSorterCase {

  public LastModifiedDescendingTest(String testName) {
    super(testName);
  }

  public void testSort() throws Exception {
    LastModifiedDescending sorter = new LastModifiedDescending();
    List<File> files = createFiles(10, 100l);
    sorter.sort(files);
    log("Sorted", files);

    File firstFile = files.get(0);
    File lastFile = files.get(9);
    assertTrue(firstFile.lastModified() >= lastFile.lastModified());
  }
}
