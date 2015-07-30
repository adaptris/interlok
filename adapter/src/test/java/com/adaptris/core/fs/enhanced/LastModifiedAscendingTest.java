package com.adaptris.core.fs.enhanced;

import java.io.File;
import java.util.List;

public class LastModifiedAscendingTest extends FileSorterCase {

  public LastModifiedAscendingTest(String testName) {
    super(testName);
  }

  public void testSort() throws Exception {
    LastModifiedAscending sorter = new LastModifiedAscending();
    List<File> files = createFiles(10, 100l);
    sorter.sort(files);
    log("Sorted", files);

    File firstFile = files.get(0);
    File lastFile = files.get(9);

    assertTrue(lastFile.lastModified() >= firstFile.lastModified());
  }
}
