package com.adaptris.core.ftp;

import com.adaptris.core.BaseCase;
import com.adaptris.filetransfer.FileTransferClient;

/**
 * Abstract base class for testing Ftp connections only.
 *
 * @author lchan
 *
 */
public abstract class FtpConnectionCase extends BaseCase {

  public FtpConnectionCase(String name) {
    super(name);
  }

  protected static boolean areTestsEnabled() {
    return Boolean.parseBoolean(PROPERTIES.getProperty("ftp.tests.enabled", "false"));
  }

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

  public void testConnect() throws Exception {
    if (areTestsEnabled()) {
      FileTransferConnection connection = createConnection();
      try {
        start(connection);
        FileTransferClient client = connection.connect(getDestinationString());
      }
      finally {
        stop(connection);
      }
    }
  }

  public void testCachedConnection() throws Exception {
    if (!areTestsEnabled()) {
      return;
    }
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
    }
    finally {
      stop(connection);
    }
  }

  public void testConnection_Cached_Disconnect() throws Exception {
    if (!areTestsEnabled()) {
      return;
    }
    FileTransferConnection connection = createConnection();
    connection.setCacheConnection(true);
    try {
      start(connection);
      FileTransferClient client = null;
      client = connection.connect(getDestinationString());
      connection.disconnect(client);
      assertTrue(client.isConnected());
    }
    finally {
      stop(connection);
    }
  }

  public void testConnection_NoCache_Disconnect() throws Exception {
    if (!areTestsEnabled()) {
      return;
    }
    FileTransferConnection connection = createConnection();
    connection.setCacheConnection(false);
    try {
      start(connection);
      FileTransferClient client = null;
      client = connection.connect(getDestinationString());
      connection.disconnect(client);
      assertFalse(client.isConnected());
    }
    finally {
      stop(connection);
    }
  }

  public void testCachedConnection_ExceedsMaxSize() throws Exception {
    if (!areTestsEnabled()) {
      return;
    }
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
    }
    finally {
      stop(connection);
    }
  }

  public void testCachedConnection_DisconnectedClient() throws Exception {
    if (!areTestsEnabled()) {
      return;
    }
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
    }
    finally {
      stop(connection);
    }
  }

  public void testConnect_NoUser() throws Exception {
    if (areTestsEnabled()) {
      FileTransferConnection connection = createConnection();
      connection.setDefaultUserName(null);
      try {
        start(connection);
        FileTransferClient client = connection.connect(getDestinationString());
        fail();
      }
      catch (Exception expected) {

      }
      finally {
        stop(connection);
      }
    }
  }

  public void testConnect_UserOverride() throws Exception {
    if (areTestsEnabled()) {
      FileTransferConnection connection = createConnection();
      connection.setDefaultUserName(null);
      try {
        start(connection);
        log.debug("testConnect_UserOverride connecting to : " + getDestinationStringWithOverride());
        FileTransferClient client = connection.connect(getDestinationStringWithOverride());
      }
      finally {
        stop(connection);
      }
    }
  }

  protected abstract FileTransferConnection createConnection() throws Exception;

  protected abstract String getDestinationString() throws Exception;

  protected abstract String getDestinationStringWithOverride() throws Exception;
}
