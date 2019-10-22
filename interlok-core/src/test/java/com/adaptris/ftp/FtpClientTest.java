/*
 * Copyright 2019 Adaptris Ltd.
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
package com.adaptris.ftp;

import static com.adaptris.core.ftp.EmbeddedFtpServer.DEFAULT_BUILD_DIR_CANONICAL;
import static com.adaptris.core.ftp.EmbeddedFtpServer.DEFAULT_PASSWORD;
import static com.adaptris.core.ftp.EmbeddedFtpServer.DEFAULT_USERNAME;
import static com.adaptris.core.ftp.EmbeddedFtpServer.DEFAULT_WORK_DIR_CANONICAL;
import static com.adaptris.core.ftp.EmbeddedFtpServer.PAYLOAD;
import static com.adaptris.core.ftp.EmbeddedFtpServer.PAYLOAD_ALTERNATE;
import static com.adaptris.core.ftp.EmbeddedFtpServer.SERVER_ADDRESS;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.InetAddress;
import java.nio.charset.Charset;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPSClient;
import org.junit.Test;
import org.mockftpserver.fake.FakeFtpServer;
import org.mockito.Mockito;
import com.adaptris.core.fs.NewerThan;
import com.adaptris.core.ftp.EmbeddedFtpServer;
import com.adaptris.core.stubs.TempFileUtils;
import com.adaptris.util.GuidGenerator;

// Tests that increase coverage w/o having to "enable" the remote FTP server.
public class FtpClientTest {

  protected static final GuidGenerator GUID = new GuidGenerator();

  @Test
  public void testDisconnect() throws Exception {
    FTPClient mock = Mockito.mock(FTPClient.class);
    Mockito.when(mock.getReplyCode()).thenReturn(451);
    Mockito.when(mock.isConnected()).thenReturn(true, false);
    assertNull(ApacheFtpClientImpl.disconnect(mock));
    assertNotNull(ApacheFtpClientImpl.disconnect(mock));
    assertSame(mock, ApacheFtpClientImpl.disconnect(mock));
    assertFalse(mock.isConnected());
  }


  @Test
  public void testNoFtpServer() throws Exception {
    try (RefusedConnection client = new RefusedConnection()) {
      client.connect(DEFAULT_USERNAME, DEFAULT_PASSWORD);
      fail();
    } catch (IOException expected) {

    }
  }

  @Test
  public void testConnect() throws Exception {
    EmbeddedFtpServer embedded = new EmbeddedFtpServer();
    FakeFtpServer server = embedded.createAndStart(embedded.createFilesystem(10));
    try (CommonsNetFtpClient client = create(server);) {
      client.connect(DEFAULT_USERNAME, DEFAULT_PASSWORD);
      client.setDataMode(FtpDataMode.ACTIVE);
      client.setDataMode(FtpDataMode.PASSIVE);
      client.system();
      client.pwd();
      assertTrue(client.isConnected());
      client.setKeepAliveTimeout(10);
      assertEquals(10, client.getKeepAliveTimeout());
    } finally {
      server.stop();
    }
  }

  @Test
  public void testConnect_WithAccount() throws Exception {
    try (CommonsNetFtpClient client = new AccountLogin(true)) {
      client.connect(DEFAULT_USERNAME, DEFAULT_PASSWORD, "MyAccount");
    }
    try (CommonsNetFtpClient client = new AccountLogin(false)) {
      client.connect(DEFAULT_USERNAME, DEFAULT_PASSWORD, "MyAccount");
      fail();
    } catch (IOException expected) {

    }

  }

  @Test
  public void testKeepAlive() throws Exception {
    EmbeddedFtpServer embedded = new EmbeddedFtpServer();
    try (BrokenConnection client = new BrokenConnection()) {
      try {
        client.getKeepAliveTimeout();
        fail();
      } catch (Exception expected) {

      }
      assertFalse(client.isConnected());
    }
    try (BrokenConnection client = new BrokenConnection()) {
      try {
        client.setKeepAliveTimeout(10);
        fail();
      } catch (Exception expected) {

      }
      assertFalse(client.isConnected());
    }
    FakeFtpServer server = embedded.createAndStart(embedded.createFilesystem(10));
    try (CommonsNetFtpClient client = create(server);) {
      client.connect(DEFAULT_USERNAME, DEFAULT_PASSWORD);
      client.setKeepAliveTimeout(10);
      assertEquals(10, client.getKeepAliveTimeout());
    } finally {
      server.stop();
    }
  }

  @Test
  public void testChdir() throws Exception {
    EmbeddedFtpServer embedded = new EmbeddedFtpServer();
    FakeFtpServer server = embedded.createAndStart(embedded.createFilesystem(10));
    try (CommonsNetFtpClient client = create(server);) {
      client.connect(DEFAULT_USERNAME, DEFAULT_PASSWORD);
      client.chdir(DEFAULT_WORK_DIR_CANONICAL);
      String[] files = client.dir();
      assertEquals(10, files.length);
    } finally {
      server.stop();
    }
  }

  @Test
  public void testList() throws Exception {
    EmbeddedFtpServer embedded = new EmbeddedFtpServer();
    FakeFtpServer server = embedded.createAndStart(embedded.createFilesystem(10));
    try (CommonsNetFtpClient client = create(server);) {
      client.connect(DEFAULT_USERNAME, DEFAULT_PASSWORD);
      String[] files = client.dir(DEFAULT_WORK_DIR_CANONICAL);
      assertEquals(10, files.length);
      String[] full = client.dir(DEFAULT_WORK_DIR_CANONICAL, true);
      assertEquals(10, full.length);
    } finally {
      server.stop();
    }
  }

  @Test
  public void testListFileFilter() throws Exception {
    EmbeddedFtpServer embedded = new EmbeddedFtpServer();
    FakeFtpServer server = embedded.createAndStart(embedded.createFilesystem(10));
    try (CommonsNetFtpClient client = create(server);) {
      client.connect(DEFAULT_USERNAME, DEFAULT_PASSWORD);
      // Since INTERLOK-2916 adds filtering for some extended attributes; then this should work.
      String[] files = client.dir(DEFAULT_WORK_DIR_CANONICAL, new NewerThan("-P30D"));
      assertEquals(10, files.length);
      files = client.dir(DEFAULT_WORK_DIR_CANONICAL, (FileFilter) null);
      assertEquals(10, files.length);
    } finally {
      server.stop();
    }
  }

  @Test
  public void testListFilenameFilter() throws Exception {
    EmbeddedFtpServer embedded = new EmbeddedFtpServer();
    FakeFtpServer server = embedded.createAndStart(embedded.createFilesystem(10));
    try (CommonsNetFtpClient client = create(server);) {
      client.connect(DEFAULT_USERNAME, DEFAULT_PASSWORD);
      String[] files = client.dir(DEFAULT_WORK_DIR_CANONICAL, (dir, name) -> true);
      assertEquals(10, files.length);
      files = client.dir(DEFAULT_WORK_DIR_CANONICAL, (FilenameFilter) null);
      assertEquals(10, files.length);
    } finally {
      server.stop();
    }
  }

  @Test
  public void testGet() throws Exception {
    EmbeddedFtpServer embedded = new EmbeddedFtpServer();
    FakeFtpServer server = embedded.createAndStart(embedded.createFilesystem(10));
    try (CommonsNetFtpClient client = create(server);) {
      client.connect(DEFAULT_USERNAME, DEFAULT_PASSWORD);
      String[] files = client.dir(DEFAULT_WORK_DIR_CANONICAL);
      assertEquals(10, files.length);
      String contents = new String(client.get(DEFAULT_WORK_DIR_CANONICAL + "/" + files[0]),
          Charset.defaultCharset()).trim();
      assertEquals(PAYLOAD.trim(), contents);
    } finally {
      server.stop();
    }
  }

  @Test
  public void testGetLocalFile() throws Exception {
    EmbeddedFtpServer embedded = new EmbeddedFtpServer();
    FakeFtpServer server = embedded.createAndStart(embedded.createFilesystem(10));
    try (CommonsNetFtpClient client = create(server);) {
      client.connect(DEFAULT_USERNAME, DEFAULT_PASSWORD);
      String[] files = client.dir(DEFAULT_WORK_DIR_CANONICAL);
      assertEquals(10, files.length);
      File localFile = TempFileUtils.createTrackedFile(embedded);
      client.get(localFile.getCanonicalPath(), DEFAULT_WORK_DIR_CANONICAL + "/" + files[0]);
      String contents = FileUtils.readFileToString(localFile, Charset.defaultCharset()).trim();
      assertEquals(PAYLOAD.trim(), contents);
    } finally {
      server.stop();
    }
  }

  @Test
  public void testGetOutputStream() throws Exception {
    EmbeddedFtpServer embedded = new EmbeddedFtpServer();
    FakeFtpServer server = embedded.createAndStart(embedded.createFilesystem(10));
    try (CommonsNetFtpClient client = create(server);
        ByteArrayOutputStream out = new ByteArrayOutputStream();) {
      client.connect(DEFAULT_USERNAME, DEFAULT_PASSWORD);
      String[] files = client.dir(DEFAULT_WORK_DIR_CANONICAL);
      assertEquals(10, files.length);      
      client.get(out, DEFAULT_WORK_DIR_CANONICAL + "/" + files[0]);
      String contents = out.toString(Charset.defaultCharset()).trim();
      assertEquals(PAYLOAD.trim(), contents);
    } finally {
      server.stop();
    }
  }

  @Test
  public void testPutLocalFile() throws Exception {
    EmbeddedFtpServer embedded = new EmbeddedFtpServer();
    FakeFtpServer server = embedded.createAndStart(embedded.createFilesystem(10));
    try (CommonsNetFtpClient client = create(server);
        ByteArrayOutputStream out = new ByteArrayOutputStream();) {
      client.connect(DEFAULT_USERNAME, DEFAULT_PASSWORD);
      File localFile = TempFileUtils.createTrackedFile(embedded);
      FileUtils.write(localFile, PAYLOAD_ALTERNATE, Charset.defaultCharset());
      String remoteFileName = DEFAULT_BUILD_DIR_CANONICAL + "/" + GUID.safeUUID();
      client.put(localFile.getCanonicalPath(), remoteFileName);
      String contents = new String(client.get(remoteFileName), Charset.defaultCharset()).trim();
      assertEquals(PAYLOAD_ALTERNATE.trim(), contents);
    } finally {
      server.stop();
    }
  }

  @Test
  public void testPutLocalFileAppend() throws Exception {
    EmbeddedFtpServer embedded = new EmbeddedFtpServer();
    FakeFtpServer server = embedded.createAndStart(embedded.createFilesystem(10));
    try (CommonsNetFtpClient client = create(server);
        ByteArrayOutputStream out = new ByteArrayOutputStream();) {
      client.connect(DEFAULT_USERNAME, DEFAULT_PASSWORD);
      File localFile = TempFileUtils.createTrackedFile(embedded);
      FileUtils.write(localFile, PAYLOAD_ALTERNATE, Charset.defaultCharset());
      String remoteFileName = DEFAULT_BUILD_DIR_CANONICAL + "/" + GUID.safeUUID();
      client.put(localFile.getCanonicalPath(), remoteFileName);
      client.put(localFile.getCanonicalPath(), remoteFileName, true);
      String contents = new String(client.get(remoteFileName), Charset.defaultCharset()).trim();
      assertTrue(contents.startsWith(PAYLOAD_ALTERNATE));
    } finally {
      server.stop();
    }
  }

  @Test
  public void testPutBytes() throws Exception {
    EmbeddedFtpServer embedded = new EmbeddedFtpServer();
    FakeFtpServer server = embedded.createAndStart(embedded.createFilesystem(10));
    try (CommonsNetFtpClient client = create(server);
        ByteArrayOutputStream out = new ByteArrayOutputStream();) {
      client.connect(DEFAULT_USERNAME, DEFAULT_PASSWORD);
      String remoteFileName = DEFAULT_BUILD_DIR_CANONICAL + "/" + GUID.safeUUID();
      client.put(PAYLOAD_ALTERNATE.getBytes(Charset.defaultCharset()), remoteFileName);
      String contents = new String(client.get(remoteFileName), Charset.defaultCharset()).trim();
      assertEquals(PAYLOAD_ALTERNATE.trim(), contents);
    } finally {
      server.stop();
    }
  }

  @Test
  public void testPutBytes_Append() throws Exception {
    EmbeddedFtpServer embedded = new EmbeddedFtpServer();
    FakeFtpServer server = embedded.createAndStart(embedded.createFilesystem(10));
    try (CommonsNetFtpClient client = create(server);
        ByteArrayOutputStream out = new ByteArrayOutputStream();) {
      client.connect(DEFAULT_USERNAME, DEFAULT_PASSWORD);
      String remoteFileName = DEFAULT_BUILD_DIR_CANONICAL + "/" + GUID.safeUUID();
      client.put(PAYLOAD_ALTERNATE.getBytes(Charset.defaultCharset()), remoteFileName);
      client.put(PAYLOAD_ALTERNATE.getBytes(Charset.defaultCharset()), remoteFileName, true);
      String contents = new String(client.get(remoteFileName), Charset.defaultCharset()).trim();
      assertTrue(contents.startsWith(PAYLOAD_ALTERNATE));
    } finally {
      server.stop();
    }
  }

  @Test
  public void testDelete() throws Exception {
    EmbeddedFtpServer embedded = new EmbeddedFtpServer();
    FakeFtpServer server = embedded.createAndStart(embedded.createFilesystem(10));
    try (CommonsNetFtpClient client = create(server);
        ByteArrayOutputStream out = new ByteArrayOutputStream();) {
      client.connect(DEFAULT_USERNAME, DEFAULT_PASSWORD);
      String[] files = client.dir(DEFAULT_WORK_DIR_CANONICAL);
      assertEquals(10, files.length);
      client.delete(DEFAULT_WORK_DIR_CANONICAL + "/" + files[0]);
      assertEquals(9, client.dir(DEFAULT_WORK_DIR_CANONICAL).length);
    } finally {
      server.stop();
    }
  }

  @Test
  public void testRename() throws Exception {
    EmbeddedFtpServer embedded = new EmbeddedFtpServer();
    FakeFtpServer server = embedded.createAndStart(embedded.createFilesystem(10));
    try (CommonsNetFtpClient client = create(server);
        ByteArrayOutputStream out = new ByteArrayOutputStream();) {
      client.connect(DEFAULT_USERNAME, DEFAULT_PASSWORD);
      String[] files = client.dir(DEFAULT_WORK_DIR_CANONICAL);
      assertEquals(10, files.length);
      String renameTo = DEFAULT_BUILD_DIR_CANONICAL + "/" + GUID.safeUUID();
      client.rename(DEFAULT_WORK_DIR_CANONICAL + "/" + files[0], renameTo);
      assertEquals(9, client.dir(DEFAULT_WORK_DIR_CANONICAL).length);
    } finally {
      server.stop();
    }
  }

  @Test
  public void testMkdir() throws Exception {
    EmbeddedFtpServer embedded = new EmbeddedFtpServer();
    FakeFtpServer server = embedded.createAndStart(embedded.createFilesystem(10));
    try (CommonsNetFtpClient client = create(server);
        ByteArrayOutputStream out = new ByteArrayOutputStream();) {
      client.connect(DEFAULT_USERNAME, DEFAULT_PASSWORD);
      String remoteDir = DEFAULT_BUILD_DIR_CANONICAL + "/" + GUID.safeUUID();
      client.mkdir(remoteDir);
      assertEquals(0, client.dir(remoteDir).length);
      client.rmdir(remoteDir);
    } finally {
      server.stop();
    }
  }

  @Test
  public void testLastModified() throws Exception {
    EmbeddedFtpServer embedded = new EmbeddedFtpServer();
    FakeFtpServer server = embedded.createAndStart(embedded.createFilesystem(10));
    try (CommonsNetFtpClient client = create(server);
        ByteArrayOutputStream out = new ByteArrayOutputStream();) {
      client.connect(DEFAULT_USERNAME, DEFAULT_PASSWORD);
      String[] files = client.dir(DEFAULT_WORK_DIR_CANONICAL);
      assertEquals(10, files.length);
      String file = DEFAULT_WORK_DIR_CANONICAL + "/" + files[0];
      assertTrue(client.lastModified(file) > 0);
      assertNotNull(client.lastModifiedDate(file));
    } finally {
      server.stop();
    }
  }

  @Test(expected = FtpException.class)
  public void testLastModified_BadResponse() throws Exception {
    EmbeddedFtpServer embedded = new EmbeddedFtpServer(false);
    FakeFtpServer server = embedded.createAndStart(embedded.createFilesystem(10));
    try (CommonsNetFtpClient client = create(server);
        ByteArrayOutputStream out = new ByteArrayOutputStream();) {
      client.connect(DEFAULT_USERNAME, DEFAULT_PASSWORD);
      String[] files = client.dir(DEFAULT_WORK_DIR_CANONICAL);
      assertEquals(10, files.length);
      String file = DEFAULT_WORK_DIR_CANONICAL + "/" + files[0];
      client.lastModified(file);
    } finally {
      server.stop();
    }
  }

  @Test
  public void testFtpClient() throws Exception {
    CommonsNetFtpClient client = new CommonsNetFtpClient("localhost");
    assertNotNull(client.createFTPClient());
    FTPClient mock = Mockito.mock(FTPClient.class);
    client.preConnectSettings(mock);
    client.postConnectSettings(mock);
  }

  @Test
  public void testFtpSslClient() throws Exception {
    CommonsNetFtpSslClient client = new CommonsNetFtpSslClient("localhost");
    assertNotNull(client.createFTPClient());
    FTPSClient mock = Mockito.mock(FTPSClient.class);
    client.preConnectSettings(mock);
    client.postConnectSettings(mock);
  }

  protected <T extends ApacheFtpClientImpl<FTPClient>> T create(FakeFtpServer server)
      throws Exception {
    return (T) new CommonsNetFtpClient(SERVER_ADDRESS, server.getServerControlPort());
  }

  abstract class MockClient extends CommonsNetFtpClient {
    protected FTPClient mock;

    public MockClient() throws Exception {
      super("localhost");
    }

    @Override
    protected FTPClient createFTPClient() {
      return mock;
    }

  }

  private class AccountLogin extends MockClient {

    public AccountLogin(boolean success) throws Exception {
      mock = Mockito.mock(FTPClient.class);
      Mockito.when(mock.getReplyCode()).thenReturn(211);
      Mockito.when(mock.login(anyString(), anyString(), anyString())).thenReturn(success);
    }
  }

  private class RefusedConnection extends MockClient {

    public RefusedConnection() throws Exception {
      mock = Mockito.mock(FTPClient.class);
      Mockito.when(mock.getReplyCode()).thenReturn(451);
      Mockito.doNothing().when(mock).disconnect();
    }
  }

  private class BrokenConnection extends MockClient {

    public BrokenConnection() throws Exception {
      // Since getReplyCode() is pretty much the first thing that happens this should break everything.
      mock = Mockito.mock(FTPClient.class);
      Mockito.doThrow(new IOException()).when(mock).connect(anyString(), anyInt());
      Mockito.doThrow(new IOException()).when(mock).connect(any(InetAddress.class));
      Mockito.doThrow(new IOException()).when(mock).connect(any(InetAddress.class), anyInt());
      Mockito.doThrow(new IOException()).when(mock).connect(anyString());
      Mockito.doThrow(new IOException()).when(mock).connect(any(InetAddress.class), anyInt(),
          any(InetAddress.class), anyInt());
      Mockito.doThrow(new IOException()).when(mock).connect(anyString(), anyInt(),
          any(InetAddress.class),
          anyInt());
      Mockito.doNothing().when(mock).disconnect();
    }
  }
}
