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

package com.adaptris.core.ftp;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.BooleanUtils;

import com.adaptris.filetransfer.FileTransferClient;
import com.adaptris.filetransfer.FileTransferException;
import com.adaptris.security.password.Password;
import com.adaptris.sftp.OpenSSHConfigBuilder;
import com.adaptris.sftp.SftpClient;

public class SftpConnectionTest extends FtpPasswordConnectionCase {

  public static final String CFG_HOST = "SftpConsumerTest.host";
  public static final String CFG_USER = "SftpConsumerTest.username";
  public static final String CFG_PASSWORD = "SftpConsumerTest.password";
  public static final String CFG_REMOTE_DIR = "SftpConsumerTest.remotedir";

  // DO NOT use the top two directly, use @setupTempHostsFile and the CFG_TEMP_HOSTS_FILE
  protected static final String CFG_KNOWN_HOSTS_FILE = "SftpConsumerTest.knownHostsFile";
  protected static final String CFG_UNKNOWN_HOSTS_FILE = "SftpConsumerTest.unknownHostsFile";
  protected static final String CFG_TEMP_HOSTS_FILE = "SftpConsumerTest.tempHostsFile";

  public SftpConnectionTest(String name) {
    super(name);
  }

  @Override
  protected SftpConnection createConnection() {
    SftpConnection c = new SftpConnection();
    c.setDefaultPassword(PROPERTIES.getProperty(CFG_PASSWORD));
    c.setDefaultUserName(PROPERTIES.getProperty(CFG_USER));
    c.setAdditionalDebug(true);
    return c;
  }

  @Override
  protected String getDestinationString() {
    return "sftp://" + PROPERTIES.getProperty(CFG_HOST) + "/" + PROPERTIES.getProperty(CFG_REMOTE_DIR);
  }

  @Override
  protected String getDestinationStringWithOverride() throws Exception {
    return "sftp://" + PROPERTIES.getProperty(CFG_USER) + ":" + Password.decode(PROPERTIES.getProperty(CFG_PASSWORD)) + "@"
        + PROPERTIES.getProperty(CFG_HOST) + "/" + PROPERTIES.getProperty(CFG_REMOTE_DIR);
  }

  @Override
  protected String getDestinationStringWithOverrideUserOnly() throws Exception {
    return "sftp://" + PROPERTIES.getProperty(CFG_USER) + "@" + PROPERTIES.getProperty(CFG_HOST) + "/"
        + PROPERTIES.getProperty(CFG_REMOTE_DIR);
  }

  protected String getScheme() {
    return "sftp";
  }

  public void testSocketTimeout() throws Exception {
    SftpConnection conn = new SftpConnection();
    assertNull(conn.getSocketTimeout());
    assertEquals(60000, conn.socketTimeout());

    conn.setSocketTimeout(10);
    assertEquals(Integer.valueOf(10), conn.getSocketTimeout());
    assertEquals(10, conn.socketTimeout());

    conn.setSocketTimeout(null);
    assertNull(conn.getSocketTimeout());
    assertEquals(60000, conn.socketTimeout());

  }


  public void testSetKnownHostsFile() throws Exception {
    SftpConnection conn = new SftpConnection();
    assertNull(conn.getKnownHostsFile());
    conn.setKnownHostsFile("abc");
    assertEquals("abc", conn.getKnownHostsFile());
  }


  public void testConnectOnly_DefaultBehaviour() throws Exception {
    if (areTestsEnabled()) {
      File tempHostsFile = copyHostsFile(new File(PROPERTIES.getProperty(CFG_KNOWN_HOSTS_FILE)));

      SftpConnection conn = createConnection();
      try {
        start(conn);
        FileTransferClient c = conn.connect(getDestinationString());
        assertTrue(c.isConnected());
        c.disconnect();
      } finally {
        stop(conn);
      }
    }
  }

