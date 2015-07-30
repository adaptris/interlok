package com.adaptris.fs;

import java.io.File;


/**
 */
public class AppendingFsWorkerTest extends StandardWorkerTest {
  public AppendingFsWorkerTest(String arg0) {
    super(arg0);
  }

  @Override
  protected AppendingFsWorker createWorker() {
    return new AppendingFsWorker();
  }

  @Override
  public void testPutFileExists() throws Exception {
    FsWorker worker = createWorker();
    String[] testFiles = createTestFiles();
    worker.put(DATA.getBytes(), new File(baseDir, testFiles[0]));
    // So it should have appended it.
    byte[] readBytes = worker.get(new File(baseDir, testFiles[0]));

    assertEquals(DATA + DATA, new String(readBytes));
  }
}
