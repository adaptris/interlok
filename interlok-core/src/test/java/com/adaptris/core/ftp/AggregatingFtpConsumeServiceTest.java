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

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.core.CoreException;
import com.adaptris.core.ServiceException;
import com.adaptris.core.services.aggregator.AggregatingServiceExample;
import com.adaptris.core.services.aggregator.IgnoreOriginalMimeAggregator;
import com.adaptris.core.services.aggregator.MessageAggregator;
import com.adaptris.core.services.aggregator.ReplaceWithFirstMessage;
import com.adaptris.core.stubs.DefectiveMessageFactory;
import com.adaptris.core.stubs.MockMessageListener;
import com.adaptris.core.util.LifecycleHelper;
import com.adaptris.core.util.MimeHelper;
import com.adaptris.util.text.mime.BodyPartIterator;
import org.junit.Test;
import org.mockftpserver.fake.FakeFtpServer;
import org.mockftpserver.fake.filesystem.FileSystem;

import static com.adaptris.core.ftp.EmbeddedFtpServer.DEFAULT_FILENAME;
import static com.adaptris.core.ftp.EmbeddedFtpServer.DEFAULT_PASSWORD;
import static com.adaptris.core.ftp.EmbeddedFtpServer.DEFAULT_USERNAME;
import static com.adaptris.core.ftp.EmbeddedFtpServer.DEFAULT_WORK_DIR_CANONICAL;
import static com.adaptris.core.ftp.EmbeddedFtpServer.PAYLOAD;
import static com.adaptris.core.ftp.EmbeddedFtpServer.SLASH;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class AggregatingFtpConsumeServiceTest extends AggregatingServiceExample {


  @Test
  public void testInit() throws Exception {
    AggregatingFtpConsumeService service = new AggregatingFtpConsumeService();
    try {
      LifecycleHelper.prepare(service);
      LifecycleHelper.init(service);
      fail();
    }
    catch (CoreException expected) {

    }
    service = new AggregatingFtpConsumeService();
    service.setConnection(new FtpConnection());
    try {
      LifecycleHelper.prepare(service);
      LifecycleHelper.init(service);
      fail();
    }
    catch (CoreException expected) {

    }
    service = new AggregatingFtpConsumeService();
    service.setConsumer(new AggregatingFtpConsumer());
    try {
      LifecycleHelper.prepare(service);
      LifecycleHelper.init(service);
      fail();
    }
    catch (CoreException expected) {

    }
    service = new AggregatingFtpConsumeService(new FtpConnection(),
        createConsumer("ftp://localhost/work", null, new ReplaceWithFirstMessage()));
    LifecycleHelper.prepare(service);
    LifecycleHelper.init(service);
    LifecycleHelper.close(service);

  }

  @Test
  public void testService_SingleFile() throws Exception {
    int count = 1;

    EmbeddedFtpServer helper = new EmbeddedFtpServer();
    MockMessageListener listener = new MockMessageListener();
    FakeFtpServer server = helper.createAndStart(helper.createFilesystem(count));
    try {
      // should be ftp://localhost/home/user/work/file0 which is created when you
      // create the filesystem.
      String ftpConsumeUrl = "ftp://localhost" + DEFAULT_WORK_DIR_CANONICAL + SLASH + DEFAULT_FILENAME + 0;
      FtpConnection conn = createConnection(server);
      AggregatingFtpConsumer consumer = createConsumer(ftpConsumeUrl, null, new ReplaceWithFirstMessage());
      AggregatingFtpConsumeService service = new AggregatingFtpConsumeService(conn, consumer);
      AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage();
      execute(service, msg);
      assertEquals(PAYLOAD, msg.getContent());
    }
    finally {
      server.stop();
    }
  }

  @Test
  public void testService_SingleFile_Failure() throws Exception {
    int count = 1;

    EmbeddedFtpServer helper = new EmbeddedFtpServer();
    MockMessageListener listener = new MockMessageListener();
    FakeFtpServer server = helper.createAndStart(helper.createFilesystem(count));
    try {
      // should be ftp://localhost/home/user/work/file0 which is created when you
      // create the filesystem.
      String ftpConsumeUrl = "ftp://localhost" + DEFAULT_WORK_DIR_CANONICAL + SLASH + DEFAULT_FILENAME + 0;
      FtpConnection conn = createConnection(server);
      AggregatingFtpConsumer consumer = createConsumer(ftpConsumeUrl, null, new ReplaceWithFirstMessage());
      AggregatingFtpConsumeService service = new AggregatingFtpConsumeService(conn, consumer);
      AdaptrisMessage msg = new DefectiveMessageFactory().newMessage();
      try {
        execute(service, msg);
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
  public void testService_Single_NoDelete() throws Exception {
    int count = 1;

    EmbeddedFtpServer helper = new EmbeddedFtpServer();
    MockMessageListener listener = new MockMessageListener();
    FileSystem filesystem = helper.createFilesystem(count);
    FakeFtpServer server = helper.createAndStart(filesystem);
    try {
      // should be ftp://localhost/home/user/work/file0 which is created when you
      // create the filesystem.
      String ftpConsumeUrl = "ftp://localhost" + DEFAULT_WORK_DIR_CANONICAL + SLASH + DEFAULT_FILENAME + 0;
      FtpConnection conn = createConnection(server);
      conn.setAdditionalDebug(false);
      AggregatingFtpConsumer consumer = createConsumer(ftpConsumeUrl, null, new ReplaceWithFirstMessage());
      consumer.setDeleteAggregatedFiles(Boolean.FALSE);
      AggregatingFtpConsumeService service = new AggregatingFtpConsumeService(conn, consumer);
      AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage();
      execute(service, msg);
      assertEquals(PAYLOAD, msg.getContent());
      // didn't get dleted so should still exist.
      assertEquals(count, filesystem.listFiles(DEFAULT_WORK_DIR_CANONICAL).size());

    }
    finally {
      server.stop();
    }
  }

  @Test
  public void testService_MultipleFiles() throws Exception {
    int count = 5;

    EmbeddedFtpServer helper = new EmbeddedFtpServer();
    MockMessageListener listener = new MockMessageListener();
    FakeFtpServer server = helper.createAndStart(helper.createFilesystem(count));
    try {
      // should be ftp://localhost/home/user/work/ which is created when you
      // create the filesystem.
      String ftpConsumeUrl = "ftp://localhost" + DEFAULT_WORK_DIR_CANONICAL;
      FtpConnection conn = createConnection(server);
      AggregatingFtpConsumer consumer = createConsumer(ftpConsumeUrl, ".*", new IgnoreOriginalMimeAggregator());
      AggregatingFtpConsumeService service = new AggregatingFtpConsumeService(conn, consumer);
      AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage();
      execute(service, msg);
      BodyPartIterator input = MimeHelper.createBodyPartIterator(msg);
      assertEquals(count, input.size());

    }
    finally {
      server.stop();
    }
  }

  @Test
  public void testService_MultipleFiles_NoDelete() throws Exception {
    int count = 5;

    EmbeddedFtpServer helper = new EmbeddedFtpServer();
    MockMessageListener listener = new MockMessageListener();
    FileSystem filesystem = helper.createFilesystem(count);
    FakeFtpServer server = helper.createAndStart(filesystem);
    try {
      // should be ftp://localhost/home/user/work
      String ftpConsumeUrl = "ftp://localhost" + DEFAULT_WORK_DIR_CANONICAL;
      FtpConnection conn = createConnection(server);
      conn.setAdditionalDebug(false);
      AggregatingFtpConsumer consumer = createConsumer(ftpConsumeUrl, ".*", new IgnoreOriginalMimeAggregator());
      consumer.setDeleteAggregatedFiles(Boolean.FALSE);
      AggregatingFtpConsumeService service = new AggregatingFtpConsumeService(conn, consumer);
      AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage();
      execute(service, msg);
      BodyPartIterator input = MimeHelper.createBodyPartIterator(msg);
      assertEquals(count, input.size());
      // didn't get dleted so should still exist.
      assertEquals(count, filesystem.listFiles(DEFAULT_WORK_DIR_CANONICAL).size());

    }
    finally {
      server.stop();
    }
  }

  @Test
  public void testService_MultipleFiles_Failure() throws Exception {
    int count = 5;

    EmbeddedFtpServer helper = new EmbeddedFtpServer();
    MockMessageListener listener = new MockMessageListener();
    FakeFtpServer server = helper.createAndStart(helper.createFilesystem(count));
    try {
      // should be ftp://localhost/home/user/work/ which is created when you
      // create the filesystem.
      String ftpConsumeUrl = "ftp://localhost" + DEFAULT_WORK_DIR_CANONICAL;
      FtpConnection conn = createConnection(server);
      AggregatingFtpConsumer consumer = createConsumer(ftpConsumeUrl, ".*", new IgnoreOriginalMimeAggregator());
      AggregatingFtpConsumeService service = new AggregatingFtpConsumeService(conn, consumer);
      AdaptrisMessage msg = new DefectiveMessageFactory().newMessage();
      try {
        execute(service, msg);
        fail();
      }
      catch (ServiceException expected) {

      }
    }
    finally {
      server.stop();
    }
  }

  private AggregatingFtpConsumer createConsumer(String endpoint, String filterExpression, MessageAggregator aggr) {
    AggregatingFtpConsumer consumer = new AggregatingFtpConsumer();
    consumer.setEndpoint(endpoint);
    consumer.setFilterExpression(filterExpression);
    consumer.setMessageAggregator(aggr);
    return consumer;
  }

  private FtpConnection createConnection(FakeFtpServer server) {
    FtpConnection consumeConnection = new FtpConnection();
    consumeConnection.setDefaultControlPort(server.getServerControlPort());
    consumeConnection.setDefaultPassword(DEFAULT_PASSWORD);
    consumeConnection.setDefaultUserName(DEFAULT_USERNAME);
    consumeConnection.setCacheConnection(true);
    consumeConnection.setAdditionalDebug(true);
    return consumeConnection;
  }

  @Override
  protected Object retrieveObjectForSampleConfig() {
    AggregatingFtpConsumer consumer = new AggregatingFtpConsumer();
    consumer.setEndpoint("ftp://localhost:22/path/to/default");
    consumer.setFilterExpression(".*\\*.xml");
    consumer.setMessageAggregator(new IgnoreOriginalMimeAggregator());
    FtpConnection conn = FtpExampleHelper.ftpConnection();
    return new AggregatingFtpConsumeService(conn, consumer);
  }

  @Override
  protected String getExampleCommentHeader(Object o) {
    return super.getExampleCommentHeader(o)
        + "\n<!-- \n In the example here, you aggregate the contents of the ftp-server specified by the metadata-key 'url'"
        + "\nmatching only files that correspond to the Perl pattern .*\\.xml. "
        + "\nIf aggrUrl does not exist as metadata, then we attempt to connect to ftp://myhost.com/path/to/default"
        + "\nto pick up any files that correspond to the pattern."
        + "\nThese are then aggregated into a single MIME Multipart message. The original message is ignored." + "\n-->\n";
  }
}
