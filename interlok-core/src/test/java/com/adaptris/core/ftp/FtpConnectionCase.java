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
import static org.junit.Assert.fail;
import java.util.concurrent.TimeUnit;
import org.junit.Assume;
import org.junit.Test;
import com.adaptris.filetransfer.FileTransferClient;
import com.adaptris.util.TimeInterval;

/**
 * Abstract base class for testing Ftp connections only.
 *
 * @author lchan
 *
 */
public abstract class FtpConnectionCase extends com.adaptris.interlok.junit.scaffolding.BaseCase {

  public FtpConnectionCase() {}

  protected boolean areTestsEnabled() {
    return Boolean.parseBoolean(PROPERTIES.getProperty("ftp.tests.enabled", "false"));
  }

  @Test
  public void testDefaultControlPort() throws Exception {
    FileTransferConnection connection = createConnection();
    assertNull(connection.getDefaultControlPort());
    assertDefaultControlPort(connection.defaultControlPort());

    connection.setDefaultControlPort(1234);
    assertEquals(1234, connection.defaultControlPort());
    connection.setDefaultControlPort(null);
    assertDefaultControlPort(connection.defaultControlPort());
  }

  protected abstract void assertDefaultControlPort(int defaultControlPort);

  @Test
  public void testSetWindowsWorkAround() throws Exception {
    FileTransferConnection connection = createConnection();
    assertNull(connection.getWindowsWorkAround());
    assertFalse(connection.windowsWorkaround());
    connection.setWindowsWorkAround(Boolean.TRUE);
    assertEquals(Boolean.TRUE, connection.getWindowsWorkAround());
    assertTrue(connection.windowsWorkaround());
    connection.setWindowsWorkAround(null);
    assertNull(connection.getWindowsWorkAround());
    assertFalse(connection.windowsWorkaround());
  }

  @Test
  public void testSetForceRelativePath() throws Exception {
    FileTransferConnection connection = createConnection();
    assertNull(connection.getForceRelativePath());
    assertFalse(connection.forceRelativePath());
    connection.setForceRelativePath(Boolean.TRUE);
    assertEquals(Boolean.TRUE, connection.getForceRelativePath());
    assertTrue(connection.forceRelativePath());
    connection.setForceRelativePath(null);
    assertNull(connection.getForceRelativePath());
    assertFalse(connection.forceRelativePath());
  }

  @Test
  public void testSetAdditionalDebug() throws Exception {
    FileTransferConnection connection = createConnection();
    connection.setAdditionalDebug(null);
    assertNull(connection.getAdditionalDebug());
    assertFalse(connection.additionalDebug());
    connection.setAdditionalDebug(Boolean.TRUE);
    assertEquals(Boolean.TRUE, connection.getAdditionalDebug());
    assertTrue(connection.additionalDebug());
    connection.setAdditionalDebug(null);
    assertNull(connection.getAdditionalDebug());
    assertFalse(connection.additionalDebug());
  }

  @Test
  public void testSetCacheConnection() throws Exception {
    FileTransferConnection connection = createConnection();
    assertNull(connection.getCacheConnection());
    assertFalse(connection.cacheConnection());
    connection.setCacheConnection(Boolean.TRUE);
    assertEquals(Boolean.TRUE, connection.getCacheConnection());
    assertTrue(connection.cacheConnection());
    connection.setCacheConnection(null);
    assertNull(connection.getCacheConnection());
    assertFalse(connection.cacheConnection());
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
  public void testSetCacheExpiration() throws Exception {
    FileTransferConnection connection = createConnection();
    assertNull(connection.getCacheExpiration());
    assertEquals(FileTransferConnection.DEFAULT_EXPIRATION.toMilliseconds(), connection.expirationMillis());

    TimeInterval t = new TimeInterval(10L, TimeUnit.SECONDS);
    connection.setCacheExpiration(t);
    assertNotNull(connection.getCacheExpiration());
    assertEquals(t, connection.getCacheExpiration());
    assertEquals(t.toMilliseconds(), connection.expirationMillis());
    assertNull(connection.withCacheExpiration(null).getCacheExpiration());
    assertEquals(FileTransferConnection.DEFAULT_EXPIRATION.toMilliseconds(), connection.expirationMillis());
  }

  @Test
  public void testConnect() throws Exception {
    Assume.assumeTrue(areTestsEnabled());
    FileTransferConnection connection = createConnection();
    try {
      start(connection);
      FileTransferClient client = connection.connect(getDestinationString());
    } finally {
      stop(connection);
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
  public void testConnection_Cached_Disconnect() throws Exception {
    Assume.assumeTrue(areTestsEnabled());

    FileTransferConnection connection = createConnection();
    connection.setCacheConnection(true);
    try {
      start(connection);
      FileTransferClient client = null;
      client = connection.connect(getDestinationString());
      connection.disconnect(client);
      assertTrue(client.isConnected());
    } finally {
      stop(connection);
    }
  }

  @Test
  public void testConnection_NoCache_Disconnect() throws Exception {
    Assume.assumeTrue(areTestsEnabled());

    FileTransferConnection connection = createConnection();
    connection.setCacheConnection(false);
    try {
      start(connection);
      FileTransferClient client = null;
      client = connection.connect(getDestinationString());
      connection.disconnect(client);
      assertFalse(client.isConnected());
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

  @Test
  public void testConnect_NoUser() throws Exception {
    Assume.assumeTrue(areTestsEnabled());
    FileTransferConnection connection = createConnection();
    connection.setDefaultUserName(null);
    try {
      start(connection);
      FileTransferClient client = connection.connect(getDestinationString());
      fail();
    } catch (Exception expected) {

    } finally {
      stop(connection);
    }

  }

  @Test
  public void testConnect_UserOverride() throws Exception {
    Assume.assumeTrue(areTestsEnabled());
    FileTransferConnection connection = createConnection();
    connection.setDefaultUserName(null);
    try {
      start(connection);
      log.debug("testConnect_UserOverride connecting to : " + getDestinationStringWithOverride());
      FileTransferClient client = connection.connect(getDestinationStringWithOverride());
    } finally {
      stop(connection);
    }

  }

  protected abstract FileTransferConnection createConnection() throws Exception;

  protected abstract String getDestinationString() throws Exception;

  protected abstract String getDestinationStringWithOverride() throws Exception;
}
