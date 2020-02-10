/*
 * Copyright 2015 Adaptris Ltd.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS"
 * BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */

package com.adaptris.sftp.test;

import static org.junit.Assert.fail;
import java.io.FileFilter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Random;
import org.apache.commons.lang3.StringUtils;
import org.apache.oro.io.GlobFilenameFilter;
import org.junit.Assume;
import org.junit.Test;
import com.adaptris.filetransfer.FileTransferClient;
import com.adaptris.filetransfer.FtpCase;
import com.adaptris.security.password.Password;
import com.adaptris.sftp.SftpClient;

public class TestSftp extends FtpCase {

  private static final String SFTP_GET_FILTER = "sftp.get.filter";
  private static final String SFTP_PUT_REMOTEDIR = "sftp.put.remotedir";
  private static final String SFTP_PUT_FILENAME = "sftp.put.filename";
  private static final String SFTP_GET_FILENAME = "sftp.get.filename";
  private static final String SFTP_GET_REMOTEDIR = "sftp.get.remotedir";
  private static final String SFTP_HOST = "sftp.host";
  private static final String SFTP_PASSWORD = "sftp.password";
  private static final String SFTP_USERNAME = "sftp.username";

  @Override
  public boolean isAnnotatedForJunit4() {
    return true;
  }

  @Test
  public void testListBadDirectory() throws Exception {
    Assume.assumeTrue(areTestsEnabled());
    String oldName = Thread.currentThread().getName();
    try {
      Thread.currentThread().setName("testListBadDirectory");
      FileTransferClient client = connectClientImpl();
      try {
        Random r = new Random();
        String dir = config.getProperty(SFTP_GET_REMOTEDIR) + "/" + r.nextInt();
        client.dir(dir);
        fail("LS of  " + dir + " should not work");
      } catch (Exception e) {
        client.disconnect();
      }
    } finally {
      Thread.currentThread().setName(oldName);
    }

  }

  @Override
  protected String getRemoteGetDirectory() throws IOException {
    return config.getProperty(SFTP_GET_REMOTEDIR);
  }

  @Override
  protected String getRemotePutDirectory() throws IOException {
    return config.getProperty(SFTP_PUT_REMOTEDIR);
  }

  @Override
  protected String getRemoteGetFilename() throws IOException {
    return config.getProperty(SFTP_GET_FILENAME);
  }

  @Override
  protected String getRemotePutFilename() throws IOException {
    return config.getProperty(SFTP_PUT_FILENAME);
  }

  @Override
  protected FilenameFilter getRemoteGetFilenameFilter() throws IOException {
    return new GlobFilenameFilter(config.getProperty(SFTP_GET_FILTER));
  }

  @Override
  protected String getRemoteGetFilterString() {
    return config.getProperty(SFTP_GET_FILTER);
  }

  @Override
  protected FileFilter getRemoteGetFileFilter() throws IOException {
    return new GlobFilenameFilter(config.getProperty(SFTP_GET_FILTER));
  }

  @Override
  protected FileTransferClient connectClientImpl() throws Exception {
    SftpClient client = new SftpClient(config.getProperty(SFTP_HOST));
    client.setAdditionalDebug(true);
    client.connect(config.getProperty(SFTP_USERNAME), Password.decode(config.getProperty(SFTP_PASSWORD)));
    return client;
  }

  @Override
  protected boolean areTestsEnabled() {
    String sftpTests = config.getProperty("sftp.tests.enabled");
    if (!StringUtils.isEmpty(sftpTests)) {
      return Boolean.parseBoolean(sftpTests);
    }
    return super.areTestsEnabled();
  }
}
