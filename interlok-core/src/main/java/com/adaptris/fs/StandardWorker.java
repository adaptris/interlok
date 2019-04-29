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
  
  public byte[] get(File file) throws FsException {
    byte[] result = null;
    try (RandomAccessFile raf = new RandomAccessFile(checkWriteable(file), "r")) {
      result = new byte[(int) raf.length()];
      raf.readFully(result);
    }
    catch (Exception e) {
      throw wrapException(e);
    }
    return result;
  }

  public void put(byte[] data, File file) throws FsException {
    try (RandomAccessFile raf = new RandomAccessFile(checkNonExistent(file), "rw")){
      raf.write(data);
    } catch (Exception e) {
      throw wrapException(e);
    }
  }

  protected FsException wrapException(Exception e) {
    if (e instanceof FsException) {
      return (FsException) e;
    }
    return new FsException(e);
  }
}
