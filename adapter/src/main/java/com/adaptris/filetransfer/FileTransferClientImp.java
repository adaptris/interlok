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
import java.util.Properties;

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
   * Set up SOCKS v4/v5 proxy settings. This can be used if there is a SOCKS
   * proxy server in place that must be connected thru. Note that setting these
   * properties directs <b>all</b> TCP sockets in this JVM to the SOCKS proxy
   * 
   * @param port SOCKS proxy port
   * @param host SOCKS proxy hostname
   */
  public static void initSOCKS(String port, String host) {
    Properties props = System.getProperties();
    props.put("socksProxyPort", port);
    props.put("socksProxyHost", host);
    System.setProperties(props);
  }

  /**
   * Set up SOCKS username and password for SOCKS username/password
   * authentication. Often, no authentication will be required but the SOCKS
   * server may be configured to request these.
   * 
   * @param username the SOCKS username
   * @param password the SOCKS password
   */
  public static void initSOCKSAuthentication(String username, String password) {
    Properties props = System.getProperties();
    props.put("java.net.socks.username", username);
    props.put("java.net.socks.password", password);
    System.setProperties(props);
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
      if (in != null) in.close();
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
    String[] filelist = this.dir(directory);
    if (filelist == null) return null;
    if (filter == null) return (filelist);
    HashSet data = new HashSet();
    for (int i = 0; i < filelist.length; i++) {
      File file = new File(filelist[i]);
      if (filter.accept(file.getParentFile(), file.getName())) {
        data.add(filelist[i]);
      }
    }
    String[] s = new String[0];
    return ((String[]) data.toArray(s));
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
      out.close();
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
    if (filelist == null) return null;
    if (filter == null) return filelist;
    HashSet<String> result = new HashSet<String>();
    for (int i = 0; i < filelist.length; i++) {
      if (filter.accept(new File(filelist[i]))) {
        result.add(filelist[i]);
      }
    }
    return result.toArray(new String[0]);
  }
}
