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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.oro.io.GlobFilenameFilter;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.core.CoreConstants;
import com.adaptris.core.CoreException;
import com.adaptris.core.MetadataFileNameCreator;
import com.adaptris.core.MimeEncoder;
import com.adaptris.core.QuartzCronPoller;
import com.adaptris.core.StandaloneConsumer;
import com.adaptris.core.StandaloneProducer;
import com.adaptris.core.stubs.MockMessageListener;
import com.adaptris.filetransfer.FileTransferClient;
import com.adaptris.security.password.Password;
import com.adaptris.util.GuidGenerator;
import com.adaptris.util.SafeGuidGenerator;
import com.adaptris.util.TimeInterval;

public abstract class RelaxedFtpCase extends FtpConsumerExample {
  private static final TimeInterval DEFAULT_QUIET_PERIOD = new TimeInterval(1L, TimeUnit.SECONDS);
  protected static final String COLON = ":";
  protected static final String HYPHEN = "-";

  protected static final String PAYLOAD = "Quick zephyrs blow, vexing daft Jim";

  @BeforeEach
  public void setUp() throws Exception {
    if (areTestsEnabled()) {
      FileTransferClient client = connectRawClient();
      try {
        client.mkdir(getRemoteDirectory());
      }
      catch (Exception e) {

      }
      finally {
        client.disconnect();
      }
    }
  }

  @AfterEach
  public void tearDown() throws Exception {
    if (areTestsEnabled()) {
      FileTransferClient client = connectRawClient();
      cleanup(client, getRemoteDirectory());
      client.rmdir(getRemoteDirectory());
      client.disconnect();
    }
  }

  @Test
  public void testBasicConsume() throws Exception {
    if (areTestsEnabled()) {
      MockMessageListener listener = new MockMessageListener();
      RelaxedFtpConsumer ftpConsumer = new RelaxedFtpConsumer();
      ftpConsumer.setFtpEndpoint(getDestinationString());
      ftpConsumer.registerAdaptrisMessageListener(listener);
      ftpConsumer.setQuietInterval(DEFAULT_QUIET_PERIOD);
      ftpConsumer.setPoller(new QuartzCronPoller("*/1 * * * * ?"));
      StandaloneConsumer sc = new StandaloneConsumer(createConnection(), ftpConsumer);
      start(sc);
      int count = 1;
      try {
        produce(new StandaloneProducer(createConnection(), createFtpProducer()), count);
        waitForMessages(listener, count);
        assertMessages(listener.getMessages(), count);
      }
      catch (Exception e) {
        log.error(e.getMessage(), e);
        throw e;
      }
      finally {
        stop(sc);

      }
    }
  }

  @Test
  public void testConsume_CachedConnection() throws Exception {
    if (areTestsEnabled()) {
      MockMessageListener listener = new MockMessageListener();
      RelaxedFtpConsumer ftpConsumer = new RelaxedFtpConsumer();
      ftpConsumer.setFtpEndpoint(getDestinationString());
      ftpConsumer.registerAdaptrisMessageListener(listener);
      ftpConsumer.setQuietInterval(DEFAULT_QUIET_PERIOD);
      ftpConsumer.setPoller(new QuartzCronPoller("*/1 * * * * ?"));
      FileTransferConnectionUsingPassword consumeConnection = createConnection();
      consumeConnection.setCacheConnection(true);
      StandaloneConsumer sc = new StandaloneConsumer(consumeConnection, ftpConsumer);
      start(sc);
      int count = 1;
      try {
        FileTransferConnectionUsingPassword produceConnection = createConnection();
        produceConnection.setCacheConnection(true);
        produce(new StandaloneProducer(produceConnection, createFtpProducer()), count);
        waitForMessages(listener, count);
        assertMessages(listener.getMessages(), count);
      }
      catch (Exception e) {
        log.error(e.getMessage(), e);
        throw e;
      }
      finally {
        stop(sc);

      }
    }
  }

  @Test
  public void testConsume_WithEncoder() throws Exception {
    if (areTestsEnabled()) {
      MockMessageListener listener = new MockMessageListener();
      RelaxedFtpConsumer ftpConsumer = new RelaxedFtpConsumer();
      ftpConsumer.setFtpEndpoint(getDestinationString());
      ftpConsumer.registerAdaptrisMessageListener(listener);
      ftpConsumer.setQuietInterval(DEFAULT_QUIET_PERIOD);
      ftpConsumer.setPoller(new QuartzCronPoller("*/1 * * * * ?"));
      ftpConsumer.setEncoder(new MimeEncoder());
      FileTransferConnection consumeConnection = createConnection();
      consumeConnection.setCacheConnection(true);
      StandaloneConsumer sc = new StandaloneConsumer(consumeConnection, ftpConsumer);
      start(sc);
      int count = 1;
      try {
        FileTransferConnection produceConnection = createConnection();
        produceConnection.setCacheConnection(true);
        RelaxedFtpProducer producer = createFtpProducer();
        producer.setEncoder(new MimeEncoder());
        produce(new StandaloneProducer(produceConnection, producer), count);
        waitForMessages(listener, count);
        assertMessages(listener.getMessages(), count);
      }
      catch (Exception e) {
        log.error(e.getMessage(), e);
        throw e;
      }
      finally {
        stop(sc);
      }
    }
  }

