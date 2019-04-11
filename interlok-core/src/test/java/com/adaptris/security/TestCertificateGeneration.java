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
import static org.junit.Assert.fail;
import java.io.ByteArrayInputStream;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.util.Properties;
import java.util.Random;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
 */
public class TestCertificateGeneration {

  private KeystoreProxy ksp = null;
  private KeystoreLocation ksc = null;
  private Properties cfg;
  private static Logger logR = LoggerFactory.getLogger(TestCertificateGeneration.class);
  private static final Random random = new Random();

  public TestCertificateGeneration() {
  }

  @Before
  public void setUp() throws Exception {
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

  @Test
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

  @Test
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

  @Test
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

  @Test
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

  @Test
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
