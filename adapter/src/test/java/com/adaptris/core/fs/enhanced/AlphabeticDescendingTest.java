package com.adaptris.core.fs.enhanced;

import java.io.File;
import java.util.List;

public class AlphabeticDescendingTest extends FileSorterCase {

  public AlphabeticDescendingTest(String testName) {
    super(testName);
  }

  public void testSort() throws Exception {
    AlphabeticDescending sorter = new AlphabeticDescending();
    List<File> files = createFiles(10);
    sorter.sort(files);
    log("Sorted", files);
    String lastFilename = String.format("%1$s-%2$03d%3$s", AlphabeticDescendingTest.class.getSimpleName(), 1, ".xml");
    String firstFilename = String.format("%1$s-%2$03d%3$s", AlphabeticDescendingTest.class.getSimpleName(), 10, ".xml");
    File firstFile = files.get(0);
    File lastFile = files.get(9);
    assertEquals(firstFilename, firstFile.getName());
    assertEquals(lastFilename, lastFile.getName());
  }
}
