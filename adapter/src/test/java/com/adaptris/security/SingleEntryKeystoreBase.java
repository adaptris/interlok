/*
 * SingleEntryKeystoreBase.java,v 1.1 2008/03/27 15:33:01 lchan Exp
 */

package com.adaptris.security;

import java.io.File;
import java.io.InputStream;
import java.security.cert.Certificate;
import java.util.Properties;
import java.util.Random;

import junit.framework.TestCase;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.adaptris.security.exc.KeystoreException;
import com.adaptris.security.keystore.KeystoreFactory;
import com.adaptris.security.keystore.KeystoreLocation;
import com.adaptris.security.keystore.KeystoreProxy;

/**
 * Test Keystore Functionality wrapping a single KEYSTORE_PKCS12 certificate
 * 
 * @author lchan
 */
public abstract class SingleEntryKeystoreBase extends TestCase {
  protected KeystoreLocation kloc = null;
  protected Properties cfg;
  protected Config config;
  protected transient Log logR = null;

  /** @see TestCase */
  public SingleEntryKeystoreBase(String testName) {
    super(testName);
    logR = LogFactory.getLog(this.getClass());
  }

  /**
   * Get a certificate out of the keystore.
   */
  public void testContainsNonExistentAlias() throws Exception {
    KeystoreProxy ksp = KeystoreFactory.getDefault().create(kloc);
    ksp.load();

    String alias = String.valueOf(new Random().nextInt());
    if (ksp.containsAlias(alias)) {
      fail(alias + " exists in the specified keystore!");
    }
  }

  /**
   * Get the underlying keystore object
   */
  public void testKeystoreGetKeyStore() {
    try {
      KeystoreProxy ksp = KeystoreFactory.getDefault().create(kloc);
      ksp.load();
      assertNotNull("Keystore should not be null", ksp.getKeystore());
    }
    catch (Exception e) {
      logR.error(e.getMessage(), e);
      fail(e.getMessage());
    }
  }

  public void testImportCertificate() throws Exception {
    KeystoreProxy ksp = KeystoreFactory.getDefault().create(kloc);
    ksp.load();
    try {
      ksp.setCertificate("", "");
      fail("Import successful");
    }
    catch (Exception e) {
      assertEquals(KeystoreException.class, e.getClass());
      assertTrue(e.getMessage().matches(".*is implicitly read-only.*"));
    }
    try {
      ksp.setCertificate("", (Certificate) null);
      fail("Import successful");
    }
    catch (Exception e) {
      assertEquals(KeystoreException.class, e.getClass());
      assertTrue(e.getMessage().matches(".*is implicitly read-only.*"));
    }
    try {
      ksp.setCertificate("", (InputStream) null);
      fail("Import successful");
    }
    catch (Exception e) {
      assertEquals(KeystoreException.class, e.getClass());
      assertTrue(e.getMessage().matches(".*is implicitly read-only.*"));
    }
    try {
      ksp.setCertificate("", (File) null);
      fail("Import successful");
    }
    catch (Exception e) {
      assertEquals(KeystoreException.class, e.getClass());
      assertTrue(e.getMessage().matches(".*is implicitly read-only.*"));
    }
  }

  public void testImportCertificateChain() throws Exception {
    KeystoreProxy ksp = KeystoreFactory.getDefault().create(kloc);
    ksp.load();
    try {
      ksp.importCertificateChain("", "".toCharArray(), "");
      fail("Import successful");
    }
    catch (Exception e) {
      assertEquals(KeystoreException.class, e.getClass());
      assertTrue(e.getMessage().matches(".*is implicitly read-only.*"));
    }
    try {
      ksp.importCertificateChain("", "".toCharArray(), (InputStream) null);
      fail("Import successful");
    }
    catch (Exception e) {
      assertEquals(KeystoreException.class, e.getClass());
      assertTrue(e.getMessage().matches(".*is implicitly read-only.*"));
    }
    try {
      ksp.importCertificateChain("", "".toCharArray(), (File) null);
      fail("Import successful");
    }
    catch (Exception e) {
      assertEquals(KeystoreException.class, e.getClass());
      assertTrue(e.getMessage().matches(".*is implicitly read-only.*"));
    }
  }

  public void testImportPrivateKey() throws Exception {
    KeystoreProxy ksp = KeystoreFactory.getDefault().create(kloc);
    ksp.load();
    try {
      ksp.importPrivateKey("", "".toCharArray(), "", "".toCharArray());
      fail("Import successful");
    }
    catch (Exception e) {
      assertEquals(KeystoreException.class, e.getClass());
      assertTrue(e.getMessage().matches(".*is implicitly read-only.*"));
    }
    try {
      ksp.importPrivateKey("", "".toCharArray(), (InputStream) null, ""
          .toCharArray());
      fail("Import successful");
    }
    catch (Exception e) {
      assertEquals(KeystoreException.class, e.getClass());
      assertTrue(e.getMessage().matches(".*is implicitly read-only.*"));
    }
    try {
      ksp.importPrivateKey("", "".toCharArray(), (File) null, "".toCharArray());
      fail("Import successful");
    }
    catch (Exception e) {
      assertEquals(KeystoreException.class, e.getClass());
      assertTrue(e.getMessage().matches(".*is implicitly read-only.*"));
    }
  }
}