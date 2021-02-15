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

import static com.adaptris.core.ftp.EmbeddedFtpServer.DEFAULT_BUILD_DIR_CANONICAL;
import static com.adaptris.core.ftp.EmbeddedFtpServer.DEFAULT_BUILD_DIR_NAME;
import static com.adaptris.core.ftp.EmbeddedFtpServer.DEFAULT_PASSWORD;
import static com.adaptris.core.ftp.EmbeddedFtpServer.DEFAULT_PROC_DIR_CANONICAL;
import static com.adaptris.core.ftp.EmbeddedFtpServer.DEFAULT_PROC_DIR_NAME;
import static com.adaptris.core.ftp.EmbeddedFtpServer.DEFAULT_REPLY_DIR_CANONICAL;
import static com.adaptris.core.ftp.EmbeddedFtpServer.DEFAULT_USERNAME;
import static com.adaptris.core.ftp.EmbeddedFtpServer.DEFAULT_WORK_DIR_CANONICAL;
import static com.adaptris.core.ftp.EmbeddedFtpServer.DEFAULT_WORK_DIR_NAME;
import static com.adaptris.core.ftp.EmbeddedFtpServer.PAYLOAD;
import static com.adaptris.core.ftp.EmbeddedFtpServer.PAYLOAD_ALTERNATE;
import static com.adaptris.core.ftp.EmbeddedFtpServer.SERVER_ADDRESS;
import static com.adaptris.core.ftp.EmbeddedFtpServer.SLASH;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import java.util.concurrent.TimeUnit;
import org.junit.Test;
import org.mockftpserver.fake.FakeFtpServer;
import org.mockftpserver.fake.filesystem.FileEntry;
import org.mockftpserver.fake.filesystem.FileSystem;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.core.ConfiguredProduceDestination;
import com.adaptris.core.CoreConstants;
import com.adaptris.core.CoreException;
import com.adaptris.core.FormattedFilenameCreator;
import com.adaptris.core.MetadataFileNameCreator;
import com.adaptris.core.MimeEncoder;
import com.adaptris.core.ServiceCase;
import com.adaptris.core.ServiceException;
import com.adaptris.core.StandaloneProducer;
import com.adaptris.core.StandaloneRequestor;
import com.adaptris.core.util.LifecycleHelper;
import com.adaptris.util.TimeInterval;

@SuppressWarnings("deprecation")
public class FtpProducerTest extends FtpProducerCase {

  private static final TimeInterval DEFAULT_TIMEOUT = new TimeInterval(100L, TimeUnit.MILLISECONDS);
  private static final String BASE_DIR_KEY = "FtpProducerExamples.baseDir";

  public FtpProducerTest() {
    if (PROPERTIES.getProperty(BASE_DIR_KEY) != null) {
      setBaseDir(PROPERTIES.getProperty(BASE_DIR_KEY));
    }
  }


  @Override
  protected FtpConnection createConnectionForExamples() {
    return FtpExampleHelper.ftpConnection();

  }

  @Override
  protected String getScheme() {
    return "ftp";
  }

  @Test
  public void testInit_NoBuildDir() throws Exception {
    FtpProducer ftpProducer = new FtpProducer();
    ftpProducer.setBuildDirectory(null);
    try {
      ftpProducer.init();
      fail();
    }
    catch (CoreException expected) {
    }
  }

  @Test
  public void testInit_NoDestDir() throws Exception {
    FtpProducer ftpProducer = new FtpProducer();
    ftpProducer.setDestDirectory(null);
    try {
      ftpProducer.init();
      fail();
    }
    catch (CoreException expected) {
    }
  }

