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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.apache.oro.io.GlobFilenameFilter;
import org.awaitility.Awaitility;
import org.junit.After;
import org.junit.Assume;
import org.junit.Test;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.core.ConfiguredConsumeDestination;
import com.adaptris.core.ConfiguredProduceDestination;
import com.adaptris.core.CoreConstants;
import com.adaptris.core.CoreException;
import com.adaptris.core.MetadataFileNameCreator;
import com.adaptris.core.MimeEncoder;
import com.adaptris.core.QuartzCronPoller;
import com.adaptris.core.StandaloneConsumer;
import com.adaptris.core.StandaloneProducer;
import com.adaptris.core.stubs.MockMessageListener;
import com.adaptris.filetransfer.FileTransferClient;
import com.adaptris.util.GuidGenerator;
import com.adaptris.util.TimeInterval;

@SuppressWarnings("deprecation")
public abstract class FtpCase extends FtpConsumerExample {

  public static final String DEFAULT_WORK_DIR = "/work";
  public static final String DEFAULT_BUILD_DIR = "/build";
  protected static final String PAYLOAD = "Quick zephyrs blow, vexing daft Jim";


  @After
  public void tearDown() throws Exception {
    if (areTestsEnabled()) {

      FileTransferClient client = connectRawClient();
      cleanup(client, getRemoteDirectory() + DEFAULT_WORK_DIR);
      cleanup(client, getRemoteDirectory() + DEFAULT_BUILD_DIR);
      client.disconnect();
    }
  }

  @Test
  public void testSetCacheMaxSize() throws Exception {
    FileTransferConnection connection = createConnection();
    assertNull(connection.getMaxClientCacheSize());
    assertEquals(FileTransferConnection.DEFAULT_MAX_CACHE_SIZE, connection.maxClientCacheSize());

    connection.setMaxClientCache(99);
    assertNotNull(connection.getMaxClientCacheSize());
    assertEquals(Integer.valueOf(99), connection.getMaxClientCacheSize());
    assertEquals(99, connection.maxClientCacheSize());
    connection.setMaxClientCache(null);

    assertNull(connection.getMaxClientCacheSize());
    assertEquals(FileTransferConnection.DEFAULT_MAX_CACHE_SIZE, connection.maxClientCacheSize());
  }

  @Test
  public void testBasicConsume() throws Exception {
    Assume.assumeTrue(areTestsEnabled());
    MockMessageListener listener = new MockMessageListener();
    FtpConsumer ftpConsumer = new FtpConsumer();
    ConfiguredConsumeDestination ccd = new ConfiguredConsumeDestination(getDestinationString());
    ccd.setConfiguredThreadName("testBasicConsume");
    ftpConsumer.setDestination(ccd);
    ftpConsumer.setWorkDirectory(DEFAULT_WORK_DIR);
    ftpConsumer.registerAdaptrisMessageListener(listener);
    ftpConsumer.setPoller(new QuartzCronPoller("*/1 * * * * ?"));
    StandaloneConsumer sc = new StandaloneConsumer(createConnection(), ftpConsumer);
    start(sc);

    int count = 1;
    try {
      produce(new StandaloneProducer(createConnection(), createFtpProducer()), count);
      waitForMessages(listener, count);
      assertMessages(listener.getMessages(), count);
    } finally {
      stop(sc);
    }
  }

