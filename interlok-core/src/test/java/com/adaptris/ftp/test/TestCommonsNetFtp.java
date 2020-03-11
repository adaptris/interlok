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

package com.adaptris.ftp.test;

import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import java.io.FileFilter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.NoRouteToHostException;
import java.net.SocketTimeoutException;
import java.util.Date;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.oro.io.GlobFilenameFilter;
import org.junit.Assume;
import org.junit.Test;
import com.adaptris.filetransfer.FileTransferClient;
import com.adaptris.filetransfer.FtpCase;
import com.adaptris.ftp.CommonsNetFtpClient;
import com.adaptris.security.password.Password;

/**
 * Copied from TestFtp to test the Apache Commons Net Ftp client
 *
 * @author D.Sefton
 * @author $Author: D.Sefton $
 */
public class TestCommonsNetFtp extends FtpCase {

  private static final String ALTERNATE_TZ = "Australia/Sydney";

  private static String FTP_GET_FILENAME = "ftp.get.filename";
  private static final String FTP_PUT_FILENAME = "ftp.put.filename";
  private static final String FTP_PUT_REMOTEDIR = "ftp.put.remotedir";
  private static final String FTP_GET_FILTER = "ftp.get.filter";
  private static final String FTP_HOST = "ftp.host";
  private static final String FTP_GET_REMOTEDIR = "ftp.get.remotedir";
  private static final String FTP_PASSWORD = "ftp.password";
  private static final String FTP_USERNAME = "ftp.username";
  // -rwxrwxrwx 1 user group 28725 Jun 30 09:38 RAC DMAIL FUEL 10X2 100X73.pdf
  private static final String LIST_DIR_FULL =
      "^(\\S*)\\s+(\\S*)\\s+(\\S*)\\s+(\\S*)" + "\\s+(\\d*)\\s+(\\S*)\\s+(\\S*)\\s+(\\S*)\\s+(.*)";

  private static final Pattern LIST_DIR_PATTERN = Pattern.compile(LIST_DIR_FULL);

  @Override
  public boolean isAnnotatedForJunit4() {
    return true;
  }

  @Test
  public void testBug1924() throws Exception {
    Assume.assumeTrue(areTestsEnabled());
    String oldName = Thread.currentThread().getName();
    try {
      Thread.currentThread().setName("testBug1924");
      // Use an arbitarily small timeout to a range that almost certainly
      // shouldn't exist when the tests are running.
      CommonsNetFtpClient client = new CommonsNetFtpClient("192.168.17.1", 21, 1);
      client.disconnect();
      fail("Successfully connected, with a timeout of 1ms (surely not possible!)");
    } catch (SocketTimeoutException e) {
      ; // expected.
    } catch (NoRouteToHostException e) {
      ; // also possibly expected
    } catch (IOException e) {
      ; // also possibly expected from FtpCommonsNetClient
    } finally {
      Thread.currentThread().setName(oldName);
    }

  }

  @Test
  public void testConnectWithTimeout() throws Exception {
    Assume.assumeTrue(areTestsEnabled());
    String oldName = Thread.currentThread().getName();
    try {
      Thread.currentThread().setName("testBug1924");
      // Use an arbitarily small timeout.
      CommonsNetFtpClient client = new CommonsNetFtpClient("192.168.17.1", 21, 1);
      client.setAdditionalDebug(true);
      client.connect(config.getProperty(FTP_USERNAME), config.getProperty(FTP_PASSWORD));
      client.disconnect();
      fail("Successfully connected, with a timeout of 1ms (surely not possible!)");
    } catch (SocketTimeoutException e) {
      ; // expected.
    } catch (NoRouteToHostException e) {
      ; // also possibly expected
    } catch (IOException e) {
      ; // also possibly expected from FtpCommonsNetClient
    } finally {
      Thread.currentThread().setName(oldName);
    }

  }

  @Test
  public void testBug1483() throws Exception {
    Assume.assumeTrue(areTestsEnabled());
    String oldName = Thread.currentThread().getName();
    try {
      Thread.currentThread().setName("testBug1483");
      FileTransferClient client = connectClientImpl();
      String[] files = client.dir(getRemoteGetDirectory(), true);
      for (int i = 0; i < files.length; i++) {
        logR.debug(files[i]);
        Matcher m = LIST_DIR_PATTERN.matcher(files[i]);
        assertTrue("Output Should match " + LIST_DIR_FULL, m.matches());
        // logR.debug(HexDump.parse(files[i].getBytes()));
      }
      assertTrue(files.length > 0);
      client.disconnect();
    } finally {
      Thread.currentThread().setName(oldName);
    }

  }

