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

import static com.adaptris.core.ftp.EmbeddedFtpServer.DEFAULT_PASSWORD;
import static com.adaptris.core.ftp.EmbeddedFtpServer.DEFAULT_USERNAME;
import static com.adaptris.core.ftp.EmbeddedFtpServer.DEFAULT_WORK_DIR_CANONICAL;
import static com.adaptris.core.ftp.EmbeddedFtpServer.DEFAULT_WORK_DIR_NAME;
import static com.adaptris.core.ftp.EmbeddedFtpServer.DESTINATION_URL;
import static com.adaptris.core.ftp.EmbeddedFtpServer.PAYLOAD;
import static com.adaptris.core.ftp.EmbeddedFtpServer.SLASH;

import org.mockftpserver.fake.FakeFtpServer;
import org.mockftpserver.fake.filesystem.FileSystem;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.core.ConfiguredProduceDestination;
import com.adaptris.core.FormattedFilenameCreator;
import com.adaptris.core.MetadataFileNameCreator;
import com.adaptris.core.MimeEncoder;
import com.adaptris.core.ServiceCase;
import com.adaptris.core.StandaloneProducer;


public class RelaxedFtpProducerTest extends RelaxedFtpProducerCase {

  private static final String BASE_DIR_KEY = "FtpProducerExamples.baseDir";

  public RelaxedFtpProducerTest(String name) {
    super(name);
    if (PROPERTIES.getProperty(BASE_DIR_KEY) != null) {
      setBaseDir(PROPERTIES.getProperty(BASE_DIR_KEY));
    }
  }

  @Override
  protected void setUp() throws Exception {
  }

  @Override
  protected FtpConnection createConnectionForExamples() {
    return FtpExampleHelper.ftpConnection();
  }

  @Override
  protected String getScheme() {
    return "ftp";
  }

  public void testSetFilenameCreator() throws Exception {
    RelaxedFtpProducer ftpProducer = new RelaxedFtpProducer();
    assertEquals(FormattedFilenameCreator.class, ftpProducer.filenameCreator().getClass());
    ftpProducer.setFilenameCreator(new MetadataFileNameCreator());
    assertEquals(MetadataFileNameCreator.class, ftpProducer.getFilenameCreator().getClass());
    assertEquals(MetadataFileNameCreator.class, ftpProducer.filenameCreator().getClass());
  }

  public void testProduce() throws Exception {
    EmbeddedFtpServer helper = new EmbeddedFtpServer();
    FileSystem filesystem = helper.createFilesystem_DirsOnly();
    FakeFtpServer server = helper.createAndStart(filesystem);
    try {
      RelaxedFtpProducer ftpProducer = createForTests(new ConfiguredProduceDestination(DESTINATION_URL + SLASH
          + DEFAULT_WORK_DIR_NAME));
      FtpConnection produceConnection = create(server);
      StandaloneProducer sp = new StandaloneProducer(produceConnection, ftpProducer);
      AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(PAYLOAD);
      ServiceCase.execute(sp, msg);
      assertEquals(1, filesystem.listFiles(DEFAULT_WORK_DIR_CANONICAL).size());
    }
    finally {
      server.stop();
    }
  }

  public void testProduce_NoDebug() throws Exception {
    EmbeddedFtpServer helper = new EmbeddedFtpServer();
    FileSystem filesystem = helper.createFilesystem_DirsOnly();
    FakeFtpServer server = helper.createAndStart(filesystem);
    try {
      RelaxedFtpProducer ftpProducer = createForTests(new ConfiguredProduceDestination(DESTINATION_URL + SLASH
          + DEFAULT_WORK_DIR_NAME));
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

  public void testProduce_WithEncoder() throws Exception {
    EmbeddedFtpServer helper = new EmbeddedFtpServer();
    FileSystem filesystem = helper.createFilesystem_DirsOnly();
    FakeFtpServer server = helper.createAndStart(filesystem);
    try {
      RelaxedFtpProducer ftpProducer = createForTests(new ConfiguredProduceDestination(DESTINATION_URL + SLASH
          + DEFAULT_WORK_DIR_NAME));
      ftpProducer.setEncoder(new MimeEncoder());
      FtpConnection produceConnection = create(server);
      StandaloneProducer sp = new StandaloneProducer(produceConnection, ftpProducer);
      AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(PAYLOAD);
      ServiceCase.execute(sp, msg);
      assertEquals(1, filesystem.listFiles(DEFAULT_WORK_DIR_CANONICAL).size());
    }
    finally {
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

  private RelaxedFtpProducer createForTests(ConfiguredProduceDestination dest) {
    RelaxedFtpProducer ftpProducer = new RelaxedFtpProducer();
    ftpProducer.setDestination(dest);
    return ftpProducer;
  }

}