  @Test
  public void testConsume_CachedConnection() throws Exception {
    Assume.assumeTrue(areTestsEnabled());
    MockMessageListener listener = new MockMessageListener();
    FtpConsumer ftpConsumer = new FtpConsumer();
    ConfiguredConsumeDestination ccd = new ConfiguredConsumeDestination(getDestinationString());
    ccd.setConfiguredThreadName("testCasualBasicConsume");
    ftpConsumer.setDestination(ccd);
    ftpConsumer.registerAdaptrisMessageListener(listener);
    ftpConsumer.setQuietInterval(new TimeInterval(300l, TimeUnit.MILLISECONDS));
    ftpConsumer.setPoller(new QuartzCronPoller("*/1 * * * * ?"));
    FileTransferConnection consumeConnection = createConnection();
    consumeConnection.setCacheConnection(true);
    StandaloneConsumer sc = new StandaloneConsumer(consumeConnection, ftpConsumer);
    start(sc);
    int count = 10;
    try {
      FileTransferConnection produceConnection = createConnection();
      produceConnection.setCacheConnection(true);
      produce(new StandaloneProducer(produceConnection, createFtpProducer()), count);
      waitForMessages(listener, count);
      assertMessages(listener.getMessages(), count);
    } catch (Exception e) {
      log.error(e.getMessage(), e);
      throw e;
    } finally {
      stop(sc);

    }

  }

  @Test
  public void testConsume_WithEncoder() throws Exception {
    Assume.assumeTrue(areTestsEnabled());
    MockMessageListener listener = new MockMessageListener();
    FtpConsumer ftpConsumer = new FtpConsumer();
    ConfiguredConsumeDestination ccd = new ConfiguredConsumeDestination(getDestinationString());
    ccd.setConfiguredThreadName("testCasualBasicConsume");
    ftpConsumer.setDestination(ccd);
    ftpConsumer.registerAdaptrisMessageListener(listener);
    ftpConsumer.setQuietInterval(new TimeInterval(300l, TimeUnit.MILLISECONDS));
    ftpConsumer.setPoller(new QuartzCronPoller("*/1 * * * * ?"));
    ftpConsumer.setEncoder(new MimeEncoder());
    FileTransferConnection consumeConnection = createConnection();
    consumeConnection.setCacheConnection(true);
    StandaloneConsumer sc = new StandaloneConsumer(consumeConnection, ftpConsumer);
    start(sc);
    int count = 10;
    try {
      FileTransferConnection produceConnection = createConnection();
      produceConnection.setCacheConnection(true);
      FtpProducer producer = createFtpProducer();
      producer.setEncoder(new MimeEncoder());
      produce(new StandaloneProducer(produceConnection, producer), count);
      waitForMessages(listener, count);
      assertMessages(listener.getMessages(), count);
    } catch (Exception e) {
      log.error(e.getMessage(), e);
      throw e;
    } finally {
      stop(sc);
    }

  }

  @Test
  public void testBasicConsumeWithOverride() throws Exception {
    Assume.assumeTrue(areTestsEnabled());

    MockMessageListener listener = new MockMessageListener();
    FtpConsumer ftpConsumer = new FtpConsumer();
    ConfiguredConsumeDestination ccd = new ConfiguredConsumeDestination(getDestinationStringWithOverride());
    ccd.setConfiguredThreadName("testBasicConsumeWithOverride");
    ftpConsumer.setDestination(ccd);
    ftpConsumer.setWorkDirectory(DEFAULT_WORK_DIR);
    ftpConsumer.registerAdaptrisMessageListener(listener);
    // Pass in a QuartzId so we don't print out the uname+password.
    ftpConsumer.setPoller(new QuartzCronPoller("*/1 * * * * ?", "testBasicConsumeWithOverride"));
    FileTransferConnection con = createConnection();
    con.setDefaultUserName(null);
    StandaloneConsumer sc = new StandaloneConsumer(con, ftpConsumer);
    start(sc);
    int count = 1;
    try {
      produce(new StandaloneProducer(createConnection(), createFtpProducer()), count);
      Awaitility.await().atMost(Duration.ofSeconds(5)).with().pollInterval(Duration.ofMillis(100))
          .until(() -> listener.getMessages().size() >= count);
      assertMessages(listener.getMessages(), count);
    } finally {
      stop(sc);
    }

  }