  @Test
  public void testBasicConsume_WithTrailingSlash() throws Exception {
    if (areTestsEnabled()) {
      MockMessageListener listener = new MockMessageListener();
      RelaxedFtpConsumer ftpConsumer = new RelaxedFtpConsumer();
      String destination = getDestinationString() + "/";
      ftpConsumer.setFtpEndpoint(getDestinationString());
      ftpConsumer.registerAdaptrisMessageListener(listener);
      ftpConsumer.setQuietInterval(DEFAULT_QUIET_PERIOD);
      ftpConsumer.setPoller(new QuartzCronPoller("*/1 * * * * ?"));
      StandaloneConsumer sc = new StandaloneConsumer(createConnection(), ftpConsumer);
      start(sc);
      int count = 1;
      try {
        RelaxedFtpProducer producer = createFtpProducer();
        producer.setFtpEndpoint(destination);
        produce(new StandaloneProducer(createConnection(), producer), count);
        waitForMessages(listener, count);
        assertMessages(listener.getMessages(), count);
      }
      catch (Exception e) {
        log.error(e.getMessage(), e);
        throw e;
      }
      finally {
        stop(sc);

      }
    }
  }

  @Test
  public void testConsume_EncryptedPassword() throws Exception {
    if (areTestsEnabled()) {
      MockMessageListener listener = new MockMessageListener();
      RelaxedFtpConsumer ftpConsumer = new RelaxedFtpConsumer();
      ftpConsumer.setFtpEndpoint(getDestinationString());
      ftpConsumer.setQuietInterval(DEFAULT_QUIET_PERIOD);
      ftpConsumer.registerAdaptrisMessageListener(listener);
      ftpConsumer.setPoller(new QuartzCronPoller("*/1 * * * * ?"));
      StandaloneConsumer sc = new StandaloneConsumer(createConnection(), ftpConsumer);
      start(sc);
      int count = 1;
      try {
        FileTransferConnectionUsingPassword conn = createConnection();
        String pw = conn.getDefaultPassword();
        String decodedPw = Password.decode(pw);
        if (pw.equals(decodedPw)) {
          // The password was never encrypted!
          conn.setDefaultPassword(Password.encode(conn.getDefaultPassword(), Password.PORTABLE_PASSWORD));
        }
        produce(new StandaloneProducer(conn, createFtpProducer()), count);
        waitForMessages(listener, count);

        assertMessages(listener.getMessages(), count);
      }
      finally {
        stop(sc);

      }
    }
  }

  @Test
  public void testConsume_OverrideUrl() throws Exception {
    if (areTestsEnabled()) {

      MockMessageListener listener = new MockMessageListener();
      RelaxedFtpConsumer ftpConsumer = new RelaxedFtpConsumer();
      ftpConsumer.setFtpEndpoint(getDestinationStringWithOverride());
      ftpConsumer.setQuietInterval(DEFAULT_QUIET_PERIOD);
      ftpConsumer.registerAdaptrisMessageListener(listener);
      // Pass in a QuartzId so we don't print out the uname+password.
      ftpConsumer.setPoller(new QuartzCronPoller("*/1 * * * * ?", "testBasicConsumeWithOverride"));
      FileTransferConnectionUsingPassword con = createConnection();
      con.setDefaultPassword(null);
      con.setDefaultUserName(null);
      StandaloneConsumer sc = new StandaloneConsumer(con, ftpConsumer);
      start(sc);

      int count = 1;
      try {
        produce(new StandaloneProducer(createConnection(), createFtpProducer()), count);
        waitForMessages(listener, count);

        assertMessages(listener.getMessages(), count);
      }
      finally {
        stop(sc);

      }
    }
  }

