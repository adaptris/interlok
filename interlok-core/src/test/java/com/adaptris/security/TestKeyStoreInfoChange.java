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
import java.security.cert.Certificate;
import java.util.Properties;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.adaptris.security.keystore.KeystoreFactory;
import com.adaptris.security.keystore.KeystoreLocation;
import com.adaptris.security.keystore.KeystoreProxy;

/**
 * Test Keystore Functionality.
 * 
 * @author $Author: lchan $
 */
public class TestKeyStoreInfoChange {
  private KeystoreProxy ksm = null;
  private KeystoreLocation ksi = null;
  private KeystoreLocation pwKsi = null;
  private KeystoreLocation newKsi = null;
  private Properties config;
  private Logger logR = LoggerFactory.getLogger(this.getClass());

  public TestKeyStoreInfoChange() {

  }


  @Before
  public void setUp() throws Exception {
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

  @Test
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
        assertNotNull(thisCert);
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

  @Test
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
        assertNotNull(thisCert);
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