  @Test
  public void testConsumeWithFilter() throws Exception {
    Assume.assumeTrue(areTestsEnabled());
    MockMessageListener listener = new MockMessageListener();
    FtpConsumer ftpConsumer = new FtpConsumer();
    ConfiguredConsumeDestination ccd = new ConfiguredConsumeDestination(getDestinationString(), "*.txt");
    ccd.setConfiguredThreadName("testConsumeWithFilter");
    ftpConsumer.setDestination(ccd);

    ftpConsumer.setWorkDirectory(DEFAULT_WORK_DIR);
    ftpConsumer.setFileFilterImp(GlobFilenameFilter.class.getCanonicalName());
    ftpConsumer.registerAdaptrisMessageListener(listener);
    ftpConsumer.setPoller(new QuartzCronPoller("*/1 * * * * ?"));
    StandaloneConsumer sc = new StandaloneConsumer(createConnection(), ftpConsumer);
    start(sc);

    int count = 1;
    try {
      FtpProducer ftpProducer = createFtpProducer();
      MetadataFileNameCreator mfc = new MetadataFileNameCreator();
      mfc.setDefaultName(new GuidGenerator().getUUID() + ".txt");
      mfc.setMetadataKey(new GuidGenerator().getUUID());
      ftpProducer.setFilenameCreator(mfc);
      produce(new StandaloneProducer(createConnection(), ftpProducer), count);
      Awaitility.await().atMost(Duration.ofSeconds(5)).with().pollInterval(Duration.ofMillis(100))
          .until(() -> listener.getMessages().size() >= count);
      assertMessages(listener.getMessages(), count);
    } finally {
      stop(sc);
    }

  }

  @Test
  public void testConsumeWithQuietPeriod() throws Exception {
    Assume.assumeTrue(areTestsEnabled());
    MockMessageListener listener = new MockMessageListener();
    FtpConsumer ftpConsumer = new FtpConsumer();
    ConfiguredConsumeDestination ccd = new ConfiguredConsumeDestination(getDestinationString());
    ccd.setConfiguredThreadName("testConsumeWithQuietPeriod");
    ftpConsumer.setDestination(ccd);
    ftpConsumer.setWorkDirectory(DEFAULT_WORK_DIR);
    ftpConsumer.setQuietInterval(new TimeInterval(3L, TimeUnit.SECONDS));
    ftpConsumer.registerAdaptrisMessageListener(listener);
    ftpConsumer.setPoller(new QuartzCronPoller("*/1 * * * * ?"));
    StandaloneConsumer sc = new StandaloneConsumer(createConnection(), ftpConsumer);
    start(sc);

    int count = 1;
    try {
      produce(new StandaloneProducer(createConnection(), createFtpProducer()), count);
      Awaitility.await().atMost(Duration.ofSeconds(5)).with().pollInterval(Duration.ofMillis(100))
          .until(() -> listener.getMessages().size() >= count);
      assertMessages(listener.getMessages(), count);
    } finally {
      stop(sc);
    }

  }

  @Test
  public void testConsumeWithNonMatchingFilter() throws Exception {
    Assume.assumeTrue(areTestsEnabled());
    MockMessageListener listener = new MockMessageListener();
    FtpConsumer ftpConsumer = new FtpConsumer();
    ConfiguredConsumeDestination ccd = new ConfiguredConsumeDestination(getDestinationString(), "*.xml");
    ccd.setConfiguredThreadName("testConsumeWithNonMatchingFilter");
    ftpConsumer.setDestination(ccd);
    ftpConsumer.setWorkDirectory(DEFAULT_WORK_DIR);
    ftpConsumer.setFileFilterImp(GlobFilenameFilter.class.getCanonicalName());
    ftpConsumer.registerAdaptrisMessageListener(listener);
    ftpConsumer.setPoller(new QuartzCronPoller("*/1 * * * * ?"));
    StandaloneConsumer sc = new StandaloneConsumer(createConnection(), ftpConsumer);

    start(sc);

    int count = 1;
    try {
      produce(new StandaloneProducer(createConnection(), createFtpProducer()), count);
      try {
        Thread.sleep(1500);
      } catch (Exception e) {
        ;
      }
      assertMessages(listener.getMessages(), 0);
    } finally {
      stop(sc);
    }

  }

