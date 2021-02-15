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

import static com.adaptris.core.ftp.EmbeddedFtpServer.DEFAULT_FILENAME;
import static com.adaptris.core.ftp.EmbeddedFtpServer.DEFAULT_PASSWORD;
import static com.adaptris.core.ftp.EmbeddedFtpServer.DEFAULT_PROC_DIR_CANONICAL;
import static com.adaptris.core.ftp.EmbeddedFtpServer.DEFAULT_USERNAME;
import static com.adaptris.core.ftp.EmbeddedFtpServer.DEFAULT_WORK_DIR_CANONICAL;
import static com.adaptris.core.ftp.EmbeddedFtpServer.DEFAULT_WORK_DIR_NAME;
import static com.adaptris.core.ftp.EmbeddedFtpServer.DESTINATION_URL_OVERRIDE;
import static com.adaptris.core.ftp.EmbeddedFtpServer.PAYLOAD;
import static com.adaptris.core.ftp.EmbeddedFtpServer.SERVER_ADDRESS;
import static com.adaptris.core.ftp.EmbeddedFtpServer.SLASH;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import org.apache.commons.io.filefilter.RegexFileFilter;
import org.apache.oro.io.GlobFilenameFilter;
import org.junit.Test;
import org.mockftpserver.fake.FakeFtpServer;
import org.mockftpserver.fake.filesystem.FileEntry;
import org.mockftpserver.fake.filesystem.FileSystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.core.ConfiguredConsumeDestination;
import com.adaptris.core.ConsumeDestination;
import com.adaptris.core.FixedIntervalPoller;
import com.adaptris.core.MimeEncoder;
import com.adaptris.core.Poller;
import com.adaptris.core.PollerImp;
import com.adaptris.core.StandaloneConsumer;
import com.adaptris.core.lms.FileBackedMessageFactory;
import com.adaptris.core.stubs.MockMessageListener;
import com.adaptris.core.util.LifecycleHelper;
import com.adaptris.ftp.ClientSettings;
import com.adaptris.ftp.FtpDataMode;
import com.adaptris.util.KeyValuePair;
import com.adaptris.util.KeyValuePairSet;
import com.adaptris.util.TimeInterval;

public class FtpConsumerTest extends FtpConsumerCase {

  private transient Logger log = LoggerFactory.getLogger(this.getClass());

  @Override
  protected FtpConnection createConnectionForExamples() {
    return FtpExampleHelper.ftpConnection();
  }

  @Override
  protected String getScheme() {
    return "ftp";
  }

  @Override
  protected String createBaseFileName(Object object) {
    return super.createBaseFileName(object);
  }

  @Test
  public void testFileFilterImp() throws Exception {
    FtpConsumer ftpConsumer = new FtpConsumer();
    assertNull(ftpConsumer.getFileFilterImp());
    assertEquals(RegexFileFilter.class.getCanonicalName(), ftpConsumer.fileFilterImp());

    ftpConsumer.setFileFilterImp("ABCDE");
    assertEquals("ABCDE", ftpConsumer.getFileFilterImp());
    assertEquals("ABCDE", ftpConsumer.fileFilterImp());

    ftpConsumer.setFileFilterImp(null);
    assertNull(ftpConsumer.getFileFilterImp());
    assertEquals(RegexFileFilter.class.getCanonicalName(), ftpConsumer.fileFilterImp());
  }

  @Test
  public void testWipSuffix() throws Exception {
    FtpConsumer ftpConsumer = new FtpConsumer();
    assertNull(ftpConsumer.getWipSuffix());
    assertEquals("_wip", ftpConsumer.wipSuffix());

    ftpConsumer.setWipSuffix("ABCDE");
    assertEquals("ABCDE", ftpConsumer.getWipSuffix());
    assertEquals("ABCDE", ftpConsumer.wipSuffix());

    ftpConsumer.setWipSuffix(null);
    assertNull(ftpConsumer.getWipSuffix());
    assertEquals("_wip", ftpConsumer.wipSuffix());
  }

