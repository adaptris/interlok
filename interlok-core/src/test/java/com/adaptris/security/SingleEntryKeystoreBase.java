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
import static org.junit.Assert.fail;
import java.io.File;
import java.io.InputStream;
import java.security.cert.Certificate;
import java.util.Random;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.adaptris.security.exc.KeystoreException;
import com.adaptris.security.keystore.KeystoreFactory;
import com.adaptris.security.keystore.KeystoreLocation;
import com.adaptris.security.keystore.KeystoreProxy;

/**
 * Test Keystore Functionality wrapping a single KEYSTORE_PKCS12 certificate
 * 
 */
public abstract class SingleEntryKeystoreBase {
  protected KeystoreLocation kloc = null;
  protected Config config;
  protected transient Logger logR = null;

  public SingleEntryKeystoreBase() {
    logR = LoggerFactory.getLogger(this.getClass());
  }

  protected abstract void setUp() throws Exception;

  @Test
  public void testContainsNonExistentAlias() throws Exception {
    KeystoreProxy ksp = KeystoreFactory.getDefault().create(kloc);
    ksp.load();

    String alias = String.valueOf(new Random().nextInt());
    if (ksp.containsAlias(alias)) {
      fail(alias + " exists in the specified keystore!");
    }
  }

  @Test
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

  @Test
  public void testImportCertificate() throws Exception {
    KeystoreProxy ksp = KeystoreFactory.getDefault().create(kloc);
    ksp.load();
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
    KeystoreProxy ksp = KeystoreFactory.getDefault().create(kloc);
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
    KeystoreProxy ksp = KeystoreFactory.getDefault().create(kloc);
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
