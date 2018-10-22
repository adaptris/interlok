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

import java.util.Properties;
import java.util.Random;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.adaptris.security.keystore.Alias;
import com.adaptris.security.keystore.ConfiguredUrl;
import com.adaptris.security.keystore.KeystoreLocation;
import com.adaptris.security.keystore.KeystoreProxy;
import com.adaptris.security.util.Constants;

/**
 * Test the default security service.
 * 
 */
public class TestDefaultSecurityService {
  private KeystoreProxy ksm = null;
  private KeystoreLocation ksi = null;
  private Properties config;
  private static Logger logR = null;
  private SecurityService service = null;
  private Alias us;
  private Alias them;

  private static final String RAW_DATA = "The quick brown fox " + "jumps over the lazy dog";

  public TestDefaultSecurityService() {
    if (logR == null) {
      logR = LoggerFactory.getLogger(TestDefaultSecurityService.class);
    }
  }

  @Before
  public void setUp() throws Exception {
    config = Config.getInstance().getProperties();
    if (config == null) {
      fail("No Configuration(!) available");
    }
    ConfiguredUrl configuredKeystore = new ConfiguredUrl();
    configuredKeystore.setUrl(config.getProperty(Config.KEYSTORE_TEST_URL) + "&" + Constants.KEYSTORE_PASSWORD + "="
        + config.getProperty(Config.KEYSTORE_COMMON_KEYSTORE_PW));
    Config.getInstance().buildKeystore(config.getProperty(Config.KEYSTORE_TEST_URL), null, true);
    service = SecurityServiceFactory.defaultInstance().createService();
    service.registerKeystore(configuredKeystore);
    EncryptionAlgorithm alg = new EncryptionAlgorithm(config.getProperty(Config.SECURITY_ALG),
        config.getProperty(Config.SECURITY_ALGSIZE));
    service.setEncryptionAlgorithm(alg);

    us = new Alias(config.getProperty(Config.KEYSTORE_COMMON_PRIVKEY_ALIAS), config.getProperty(Config.KEYSTORE_COMMON_PRIVKEY_PW));

    them = new Alias(config.getProperty(Config.KEYSTORE_COMMON_PRIVKEY_ALIAS));
  }

  @After
  public void tearDown() throws Exception {
  }

  @Test
  public void testDefaultEncryption() throws Exception {
    Output output = service.encrypt(RAW_DATA, us, them);
    String payload = output.getAsString();
    assertNotNull(payload);
  }

  @Test
  public void testDefaultDecryption() throws Exception {
    Output output = service.encrypt(RAW_DATA, us, them);
    String enc = output.getAsString();
    output = service.verify(enc, us, them);
    String dec = output.getAsString();
    assertEquals("Data Verification", dec, RAW_DATA);

  }

  @Test
  public void testDefaultSign() throws Exception {
    Output output = service.sign(RAW_DATA, us);
    String payload = output.getAsString();
    assertNotNull(payload);
  }

  @Test
  public void testDefaultVerifyAfterSign() throws Exception {
    Output output = service.sign(RAW_DATA, us);
    String enc = output.getAsString();
    output = service.verify(enc, us, them);
    String dec = output.getAsString();
    assertEquals("Data Verification", dec, RAW_DATA);
  }

  @Test
  public void testDefaultEncryptionAndSignature() throws Exception {
    Output output = service.encrypt(RAW_DATA, us, them);
    output = service.sign(RAW_DATA, us, output);
    String payload = output.getAsString();
    assertNotNull(payload);
  }

  @Test
  public void testDefaultDecryptionAndSignatureVerify() throws Exception {
    Output output = service.encrypt(RAW_DATA, us, them);
    output = service.sign(RAW_DATA, us, output);
    String enc = output.getAsString();
    output = service.verify(enc, us, them);
    String payload = output.getAsString();
    assertEquals("Data Verification", payload, RAW_DATA);
  }

  @Test
  public void testDefaultEncryptionWithMultipleKeystores() throws Exception {
    ConfiguredUrl keystore = new ConfiguredUrl();
    String filename = "" + Math.abs(new Random().nextInt()) + ".ks";
    String newUrl = "file:///" + config.getProperty(Config.CFG_ROOT) + "/" + filename + "?keystoreType=JKS";
    keystore.setUrl(newUrl + "&" + Constants.KEYSTORE_PASSWORD + "=" + config.getProperty(Config.KEYSTORE_COMMON_KEYSTORE_PW));
    String cn = "OtherUniqueAlias";
    Config.getInstance().buildKeystore(newUrl, cn, false);
    them = new Alias(cn, config.getProperty(Config.KEYSTORE_COMMON_PRIVKEY_PW));
    service.registerKeystore(keystore);
    Output output = service.encrypt(RAW_DATA, us, them);
    output = service.sign(RAW_DATA, us, output);
    String encrypted = output.getAsString();
    output = service.verify(encrypted, them, us);
    String payload = output.getAsString();

    assertEquals("Data Verification", payload, RAW_DATA);
  }

}