  @Test
  public void testInit() throws Exception {
    FtpProducer ftpProducer = new FtpProducer();
    ftpProducer.setBuildDirectory("buildDir");
    ftpProducer.setDestDirectory("destDir");
    ftpProducer.init();
    ftpProducer.close();
    ftpProducer.setReplyDirectory("/replyDir");
    ftpProducer.init();
    ftpProducer.close();
    ftpProducer.setReplyDirectory("replyDir");
    ftpProducer.init();
    ftpProducer.close();
    ftpProducer.setReplyProcDirectory("replyProcDir");
    ftpProducer.init();
    ftpProducer.close();
    ftpProducer.setReplyProcDirectory("/replyProcDir");
    ftpProducer.init();
    ftpProducer.close();
  }

  @Test
  public void testSetFilenameCreator() throws Exception {
    FtpProducer ftpProducer = new FtpProducer();
    assertEquals(FormattedFilenameCreator.class, ftpProducer.filenameCreatorToUse().getClass());
    ftpProducer.setFilenameCreator(new MetadataFileNameCreator());
    assertEquals(MetadataFileNameCreator.class, ftpProducer.getFilenameCreator().getClass());
    assertEquals(MetadataFileNameCreator.class, ftpProducer.filenameCreatorToUse().getClass());
  }

  @Test
  public void testProduce() throws Exception {
    EmbeddedFtpServer helper = new EmbeddedFtpServer();
    FileSystem filesystem = helper.createFilesystem_DirsOnly();
    FakeFtpServer server = helper.createAndStart(filesystem);
    try {
      FtpProducer ftpProducer = createForTests();
      FtpConnection produceConnection = create(server);
      StandaloneProducer sp = new StandaloneProducer(produceConnection, ftpProducer);
      // INTERLOK-3329 For coverage so the prepare() warning is executed 2x
      LifecycleHelper.prepare(sp);
      AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(PAYLOAD);
      ServiceCase.execute(sp, msg);
      assertEquals(1, filesystem.listFiles(DEFAULT_WORK_DIR_CANONICAL).size());
    }
    finally {
      server.stop();
    }

  }

  @Test
  public void testProduce_WithEncoder() throws Exception {
    EmbeddedFtpServer helper = new EmbeddedFtpServer();
    FileSystem filesystem = helper.createFilesystem_DirsOnly();
    FakeFtpServer server = helper.createAndStart(filesystem);
    try {
      FtpProducer ftpProducer = createForTests();
      FtpConnection produceConnection = create(server);
      ftpProducer.setEncoder(new MimeEncoder());
      StandaloneProducer sp = new StandaloneProducer(produceConnection, ftpProducer);
      AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(PAYLOAD);
      ServiceCase.execute(sp, msg);
      assertEquals(1, filesystem.listFiles(DEFAULT_WORK_DIR_CANONICAL).size());
    }
    finally {
      server.stop();
    }

  }

  @Test
  public void testProduce_NoDebug() throws Exception {
    EmbeddedFtpServer helper = new EmbeddedFtpServer();
    FileSystem filesystem = helper.createFilesystem_DirsOnly();
    FakeFtpServer server = helper.createAndStart(filesystem);
    try {
      FtpProducer ftpProducer = createForTests();
      FtpConnection produceConnection = create(server);
      produceConnection.setAdditionalDebug(false);
      StandaloneProducer sp = new StandaloneProducer(produceConnection, ftpProducer);
      AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(PAYLOAD);
      ServiceCase.execute(sp, msg);
      assertEquals(1, filesystem.listFiles(DEFAULT_WORK_DIR_CANONICAL).size());
    }
    finally {
      server.stop();
    }

  }

  @Test
  public void testProduce_NoBuildDirectory() throws Exception {
    EmbeddedFtpServer helper = new EmbeddedFtpServer();
    FileSystem filesystem = helper.createFilesystem_DirsOnly(DEFAULT_WORK_DIR_NAME, DEFAULT_PROC_DIR_NAME);
    FakeFtpServer server = helper.createAndStart(filesystem);
    try {
      FtpProducer ftpProducer = createForTests();
      FtpConnection produceConnection = create(server);
      produceConnection.setAdditionalDebug(false);
      StandaloneProducer sp = new StandaloneProducer(produceConnection, ftpProducer);
      AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(PAYLOAD);
      try {
        ServiceCase.execute(sp, msg);
        fail();
      }
      catch (ServiceException expected) {

      }
    }
    finally {
      server.stop();
    }
  }

