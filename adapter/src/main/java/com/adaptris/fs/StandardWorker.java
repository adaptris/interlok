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

import java.io.Closeable;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.RandomAccessFile;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Implementation of {@link FsWorker} that uses standard java.io to perform put and get operations.
 * 
 * @config fs-standard-worker
 */
@XStreamAlias("fs-standard-worker")
public class StandardWorker implements FsWorker {
  private transient Logger log = LoggerFactory.getLogger(this.getClass());

  public File[] listFiles(File dir) throws FsException {
    return listFiles(dir, null);
  }

  public File[] listFiles(File dir, FileFilter filter) throws FsException {
    File[] result = isDir(checkAcl(dir)).listFiles(filter);
    if (result == null) {
      throw new FsException("problem listing files in [" + dir + "]");
    }
    return result;
  }

  protected File checkAcl(File file) throws FsException {
    if (file == null) {
      throw new FsException("reference is null");
    }

    if (!file.exists()) {
      throw new FsFileNotFoundException(file.getAbsolutePath());
    }

    if (!file.canRead()) {
      throw new FsException("file not readable");
    }

    if (!file.canWrite()) {
      throw new FsException("file not writable");
    }

    return file; // for method chaining
  }

  protected File isDir(File file) throws FsException {
    if (!file.isDirectory()) {
      throw new FsException("invalid directory [" + file + "]");
    }

    return file;
  }

  public byte[] get(File file) throws FsException {
    byte[] result = null;
    RandomAccessFile raf = null;
    try {
      raf = new RandomAccessFile(checkAcl(file), "r");
      result = new byte[(int) raf.length()];
      raf.readFully(result);
    }
    catch (IOException e) {
      throw new FsException(e);
    }
    finally {
      closeQuietly(raf);
    }
    return result;
  }

  public void put(byte[] data, File file) throws FsException {
    RandomAccessFile raf = null;
    try {
      if (file.exists()) {
        throw new FsException("trying to write to file [" + file + "] which exists");
      }
      raf = new RandomAccessFile(file, "rw");
      raf.write(data);
    }
    catch (IOException e) {
      throw new FsException(e);
    }
    finally {
      closeQuietly(raf);
    }
  }

  protected void closeQuietly(Closeable c) {
    try {
      if (c != null) {
        c.close();
      }
    }
    catch (IOException ignoredIntentionally) {
      //
    }
  }

  public void rename(File oldFile, File newFile) throws FsException {
    if (newFile.exists()) {
      throw new FsFilenameExistsException("name [" + newFile + "] already exists");
    }
    if (!checkAcl(oldFile).renameTo(newFile)) {
      throw new FsException("problem renaming file [" + oldFile + "] to [" + newFile + "]");
    }
  }

  public void delete(File file) throws FsException {
    try {
      if (!checkAcl(file).delete()) {
        throw new FsException("problem deleting file [" + file + "]");
      }
    }
    catch (FsFileNotFoundException e) {
    }
  }

  public boolean isWriteableDir(File dir) throws FsException {
    try {
      isDir(checkAcl(dir));
      return true;
    }
    catch (FsException e) {
      return false;
    }
  }
}
