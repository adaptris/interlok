/*
 * $Id: TestCertificateGeneration.java,v 1.12 2008/03/27 15:33:01 lchan Exp $
 */
package com.adaptris.security;

import java.io.ByteArrayInputStream;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.util.Properties;
import java.util.Random;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.adaptris.security.certificate.CertRequestHandler;
import com.adaptris.security.certificate.CertificateBuilder;
import com.adaptris.security.certificate.CertificateHandler;
import com.adaptris.security.certificate.CertificateHandlerFactory;
import com.adaptris.security.keystore.KeystoreFactory;
import com.adaptris.security.keystore.KeystoreLocation;
import com.adaptris.security.keystore.KeystoreProxy;

/**
 * Test Certificate Generation.
 * <p>
 * This will also test the write capabilities of the keystore manager
 *
 * @author $Author: lchan $
 */
public class TestCertificateGeneration extends TestCase {

  private KeystoreProxy ksp = null;
  private KeystoreLocation ksc = null;
  private Properties cfg;
  private static Log logR = LogFactory.getLog(TestCertificateGeneration.class);
  private static final Random random = new Random();

  /**
   * @see TestCase
   */
  public TestCertificateGeneration(String testName) {
    super(testName);
  }

  /** Main Class. */
  public static void main(java.lang.String[] args) {
    junit.textui.TestRunner.run(suite());
  }

  public static Test suite() {

    TestSuite suite = new TestSuite(TestCertificateGeneration.class);
    return suite;
  }

  /** @see TestCase#setUp() */
  @Override
  public void setUp() throws Exception {
    super.setUp();
    cfg = Config.getInstance().getProperties();
    if (cfg == null) {
      fail("No Configuration(!) available");
    }
    ksc = KeystoreFactory.getDefault().create(
        cfg.getProperty(Config.KEYSTORE_TEST_URL),
        cfg.getProperty(Config.KEYSTORE_COMMON_KEYSTORE_PW).toCharArray());
    ksp = KeystoreFactory.getDefault().create(ksc);
    Config.getInstance().buildKeystore(
        cfg.getProperty(Config.KEYSTORE_TEST_URL), null, true);
  }

  /** @see TestCase#tearDown() */
  @Override
  public void tearDown() throws Exception {
    super.tearDown();
  }

  /**
   * Test the generation of a certificate.
   */
  public void testCertificateGeneration() throws Exception {
    String commonName = String.valueOf(random.nextInt(1000));
    CertificateBuilder builder = Config.getInstance().getBuilder(commonName);
    Certificate selfCert = builder.createSelfSignedCertificate();
    CertificateHandler ch = CertificateHandlerFactory.getInstance()
        .generateHandler(selfCert);
    
    String signatureAlgorithm = ch.getSignatureAlgorithm();
    String property = cfg.getProperty(Config.CERTIFICATE_SIGALG);
    
//    AlgorithmID algorithmID = SecurityUtil.getAlgorithmID(signatureAlgorithm);
//    AlgorithmID configID = SecurityUtil.getAlgorithmID(property);

    assertEquals("Signature Algorithm", signatureAlgorithm,
            property);

    assertEquals("Key Algorithm", cfg.getProperty(Config.CERTIFICATE_KEYALG),
        ch.getKeyAlgorithm());

  }

  /**
   * Test writing the newly generated certificate to a keystore.
   */
  public void testCertificateAndPrivateKeyToKeystore() throws Exception {
    String commonName = String.valueOf(random.nextInt(1000));
    CertificateBuilder builder = Config.getInstance().getBuilder(commonName);
    Certificate selfCert = builder.createSelfSignedCertificate();
    PrivateKey privkey = builder.getPrivateKey();
    ksp = KeystoreFactory.getDefault().create(ksc);
    try {
      ksp.load();
    }
    catch (Exception e) {
      // Ignore the error...
    }

    String alias = cfg.getProperty(Config.KEYSTORE_COMMON_PRIVKEY_ALIAS);
    char[] password = cfg.getProperty(Config.KEYSTORE_COMMON_PRIVKEY_PW)
        .toCharArray();
    Certificate[] certChain = new Certificate[1];
    certChain[0] = selfCert;
    ksp.setPrivateKey(alias, privkey, password, certChain);
    ksp.commit();
  }

  /**
   * Test writing the newly generated certificate to a keystore.
   */
  public void testCertificateToKeystore() throws Exception {
    String commonName = String.valueOf(random.nextInt(1000));
    CertificateBuilder builder = Config.getInstance().getBuilder(commonName);
    Certificate selfCert = builder.createSelfSignedCertificate();
    builder.getPrivateKey();
    ksp = KeystoreFactory.getDefault().create(ksc);
    try {
      ksp.load();
    }
    catch (Exception e) {
      ;
    }

    ksp.setCertificate(commonName, selfCert);
    ksp.commit();
  }

  public void testEncodedCertificateToKeystore() throws Exception {
    String commonName = String.valueOf(random.nextInt(1000));
    CertificateBuilder builder = Config.getInstance().getBuilder(commonName);
    Certificate selfCert = builder.createSelfSignedCertificate();
    builder.getPrivateKey();
    ksp = KeystoreFactory.getDefault().create(ksc);
    try {
      ksp.load();
    }
    catch (Exception e) {
      ;
    }

    ksp.setCertificate(commonName, new ByteArrayInputStream(selfCert
        .getEncoded()));
    ksp.commit();
  }

  public void testCreateCertificateRequest() {
    try {
      String commonName = String.valueOf(random.nextInt(1000));
      CertificateBuilder builder = Config.getInstance().getBuilder(commonName);
      Certificate selfCert = builder.createSelfSignedCertificate();
      PrivateKey privkey = builder.getPrivateKey();

      String s = CertRequestHandler.createRequest(selfCert, privkey);
      logR.trace(s);
    }
    catch (Exception e) {
      logR.error(e.getMessage(), e);
      fail(e.getMessage());
    }
  }

}