  @Test
  public void testProduce_NoTargetDirectory() throws Exception {
    EmbeddedFtpServer helper = new EmbeddedFtpServer();
    FileSystem filesystem = helper.createFilesystem_DirsOnly(DEFAULT_BUILD_DIR_NAME);
    FakeFtpServer server = helper.createAndStart(filesystem);
    try {
      FtpProducer ftpProducer = createForTests();
      FtpConnection produceConnection = create(server);
      produceConnection.setAdditionalDebug(false);
      StandaloneProducer sp = new StandaloneProducer(produceConnection, ftpProducer);
      AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(PAYLOAD);
      try {
        ServiceCase.execute(sp, msg);
        fail();
      }
      catch (ServiceException expected) {

      }
    }
    finally {
      server.stop();
    }
  }

  @Test
  public void testBasicRequestReply() throws Exception {
    EmbeddedFtpServer helper = new EmbeddedFtpServer();
    FileSystem filesystem = helper.createFilesystem_DirsOnly();
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(PAYLOAD);
    filesystem.add(new FileEntry(DEFAULT_REPLY_DIR_CANONICAL + SLASH + msg.getUniqueId(), PAYLOAD_ALTERNATE));
    FakeFtpServer server = helper.createAndStart(filesystem);
    StandaloneRequestor requestor = null;
    try {
      FtpProducer ftpProducer = createForTests();
      ftpProducer.setReplyDirectory(DEFAULT_REPLY_DIR_CANONICAL);
      FtpConnection produceConnection = create(server);
      requestor = new StandaloneRequestor(produceConnection, ftpProducer);
      requestor.setReplyTimeout(DEFAULT_TIMEOUT);

      start(requestor);
      requestor.doService(msg);
      assertEquals(PAYLOAD_ALTERNATE, msg.getContent());
    }
    finally {
      stop(requestor);
      server.stop();
    }
  }

  @Test
  public void testBasicRequestReply_NoDebug() throws Exception {
    EmbeddedFtpServer helper = new EmbeddedFtpServer();
    FileSystem filesystem = helper.createFilesystem_DirsOnly();
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(PAYLOAD);
    filesystem.add(new FileEntry(DEFAULT_REPLY_DIR_CANONICAL + SLASH + msg.getUniqueId(), PAYLOAD_ALTERNATE));
    FakeFtpServer server = helper.createAndStart(filesystem);
    StandaloneRequestor requestor = null;
    try {
      FtpProducer ftpProducer = createForTests();
      ftpProducer.setReplyDirectory(DEFAULT_REPLY_DIR_CANONICAL);
      FtpConnection produceConnection = create(server);
      produceConnection.setAdditionalDebug(false);
      requestor = new StandaloneRequestor(produceConnection, ftpProducer);
      requestor.setReplyTimeout(DEFAULT_TIMEOUT);

      start(requestor);
      requestor.doService(msg);
      assertEquals(PAYLOAD_ALTERNATE, msg.getContent());
    }
    finally {
      stop(requestor);
      server.stop();
    }
  }

  @Test
  public void testRequestReply_NoReplyDirectory() throws Exception {
    EmbeddedFtpServer helper = new EmbeddedFtpServer();
    FileSystem filesystem = helper.createFilesystem_DirsOnly();
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(PAYLOAD);
    filesystem.add(new FileEntry(DEFAULT_REPLY_DIR_CANONICAL + SLASH + msg.getUniqueId(), PAYLOAD_ALTERNATE));
    FakeFtpServer server = helper.createAndStart(filesystem);
    StandaloneRequestor requestor = null;
    try {
      FtpProducer ftpProducer = createForTests();
      FtpConnection produceConnection = create(server);
      requestor = new StandaloneRequestor(produceConnection, ftpProducer);
      requestor.setReplyTimeout(DEFAULT_TIMEOUT);
      start(requestor);

      requestor.doService(msg);
      fail();
    }
    catch (ServiceException expected) {

    }
    finally {
      stop(requestor);
      server.stop();
    }
  }

