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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import org.junit.Before;
import org.junit.Test;
import com.adaptris.security.keystore.KeystoreFactory;
import com.adaptris.security.keystore.KeystoreProxy;

/**
 * Test Keystore Functionality wrapping a single KEYSTORE_X509 certificate
 * 
 * 
 */
public class TestSingleX509Keystore extends SingleEntryKeystoreBase {

  public TestSingleX509Keystore() {
    super();
  }

  @Override
  @Before
  public void setUp() throws Exception {
    config = Config.getInstance();
    kloc = KeystoreFactory.getDefault().create(
        config.getProperty(Config.KEYSTORE_SINGLE_X509_URL),
        config.getProperty(Config.KEYSTORE_COMMON_KEYSTORE_PW).toCharArray());
  }


  @Test
  public void testContainsAlias() {
    try {
      KeystoreProxy ksp = KeystoreFactory.getDefault().create(kloc);
      ksp.load();

      String alias = config.getProperty(Config.KEYSTORE_SINGLE_X509_ALIAS);
      if (!ksp.containsAlias(alias)) {
        fail(alias + " doesn't exist in the specified keystore!");
      }
    }
    catch (Exception e) {
      logR.error(e.getMessage(), e);
      fail(e.getMessage());
    }
  }

  @Test
  public void testKeystoreGetCertificate() {
    try {
      KeystoreProxy ksp = KeystoreFactory.getDefault().create(kloc);
      ksp.load();
      String alias = config.getProperty(Config.KEYSTORE_SINGLE_X509_ALIAS);
      if (ksp.containsAlias(alias)) {
        Certificate thisCert = ksp.getCertificate(alias);
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
  public void testKeystoreGetCertificateChain() {
    try {
      KeystoreProxy ksp = KeystoreFactory.getDefault().create(kloc);
      ksp.load();
      String alias = config.getProperty(Config.KEYSTORE_SINGLE_X509_ALIAS);
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

  @Test
  public void testKeystoreGetPrivateKey() {
    try {
      KeystoreProxy ksp = KeystoreFactory.getDefault().create(kloc);
      ksp.load();
      String alias = config.getProperty(Config.KEYSTORE_SINGLE_X509_ALIAS);
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
