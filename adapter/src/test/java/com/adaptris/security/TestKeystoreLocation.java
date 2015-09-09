package com.adaptris.security;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.util.Properties;

import junit.framework.TestCase;

import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.adaptris.security.keystore.KeystoreFactory;
import com.adaptris.security.keystore.KeystoreLocation;

/**
 * Test Keystore Functionality.
 *
 * @author $Author: lchan $
 */
public class TestKeystoreLocation extends TestCase {
  private Properties cfg;
  private Config config;
  private static Log logR = null;

  /** @see TestCase */
  public TestKeystoreLocation(String testName) {
    super(testName);
    if (logR == null) {
      logR = LogFactory.getLog(TestKeystoreLocation.class);
    }
  }

  /**
   * @see TestCase#setUp()
   */
  @Override
  public void setUp() throws Exception {
    super.setUp();
    config = Config.getInstance();
    cfg = config.getProperties();

    if (cfg == null) {
      fail("No Configuration(!) available");
    }
    config
        .buildKeystore(cfg.getProperty(Config.KEYSTORE_TEST_URL), null, false);
  }

  /**
   * @see TestCase#tearDown()
   */
  @Override
  public void tearDown() throws Exception {
    super.tearDown();
  }

  public void testKeystoreEquality() {
    try {
      KeystoreLocation ksi = KeystoreFactory.getDefault().create(
          "JKS:///c:/fred.ks", "abcde".toCharArray());
      KeystoreLocation ksi2 = KeystoreFactory.getDefault().create(
          "file:///c:/fred.ks?keystoreType=JKS", "abcde".toCharArray());
      assertEquals("Keystore Equality", ksi, ksi2);
    }
    catch (Exception e) {
      logR.error("testKeystoreFactory failed", e);
      fail(e.getMessage());
    }
  }

  public void testKeystoreFactory() {
    try {
      KeystoreLocation ksi = KeystoreFactory.getDefault().create(
          "jks:///c:/fred.ks", "abcde".toCharArray());
      KeystoreLocation ksi2 = KeystoreFactory.getDefault().create(
          "file:///c:/fred.ks?keystoreType=JKS&keystorePassword=abcde");
      assertEquals("Keystore Equality", ksi, ksi2);
    }
    catch (Exception e) {
      logR.error("testKeystoreFactory failed", e);
      fail(e.getMessage());
    }
  }

  public void testLocalKeystore() {
    try {
      KeystoreLocation k = KeystoreFactory.getDefault().create(
          cfg.getProperty(Config.KEYSTORE_TEST_URL),
          cfg.getProperty(Config.KEYSTORE_COMMON_KEYSTORE_PW).toCharArray());
      assertTrue("Keystore Location", k.exists());
      InputStream in = k.openInput();
      in.close();
    }
    catch (Exception e) {
      logR.error("testLocalKeystore failed", e);
      fail(e.getMessage());
    }
  }

  public void testNonExistentLocalKeystore() {
    try {
      KeystoreLocation k = KeystoreFactory.getDefault().create(
          "file:///c:/fredblahblahblh?keystoreType=JKS",
          cfg.getProperty(Config.KEYSTORE_COMMON_KEYSTORE_PW).toCharArray());
      assertTrue("Keystore Location", !k.exists());
    }
    catch (Exception e) {
      logR.error("testLocalKeystore failed", e);
      fail(e.getMessage());
    }
  }

  public void testRemoteKeystore() throws Exception {
    if (Boolean.parseBoolean(cfg.getProperty(Config.REMOTE_TESTS_ENABLED,
        "false"))) {
      String ks = cfg.getProperty(Config.KEYSTORE_TEST_URL);
      ks = ks.replaceAll("\\\\", "/");
      URI uri = new URI(ks);
      File keystore = new File(uri.getPath());
      String filename = keystore.getName();
      File newFile = new File(cfg.getProperty(Config.KEYSTORE_REMOTE_REALPATH)
          + "/" + filename);
      FileUtils.copyFile(keystore, newFile);
      Thread.sleep(10000);
      logR.debug("newFile " + newFile.getCanonicalPath());
      String keystoreType = uri.getQuery();
      String url = cfg.getProperty(Config.KEYSTORE_REMOTE_ROOT) + filename
          + "?" + keystoreType;
      KeystoreLocation k = KeystoreFactory.getDefault().create(url,
          cfg.getProperty(Config.KEYSTORE_COMMON_KEYSTORE_PW).toCharArray());
      assertTrue("Remote Keystore exists", k.exists());
      InputStream in = k.openInput();
      in.close();
      assertTrue(!k.isWriteable());
      boolean openOutputSuccess = false;
      try {
        OutputStream o = k.openOutput();
        openOutputSuccess = true;
        o.close();
      }
      catch (Exception e) {
        // Expected as it's non-writeable
      }
      newFile.delete();
      if (openOutputSuccess) {
        fail("Successfully opened output to a remote keystore!");
      }
    }
  }

  public void testNonExistentRemoteKeystore() {
    if (Boolean.parseBoolean(cfg.getProperty(Config.REMOTE_TESTS_ENABLED,
        "false"))) {
      try {
        String url = cfg.getProperty(Config.KEYSTORE_REMOTE_ROOT)
            + "fred.ks?keystoreType=jks";
        KeystoreLocation k = KeystoreFactory.getDefault().create(url,
            cfg.getProperty(Config.KEYSTORE_COMMON_KEYSTORE_PW).toCharArray());
        assertTrue("Keystore Location", !k.exists());
      }
      catch (Exception e) {
        logR.error("testNonExistentRemoteKeystore failed", e);
        fail(e.getMessage());
      }
    }
  }

}