  @Test
  public void testRequestReply_ReplyEncoderEnabled() throws Exception {
    EmbeddedFtpServer helper = new EmbeddedFtpServer();
    FileSystem filesystem = helper.createFilesystem_DirsOnly();
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(PAYLOAD);
    byte[] encodedReply = new MimeEncoder().encode(AdaptrisMessageFactory.getDefaultInstance().newMessage(PAYLOAD_ALTERNATE));
    FileEntry ftpReplyFile = new FileEntry(DEFAULT_REPLY_DIR_CANONICAL + SLASH + msg.getUniqueId());
    ftpReplyFile.setContents(encodedReply);
    filesystem.add(ftpReplyFile);
    FakeFtpServer server = helper.createAndStart(filesystem);
    StandaloneRequestor requestor = null;
    try {
      FtpProducer ftpProducer = createForTests();
      ftpProducer.setEncoder(new MimeEncoder());
      ftpProducer.setReplyDirectory(DEFAULT_REPLY_DIR_CANONICAL);
      ftpProducer.setReplyUsesEncoder(true);
      FtpConnection produceConnection = create(server);
      requestor = new StandaloneRequestor(produceConnection, ftpProducer);
      requestor.setReplyTimeout(DEFAULT_TIMEOUT);
      start(requestor);

      requestor.doService(msg);
      assertEquals(PAYLOAD_ALTERNATE, msg.getContent());
    }
    finally {
      stop(requestor);
      server.stop();
    }
  }

  @Test
  public void testRequestReply_ReplyEncoderDefault() throws Exception {
    EmbeddedFtpServer helper = new EmbeddedFtpServer();
    FileSystem filesystem = helper.createFilesystem_DirsOnly();
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(PAYLOAD);
    byte[] encodedReply = new MimeEncoder().encode(AdaptrisMessageFactory.getDefaultInstance().newMessage(PAYLOAD_ALTERNATE));
    FileEntry ftpReplyFile = new FileEntry(DEFAULT_REPLY_DIR_CANONICAL + SLASH + msg.getUniqueId());
    ftpReplyFile.setContents(encodedReply);
    filesystem.add(ftpReplyFile);
    FakeFtpServer server = helper.createAndStart(filesystem);
    StandaloneRequestor requestor = null;
    try {
      FtpProducer ftpProducer = createForTests();
      ftpProducer.setEncoder(new MimeEncoder());
      ftpProducer.setReplyDirectory(DEFAULT_REPLY_DIR_CANONICAL);
      FtpConnection produceConnection = create(server);
      requestor = new StandaloneRequestor(produceConnection, ftpProducer);
      requestor.setReplyTimeout(DEFAULT_TIMEOUT);
      start(requestor);

      requestor.doService(msg);
      assertEquals(PAYLOAD_ALTERNATE, msg.getContent());
    }
    finally {
      stop(requestor);
      server.stop();
    }
  }

  @Test
  public void testRequestReply_ReplyEncoderDisabled() throws Exception {

    EmbeddedFtpServer helper = new EmbeddedFtpServer();
    FileSystem filesystem = helper.createFilesystem_DirsOnly();
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(PAYLOAD);
    filesystem.add(new FileEntry(DEFAULT_REPLY_DIR_CANONICAL + SLASH + msg.getUniqueId(), PAYLOAD_ALTERNATE));
    FakeFtpServer server = helper.createAndStart(filesystem);
    StandaloneRequestor requestor = null;
    try {
      FtpProducer ftpProducer = createForTests();
      ftpProducer.setReplyDirectory(DEFAULT_REPLY_DIR_CANONICAL);
      ftpProducer.setEncoder(new MimeEncoder());
      ftpProducer.setReplyUsesEncoder(false);
      FtpConnection produceConnection = create(server);
      requestor = new StandaloneRequestor(produceConnection, ftpProducer);
      requestor.setReplyTimeout(DEFAULT_TIMEOUT);

      start(requestor);
      requestor.doService(msg);
      assertEquals(PAYLOAD_ALTERNATE, msg.getContent());
    }
    finally {
      stop(requestor);
      server.stop();
    }
  }

