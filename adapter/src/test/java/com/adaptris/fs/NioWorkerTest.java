/*
 * Copyright 2015 Adaptris Ltd.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/

package com.adaptris.fs;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.RandomAccessFile;
import java.nio.channels.FileLock;
import java.nio.channels.OverlappingFileLockException;

import org.apache.commons.io.FileUtils;
import org.junit.Test;

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

  @Override
  protected NioWorker createWorker() {
    return new NioWorker();
  }

  @Test
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
      catch (FsException expected) {
        assertEquals(OverlappingFileLockException.class, expected.getCause().getClass());
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

  @Test
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
      catch (FsException expected) {
        assertEquals(OverlappingFileLockException.class, expected.getCause().getClass());
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