  @Test
  public void testBasicConsume() throws Exception {
    int count = 1;
    EmbeddedFtpServer helper = new EmbeddedFtpServer();
    MockMessageListener listener = new MockMessageListener();
    FakeFtpServer server = helper.createAndStart(helper.createFilesystem(count));
    StandaloneConsumer sc = null;
    try {
      FtpConsumer ftpConsumer = createForTests(listener, "testBasicConsume");
      FtpConnection consumeConnection = create(server);
      sc = new StandaloneConsumer(consumeConnection, ftpConsumer);
      // INTERLOK-3329 For coverage so the prepare() warning is executed 2x
      LifecycleHelper.prepare(sc);
      start(sc);
      waitForMessages(listener, count);
      helper.assertMessages(listener.getMessages(), count);
    }
    catch (Exception e) {
      throw e;
    }
    finally {
      stop(sc);
      server.stop();
    }

  }

  @Test
  public void testBasicConsume_NoDebug() throws Exception {
    int count = 1;
    EmbeddedFtpServer helper = new EmbeddedFtpServer();
    MockMessageListener listener = new MockMessageListener();
    FakeFtpServer server = helper.createAndStart(helper.createFilesystem(count));
    StandaloneConsumer sc = null;
    try {
      FtpConsumer ftpConsumer = createForTests(listener, "testBasicConsume");
      FtpConnection consumeConnection = create(server);
      consumeConnection.setAdditionalDebug(false);
      sc = new StandaloneConsumer(consumeConnection, ftpConsumer);
      start(sc);
      waitForMessages(listener, count);
      helper.assertMessages(listener.getMessages(), count);
    }
    catch (Exception e) {
      throw e;
    }
    finally {
      stop(sc);
      server.stop();
    }

  }

  @Test
  public void testConsumeWithOverride() throws Exception {
    int count = 1;
    EmbeddedFtpServer helper = new EmbeddedFtpServer();
    MockMessageListener listener = new MockMessageListener();
    FakeFtpServer server = helper.createAndStart(helper.createFilesystem(count));
    StandaloneConsumer sc = null;
    try {
      FtpConsumer ftpConsumer = createForTests(listener, DESTINATION_URL_OVERRIDE);
      FtpConnection consumeConnection = create(server);
      sc = new StandaloneConsumer(consumeConnection, ftpConsumer);
      start(sc);
      waitForMessages(listener, count);
      helper.assertMessages(listener.getMessages(), count);
    }
    catch (Exception e) {
      throw e;
    }
    finally {
      stop(sc);
      server.stop();
    }
  }

  @Test
  public void testConsumeWithFilter() throws Exception {
    int count = 1;
    EmbeddedFtpServer helper = new EmbeddedFtpServer();
    MockMessageListener listener = new MockMessageListener();
    FileSystem filesystem = helper.createFilesystem_DirsOnly();
    for (int i = 0; i < count; i++) {
      filesystem.add(new FileEntry(DEFAULT_WORK_DIR_CANONICAL + SLASH + DEFAULT_FILENAME + i + ".txt", PAYLOAD));
    }
    FakeFtpServer server = helper.createAndStart(filesystem);
    StandaloneConsumer sc = null;
    try {
      FtpConsumer ftpConsumer = createForTests(listener, SERVER_ADDRESS);
      ftpConsumer.setFilterExpression("*.txt");
      ftpConsumer.setFileFilterImp(GlobFilenameFilter.class.getCanonicalName());
      FtpConnection consumeConnection = create(server);
      sc = new StandaloneConsumer(consumeConnection, ftpConsumer);
      start(sc);
      waitForMessages(listener, count);
      helper.assertMessages(listener.getMessages(), count);
    }
    catch (Exception e) {
      throw e;
    }
    finally {
      stop(sc);
      server.stop();
    }
  }

