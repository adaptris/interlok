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

import java.io.File;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;

import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Implementation of {@link FsWorker} that uses java.nio to perform put and get operations.
 * 
 * @config fs-nio-worker
 */
@XStreamAlias("fs-nio-worker")
public class NioWorker extends StandardWorker {

  @Override
  public void put(byte[] data, File file) throws FsException {
    if (file.exists()) {
      throw new FsException("trying to write to file [" + file + "] which exists");
    }
    write(data, file);
  }

  protected void write(byte[] data, File file) throws FsException {
    ByteBuffer buffer = ByteBuffer.wrap(data);
    FileLock lock = null;

    try (RandomAccessFile raf = new RandomAccessFile(file, "rw"); FileChannel channel = raf.getChannel()) {
      lock = channel.lock();
      while (buffer.hasRemaining()) {
        channel.write(buffer);
      }
    }
    catch (Exception e) {
      throw wrapException(e);
    }
    finally {
      releaseQuietly(lock);
    }
  }

  @Override
  public byte[] get(File file) throws FsException {
    FileLock lock = null;
    ByteBuffer buffer = null;

    try (RandomAccessFile raf = new RandomAccessFile(checkAcl(file), "rw"); FileChannel channel = raf.getChannel()) {
      lock = channel.lock();
      buffer = ByteBuffer.allocate((int) raf.length());
      while (buffer.hasRemaining()) {
        channel.read(buffer);
      }
    }
    catch (Exception e) {
      throw wrapException(e);
    }
    finally {
      releaseQuietly(lock);
    }
    return buffer.array();
  }

  protected void releaseQuietly(FileLock lock) {
    try {
      if (lock != null) {
        lock.release();
      }
    }
    catch (Exception e) {

    }

  }
}
