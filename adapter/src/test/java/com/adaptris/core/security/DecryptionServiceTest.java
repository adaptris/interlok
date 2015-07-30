package com.adaptris.core.security;

import static com.adaptris.core.security.EncryptionServiceCase.applyConfigForSamples;
import static com.adaptris.core.security.JunitSecurityHelper.SECURITY_ALIAS;
import static com.adaptris.core.security.JunitSecurityHelper.SECURITY_PASSWORD;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.core.CoreConstants;
import com.adaptris.security.exc.AdaptrisSecurityException;
import com.adaptris.security.keystore.ConfiguredUrl;

public class DecryptionServiceTest extends SecurityServiceCase {

  public DecryptionServiceTest(String name) {
    super(name);
  }

  @Override
  protected void setUp() throws Exception {
    super.setUp();
  }

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
    assertTrue(msg.getObjectMetadata().containsKey(CoreConstants.OBJ_METADATA_EXCEPTION));
    assertTrue(msg.getObjectMetadata().get(CoreConstants.OBJ_METADATA_EXCEPTION) instanceof AdaptrisSecurityException);

  }

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
    assertEquals("Payload equality", EXAMPLE_MSG, msg.getStringPayload());
    assertEquals(SUCCESS, msg.getNextServiceId());
    assertTrue(!msg.getObjectMetadata().containsKey(CoreConstants.OBJ_METADATA_EXCEPTION));

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