  @Test
  public void testConsumeWithQuietPeriod() throws Exception {

    int count = 1;
    EmbeddedFtpServer helper = new EmbeddedFtpServer();
    MockMessageListener listener = new MockMessageListener();
    FakeFtpServer server = helper.createAndStart(helper.createFilesystem(count));
    StandaloneConsumer sc = null;
    try {
      FtpConsumer ftpConsumer = createForTests(listener, "testConsumeWithQuietPeriod");
      ftpConsumer.setQuietInterval(new TimeInterval(1L, TimeUnit.SECONDS));
      FtpConnection consumeConnection = create(server);
      sc = new StandaloneConsumer(consumeConnection, ftpConsumer);
      start(sc);
      waitForMessages(listener, count);
      helper.assertMessages(listener.getMessages(), count);
    }
    catch (Exception e) {
      throw e;
    }
    finally {
      stop(sc);
      server.stop();
    }
  }

  @Test
  public void testConsumeWithNonMatchingFilter() throws Exception {
    int count = 1;
    EmbeddedFtpServer helper = new EmbeddedFtpServer();
    MockMessageListener listener = new MockMessageListener();
    FileSystem filesystem = helper.createFilesystem_DirsOnly();
    for (int i = 0; i < count; i++) {
      filesystem.add(new FileEntry(DEFAULT_WORK_DIR_CANONICAL + SLASH + DEFAULT_FILENAME + i + ".txt", PAYLOAD));
    }
    FakeFtpServer server = helper.createAndStart(filesystem);
    StandaloneConsumer sc = null;
    try {
      AtomicBoolean pollFired = new AtomicBoolean(false);
      FixedIntervalPoller poller = new FixedIntervalPoller(new TimeInterval(300L, TimeUnit.MILLISECONDS)).withPollerCallback(e -> {
        log.trace("Poll Fired {}", getName());
        if (e == 0) {
          pollFired.set(true);
        }
      });
      FtpConsumer ftpConsumer = createForTests(listener, SERVER_ADDRESS, poller);
      ftpConsumer.setFilterExpression("^*.xml$");
      ftpConsumer.setFileFilterImp(RegexFileFilter.class.getCanonicalName());
      FtpConnection consumeConnection = create(server);
      sc = new StandaloneConsumer(consumeConnection, ftpConsumer);
      start(sc);
      long waitTime = waitForPollCallback(pollFired);
      log.trace("Waited for {}ms for == 0 poll", waitTime);
      helper.assertMessages(listener.getMessages(), 0);
      assertEquals(count, filesystem.listFiles(DEFAULT_WORK_DIR_CANONICAL).size());
    }
    catch (Exception e) {
      throw e;
    }
    finally {
      stop(sc);
      server.stop();
    }
  }

  @Test
  public void testActiveModeConsume() throws Exception {

    int count = 1;
    EmbeddedFtpServer helper = new EmbeddedFtpServer();
    MockMessageListener listener = new MockMessageListener(100);
    FakeFtpServer server = helper.createAndStart(helper.createFilesystem(count));
    StandaloneConsumer sc = null;
    try {
      FtpConsumer ftpConsumer = createForTests(listener, "testActiveModeConsume");
      FtpConnection consumeConnection = create(server);
      consumeConnection.setFtpDataMode(FtpDataMode.ACTIVE);
      sc = new StandaloneConsumer(consumeConnection, ftpConsumer);
      start(sc);
      waitForMessages(listener, count);
      helper.assertMessages(listener.getMessages(), count);
    }
    catch (Exception e) {
      throw e;
    }
    finally {
      stop(sc);
      server.stop();
    }
  }

  @Test
  public void testPassiveModeConsume() throws Exception {

    int count = 1;
    EmbeddedFtpServer helper = new EmbeddedFtpServer();
    MockMessageListener listener = new MockMessageListener(100);
    FakeFtpServer server = helper.createAndStart(helper.createFilesystem(count));
    StandaloneConsumer sc = null;
    try {
      FtpConsumer ftpConsumer = createForTests(listener, "testPassiveModeConsume");
      FtpConnection consumeConnection = create(server);
      consumeConnection.setFtpDataMode(FtpDataMode.PASSIVE);
      sc = new StandaloneConsumer(consumeConnection, ftpConsumer);
      start(sc);
      waitForMessages(listener, count);
      helper.assertMessages(listener.getMessages(), count);
    }
    catch (Exception e) {
      throw e;
    }
    finally {
      stop(sc);
      server.stop();
    }
  }