  @Test
  public void testCachedConnection() throws Exception {
    Assume.assumeTrue(areTestsEnabled());
    FileTransferConnection connection = createConnection();
    connection.setCacheConnection(true);
    try {
      start(connection);
      FileTransferClient client = null;
      client = connection.connect(getDestinationString());
      // Should be cached, and equivalent.

      FileTransferClient cached = connection.connect(getDestinationString());
      assertEquals(client, cached);
      FileTransferClient client2 = connection.connect(getDestinationStringWithOverride());
      // Even though it's effectively the same "host", they become different keys.
      assertNotSame(client, client2);

      cached = connection.connect(getDestinationStringWithOverride());
      assertEquals(client2, cached);
    } finally {
      stop(connection);
    }
  }

  @Test
  public void testCachedConnection_ExceedsMaxSize() throws Exception {
    Assume.assumeTrue(areTestsEnabled());

    FileTransferConnection connection = createConnection();
    connection.setCacheConnection(true);
    connection.setMaxClientCache(1);
    try {
      start(connection);
      FileTransferClient client = connection.connect(getDestinationString());
      // Should be cached, and equivalent.

      FileTransferClient cached = connection.connect(getDestinationString());
      assertEquals(client, cached);

      FileTransferClient client2 = connection.connect(getDestinationStringWithOverride());
      // This should have emptied the cache (and disconnected client1)...
      cached = connection.connect(getDestinationString());
      assertNotSame(client, cached);
      assertFalse(client.isConnected());
    } finally {
      stop(connection);
    }
  }

  @Test
  public void testCachedConnection_DisconnectedClient() throws Exception {
    Assume.assumeTrue(areTestsEnabled());

    FileTransferConnection connection = createConnection();
    connection.setCacheConnection(true);
    try {
      start(connection);
      FileTransferClient client = connection.connect(getDestinationString());
      // Should be cached, and equivalent.

      FileTransferClient cached = connection.connect(getDestinationString());
      assertEquals(client, cached);
      client.disconnect();

      // Now it should be a new entry.
      cached = connection.connect(getDestinationString());
      assertNotSame(client, cached);
    } finally {
      stop(connection);
    }
  }

  protected static void produce(StandaloneProducer p, int count) throws CoreException {
    start(p);
    for (int i = 0; i < count; i++) {
      AdaptrisMessage m = AdaptrisMessageFactory.getDefaultInstance().newMessage(PAYLOAD);
      p.produce(m);
    }
    stop(p);
  }

  protected abstract FileTransferConnection createConnection() throws Exception;

  protected FtpProducer createFtpProducer() throws Exception {
    FtpProducer p = new FtpProducer();
    ConfiguredProduceDestination d = new ConfiguredProduceDestination(getDestinationString());
    p.setDestination(d);
    p.setBuildDirectory(DEFAULT_BUILD_DIR);
    p.setDestDirectory(DEFAULT_WORK_DIR);
    return p;
  }

  protected abstract String getDestinationString() throws Exception;

  protected abstract String getDestinationStringWithOverride() throws Exception;

  protected abstract FileTransferClient connectRawClient() throws Exception;

  protected abstract String getRemoteDirectory();

  protected void assertMessages(List<AdaptrisMessage> list, int count) {
    assertEquals("All files consumed/produced", count, list.size());
    for (AdaptrisMessage m : list) {
      assertTrue(m.containsKey(CoreConstants.ORIGINAL_NAME_KEY));
      assertEquals(PAYLOAD, m.getContent().trim());
    }
  }

  private void cleanup(FileTransferClient client, String dir) throws Exception {
    client.chdir(dir);
    String[] files = client.dir();
    for (String file : files) {
      client.delete(file);
    }
  }

  protected static boolean areTestsEnabled() {
    return Boolean.parseBoolean(PROPERTIES.getProperty("ftp.tests.enabled", "false"));
  }

}
