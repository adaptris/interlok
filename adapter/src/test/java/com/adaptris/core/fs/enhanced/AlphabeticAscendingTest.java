package com.adaptris.core.fs.enhanced;

import java.io.File;
import java.util.List;

public class AlphabeticAscendingTest extends FileSorterCase {

  public AlphabeticAscendingTest(String testName) {
    super(testName);
  }

  public void testSort() throws Exception {
    AlphabeticAscending sorter = new AlphabeticAscending();
    List<File> files = createFiles(10);
    sorter.sort(files);
    log("Sorted", files);
    String firstFilename = String.format("%1$s-%2$03d%3$s", AlphabeticAscendingTest.class.getSimpleName(), 1, ".xml");
    String lastFilename = String.format("%1$s-%2$03d%3$s", AlphabeticAscendingTest.class.getSimpleName(), 10, ".xml");
    File firstFile = files.get(0);
    File lastFile = files.get(9);
    assertEquals(firstFilename, firstFile.getName());
    assertEquals(lastFilename, lastFile.getName());
  }
}
