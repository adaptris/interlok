package com.adaptris.fs;

import java.io.File;


/**
 */
public class OverwriteIfExistsWorkerTest extends StandardWorkerTest {
  public OverwriteIfExistsWorkerTest(String arg0) {
    super(arg0);
  }

  @Override
  protected OverwriteIfExistsWorker createWorker() {
    return new OverwriteIfExistsWorker();
  }

  @Override
  public void testPutFileExists() throws Exception {
    FsWorker worker = createWorker();
    String[] testFiles = createTestFiles();
    worker.put(DATA.getBytes(), new File(baseDir, testFiles[0]));
    // So it should have overwritten appended it.
    byte[] readBytes = worker.get(new File(baseDir, testFiles[0]));
    assertEquals(DATA, new String(readBytes));
  }
}
