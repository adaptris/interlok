/* $Id: NioWorkerTest.java,v 1.2 2006/11/21 14:39:37 lchan Exp $ */
package com.adaptris.fs;

import java.io.File;
import java.io.RandomAccessFile;
import java.nio.channels.FileLock;
import java.nio.channels.OverlappingFileLockException;

import org.apache.commons.io.FileUtils;

/**
 */
public class NioWorkerTest extends StandardWorkerTest {
  private static final byte[] BYTES;

  static {
    String data = "xxxxxxxx";
    for (int i = 0; i < 20; i++) {
      data = data + data;
    }
    BYTES = data.getBytes();
  }

  public NioWorkerTest(String arg0) {
    super(arg0);
  }

  @Override
  protected NioWorker createWorker() {
    return new NioWorker();
  }

  public void testLockWhileWriting() throws Exception {
    NioWorker worker = createWorker();
    File f = File.createTempFile(this.getClass().getSimpleName(), "");
    f.delete();
    try {
      RandomAccessFile raf = new RandomAccessFile(f, "rwd");
      FileLock lock = raf.getChannel().lock();
      try {
        // Use the write method, because this "bypasses" the file.exists() check
        worker.write(BYTES, f);
        fail();
      }
      catch (OverlappingFileLockException expected) {
      }
      lock.release();
      raf.close();
      f.delete();
      worker.put(BYTES, f);
    }
    finally {
      FileUtils.deleteQuietly(f);
    }
  }

  public void testLockWhileReading() throws Exception {
    FsWorker worker = createWorker();
    File f = File.createTempFile(this.getClass().getSimpleName(), "");
    f.delete();
    try {
      worker.put(BYTES, f);

      RandomAccessFile raf = new RandomAccessFile(f, "rwd");
      FileLock lock = raf.getChannel().lock();
      try {
        worker.get(f);
        fail();
      }
      catch (OverlappingFileLockException expected) {
      }
      lock.release();
      raf.close();
      worker.get(f);
    }
    finally {
      FileUtils.deleteQuietly(f);
    }
  }
}
