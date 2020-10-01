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

package com.adaptris.core.ftp;

import static com.adaptris.core.ftp.SftpExampleHelper.CFG_HOST;
import static com.adaptris.core.ftp.SftpExampleHelper.CFG_KNOWN_HOSTS_FILE;
import static com.adaptris.core.ftp.SftpExampleHelper.CFG_PASSWORD;
import static com.adaptris.core.ftp.SftpExampleHelper.CFG_PRIVATE_KEY_FILE;
import static com.adaptris.core.ftp.SftpExampleHelper.CFG_PRIVATE_KEY_PW;
import static com.adaptris.core.ftp.SftpExampleHelper.CFG_REMOTE_DIR;
import static com.adaptris.core.ftp.SftpExampleHelper.CFG_TEMP_HOSTS_FILE;
import static com.adaptris.core.ftp.SftpExampleHelper.CFG_UNKNOWN_HOSTS_FILE;
import static com.adaptris.core.ftp.SftpExampleHelper.CFG_USER;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import org.apache.commons.io.FileCleaningTracker;
import org.apache.commons.io.FileDeleteStrategy;
import org.apache.commons.io.FileUtils;
import org.junit.Assume;
import org.junit.Test;
import com.adaptris.filetransfer.FileTransferClient;
import com.adaptris.filetransfer.FileTransferException;
import com.adaptris.security.exc.PasswordException;
import com.adaptris.security.password.Password;
import com.adaptris.util.TimeInterval;

public class StandardSftpConnectionTest extends FtpConnectionCase {


  private static FileCleaningTracker cleaner = new FileCleaningTracker();
  private Object fileTracker = new Object();


  @Override
  protected boolean areTestsEnabled() {
    return Boolean.parseBoolean(PROPERTIES.getProperty("sftp.tests.enabled", "false"));
  }

  @Test
  public void testSocketTimeout() throws Exception {
    StandardSftpConnection conn = new StandardSftpConnection();
    assertNull(conn.getSocketTimeout());
    assertEquals(60000, conn.socketTimeout());

    TimeInterval timeout = new TimeInterval(10L, TimeUnit.SECONDS);
    conn.setSocketTimeout(timeout);
    assertEquals(timeout, conn.getSocketTimeout());
    assertEquals(10000, conn.socketTimeout());

    conn.setSocketTimeout(null);
    assertNull(conn.getSocketTimeout());
    assertEquals(60000, conn.socketTimeout());

  }

  @Test
  public void testSetKnownHostsFile() throws Exception {
    StandardSftpConnection conn = new StandardSftpConnection();
    assertNull(conn.getKnownHostsFile());
    conn.setKnownHostsFile("abc");
    assertEquals("abc", conn.getKnownHostsFile());
  }

  @Test
  public void testConnectOnly_KeyAuth() throws Exception {
    Assume.assumeTrue(areTestsEnabled());
    StandardSftpConnection conn = createConnection();
    try {
      start(conn);
      FileTransferClient c = conn.connect(getDestinationString());
      assertTrue(c.isConnected());
      c.disconnect();
    } finally {
      stop(conn);
    }

  }

  @Test
  public void testConnectOnly_PasswordAuth() throws Exception {
    Assume.assumeTrue(areTestsEnabled());
    StandardSftpConnection conn = createConnection();
    try {
      conn.setAuthentication(new SftpPasswordAuthentication(PROPERTIES.getProperty(CFG_PASSWORD)));
      start(conn);
      FileTransferClient c = conn.connect(getDestinationString());
      assertTrue(c.isConnected());
      c.disconnect();
    } finally {
      stop(conn);
    }

  }

  @Test
  public void testConnectOnly_PasswordAuth_Override() throws Exception {
    Assume.assumeTrue(areTestsEnabled());
    StandardSftpConnection conn = createConnection();
    try {
      conn.setAuthentication(new SftpPasswordAuthentication(""));
      start(conn);
      FileTransferClient c = conn.connect(getDestinationStringWithOverridePassword());
      assertTrue(c.isConnected());
      c.disconnect();
    } finally {
      stop(conn);
    }

  }

  @Test
  public void testConnectOnly_Composite() throws Exception {
    Assume.assumeTrue(areTestsEnabled());
    StandardSftpConnection conn = createConnection();
    try {
      SftpAuthenticationWrapper auth =
          new SftpAuthenticationWrapper(new SftpKeyAuthentication(PROPERTIES.getProperty(CFG_PRIVATE_KEY_FILE), "PW:abde"),
              new SftpKeyAuthentication(PROPERTIES.getProperty(CFG_PRIVATE_KEY_FILE), PROPERTIES.getProperty(CFG_PRIVATE_KEY_PW)));
      conn.setAuthentication(auth);
      start(conn);
      FileTransferClient c = conn.connect(getDestinationString());
      assertTrue(c.isConnected());
      c.disconnect();
    } finally {
      stop(conn);
    }

  }

  @Test
  public void testConnectOnly_Composite_Fails() throws Exception {
    Assume.assumeTrue(areTestsEnabled());
    StandardSftpConnection conn = createConnection();
    SftpAuthenticationWrapper auth =
        new SftpAuthenticationWrapper(new SftpKeyAuthentication(PROPERTIES.getProperty(CFG_PRIVATE_KEY_FILE), "PW:abde"),
            new SftpKeyAuthentication("/some/path/that/does/not/exist", PROPERTIES.getProperty(CFG_PRIVATE_KEY_PW)));
    conn.setAuthentication(auth);
    try {
      start(conn);
      FileTransferClient c = conn.connect(getDestinationString());
      fail();
    } catch (IOException | PasswordException expected) {
    } finally {
      stop(conn);
    }

  }

