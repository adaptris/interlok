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
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.fail;
import java.util.Properties;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.adaptris.security.keystore.ConfiguredUrl;
import com.adaptris.security.util.Constants;
import junit.framework.TestCase;

/**
 * Test Inline Keystore Functionality wrapping a single KEYSTORE_X509 certificate
 */
public class TestConfiguredUrl {

  private Properties cfg;
  private ConfiguredUrl url, copy;
  private Logger logR = LoggerFactory.getLogger(this.getClass());

  public TestConfiguredUrl() {

  }

  /**
   * @see TestCase#setUp()
   */
  @Before
  public void setUp() throws Exception {
    Config config = Config.getInstance();
    cfg = config.getProperties();

    if (cfg == null) {
      fail("No Configuration(!) available");
    }
    url = new ConfiguredUrl();
    url.setUrl(cfg.getProperty(Config.KEYSTORE_TEST_URL) + "&"
        + Constants.KEYSTORE_PASSWORD + "="
        + cfg.getProperty(Config.KEYSTORE_COMMON_KEYSTORE_PW));
    copy = new ConfiguredUrl(cfg.getProperty(Config.KEYSTORE_TEST_URL) + "&" + Constants.KEYSTORE_PASSWORD + "="
        + cfg.getProperty(Config.KEYSTORE_COMMON_KEYSTORE_PW));

    config.buildKeystore(cfg.getProperty(Config.KEYSTORE_TEST_URL), null, true);
  }

  @Test
  public void testAsKeystoreLocation() throws Exception {
    assertNotNull(url.asKeystoreLocation());
    assertEquals(url.asKeystoreLocation(), copy.asKeystoreLocation());
    assertEquals(url.asKeystoreLocation().hashCode(), copy.asKeystoreLocation().hashCode());
  }

  @Test
  public void testAsKeystoreProxy() throws Exception {
    assertNotNull(url.asKeystoreProxy());
  }

  @Test
  public void testEquality() throws Exception {
    assertNotSame(url, new Object());
    assertEquals(url, copy);
    assertEquals(url.hashCode(), copy.hashCode());
    assertEquals(0, new ConfiguredUrl().hashCode());
    assertNotNull(url.toString());
  }

}