  @Test
  public void testConsume_ForceRelativePath() throws Exception {
    int count = 1;
    EmbeddedFtpServer helper = new EmbeddedFtpServer();
    MockMessageListener listener = new MockMessageListener(100);
    FakeFtpServer server = helper.createAndStart(helper.createFilesystem(count));
    StandaloneConsumer sc = null;
    try {
      FtpConsumer ftpConsumer = createForTests(listener, "testConsume_ForceRelativePath");
      ftpConsumer.setWorkDirectory(SLASH + DEFAULT_WORK_DIR_NAME);
      FtpConnection consumeConnection = create(server);
      consumeConnection.setForceRelativePath(Boolean.TRUE);
      sc = new StandaloneConsumer(consumeConnection, ftpConsumer);
      start(sc);
      waitForMessages(listener, count);
      helper.assertMessages(listener.getMessages(), count);
    }
    catch (Exception e) {
      throw e;
    }
    finally {
      stop(sc);
      server.stop();
    }
  }

  @Test
  public void testConsumeWithQuietPeriodAndTimezone() throws Exception {
    int count = 1;
    EmbeddedFtpServer helper = new EmbeddedFtpServer();
    MockMessageListener listener = new MockMessageListener(100);
    FileSystem filesystem = helper.createFilesystem_DirsOnly();
    for (int i = 0; i < count; i++) {
      filesystem.add(new FileEntry(DEFAULT_WORK_DIR_CANONICAL + SLASH + DEFAULT_FILENAME + i + ".txt", PAYLOAD));
    }
    FakeFtpServer server = helper.createAndStart(filesystem);
    StandaloneConsumer sc = null;
    try {

      AtomicBoolean pollFired = new AtomicBoolean(false);
      PollerImp poller = new FixedIntervalPoller(new TimeInterval(300L, TimeUnit.MILLISECONDS)).withPollerCallback(e -> {
        log.trace("Poll Fired {}", getName());
        if (e == 0) {
          pollFired.set(true);
        }
      });
      FtpConsumer ftpConsumer = createForTests(listener, "testConsumeWithQuietPeriodAndTimezone", poller);

      ftpConsumer.setQuietInterval(new TimeInterval(3L, TimeUnit.SECONDS));
      FtpConnection consumeConnection = create(server);
      consumeConnection.setAdditionalDebug(true);
      consumeConnection.setServerTimezone("America/Los_Angeles");

      sc = new StandaloneConsumer(consumeConnection, ftpConsumer);
      start(sc);
      long waitTime = waitForPollCallback(pollFired);
      log.trace("Waited for {}ms for == 0 poll", waitTime);

      helper.assertMessages(listener.getMessages(), 0);
      assertEquals(count, filesystem.listFiles(DEFAULT_WORK_DIR_CANONICAL).size());

    }
    catch (Exception e) {
      throw e;
    }
    finally {
      stop(sc);
      server.stop();
    }
  }

  @Test
  public void testConsume_WithProcDirectory() throws Exception {

    int count = 1;
    EmbeddedFtpServer helper = new EmbeddedFtpServer();
    MockMessageListener listener = new MockMessageListener(100);
    FileSystem filesystem = helper.createFilesystem_DirsOnly();
    for (int i = 0; i < count; i++) {
      filesystem.add(new FileEntry(DEFAULT_WORK_DIR_CANONICAL + SLASH + DEFAULT_FILENAME + i + ".txt", PAYLOAD));
    }
    FakeFtpServer server = helper.createAndStart(filesystem);
    StandaloneConsumer sc = null;
    try {
      FtpConsumer ftpConsumer = createForTests(listener, "testConsume_WithProcDirectory");
      ftpConsumer.setProcDirectory(DEFAULT_PROC_DIR_CANONICAL);
      FtpConnection consumeConnection = create(server);
      sc = new StandaloneConsumer(consumeConnection, ftpConsumer);
      start(sc);
      waitForMessages(listener, count);
      Thread.sleep(500);
      helper.assertMessages(listener.getMessages(), count);
      // assertEquals(count, filesystem.listFiles(DEFAULT_PROC_DIR_CANONICAL).size());

    }
    catch (Exception e) {
      throw e;
    }
    finally {
      stop(sc);
      server.stop();
    }
  }

