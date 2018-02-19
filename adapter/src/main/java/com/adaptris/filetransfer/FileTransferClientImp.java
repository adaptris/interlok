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

package com.adaptris.filetransfer;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract implementation of FileTransferClient.
 * 
 * @author lchan
 * @author $Author: lchan $
 */
public abstract class FileTransferClientImp implements FileTransferClient {

  protected transient Logger logR;
  private transient boolean additionalDebug = false;
  public FileTransferClientImp() {
    logR = LoggerFactory.getLogger(this.getClass().getName());
  }


  /**
   * @see com.adaptris.filetransfer.FileTransferClient#put(java.lang.String,
   *      java.lang.String)
   */
  public void put(String localPath, String remoteFile) throws IOException,
      FileTransferException {
    put(localPath, remoteFile, false);
  }

  /**
   * 
   * @see FileTransferClient#put(java.lang.String, java.lang.String, boolean)
   */
  public void put(String localPath, String remoteFile, boolean append)
      throws IOException, FileTransferException {
    FileInputStream in = null;
    try {
      in = new FileInputStream(localPath);
      put(in, remoteFile, append);
    }
    finally {
      IOUtils.closeQuietly(in);
    }
  }

  /**
   * 
   * @see FileTransferClient#put(byte[], java.lang.String, boolean)
   */
  public void put(byte[] bytes, String remoteFile, boolean append)
      throws IOException, FileTransferException {
    put(new ByteArrayInputStream(bytes), remoteFile, append);
  }

  /**
   * @see com.adaptris.filetransfer.FileTransferClient#put(java.io.InputStream,
   *      java.lang.String)
   */
  public void put(InputStream srcStream, String remoteFile) throws IOException,
      FileTransferException {
    put(srcStream, remoteFile, false);
  }

  /**
   * @see com.adaptris.filetransfer.FileTransferClient#put(byte[],
   *      java.lang.String)
   */
  public void put(byte[] bytes, String remoteFile) throws IOException,
      FileTransferException {
    put(bytes, remoteFile, false);
  }

  /**
   * @see FileTransferClient#dir()
   */
  public String[] dir() throws IOException, FileTransferException {
    return dir(null, false);
  }

  /**
   * @see FileTransferClient#dir(java.lang.String)
   */
  public String[] dir(String dirname) throws IOException, FileTransferException {
    return dir(dirname, false);
  }

  /**
   * @see FileTransferClient#dir(java.lang.String, java.io.FileFilter)
   */
  public String[] dir(String directory, FileFilter filter)
      throws FileTransferException, IOException {
    return filter(this.dir(directory), filter);
  }

  /**
   * @see FileTransferClient#dir(java.lang.String, java.io.FilenameFilter)
   */
  public String[] dir(String directory, FilenameFilter filter)
      throws FileTransferException, IOException {
    return filter(this.dir(directory), new FilenameFilterProxy(filter));
  }

  /**
   * @see FileTransferClient#get(java.lang.String, java.lang.String)
   */
  public void get(String localPath, String remoteFile) throws IOException,
      FileTransferException {

    FileOutputStream out = new FileOutputStream(localPath);
    try {
      get(out, remoteFile);
    }
    finally {
      IOUtils.closeQuietly(out);
    }
  }



  @Override
  public void setAdditionalDebug(boolean on) {
    additionalDebug = on;
  }

  protected boolean isAdditionaDebug() {
    return additionalDebug;
  }


  protected void log(String msg, Object... params) {
    if (isAdditionaDebug()) {
      logR.trace(msg, params);
    }
  }


  private String[] filter(String[] filelist, FileFilter filter) {
    if (filter == null) return filelist;
    HashSet<String> result = new HashSet<String>();
    for (int i = 0; i < filelist.length; i++) {
      if (filter.accept(new File(filelist[i]))) {
        result.add(filelist[i]);
      }
    }
    return result.toArray(new String[0]);
  }

  private class FilenameFilterProxy implements FileFilter {
    private FilenameFilter proxy;

    private FilenameFilterProxy(FilenameFilter f) {
      proxy = f != null ? f : new FilenameFilter() {
        @Override
        public boolean accept(File dir, String name) {
          return true;
        }
        
      };
    }

    @Override
    public boolean accept(File pathname) {
      return proxy.accept(pathname.getParentFile(), pathname.getName());
    }
  }
}
