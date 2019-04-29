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

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.util.Properties;
import java.util.Random;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.adaptris.security.exc.AdaptrisSecurityException;
import com.adaptris.security.keystore.KeystoreFactory;
import com.adaptris.security.keystore.KeystoreLocation;
import com.adaptris.security.keystore.KeystoreProxy;

/**
 * Test Keystore Functionality.
 * 
 */
public class TestKeyStore {
  private KeystoreProxy ksp = null;
  private KeystoreLocation kloc = null;
  private Properties cfg;
  private Config config;
  private Logger logR = LoggerFactory.getLogger(this.getClass());

  public TestKeyStore() {
  }


  @Before
  public void setUp() throws Exception {
    config = Config.getInstance();
    cfg = config.getProperties();

    if (cfg == null) {
      fail("No Configuration(!) available");
    }
    kloc = KeystoreFactory.getDefault().create(
        cfg.getProperty(Config.KEYSTORE_TEST_URL),
        cfg.getProperty(Config.KEYSTORE_COMMON_KEYSTORE_PW).toCharArray());
    config
        .buildKeystore(cfg.getProperty(Config.KEYSTORE_TEST_URL), null, false);
  }


  @Test
  public void testKeystoreGetCertificate() throws Exception {
    Certificate thisCert;
    ksp = KeystoreFactory.getDefault().create(kloc);
    ksp.load();
    String alias = cfg.getProperty(Config.KEYSTORE_COMMON_PRIVKEY_ALIAS);
    if (ksp.containsAlias(alias)) {
      thisCert = ksp.getCertificate(alias);
      assertNotNull("Certificate is not null", thisCert);
    }
    else {
      fail(alias + " does not exist in the specified keystore");
    }
  }

  @Test
  public void testKeystoreImportCertificate() throws Exception {
    ksp = KeystoreFactory.getDefault().create(kloc);
    ksp.load();
    ksp.setCertificate(String.valueOf(new Random().nextInt()), cfg
        .getProperty(Config.KEYSTORE_IMPORT_X509_FILE));
  }

  @Test
  public void testKeystoreImportCertificateInvalidFilename() throws Exception {
    ksp = KeystoreFactory.getDefault().create(kloc);
    ksp.load();
    try {
      ksp.setCertificate(String.valueOf(new Random().nextInt()), "XXXX");
      fail("Import of missing cert succeeded");
    }
    catch (AdaptrisSecurityException e) {
      ; // expected
    }
  }

  @Test
  public void testKeystoreGetPrivateKey() throws Exception {
    ksp = KeystoreFactory.getDefault().create(kloc);
    ksp.load();
    String alias = cfg.getProperty(Config.KEYSTORE_COMMON_PRIVKEY_ALIAS);
    if (ksp.containsAlias(alias)) {
      PrivateKey pk = ksp.getPrivateKey(alias, cfg.getProperty(
          Config.KEYSTORE_COMMON_PRIVKEY_PW).toCharArray());
      assertNotNull("PrivateKey is not null", pk);
    }
    else {
      fail(alias + " does not exist in the specified keystore");
    }
  }

  @Test
  public void testKeystoreNoPrivateKeyPassword() throws Exception {
    ksp = KeystoreFactory.getDefault().create(kloc);
    ksp.load();
    String alias = cfg.getProperty(Config.KEYSTORE_COMMON_PRIVKEY_ALIAS);
    if (ksp.containsAlias(alias)) {
      PrivateKey pk = ksp.getPrivateKey(alias, null);
      assertNotNull("PrivateKey is not null", pk);
    }
    else {
      fail(alias + " does not exist in the specified keystore");
    }
  }

  @Test
  public void testKeystoreImportPrivateKey() throws Exception {
    config.importPrivateKey(cfg.getProperty(Config.KEYSTORE_TEST_URL), cfg
        .getProperty(Config.KEYSTORE_IMPORT_PKCS12_FILE), false);
    ksp = KeystoreFactory.getDefault().create(kloc);
    ksp.load();
    String alias = cfg.getProperty(Config.KEYSTORE_COMMON_PRIVKEY_ALIAS);
    if (ksp.containsAlias(alias)) {
      PrivateKey pk = ksp.getPrivateKey(alias, cfg.getProperty(
          Config.KEYSTORE_COMMON_PRIVKEY_PW).toCharArray());
      assertNotNull("PrivateKey is not null", pk);
    }
    else {
      fail(alias + " does not exist in the specified keystore");
    }
  }

  @Test
  public void testKeystoreImportInvalidPrivateKey() throws Exception {
    try {
      config.importPrivateKey(cfg.getProperty(Config.KEYSTORE_TEST_URL),
          "XXXX", true);
      fail("Import of non-existent privatekey succeeded");
    }
    catch (AdaptrisSecurityException e) {
      ;// expected
    }
  }

  @Test
  public void testKeystoreImportCertificateChain() throws Exception {
    config.importPrivateKey(cfg.getProperty(Config.KEYSTORE_TEST_URL), cfg
        .getProperty(Config.KEYSTORE_IMPORT_PKCS12_FILE), false);
    ksp = KeystoreFactory.getDefault().create(kloc);
    ksp.load();
    ksp.importCertificateChain(cfg
        .getProperty(Config.KEYSTORE_COMMON_PRIVKEY_ALIAS), cfg.getProperty(
        Config.KEYSTORE_COMMON_KEYSTORE_PW).toCharArray(), cfg
        .getProperty(Config.KEYSTORE_IMPORT_CERTCHAIN_FILE));
    ksp.commit();
  }

  @Test
  public void testKeystoreImportCertificateChainInvalidFile() throws Exception {
    config.importPrivateKey(cfg.getProperty(Config.KEYSTORE_TEST_URL), cfg
        .getProperty(Config.KEYSTORE_IMPORT_PKCS12_FILE), true);
    ksp = KeystoreFactory.getDefault().create(kloc);
    ksp.load();
    try {
      ksp.importCertificateChain(cfg
          .getProperty(Config.KEYSTORE_COMMON_PRIVKEY_ALIAS), cfg.getProperty(
          Config.KEYSTORE_COMMON_KEYSTORE_PW).toCharArray(), "XXXX");
      fail("Import of non-existent certchain succeeded");
    }
    catch (AdaptrisSecurityException e) {
      ;// expected
    }
    ksp.commit();
  }

  @Test
  public void testKeystoreImportCertificateChainInvalidAlias() throws Exception {
    config.importPrivateKey(cfg.getProperty(Config.KEYSTORE_TEST_URL), cfg
        .getProperty(Config.KEYSTORE_IMPORT_PKCS12_FILE), true);
    ksp = KeystoreFactory.getDefault().create(kloc);
    ksp.load();
    try {
      ksp.importCertificateChain(String.valueOf(new Random().nextInt()), cfg
          .getProperty(Config.KEYSTORE_COMMON_KEYSTORE_PW).toCharArray(), cfg
          .getProperty(Config.KEYSTORE_IMPORT_CERTCHAIN_FILE));
      fail("Import of non-existent certchain succeeded");
    }
    catch (AdaptrisSecurityException e) {
      ;// expected
    }
    ksp.commit();
  }

}
