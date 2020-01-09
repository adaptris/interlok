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

package com.adaptris.core.security;

import static com.adaptris.core.security.JunitSecurityHelper.KEYSTORE_PATH;
import static com.adaptris.core.security.JunitSecurityHelper.KEYSTORE_TYPE;
import static com.adaptris.core.security.JunitSecurityHelper.KEYSTORE_URL;
import static com.adaptris.core.security.JunitSecurityHelper.SECURITY_ALIAS;
import static com.adaptris.core.security.JunitSecurityHelper.SECURITY_PASSWORD;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import java.io.File;
import java.util.ArrayList;
import org.junit.Test;
import com.adaptris.core.CoreException;
import com.adaptris.core.util.LifecycleHelper;
import com.adaptris.security.EncryptionAlgorithm;
import com.adaptris.security.exc.PasswordException;
import com.adaptris.security.keystore.ConfiguredKeystore;
import com.adaptris.security.keystore.ConfiguredUrl;
import com.adaptris.util.GuidGenerator;

public abstract class SecurityServiceCase extends SecurityServiceExample {
  protected static final String NON_EXISTENT_ALIAS = "non-existent-alias";
  protected static final String SUCCESS = "SUCCESS";
  protected static final String FAIL = "FAIL";
  protected static final String EXAMPLE_MSG = "The quick brown fox jumps over the lazy dog.";

  protected abstract CoreSecurityService create();

  @Test
  public void testSetEncryptionAlgorithm() throws Exception {
    CoreSecurityService input = create();
    assertNotNull(input.getEncryptionAlgorithm());
    assertEquals("DESede/CBC/PKCS5Padding", input.getEncryptionAlgorithm().getAlgorithm());
    EncryptionAlgorithm newAlg = new EncryptionAlgorithm("AES/CBC/PKCS5Padding", 128);
    input.setEncryptionAlgorithm(newAlg);
    assertEquals(newAlg, input.getEncryptionAlgorithm());
    try {
      input.setEncryptionAlgorithm(null);
      fail();
    }
    catch (IllegalArgumentException expected) {

    }
    assertEquals(newAlg, input.getEncryptionAlgorithm());
  }

  @Test
  public void testSetKeystoreUrls() throws Exception {
    CoreSecurityService input = create();
    ConfiguredUrl url = new ConfiguredUrl(PROPERTIES.getProperty(KEYSTORE_URL), PROPERTIES.getProperty(SECURITY_PASSWORD));
    input.addKeystoreUrl(url);
    assertEquals(1, input.getKeystoreUrls().size());
    assertTrue(input.getKeystoreUrls().contains(url));
    try {
      input.setKeystoreUrls(null);
      fail();
    }
    catch (IllegalArgumentException expected) {

    }
    ArrayList<ConfiguredKeystore> newList = new ArrayList<ConfiguredKeystore>();
    input.setKeystoreUrls(newList);
    assertEquals(0, input.getKeystoreUrls().size());
    assertEquals(newList, input.getKeystoreUrls());
  }

  @Test
  public void testInit_FailedToGetPrivateKey() throws Exception {
    CoreSecurityService input = create();
    input.setPrivateKeyPasswordProvider(new FailingPrivateKeyPasswordProvider());
    input.addKeystoreUrl(new ConfiguredUrl(PROPERTIES.getProperty(KEYSTORE_URL), PROPERTIES.getProperty(SECURITY_PASSWORD)));
    input.setLocalPartner(PROPERTIES.getProperty(SECURITY_ALIAS));
    input.setRemotePartner(PROPERTIES.getProperty(SECURITY_ALIAS));
    try {
      LifecycleHelper.init(input);
      fail();
    }
    catch (CoreException expected) {

    }
  }

  @Test
  public void testInit_NoLocalPartner() throws Exception {
    CoreSecurityService input = create();
    input.setPrivateKeyPasswordProvider(new ConfiguredPrivateKeyPasswordProvider(PROPERTIES.getProperty(SECURITY_PASSWORD)));
    try {
      LifecycleHelper.init(input);
      fail();
    }
    catch (CoreException expected) {

    }
    finally {
      LifecycleHelper.close(input);

    }
  }