  @Test
  public void testRequestReply_ReplyToNameSet() throws Exception {

    EmbeddedFtpServer helper = new EmbeddedFtpServer();
    FileSystem filesystem = helper.createFilesystem_DirsOnly();
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(PAYLOAD);
    String replyFilename = "testRequestReply_ReplyToNameSet";
    msg.addMetadata(CoreConstants.FTP_REPLYTO_NAME, replyFilename);
    filesystem.add(new FileEntry(DEFAULT_REPLY_DIR_CANONICAL + SLASH + replyFilename, PAYLOAD_ALTERNATE));
    FakeFtpServer server = helper.createAndStart(filesystem);
    StandaloneRequestor requestor = null;
    try {
      FtpProducer ftpProducer = createForTests();
      ftpProducer.setReplyDirectory(DEFAULT_REPLY_DIR_CANONICAL);
      FtpConnection produceConnection = create(server);
      requestor = new StandaloneRequestor(produceConnection, ftpProducer);
      requestor.setReplyTimeout(DEFAULT_TIMEOUT);

      start(requestor);
      requestor.doService(msg);
      assertEquals(PAYLOAD_ALTERNATE, msg.getContent());
    }
    finally {
      stop(requestor);
      server.stop();
    }
  }

  @Test
  public void testRequestReply_ReplyProcDirectory() throws Exception {

    EmbeddedFtpServer helper = new EmbeddedFtpServer();
    FileSystem filesystem = helper.createFilesystem_DirsOnly();
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(PAYLOAD);
    filesystem.add(new FileEntry(DEFAULT_REPLY_DIR_CANONICAL + SLASH + msg.getUniqueId(), PAYLOAD_ALTERNATE));
    FakeFtpServer server = helper.createAndStart(filesystem);
    StandaloneRequestor requestor = null;
    try {
      FtpProducer ftpProducer = createForTests();
      ftpProducer.setReplyDirectory(DEFAULT_REPLY_DIR_CANONICAL);
      ftpProducer.setReplyProcDirectory(DEFAULT_PROC_DIR_CANONICAL);
      FtpConnection produceConnection = create(server);
      requestor = new StandaloneRequestor(produceConnection, ftpProducer);
      requestor.setReplyTimeout(DEFAULT_TIMEOUT);

      start(requestor);
      requestor.doService(msg);
      assertEquals(PAYLOAD_ALTERNATE, msg.getContent());
      assertEquals(1, filesystem.listFiles(DEFAULT_PROC_DIR_CANONICAL).size());

    }
    finally {
      stop(requestor);
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
    return consumeConnection;
  }

  private FtpProducer createForTests() {
    return createForTests(new ConfiguredProduceDestination(SERVER_ADDRESS));
  }

  @SuppressWarnings("deprecation")
  private FtpProducer createForTests(ConfiguredProduceDestination dest) {
    FtpProducer ftpProducer = new FtpProducer();
    if (dest.getDestination().equals(SERVER_ADDRESS)) {
      ftpProducer.setBuildDirectory(DEFAULT_BUILD_DIR_CANONICAL);
      ftpProducer.setDestDirectory(DEFAULT_WORK_DIR_CANONICAL);
    }
    else {
      ftpProducer.setBuildDirectory(SLASH + DEFAULT_BUILD_DIR_NAME);
      ftpProducer.setDestDirectory(SLASH + DEFAULT_WORK_DIR_NAME);
    }
    ftpProducer.setFtpEndpoint(dest.getDestination());
    return ftpProducer;
  }
}
