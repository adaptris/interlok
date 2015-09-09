package com.adaptris.security;

import java.security.PrivateKey;
import java.security.cert.Certificate;

import junit.framework.TestCase;

import com.adaptris.security.keystore.KeystoreFactory;
import com.adaptris.security.keystore.KeystoreProxy;

/**
 * Test Keystore Functionality wrapping a single KEYSTORE_X509 certificate
 * 
 * @author $Author: lchan $
 */
public class TestSingleX509Keystore extends SingleEntryKeystoreBase {
  /** @see TestCase */
  public TestSingleX509Keystore(String testName) {
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
        cfg.getProperty(Config.KEYSTORE_SINGLE_X509_URL),
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
  public void testContainsAlias() {
    try {
      KeystoreProxy ksp = KeystoreFactory.getDefault().create(kloc);
      ksp.load();

      String alias = cfg.getProperty(Config.KEYSTORE_SINGLE_X509_ALIAS);
      if (!ksp.containsAlias(alias)) {
        fail(alias + " doesn't exist in the specified keystore!");
      }
    }
    catch (Exception e) {
      logR.error(e.getMessage(), e);
      fail(e.getMessage());
    }
  }

  /**
   * Get a certificate out of the keystore.
   */
  public void testKeystoreGetCertificate() {
    try {
      KeystoreProxy ksp = KeystoreFactory.getDefault().create(kloc);
      ksp.load();
      String alias = cfg.getProperty(Config.KEYSTORE_SINGLE_X509_ALIAS);
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
      String alias = cfg.getProperty(Config.KEYSTORE_SINGLE_X509_ALIAS);
      if (ksp.containsAlias(alias)) {
        Certificate[] thisCert = ksp.getCertificateChain(alias);
        assertEquals(1, thisCert.length);
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
      String alias = cfg.getProperty(Config.KEYSTORE_SINGLE_X509_ALIAS);
      if (ksp.containsAlias(alias)) {
        PrivateKey pk = ksp.getPrivateKey(alias, "".toCharArray());
        assertNull(pk);
      }
      fail(alias + " exists in the specified keystore, and getPK didn't fail");
    }
    catch (Exception e) {
      ; // Expected bhaviour
    }
  }

}