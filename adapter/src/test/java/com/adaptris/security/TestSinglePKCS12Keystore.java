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

import java.security.PrivateKey;
import java.security.cert.Certificate;

import junit.framework.TestCase;

import com.adaptris.security.keystore.KeystoreFactory;
import com.adaptris.security.keystore.KeystoreProxy;

/**
 * Test Keystore Functionality wrapping a single KEYSTORE_PKCS12 certificate
 * 
 * @author $Author: lchan $
 */
public class TestSinglePKCS12Keystore extends SingleEntryKeystoreBase {
  /** @see TestCase */
  public TestSinglePKCS12Keystore(String testName) {
    super(testName);
  }

  /**
   * @see TestCase#setUp()
   */
  public void setUp() throws Exception {
    super.setUp();
    config = Config.getInstance();
    cfg = config.getProperties();

    if (cfg == null) {
      fail("No Configuration(!) available");
    }
    kloc = KeystoreFactory.getDefault().create(
        cfg.getProperty(Config.KEYSTORE_SINGLE_PKCS12_URL),
        cfg.getProperty(Config.KEYSTORE_COMMON_KEYSTORE_PW).toCharArray());
  }

  /**
   * @see TestCase#tearDown()
   */
  public void tearDown() throws Exception {
    super.tearDown();
  }

  /**
   * Get a certificate out of the keystore.
   */
  public void testContainsAlias() throws Exception {
    KeystoreProxy ksp = KeystoreFactory.getDefault().create(kloc);
    ksp.load();

    String alias = cfg.getProperty(Config.KEYSTORE_SINGLE_PKCS12_ALIAS);
    if (!ksp.containsAlias(alias)) {
      fail(alias + " doesn't exist in the specified keystore!");
    }
  }

  /**
   * Get a certificate out of the keystore.
   */
  public void testKeystoreGetCertificate() {
    try {
      KeystoreProxy ksp = KeystoreFactory.getDefault().create(kloc);
      ksp.load();
      String alias = cfg.getProperty(Config.KEYSTORE_SINGLE_PKCS12_ALIAS);
      if (ksp.containsAlias(alias)) {
        Certificate thisCert = ksp.getCertificate(alias);
        logR.trace(thisCert);
      }
      else {
        fail(alias + " does not exist in the specified keystore");
      }
    }
    catch (Exception e) {
      logR.error(e.getMessage(), e);
      fail(e.getMessage());
    }
  }

  public void testKeystoreGetCertificateChain() {
    try {
      KeystoreProxy ksp = KeystoreFactory.getDefault().create(kloc);
      ksp.load();
      String alias = cfg.getProperty(Config.KEYSTORE_SINGLE_PKCS12_ALIAS);
      if (ksp.containsAlias(alias)) {
        Certificate[] thisCert = ksp.getCertificateChain(alias);
        assertTrue(thisCert.length > 0);
      }
      else {
        fail(alias + " does not exist in the specified keystore");
      }
    }
    catch (Exception e) {
      logR.error(e.getMessage(), e);
      fail(e.getMessage());
    }
  }

  /**
   * Get the private key out off the keystore.
   */
  public void testKeystoreGetPrivateKey() {
    try {
      KeystoreProxy ksp = KeystoreFactory.getDefault().create(kloc);
      ksp.load();
      String alias = cfg.getProperty(Config.KEYSTORE_SINGLE_PKCS12_ALIAS);
      if (ksp.containsAlias(alias)) {
        PrivateKey pk = ksp.getPrivateKey(alias, cfg.getProperty(Config.KEYSTORE_COMMON_KEYSTORE_PW).toCharArray());
        logR.trace(pk);
      }
    }
    catch (Exception e) {
      logR.error(e.getMessage(), e);
      fail(e.getMessage());
    }
  }
}
