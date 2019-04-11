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
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import java.io.File;
import java.io.InputStream;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Random;
import org.junit.Before;
import org.junit.Test;
import com.adaptris.security.exc.KeystoreException;
import com.adaptris.security.keystore.CompositeKeystore;
import com.adaptris.security.keystore.KeystoreFactory;
import com.adaptris.security.keystore.KeystoreLocation;

/**
 * Test Composite Keystore Functionality.
 * 
 */
public class TestCompositeKeystore {
  private Config config;
  private List<KeystoreLocation> keystoreLocationList = new ArrayList<>();;

  public TestCompositeKeystore() {
  }

  @Before
  public void setUp() throws Exception {
    config = Config.getInstance();
    keystoreLocationList.add(config.newKeystore(null));
    Properties p = config.getPropertySubset(Config.KEYSTORE_COMPOSITE_URLROOT);
    for (Iterator i = p.keySet().iterator(); i.hasNext();) {
      String key = (String) i.next();
      keystoreLocationList.add(KeystoreFactory.getDefault().create(
          config.getProperty(key),
          config.getProperty(Config.KEYSTORE_COMMON_KEYSTORE_PW).toCharArray()));
    }
  }


  @Test
  public void testKeystoreSize() throws Exception {
    CompositeKeystore composite = new CompositeKeystore(keystoreLocationList);
    assertEquals("Composite Keystore size", 3, composite.size());
  }

  /**
   * Get a certificate out of the keystore.
   */
  @Test
  public void testContainsAlias() throws Exception {
    CompositeKeystore composite = new CompositeKeystore(keystoreLocationList);
    String alias = config.getProperty(Config.KEYSTORE_COMMON_PRIVKEY_ALIAS);
    if (!composite.containsAlias(alias)) {
      fail(alias + " doesn't exist in the specified keystore!");
    }
  }

  /**
   * Get a certificate out of the keystore.
   */
  @Test
  public void testContainsNonExistentAlias() throws Exception {
    CompositeKeystore composite = new CompositeKeystore(keystoreLocationList);
    String alias = String.valueOf(new Random().nextInt());
    if (composite.containsAlias(alias)) {
      fail(alias + " exists in the specified keystore!");
    }
  }

  @Test
  public void testKeystoreGetCertificate() throws Exception {
    CompositeKeystore composite = new CompositeKeystore(keystoreLocationList);
    String alias = config.getProperty(Config.KEYSTORE_COMMON_PRIVKEY_ALIAS);
    if (composite.containsAlias(alias)) {
      Certificate thisCert = composite.getCertificate(alias);
      assertNotNull(thisCert);
    }
    else {
      fail(alias + " does not exist in the specified keystore");
    }
  }

  @Test
  public void testKeystoreGetCertificateChain() throws Exception {
    CompositeKeystore composite = new CompositeKeystore(keystoreLocationList);
    String alias = config.getProperty(Config.KEYSTORE_COMMON_PRIVKEY_ALIAS);
    if (composite.containsAlias(alias)) {
      Certificate[] thisCert = composite.getCertificateChain(alias);
      assertTrue(thisCert.length > 0);
    }
    else {
      fail(alias + " does not exist in the specified keystore");
    }
  }

  @Test
  public void testKeystoreGetPrivateKey() throws Exception {
    CompositeKeystore composite = new CompositeKeystore(keystoreLocationList);
    String alias = config.getProperty(Config.KEYSTORE_SINGLE_PKCS12_ALIAS);
    if (composite.containsAlias(alias)) {
      PrivateKey pk = composite.getPrivateKey(alias,
          config.getProperty(Config.KEYSTORE_COMMON_KEYSTORE_PW).toCharArray());
      assertNotNull(pk);
    }
    else {
      fail(alias + " does not exit in keystore list");
    }
  }

  @Test
  public void testKeystoreGetKeyStore() throws Exception {
    CompositeKeystore composite = new CompositeKeystore(keystoreLocationList);
    assertNull("Keystore should be null", composite.getKeystore());
  }

