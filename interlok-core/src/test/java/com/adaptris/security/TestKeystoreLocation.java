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

package com.adaptris.security;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.util.Properties;
import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.adaptris.security.keystore.KeystoreFactory;
import com.adaptris.security.keystore.KeystoreLocation;
import junit.framework.TestCase;

/**
 * Test Keystore Functionality.
 *
 */
public class TestKeystoreLocation {
  private Properties cfg;
  private Config config;
  private Logger logR = LoggerFactory.getLogger(this.getClass());

  /** @see TestCase */
  public TestKeystoreLocation() {
  }

  @Before
  public void setUp() throws Exception {
    config = Config.getInstance();
    cfg = config.getProperties();

    if (cfg == null) {
      fail("No Configuration(!) available");
    }
    config
        .buildKeystore(cfg.getProperty(Config.KEYSTORE_TEST_URL), null, false);
  }


  @Test
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

  @Test
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

  @Test
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

  @Test
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

  @Test
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

  @Test
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