  @Test
  public void testConsumeWithFilter() throws Exception {
    if (areTestsEnabled()) {
      MockMessageListener listener = new MockMessageListener();
      RelaxedFtpConsumer ftpConsumer = new RelaxedFtpConsumer();
      ftpConsumer.setFtpEndpoint(getDestinationString());
      ftpConsumer.setQuietInterval(DEFAULT_QUIET_PERIOD);

      ftpConsumer.setFileFilterImp(GlobFilenameFilter.class.getCanonicalName());
      ftpConsumer.registerAdaptrisMessageListener(listener);
      ftpConsumer.setFilterExpression(".txt");
      ftpConsumer.setPoller(new QuartzCronPoller("*/1 * * * * ?"));
      StandaloneConsumer sc = new StandaloneConsumer(createConnection(), ftpConsumer);
      start(sc);

      int count = 1;
      try {
        RelaxedFtpProducer ftpProducer = createFtpProducer();
        MetadataFileNameCreator mfc = new MetadataFileNameCreator();
        mfc.setDefaultName(new GuidGenerator().getUUID() + ".txt");
        mfc.setMetadataKey(new GuidGenerator().getUUID());
        ftpProducer.setFilenameCreator(mfc);
        produce(new StandaloneProducer(createConnection(), ftpProducer), count);
        waitForMessages(listener, count);

        assertMessages(listener.getMessages(), count);
      }
      finally {
        stop(sc);
      }
    }
  }

  @Test
  public void testConsumeWithQuietPeriod() throws Exception {
    if (areTestsEnabled()) {
      MockMessageListener listener = new MockMessageListener();
      RelaxedFtpConsumer ftpConsumer = new RelaxedFtpConsumer();
      ftpConsumer.setFtpEndpoint(getDestinationString());
      ftpConsumer.setQuietInterval(DEFAULT_QUIET_PERIOD);
      ftpConsumer.registerAdaptrisMessageListener(listener);
      ftpConsumer.setPoller(new QuartzCronPoller("*/1 * * * * ?"));
      StandaloneConsumer sc = new StandaloneConsumer(createConnection(), ftpConsumer);
      start(sc);

      int count = 1;
      try {

        produce(new StandaloneProducer(createConnection(), createFtpProducer()), count);
        waitForMessages(listener, count);

        assertMessages(listener.getMessages(), count);
      }
      finally {
        stop(sc);
      }
    }
  }

  @Test
  public void testConsumeWithNonMatchingFilter() throws Exception {
    if (areTestsEnabled()) {
      MockMessageListener listener = new MockMessageListener();
      RelaxedFtpConsumer ftpConsumer = new RelaxedFtpConsumer();
      ftpConsumer.setFtpEndpoint(getDestinationString());
      ftpConsumer.setQuietInterval(DEFAULT_QUIET_PERIOD);
      ftpConsumer.setFileFilterImp(GlobFilenameFilter.class.getCanonicalName());
      ftpConsumer.registerAdaptrisMessageListener(listener);
      ftpConsumer.setFilterExpression(".xml");
      ftpConsumer.setPoller(new QuartzCronPoller("*/1 * * * * ?"));
      StandaloneConsumer sc = new StandaloneConsumer(createConnection(), ftpConsumer);

      start(sc);

      int count = 1;
      try {
        produce(new StandaloneProducer(createConnection(), createFtpProducer()), count);
        try {
          Thread.sleep(1500);
        }
        catch (Exception e) {
          ;
        }
        assertMessages(listener.getMessages(), 0);
      }
      finally {
        stop(sc);
      }
    }
  }

  protected void produce(StandaloneProducer p, int count) throws CoreException {
    start(p);
    for (int i = 0; i < count; i++) {
      AdaptrisMessage m = AdaptrisMessageFactory.getDefaultInstance().newMessage(PAYLOAD);
      p.produce(m);
    }
    stop(p);
  }

  protected abstract FileTransferConnectionUsingPassword createConnection() throws Exception;

  protected RelaxedFtpProducer createFtpProducer() throws Exception {
    RelaxedFtpProducer p = new RelaxedFtpProducer();
    p.setFtpEndpoint(getDestinationString());
    return p;
  }

  protected abstract String getDestinationString() throws Exception;

  protected abstract String getDestinationStringWithOverride() throws Exception;

  protected abstract FileTransferClient connectRawClient() throws Exception;

  protected abstract String getRemoteDirectory();

  protected void assertMessages(List<AdaptrisMessage> list, int count) {
    assertEquals(count, list.size());
    for (AdaptrisMessage m : list) {
      assertTrue(m.headersContainsKey(CoreConstants.ORIGINAL_NAME_KEY));
      assertEquals(PAYLOAD, m.getContent().trim());
    }
  }

  private void cleanup(FileTransferClient client, String dir) throws Exception {
    client.chdir(dir);
    String[] files = client.dir();
    for (String file : files) {
      client.delete(file);
    }
    client.chdir("..");
  }

  protected boolean areTestsEnabled() {
    return Boolean.parseBoolean(PROPERTIES.getProperty("ftp.tests.enabled", "false"));
  }

  protected static String safeName() {
    return new SafeGuidGenerator().create(new Object());
  }
}
