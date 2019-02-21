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

import static com.adaptris.fs.FsWorker.checkNonExistent;
import static com.adaptris.fs.FsWorker.checkWriteable;

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
    write(data, checkNonExistent(file));
  }

  protected void write(byte[] data, File file) throws FsException {
    ByteBuffer buffer = ByteBuffer.wrap(data);
    try (RandomAccessFile raf = new RandomAccessFile(file, "rw");
        FileChannel channel = raf.getChannel();
        FileLock lock = channel.lock()) {
      while (buffer.hasRemaining()) {
        channel.write(buffer);
      }
    }
    catch (Exception e) {
      throw wrapException(e);
    }
  }

  @Override
  public byte[] get(File file) throws FsException {
    ByteBuffer buffer = null;

    try (RandomAccessFile raf = new RandomAccessFile(checkWriteable(file), "rw");
        FileChannel channel = raf.getChannel();
        FileLock lock = channel.lock()) {
      buffer = ByteBuffer.allocate((int) raf.length());
      while (buffer.hasRemaining()) {
        channel.read(buffer);
      }
    }
    catch (Exception e) {
      throw wrapException(e);
    }
    return buffer.array();
  }

}
