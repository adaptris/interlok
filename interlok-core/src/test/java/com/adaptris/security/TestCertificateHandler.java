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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.FileInputStream;
import java.io.InputStream;
import java.net.UnknownHostException;
import java.util.Calendar;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.adaptris.security.certificate.CertificateHandler;
import com.adaptris.security.certificate.CertificateHandlerFactory;
import com.adaptris.security.exc.CertException;
import com.adaptris.security.keystore.KeystoreFactory;

/**
 * Test Certificate Handling.
 */
public class TestCertificateHandler {
  private Config config;

  public TestCertificateHandler() {
  }

  @BeforeEach
  public void setUp() throws Exception {
    config = Config.getInstance();
    if (config == null) {
      fail("No Configuration(!) available");
    }
    KeystoreFactory.getDefault().create(config.getProperties().getProperty(Config.KEYSTORE_TEST_URL),
        config.getProperties().getProperty(Config.KEYSTORE_COMMON_KEYSTORE_PW).toCharArray());
    config.buildKeystore(config.getProperties().getProperty(Config.KEYSTORE_TEST_URL), null, true);
  }

  @Test
  public void testGoodCertificateExpiry() throws Exception {
    InputStream input = new FileInputStream(config.getProperties().getProperty(Config.CERTHANDLER_GOOD));

    CertificateHandler handler = CertificateHandlerFactory.getInstance().generateHandler(input);

    input.close();

    assertTrue(!handler.isExpired());
  }

  @Test
  public void testGoodCertificateRevocation() throws Exception {
    InputStream input = new FileInputStream(config.getProperties().getProperty(Config.CERTHANDLER_GOOD));

    CertificateHandler handler = CertificateHandlerFactory.getInstance().generateHandler(input);

    input.close();
    assertEquals(handler.isRevoked(), false);
    assertNotNull(handler.getLastRevocationCheck());

  }

  @Test
  public void testGoodCertificateRevocationCache() throws Exception {
    InputStream input = new FileInputStream(config.getProperties().getProperty(Config.CERTHANDLER_GOOD));

    CertificateHandler handler = CertificateHandlerFactory.getInstance().generateHandler(input);

    input.close();
    
    assertEquals(handler.isRevoked(), false);
    Calendar lastGoodCheck = handler.getLastRevocationCheck();
    assertEquals(handler.getLastRevocationCheck(), lastGoodCheck);

  }

  @Test
  public void testExpiredCertificateExpiry() throws Exception {
    InputStream input = new FileInputStream(config.getProperties().getProperty(Config.CERTHANDLER_EXPIRED));

    CertificateHandler handler = CertificateHandlerFactory.getInstance().generateHandler(input);

    input.close();

    assertTrue(handler.isExpired());

  }

  @Test
  public void testExpiredCertificateRevocation() throws Exception {
    try {
      InputStream input = new FileInputStream(config.getProperties().getProperty(Config.CERTHANDLER_EXPIRED));

      CertificateHandler handler = CertificateHandlerFactory.getInstance().generateHandler(input);

      input.close();
      assertEquals(handler.isRevoked(), false);
      assertNotNull(handler.getLastRevocationCheck());
    }
    catch (CertException e) {
      if (!(e.getCause() != null && e.getCause() instanceof UnknownHostException)) {
        throw e;
      }
    }

  }

  @Test
  public void testExpiredCertificateRevocationCache() throws Exception {
    try {
      InputStream input = new FileInputStream(config.getProperties().getProperty(Config.CERTHANDLER_EXPIRED));

      CertificateHandler handler = CertificateHandlerFactory.getInstance().generateHandler(input);

      input.close();
      
      assertEquals(handler.isRevoked(), false);
      Calendar lastExpiredCheck = handler.getLastRevocationCheck();
      assertEquals(handler.getLastRevocationCheck(), lastExpiredCheck);
    }
    catch (CertException e) {
      if (!(e.getCause() != null && e.getCause() instanceof UnknownHostException)) {
        throw e;
      }
    }
  }

}
