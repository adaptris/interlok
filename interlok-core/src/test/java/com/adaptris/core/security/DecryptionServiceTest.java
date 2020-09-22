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

import static com.adaptris.core.security.EncryptionServiceCase.applyConfigForSamples;
import static com.adaptris.core.security.JunitSecurityHelper.SECURITY_ALIAS;
import static com.adaptris.core.security.JunitSecurityHelper.SECURITY_PASSWORD;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import org.junit.Test;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.core.CoreConstants;
import com.adaptris.security.exc.AdaptrisSecurityException;
import com.adaptris.security.keystore.ConfiguredUrl;

public class DecryptionServiceTest extends SecurityServiceCase {
  @Test
  public void testFailedDecryption() throws Exception {
    String url = createKeystore();
    EncryptionSigningService input = new EncryptionSigningService();
    applyConfigForTests(input, url);
    DecryptionService output = new DecryptionService();
    applyConfigForTests(output, url);
    output.setRemotePartner(NON_EXISTENT_ALIAS);
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(EXAMPLE_MSG);
    execute(input, msg);
    try {
      execute(output, msg);
      fail("Expected a failure");
    }
    catch (Exception e) {
      // expected
    }
  }

  @Test
  public void testFailedDecryptionWithBranch() throws Exception {
    String url = createKeystore();
    EncryptionSigningService input = new EncryptionSigningService();
    applyConfigForTests(input, url);
    DecryptionService output = new DecryptionService();
    applyConfigForTests(output, url);
    output.setFailId(FAIL);
    output.setSuccessId(SUCCESS);
    output.setLocalPartner(NON_EXISTENT_ALIAS);

    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(EXAMPLE_MSG);
    execute(input, msg);
    execute(output, msg);
    assertEquals(FAIL, msg.getNextServiceId());
    assertTrue(msg.getObjectHeaders().containsKey(CoreConstants.OBJ_METADATA_EXCEPTION));
    assertTrue(msg.getObjectHeaders().get(CoreConstants.OBJ_METADATA_EXCEPTION) instanceof AdaptrisSecurityException);

  }

  @Test
  public void testSuccessfulDecryptionWithBranch() throws Exception {
    String url = createKeystore();
    EncryptionSigningService input = new EncryptionSigningService();
    applyConfigForTests(input, url);

    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(EXAMPLE_MSG);
    execute(input, msg);
    DecryptionService output = new DecryptionService();
    applyConfigForTests(output, url);
    output.setFailId(FAIL);
    output.setSuccessId(SUCCESS);
    execute(output, msg);
    assertEquals("Payload equality", EXAMPLE_MSG, msg.getContent());
    assertEquals(SUCCESS, msg.getNextServiceId());
    assertTrue(!msg.getObjectHeaders().containsKey(CoreConstants.OBJ_METADATA_EXCEPTION));

  }

  protected void applyConfigForTests(CoreSecurityService service, String url) {
    service.addKeystoreUrl(new ConfiguredUrl(url, PROPERTIES.getProperty(SECURITY_PASSWORD)));

    ConfiguredPrivateKeyPasswordProvider configured = new ConfiguredPrivateKeyPasswordProvider(
        PROPERTIES.getProperty(SECURITY_PASSWORD));
    service.setPrivateKeyPasswordProvider(configured);
    service.setLocalPartner(PROPERTIES.getProperty(SECURITY_ALIAS));
    service.setRemotePartner(PROPERTIES.getProperty(SECURITY_ALIAS));
  }

  /**
   * @see com.adaptris.core.ExampleConfigCase#retrieveObjectForSampleConfig()
   */
  @Override
  protected Object retrieveObjectForSampleConfig() {
    DecryptionService service = new DecryptionService();
    applyConfigForSamples(service);
    return service;
  }

  @Override
  protected CoreSecurityService create() {
    return new DecryptionService();
  }
}