  @Test
  public void testGetLastModifiedWithTimezone() throws Exception {
    Assume.assumeTrue(areTestsEnabled());
    String oldName = Thread.currentThread().getName();
    try {
      Thread.currentThread().setName("testGetLastModifiedWithTimezone");
      FileTransferClient client = connectClientImpl();
      client.chdir(getRemoteGetDirectory());
      long mtime = client.lastModified(getRemoteGetFilename());
      client.disconnect();

      CommonsNetFtpClient tzClient = (CommonsNetFtpClient) connectClientImpl();
      tzClient.setServerTimezone(TimeZone.getTimeZone(ALTERNATE_TZ));
      tzClient.chdir(getRemoteGetDirectory());
      long tztime = tzClient.lastModified(getRemoteGetFilename());
      tzClient.disconnect();

      logR.debug("testGetLastModifiedWithTimezone : " + new Date(tztime));
      assertNotSame("" + new Date(mtime), mtime, tztime);
    } finally {
      Thread.currentThread().setName(oldName);
    }

  }

  @Test
  public void testGetLastModifiedDateWithTimezone() throws Exception {
    Assume.assumeTrue(areTestsEnabled());
    String oldName = Thread.currentThread().getName();
    try {
      Thread.currentThread().setName("testGetLastModifiedDateWithTimezone");
      FileTransferClient client = connectClientImpl();
      client.chdir(getRemoteGetDirectory());
      Date mtime = client.lastModifiedDate(getRemoteGetFilename());
      client.disconnect();

      CommonsNetFtpClient tzClient = (CommonsNetFtpClient) connectClientImpl();
      tzClient.setServerTimezone(TimeZone.getTimeZone(ALTERNATE_TZ));
      tzClient.chdir(getRemoteGetDirectory());
      Date tzDate = tzClient.lastModifiedDate(getRemoteGetFilename());
      tzClient.disconnect();
      logR.debug("testGetLastModifiedWithTimezone : " + tzDate);

      assertNotSame("" + mtime, mtime, tzDate);
    } finally {
      Thread.currentThread().setName(oldName);

    }
  }

  @Test
  public void testSetTimeout() throws Exception {
    Assume.assumeTrue(areTestsEnabled());
    String oldName = Thread.currentThread().getName();
    try {
      Thread.currentThread().setName("testSetTimeout");
      CommonsNetFtpClient client = new CommonsNetFtpClient(config.getProperty(FTP_HOST));
      client.setTimeout(client.getTimeout());
      client.setKeepAliveTimeout(client.getKeepAliveTimeout());
    } finally {
      Thread.currentThread().setName(oldName);
    }

  }

  @Override
  protected String getRemoteGetDirectory() throws IOException {
    return config.getProperty(FTP_GET_REMOTEDIR);
  }

  @Override
  protected String getRemotePutDirectory() throws IOException {
    return config.getProperty(FTP_PUT_REMOTEDIR);
  }

  @Override
  protected String getRemoteGetFilename() throws IOException {
    return config.getProperty(FTP_GET_FILENAME);
  }

  @Override
  protected String getRemotePutFilename() throws IOException {
    return config.getProperty(FTP_PUT_FILENAME);
  }

  @Override
  protected FilenameFilter getRemoteGetFilenameFilter() throws IOException {
    return new GlobFilenameFilter(config.getProperty(FTP_GET_FILTER));
  }

  @Override
  protected String getRemoteGetFilterString() {
    return config.getProperty(FTP_GET_FILTER);
  }

  @Override
  protected FileFilter getRemoteGetFileFilter() throws IOException {
    return new GlobFilenameFilter(config.getProperty(FTP_GET_FILTER));
  }

  @Override
  protected FileTransferClient connectClientImpl() throws Exception {
    CommonsNetFtpClient client = new CommonsNetFtpClient(config.getProperty(FTP_HOST));
    client.setAdditionalDebug(true);
    client.connect(config.getProperty(FTP_USERNAME), Password.decode(config.getProperty(FTP_PASSWORD)));
    logR.trace("Server OS         : " + client.system());
    logR.trace("Current Directory : " + client.pwd());
    // logR.trace("Transfer Type : " + client.getType().toString());
    return client;
  }
}