  @Test
  public void testConsume_WithProcDirectory_FileAlreadyExists() throws Exception {
    int count = 1;
    EmbeddedFtpServer helper = new EmbeddedFtpServer();
    MockMessageListener listener = new MockMessageListener(100);
    FileSystem filesystem = helper.createFilesystem_DirsOnly();
    for (int i = 0; i < count; i++) {
      filesystem.add(new FileEntry(DEFAULT_WORK_DIR_CANONICAL + SLASH + DEFAULT_FILENAME + i + ".txt", PAYLOAD));
      filesystem.add(new FileEntry(DEFAULT_PROC_DIR_CANONICAL + SLASH + DEFAULT_FILENAME + i + ".txt", PAYLOAD));
    }
    FakeFtpServer server = helper.createAndStart(filesystem);
    StandaloneConsumer sc = null;
    try {
      FtpConsumer ftpConsumer = createForTests(listener, "testConsume_WithProcDirectory_FileAlreadyExists");
      ftpConsumer.setProcDirectory(DEFAULT_PROC_DIR_CANONICAL);
      FtpConnection consumeConnection = create(server);
      sc = new StandaloneConsumer(consumeConnection, ftpConsumer);
      start(sc);
      waitForMessages(listener, count);
      Thread.sleep(500);
      // Because the files already exist in the PROC dir, we expect file1.txt and file1.txt.timestamp;
      // assertEquals(count * 2, filesystem.listFiles(DEFAULT_PROC_DIR_CANONICAL).size());
    }
    catch (Exception e) {
      throw e;
    }
    finally {
      stop(sc);
      server.stop();
    }
  }

  @Test
  public void testConsumeWithEncoder() throws Exception {
    int count = 1;
    EmbeddedFtpServer helper = new EmbeddedFtpServer();
    MockMessageListener listener = new MockMessageListener(100);
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(PAYLOAD);
    byte[] bytes = new MimeEncoder().encode(msg);
    FileSystem filesystem = helper.createFilesystem_DirsOnly();
    for (int i = 0; i < count; i++) {
      FileEntry entry = new FileEntry(DEFAULT_WORK_DIR_CANONICAL + SLASH + DEFAULT_FILENAME + i + ".txt");
      entry.setContents(bytes);
      filesystem.add(entry);
    }
    FakeFtpServer server = helper.createAndStart(filesystem);
    StandaloneConsumer sc = null;
    try {
      FtpConsumer ftpConsumer = createForTests(listener, "testConsumeWithEncoder");
      ftpConsumer.setEncoder(new MimeEncoder());
      FtpConnection consumeConnection = create(server);
      sc = new StandaloneConsumer(consumeConnection, ftpConsumer);
      start(sc);
      waitForMessages(listener, count);
      helper.assertMessages(listener.getMessages(), count);
    }
    catch (Exception e) {
      throw e;
    }
    finally {
      stop(sc);
      server.stop();
    }
  }