  @Test
  public void testInit_NoRemotePartner() throws Exception {
    CoreSecurityService input = create();
    String url = createKeystore();
    input.addKeystoreUrl(new ConfiguredUrl(url, PROPERTIES.getProperty(SECURITY_PASSWORD)));
    input.setPrivateKeyPasswordProvider(new ConfiguredPrivateKeyPasswordProvider(PROPERTIES.getProperty(SECURITY_PASSWORD)));
    input.setLocalPartner(PROPERTIES.getProperty(SECURITY_ALIAS));
    try {
      LifecycleHelper.init(input);
    }
    finally {
      LifecycleHelper.close(input);
    }
  }

  @Test
  public void testInit_InvalidUrl() throws Exception {
    CoreSecurityService input = create();
    input.addKeystoreUrl(new ConfiguredUrl(createDummyKeystoreUrl(), PROPERTIES.getProperty(SECURITY_PASSWORD)));
    input.setPrivateKeyPasswordProvider(new ConfiguredPrivateKeyPasswordProvider(PROPERTIES.getProperty(SECURITY_PASSWORD)));
    input.setLocalPartner(PROPERTIES.getProperty(SECURITY_ALIAS));
    input.setRemotePartner(PROPERTIES.getProperty(SECURITY_ALIAS));
    try {
      LifecycleHelper.init(input);
      fail();
    }
    catch (CoreException expected) {

    }
    finally {
      LifecycleHelper.close(input);

    }
  }

  @Test
  public void testInit_Branching() throws Exception {
    CoreSecurityService input = create();
    String url = createKeystore();
    input.addKeystoreUrl(new ConfiguredUrl(url, PROPERTIES.getProperty(SECURITY_PASSWORD)));
    input.setPrivateKeyPasswordProvider(new ConfiguredPrivateKeyPasswordProvider(PROPERTIES.getProperty(SECURITY_PASSWORD)));
    input.setLocalPartner(PROPERTIES.getProperty(SECURITY_ALIAS));
    input.setRemotePartner(PROPERTIES.getProperty(SECURITY_ALIAS));
    try {
      // By default branching is false.
      LifecycleHelper.init(input);
      assertFalse(input.isBranching());
      LifecycleHelper.close(input);

      // If only one ID is set then it's false as well.
      input.setFailId(FAIL);
      LifecycleHelper.init(input);
      assertFalse(input.isBranching());
      LifecycleHelper.close(input);
      input.setFailId(null);
      input.setSuccessId(SUCCESS);
      LifecycleHelper.init(input);
      assertFalse(input.isBranching());
      LifecycleHelper.close(input);

      // Only if both are set is it branching.
      input.setSuccessId(SUCCESS);
      input.setFailId(FAIL);
      LifecycleHelper.init(input);
      assertTrue(input.isBranching());
    }
    finally {
      LifecycleHelper.close(input);

    }

  }

  protected String createKeystore() throws Exception {
    return new JunitSecurityHelper(PROPERTIES).newKeystore();
  }

  private String createDummyKeystoreUrl() throws Exception {
    File defaultKeystore = new File(PROPERTIES.getProperty(KEYSTORE_PATH));
    File keystoreDir = defaultKeystore.getParentFile();
    String ksType = PROPERTIES.getProperty(KEYSTORE_TYPE);
    String ksFilename = new GuidGenerator().safeUUID();
    String ksUrl = "file:///" + keystoreDir.getCanonicalPath() + "/" + ksFilename + "?keystoreType=" + ksType;
    return ksUrl;
  }

  protected static class FailingPrivateKeyPasswordProvider implements PrivateKeyPasswordProvider {

    @Override
    public char[] retrievePrivateKeyPassword() throws PasswordException {
      throw new PasswordException("Expected Exception");
    }

  }
}
