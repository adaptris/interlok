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

import java.security.cert.Certificate;
import java.util.Properties;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.adaptris.security.keystore.KeystoreFactory;
import com.adaptris.security.keystore.KeystoreLocation;
import com.adaptris.security.keystore.KeystoreProxy;

/**
 * Test Keystore Functionality.
 * 
 * @author $Author: lchan $
 */
public class TestKeyStoreInfoChange extends TestCase {
  private KeystoreProxy ksm = null;
  private KeystoreLocation ksi = null;
  private KeystoreLocation pwKsi = null;
  private KeystoreLocation newKsi = null;
  private Properties config;
  private static Log logR = null;

  /** @see TestCase */
  public TestKeyStoreInfoChange(String testName) {
    super(testName);
    if (logR == null) {
      logR = LogFactory.getLog(TestKeyStoreInfoChange.class);
    }
  }

  /** main. */
  public static void main(java.lang.String[] args) {
    junit.textui.TestRunner.run(suite());
  }

  public static Test suite() {
    TestSuite suite = new TestSuite(TestKeyStoreInfoChange.class);
    return suite;
  }

  /**
   * @see TestCase#setUp()
   */
  public void setUp() throws Exception {
    super.setUp();
    config = Config.getInstance().getProperties();
    if (config == null) {
      fail("No Configuration(!) available");
    }
    ksi = KeystoreFactory.getDefault().create(
        config.getProperty(Config.KEYSTORE_TEST_URL),
        config.getProperty(Config.KEYSTORE_COMMON_KEYSTORE_PW).toCharArray());
    newKsi = KeystoreFactory.getDefault().create(
        config.getProperty(Config.KEYSTORE_TEST_NEW_URL),
        config.getProperty(Config.KEYSTORE_TEST_NEW_PW).toCharArray());
    pwKsi = KeystoreFactory.getDefault().create(
        config.getProperty(Config.KEYSTORE_TEST_NEW_URL),
        config.getProperty(Config.KEYSTORE_TEST_NEW_PW).toCharArray());
    Config.getInstance().buildKeystore(
        config.getProperty(Config.KEYSTORE_TEST_URL), null, false);
  }

  /**
   * @see TestCase#tearDown()
   */
  public void tearDown() throws Exception {
    super.tearDown();
  }

  /**
   * This tests a generic change of the keystore info to a new location and
   * password.
   */
  public void testChangeKeyStoreInfo() {
    Certificate thisCert;
    try {
      ksm = KeystoreFactory.getDefault().create(ksi);
      ksm.load();
      String alias = config.getProperty(Config.KEYSTORE_COMMON_PRIVKEY_ALIAS);
      ksm.setKeystoreLocation(newKsi);
      ksm.commit();
      KeystoreProxy tempKsm = KeystoreFactory.getDefault().create(newKsi);
      tempKsm.load();
      if (tempKsm.containsAlias(alias)) {
        thisCert = tempKsm.getCertificate(alias);
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

  /**
   * Test the change of the kestore password
   */
  public void testChangeKeyStorePassword() {
    Certificate thisCert;
    try {
      ksm = KeystoreFactory.getDefault().create(ksi);
      ksm.load();
      String alias = config.getProperty(Config.KEYSTORE_COMMON_PRIVKEY_ALIAS);

      // Now change the password
      ksm.setKeystoreLocation(pwKsi);
      ksm.commit();

      // Now attempt to reload with the new keystore info.
      KeystoreProxy tempKsm = KeystoreFactory.getDefault().create(pwKsi);
      tempKsm.load();
      // we should be able to reread the certificate information...
      if (tempKsm.containsAlias(alias)) {
        thisCert = tempKsm.getCertificate(alias);
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

}
