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

import java.io.FileInputStream;
import java.io.InputStream;
import java.net.UnknownHostException;
import java.util.Calendar;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.adaptris.security.certificate.CertificateHandler;
import com.adaptris.security.certificate.CertificateHandlerFactory;
import com.adaptris.security.exc.CertException;
import com.adaptris.security.keystore.KeystoreFactory;

/**
 * Test Certificate Handling.
 *
 * @author $Author: lchan $
 */
public class TestCertificateHandler extends TestCase {
  private Config config;
  private static Log logR = null;

  /**
   * @see TestCase
   */
  public TestCertificateHandler(String testName) {
    super(testName);
    if (logR == null) {
      logR = LogFactory.getLog(TestCertificateHandler.class);
    }
  }

  /**
   * Main class.
   */
  public static void main(java.lang.String[] args) {
    junit.textui.TestRunner.run(suite());
  }

  public static Test suite() {
    TestSuite suite = new TestSuite(TestCertificateHandler.class);
    return suite;
  }

  /**
   * @see TestCase#setUp()
   */
  @Override
  public void setUp() throws Exception {
    super.setUp();
    config = Config.getInstance();
    if (config == null) {
      fail("No Configuration(!) available");
    }
    KeystoreFactory.getDefault().create(config.getProperties().getProperty(Config.KEYSTORE_TEST_URL),
        config.getProperties().getProperty(Config.KEYSTORE_COMMON_KEYSTORE_PW).toCharArray());
    config.buildKeystore(config.getProperties().getProperty(Config.KEYSTORE_TEST_URL), null, true);
  }

  /**
   * @see TestCase#tearDown()
   */
  @Override
  public void tearDown() throws Exception {
    super.tearDown();
  }

  /**
   * Test a un-expired certificate for expiry.
   */
  public void testGoodCertificateExpiry() throws Exception {
    InputStream input = new FileInputStream(config.getProperties().getProperty(Config.CERTHANDLER_GOOD));

    CertificateHandler handler = CertificateHandlerFactory.getInstance().generateHandler(input);

    input.close();

    assertTrue("Expiry on \n" + handler.getCertificate().toString(), !handler.isExpired());
  }

  /**
   * Test that the good cert hasn't been revoked.
   * <p>
   * If we log a failure against this, then this dupont cert has been revoked
   */
  public void testGoodCertificateRevocation() throws Exception {
    InputStream input = new FileInputStream(config.getProperties().getProperty(Config.CERTHANDLER_GOOD));

    CertificateHandler handler = CertificateHandlerFactory.getInstance().generateHandler(input);

    input.close();
    assertEquals("Revocation", handler.isRevoked(), false);
    assertNotNull(handler.getLastRevocationCheck());

  }

  /**
   * Check again. but check the getLastRevocationCheck() is the same as one we stored previously
   */
  public void testGoodCertificateRevocationCache() throws Exception {
    InputStream input = new FileInputStream(config.getProperties().getProperty(Config.CERTHANDLER_GOOD));

    CertificateHandler handler = CertificateHandlerFactory.getInstance().generateHandler(input);

    input.close();
    
    assertEquals("Revocation", handler.isRevoked(), false);
    Calendar lastGoodCheck = handler.getLastRevocationCheck();
    assertEquals("Calendar", handler.getLastRevocationCheck(), lastGoodCheck);

  }

  /**
   * This certificate we know is expired...
   */
  public void testExpiredCertificateExpiry() throws Exception {
    InputStream input = new FileInputStream(config.getProperties().getProperty(Config.CERTHANDLER_EXPIRED));

    CertificateHandler handler = CertificateHandlerFactory.getInstance().generateHandler(input);

    input.close();

    assertTrue("Expiry on \n" + handler.getCertificate().toString(), handler.isExpired());

  }

  /**
   * Expired Certis should still be good. If we log a failure against this, then this dupont cert has been revoked
   */
  public void testExpiredCertificateRevocation() throws Exception {
    try {
      InputStream input = new FileInputStream(config.getProperties().getProperty(Config.CERTHANDLER_EXPIRED));

      CertificateHandler handler = CertificateHandlerFactory.getInstance().generateHandler(input);

      input.close();
      assertEquals("Revocation", handler.isRevoked(), false);
      assertNotNull(handler.getLastRevocationCheck());
    }
    catch (CertException e) {
      if (!(e.getCause() != null && e.getCause() instanceof UnknownHostException)) {
        throw e;
      }
    }

  }

  /*
   * Check again..., but check the getLastRevocationCheck() is the same as one we stored previously
   */
  public void testExpiredCertificateRevocationCache() throws Exception {
    try {
      InputStream input = new FileInputStream(config.getProperties().getProperty(Config.CERTHANDLER_EXPIRED));

      CertificateHandler handler = CertificateHandlerFactory.getInstance().generateHandler(input);

      input.close();
      
      assertEquals("Revocation", handler.isRevoked(), false);
      Calendar lastExpiredCheck = handler.getLastRevocationCheck();
      assertEquals("Calendar", handler.getLastRevocationCheck(), lastExpiredCheck);
    }
    catch (CertException e) {
      if (!(e.getCause() != null && e.getCause() instanceof UnknownHostException)) {
        throw e;
      }
    }
  }

}
