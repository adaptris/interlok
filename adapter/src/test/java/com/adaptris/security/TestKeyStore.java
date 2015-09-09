package com.adaptris.security;

import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.util.Properties;
import java.util.Random;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.adaptris.security.exc.AdaptrisSecurityException;
import com.adaptris.security.keystore.KeystoreFactory;
import com.adaptris.security.keystore.KeystoreLocation;
import com.adaptris.security.keystore.KeystoreProxy;

/**
 * Test Keystore Functionality.
 * 
 * @author $Author: lchan $
 */
public class TestKeyStore extends TestCase {
  private KeystoreProxy ksp = null;
  private KeystoreLocation kloc = null;
  private Properties cfg;
  private Config config;
  private static Log logR = null;

  /** @see TestCase */
  public TestKeyStore(String testName) {
    super(testName);
    if (logR == null) {
      logR = LogFactory.getLog(TestKeyStore.class);
    }
  }

  /** main. */
  public static void main(java.lang.String[] args) {
    junit.textui.TestRunner.run(suite());
  }

  public static Test suite() {
    TestSuite suite = new TestSuite(TestKeyStore.class);
    return suite;
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
        cfg.getProperty(Config.KEYSTORE_TEST_URL),
        cfg.getProperty(Config.KEYSTORE_COMMON_KEYSTORE_PW).toCharArray());
    config
        .buildKeystore(cfg.getProperty(Config.KEYSTORE_TEST_URL), null, false);
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
  public void testKeystoreGetCertificate() throws Exception {
    Certificate thisCert;
    ksp = KeystoreFactory.getDefault().create(kloc);
    ksp.load();
    String alias = cfg.getProperty(Config.KEYSTORE_COMMON_PRIVKEY_ALIAS);
    if (ksp.containsAlias(alias)) {
      thisCert = ksp.getCertificate(alias);
      assertNotNull("Certificate is not null", thisCert);
      logR.trace(thisCert);
    }
    else {
      fail(alias + " does not exist in the specified keystore");
    }
  }

  public void testKeystoreImportCertificate() throws Exception {
    ksp = KeystoreFactory.getDefault().create(kloc);
    ksp.load();
    ksp.setCertificate(String.valueOf(new Random().nextInt()), cfg
        .getProperty(Config.KEYSTORE_IMPORT_X509_FILE));
  }

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

  /**
   * Get the private key out off the keystore.
   */
  public void testKeystoreGetPrivateKey() throws Exception {
    ksp = KeystoreFactory.getDefault().create(kloc);
    ksp.load();
    String alias = cfg.getProperty(Config.KEYSTORE_COMMON_PRIVKEY_ALIAS);
    if (ksp.containsAlias(alias)) {
      PrivateKey pk = ksp.getPrivateKey(alias, cfg.getProperty(
          Config.KEYSTORE_COMMON_PRIVKEY_PW).toCharArray());
      assertNotNull("PrivateKey is not null", pk);
      logR.trace(pk);
    }
    else {
      fail(alias + " does not exist in the specified keystore");
    }
  }

  public void testKeystoreNoPrivateKeyPassword() throws Exception {
    ksp = KeystoreFactory.getDefault().create(kloc);
    ksp.load();
    String alias = cfg.getProperty(Config.KEYSTORE_COMMON_PRIVKEY_ALIAS);
    if (ksp.containsAlias(alias)) {
      PrivateKey pk = ksp.getPrivateKey(alias, null);
      assertNotNull("PrivateKey is not null", pk);
      logR.trace(pk);
    }
    else {
      fail(alias + " does not exist in the specified keystore");
    }
  }

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
      logR.trace(pk);
    }
    else {
      fail(alias + " does not exist in the specified keystore");
    }
  }

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