  public void testConnectOnly_StrictKeyCheck_UnknownHost() throws Exception {
    if (areTestsEnabled()) {
      File tempHostsFile = copyHostsFile(new File(PROPERTIES.getProperty(CFG_UNKNOWN_HOSTS_FILE)));

      SftpConnection conn = createConnection();
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
  }

  public void testConnectOnly_PerHost_StrictKeyCheck_UnknownHost() throws Exception {
    if (areTestsEnabled()) {
      File tempHostsFile = copyHostsFile(new File(PROPERTIES.getProperty(CFG_UNKNOWN_HOSTS_FILE)));

      SftpConnection conn = createConnection();
      conn.setConfiguration(new PerHostConfigRepositoryBuilder(tempHostsFile, true).build());
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
  }

  public void testConnectOnly_PerHost_StrictKeyCheck_KnownHost() throws Exception {
    if (areTestsEnabled()) {
      File tempHostsFile = copyHostsFile(new File(PROPERTIES.getProperty(CFG_KNOWN_HOSTS_FILE)));

      SftpConnection conn = createConnection();
      conn.setConfiguration(new PerHostConfigRepositoryBuilder(tempHostsFile, true).build());
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
  }

  public void testConnectOnly_UnknownHost() throws Exception {
    if (areTestsEnabled()) {
      File tempHostsFile = copyHostsFile(new File(PROPERTIES.getProperty(CFG_UNKNOWN_HOSTS_FILE)));

      SftpConnection conn = createConnection();
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
  }

  public void testConnectOnly_KnownHost() throws Exception {
    if (areTestsEnabled()) {
      File tempHostsFile = copyHostsFile(new File(PROPERTIES.getProperty(CFG_KNOWN_HOSTS_FILE)));

      SftpConnection conn = createConnection();
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
  }

  public void testConnectOnly_OpenSSH_Strict_UnknownHost() throws Exception {
    if (areTestsEnabled()) {
      File tempHostsFile = copyHostsFile(new File(PROPERTIES.getProperty(CFG_UNKNOWN_HOSTS_FILE)));

      SftpConnection conn = createConnection();
      conn.setConfiguration(new OpenSSHConfigBuilder(createOpenSshConfig(true).getCanonicalPath()));
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
  }

  public void testConnectOnly_OpenSSH_Strict_KnownHost() throws Exception {
    if (areTestsEnabled()) {
      File tempHostsFile = copyHostsFile(new File(PROPERTIES.getProperty(CFG_KNOWN_HOSTS_FILE)));

      SftpConnection conn = createConnection();
      conn.setConfiguration(new OpenSSHConfigBuilder(createOpenSshConfig(true).getCanonicalPath()));
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
  }


  private File copyHostsFile(File srcKnownHosts) throws Exception {
    File tempDir = new File(PROPERTIES.getProperty(CFG_TEMP_HOSTS_FILE));
    tempDir.mkdirs();
    File tempFile = File.createTempFile(SftpConnectionTest.class.getSimpleName(), "", tempDir);
    FileUtils.copyFile(srcKnownHosts, tempFile);
    return tempFile;
  }

  private File createOpenSshConfig(boolean strict) throws Exception {
    File tempDir = new File(PROPERTIES.getProperty(CFG_TEMP_HOSTS_FILE));
    tempDir.mkdirs();
    File tempFile = File.createTempFile(SftpConnectionTest.class.getSimpleName(), "", tempDir);
    try (PrintStream out = new PrintStream(new FileOutputStream(tempFile))) {
      out.println("Host *");
      out.println("  StrictHostKeyChecking " + BooleanUtils.toStringYesNo(strict));
      out.println("  " + SftpClient.SSH_PREFERRED_AUTHENTICATIONS + " " + SftpClient.NO_KERBEROS_AUTH);
    }
    return tempFile;
  }


  protected void assertDefaultControlPort(int defaultControlPort) {
    assertEquals(SftpConnection.DEFAULT_CONTROL_PORT, defaultControlPort);
  }
}