  @Test
  public void testKeystoreGetPrivateKeyNoPassword() throws Exception {
    CompositeKeystore composite = new CompositeKeystore(keystoreLocationList);
    String alias = config.getProperty(Config.KEYSTORE_COMMON_PRIVKEY_ALIAS);
    if (composite.containsAlias(alias)) {
      PrivateKey pk = composite.getPrivateKey(alias, null);
      assertNotNull(pk);
    }
    else {
      fail(alias + " does not exist in keystore list");
    }
  }

  @Test
  public void testKeystoreAliasCaseBug890() throws Exception {
    CompositeKeystore composite = new CompositeKeystore();
    String x509Alias = config
        .getProperty(Config.KEYSTORE_SINGLE_X509_ALIAS_UPPERCASE);
    String x509KeyInfoAlias = config
        .getProperty(Config.KEYSTORE_SINGLE_XML_KEY_INFO_ALIAS_UPPERCASE);
    composite.addKeystore(KeystoreFactory.getDefault().create(
        config.getProperty(Config.KEYSTORE_SINGLE_X509_URL_UPPERCASE), null));
    composite.addKeystore(KeystoreFactory.getDefault().create(
        config.getProperty(Config.KEYSTORE_SINGLE_XML_KEY_INFO_URL_UPPERCASE),
        null));
    assertTrue(composite.containsAlias(x509Alias));
    assertTrue(composite.containsAlias(x509KeyInfoAlias));
    assertNotNull(composite.getCertificate(x509Alias));
    assertNotNull(composite.getCertificate(x509KeyInfoAlias));
  }

  @Test
  public void testImportCertificate() throws Exception {
    CompositeKeystore ksp = new CompositeKeystore(keystoreLocationList);
    try {
      ksp.setCertificate("", "");
      fail("Import successful");
    }
    catch (Exception e) {
      assertEquals(KeystoreException.class, e.getClass());
    }
    try {
      ksp.setCertificate("", (Certificate) null);
      fail("Import successful");
    }
    catch (Exception e) {
      assertEquals(KeystoreException.class, e.getClass());
    }
    try {
      ksp.setCertificate("", (InputStream) null);
      fail("Import successful");
    }
    catch (Exception e) {
      assertEquals(KeystoreException.class, e.getClass());
    }
    try {
      ksp.setCertificate("", (File) null);
      fail("Import successful");
    }
    catch (Exception e) {
      assertEquals(KeystoreException.class, e.getClass());
    }
  }

  @Test
  public void testImportCertificateChain() throws Exception {
    CompositeKeystore ksp = new CompositeKeystore(keystoreLocationList);
    ksp.load();
    try {
      ksp.importCertificateChain("", "".toCharArray(), "");
      fail("Import successful");
    }
    catch (Exception e) {
      assertEquals(KeystoreException.class, e.getClass());
    }
    try {
      ksp.importCertificateChain("", "".toCharArray(), (InputStream) null);
      fail("Import successful");
    }
    catch (Exception e) {
      assertEquals(KeystoreException.class, e.getClass());
    }
    try {
      ksp.importCertificateChain("", "".toCharArray(), (File) null);
      fail("Import successful");
    }
    catch (Exception e) {
      assertEquals(KeystoreException.class, e.getClass());
    }
  }

  @Test
  public void testImportPrivateKey() throws Exception {
    CompositeKeystore ksp = new CompositeKeystore(keystoreLocationList);
    ksp.load();
    try {
      ksp.importPrivateKey("", "".toCharArray(), "", "".toCharArray());
      fail("Import successful");
    }
    catch (Exception e) {
      assertEquals(KeystoreException.class, e.getClass());
    }
    try {
      ksp.importPrivateKey("", "".toCharArray(), (InputStream) null, ""
          .toCharArray());
      fail("Import successful");
    }
    catch (Exception e) {
      assertEquals(KeystoreException.class, e.getClass());
    }
    try {
      ksp.importPrivateKey("", "".toCharArray(), (File) null, "".toCharArray());
      fail("Import successful");
    }
    catch (Exception e) {
      assertEquals(KeystoreException.class, e.getClass());
    }

  }
}