  @Test
  public void testConnectOnly_StrictKnownHosts_UnknownHost() throws Exception {
    Assume.assumeTrue(areTestsEnabled());
    File tempHostsFile = copyHostsFile(new File(PROPERTIES.getProperty(CFG_UNKNOWN_HOSTS_FILE)));

    StandardSftpConnection conn = createConnection();
    conn.setConfiguration(new InlineConfigRepositoryBuilder(true).build());
    conn.setKnownHostsFile(tempHostsFile.getCanonicalPath());
    try {
      start(conn);
      FileTransferClient c = conn.connect(getDestinationString());
      fail();
    } catch (FileTransferException expected) {

    } finally {
      stop(conn);
    }

  }

  @Test
  public void testConnectOnly_StrictKnownHosts_KnownHost() throws Exception {
    Assume.assumeTrue(areTestsEnabled());
    File tempHostsFile = copyHostsFile(new File(PROPERTIES.getProperty(CFG_KNOWN_HOSTS_FILE)));
    StandardSftpConnection conn = createConnection();
    conn.setConfiguration(new InlineConfigRepositoryBuilder(true).build());
    conn.setKnownHostsFile(tempHostsFile.getCanonicalPath());
    try {
      start(conn);
      FileTransferClient c = conn.connect(getDestinationString());
      assertTrue(c.isConnected());
      c.disconnect();
    } finally {
      stop(conn);
    }

  }

  @Test
  public void testConnectOnly_LenientKnownHosts_UnknownHost() throws Exception {
    Assume.assumeTrue(areTestsEnabled());
    File tempHostsFile = copyHostsFile(new File(PROPERTIES.getProperty(CFG_UNKNOWN_HOSTS_FILE)));

    StandardSftpConnection conn = createConnection();
    conn.setConfiguration(new InlineConfigRepositoryBuilder(false).build());
    conn.setKnownHostsFile(tempHostsFile.getCanonicalPath());
    try {
      start(conn);
      FileTransferClient c = conn.connect(getDestinationString());
      assertTrue(c.isConnected());
      c.disconnect();
    } finally {
      stop(conn);
    }

  }

  @Test
  public void testConnectOnly_LenientKnownHosts_KnownHost() throws Exception {
    Assume.assumeTrue(areTestsEnabled());
    File tempHostsFile = copyHostsFile(new File(PROPERTIES.getProperty(CFG_KNOWN_HOSTS_FILE)));

    StandardSftpConnection conn = createConnection();

    conn.setConfiguration(new InlineConfigRepositoryBuilder(false).build());
    conn.setKnownHostsFile(tempHostsFile.getCanonicalPath());
    try {
      start(conn);
      FileTransferClient c = conn.connect(getDestinationString());
      assertTrue(c.isConnected());
      c.disconnect();
    } finally {
      stop(conn);
    }

  }

  private File copyHostsFile(File srcKnownHosts) throws Exception {
    File tempDir = new File(PROPERTIES.getProperty(CFG_TEMP_HOSTS_FILE));
    if (!tempDir.exists() && !tempDir.mkdirs()) {
      throw new Exception("Couldn't make directory " + tempDir.getCanonicalPath());
    }
    File tempFile = File.createTempFile(StandardSftpConnectionTest.class.getSimpleName(), "", tempDir);
    FileUtils.copyFile(srcKnownHosts, tempFile);
    cleaner.track(tempFile, fileTracker, FileDeleteStrategy.FORCE);
    return tempFile;
  }



  @Override
  protected String getDestinationString() {
    return "sftp://" + PROPERTIES.getProperty(CFG_HOST) + "/" + PROPERTIES.getProperty(CFG_REMOTE_DIR);
  }

  @Override
  protected StandardSftpConnection createConnection() throws Exception {
    StandardSftpConnection c = new StandardSftpConnection();
    if (PROPERTIES.containsKey(CFG_KNOWN_HOSTS_FILE)) {
      File tempHostsFile = copyHostsFile(new File(PROPERTIES.getProperty(CFG_KNOWN_HOSTS_FILE)));
      c.setKnownHostsFile(tempHostsFile.getCanonicalPath());
    }
    if (PROPERTIES.containsKey(CFG_USER)) {
      c.setDefaultUserName(PROPERTIES.getProperty(CFG_USER));
    }
    if (PROPERTIES.containsKey(CFG_PRIVATE_KEY_FILE) && PROPERTIES.containsKey(CFG_PRIVATE_KEY_PW)) {
      c.setAuthentication(
          new SftpKeyAuthentication(PROPERTIES.getProperty(CFG_PRIVATE_KEY_FILE), PROPERTIES.getProperty(CFG_PRIVATE_KEY_PW)));
    }
    c.setAdditionalDebug(true);
    return c;
  }

  @Override
  protected String getDestinationStringWithOverride() throws Exception {
    return "sftp://" + PROPERTIES.getProperty(CFG_USER) + "@" + PROPERTIES.getProperty(CFG_HOST) + "/"
        + PROPERTIES.getProperty(CFG_REMOTE_DIR);
  }

  protected String getDestinationStringWithOverridePassword() throws Exception {
    return "sftp://" + PROPERTIES.getProperty(CFG_USER) + ":" + Password.decode(PROPERTIES.getProperty(CFG_PASSWORD)) + "@"
        + PROPERTIES.getProperty(CFG_HOST) + "/" + PROPERTIES.getProperty(CFG_REMOTE_DIR);
  }


  @Override
  protected void assertDefaultControlPort(int defaultControlPort) {
    assertEquals(22, defaultControlPort);
  }
}