  @Test
  public void testConsume_IgnoresWipFiles() throws Exception {

    int count = 1;
    EmbeddedFtpServer helper = new EmbeddedFtpServer();
    MockMessageListener listener = new MockMessageListener(100);
    FtpConsumer ftpConsumer = createForTests(listener, "testConsume_IgnoresWipFiles");
    FileSystem filesystem = helper.createFilesystem_DirsOnly();
    for (int i = 0; i < count; i++) {
      filesystem.add(new FileEntry(DEFAULT_WORK_DIR_CANONICAL + SLASH + DEFAULT_FILENAME + i + ".txt", PAYLOAD));
    }
    // Now create some files that have a _wip extension.
    filesystem.add(new FileEntry(DEFAULT_WORK_DIR_CANONICAL + SLASH + "shouldBeIgnored.txt" + ftpConsumer.wipSuffix(), PAYLOAD));
    FakeFtpServer server = helper.createAndStart(filesystem);
    StandaloneConsumer sc = null;
    try {
      FtpConnection consumeConnection = create(server);
      sc = new StandaloneConsumer(consumeConnection, ftpConsumer);
      start(sc);
      waitForMessages(listener, count);
      helper.assertMessages(listener.getMessages(), count);
      Thread.sleep(2000); // allow the consumer to consume the single message, should be 1 file left - the .wip file.
      assertTrue(filesystem.listFiles(DEFAULT_WORK_DIR_CANONICAL).size() > 0);
    }
    catch (Exception e) {
      throw e;
    }
    finally {
      stop(sc);
      server.stop();
    }
  }

  private FtpConnection create(FakeFtpServer server) {
    FtpConnection consumeConnection = new FtpConnection();
    consumeConnection.setDefaultControlPort(server.getServerControlPort());
    consumeConnection.setDefaultPassword(DEFAULT_PASSWORD);
    consumeConnection.setDefaultUserName(DEFAULT_USERNAME);
    consumeConnection.setCacheConnection(true);
    consumeConnection.setAdditionalDebug(true);
    KeyValuePairSet settings = new KeyValuePairSet();
    settings.add(new KeyValuePair(ClientSettings.FTP.RemoteVerificationEnabled.name(), "false"));
    consumeConnection.setAdditionalSettings(settings);
    return consumeConnection;
  }

  private FtpConsumer createForTests(MockMessageListener listener) {
    return createForTests(listener, SERVER_ADDRESS);
  }

  private FtpConsumer createForTests(MockMessageListener listener, Poller p) {
    return createForTests(listener, SERVER_ADDRESS, p);
  }

  @SuppressWarnings("deprecation")
  private FtpConsumer createForTests(MockMessageListener listener, String url, Poller poller) {
    FtpConsumer ftpConsumer = new FtpConsumer();
    if (url.equals(SERVER_ADDRESS)) {
      ftpConsumer.setWorkDirectory(DEFAULT_WORK_DIR_CANONICAL);
    }
    else {
      ftpConsumer.setWorkDirectory(SLASH + DEFAULT_WORK_DIR_NAME);
    }
    ftpConsumer.setFtpEndpoint(url);
    ftpConsumer.setPoller(poller);
    ftpConsumer.registerAdaptrisMessageListener(listener);
    return ftpConsumer;
  }

  private FtpConsumer createForTests(MockMessageListener listener, String url) {
    return createForTests(listener, url, new FixedIntervalPoller(new TimeInterval(300L, TimeUnit.MILLISECONDS)));
  }


  @Test
  public void testBasicConsume_WithFileBackedMessage() throws Exception {
    int count = 1;
    EmbeddedFtpServer helper = new EmbeddedFtpServer();
    MockMessageListener listener = new MockMessageListener();
    FakeFtpServer server = helper.createAndStart(helper.createFilesystem(count));
    StandaloneConsumer sc = null;
    try {
      FtpConsumer ftpConsumer = createForTests(listener, "testBasicConsume");
      ftpConsumer.setMessageFactory(new FileBackedMessageFactory());
      FtpConnection consumeConnection = create(server);
      sc = new StandaloneConsumer(consumeConnection, ftpConsumer);
      start(sc);
      waitForMessages(listener, count);
      helper.assertMessages(listener.getMessages(), count);
    } catch (Exception e) {
      throw e;
    } finally {
      stop(sc);
      server.stop();
    }

  }

}
