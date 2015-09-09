package com.adaptris.core.security;

import static com.adaptris.core.security.JunitSecurityHelper.SECURITY_ALIAS;
import static com.adaptris.core.security.JunitSecurityHelper.SECURITY_PASSWORD;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.core.CoreConstants;
import com.adaptris.core.ServiceException;
import com.adaptris.security.EncryptionAlgorithm;
import com.adaptris.security.exc.AdaptrisSecurityException;
import com.adaptris.security.keystore.ConfiguredUrl;
import com.adaptris.security.keystore.InlineKeystore;
import com.adaptris.security.password.Password;
import com.adaptris.security.util.Constants;

/**
 * <p>
 * This class exists to create sample XML config only, unit tests are in top-level security project.
 * </p>
 */
public abstract class EncryptionServiceCase extends SecurityServiceCase {

  protected static final String EXAMPLE_KEYINFO = "<KeyInfo xmlns=\"http://www.w3.org/2000/09/xmldsig#\""
      + "xmlns:tns=\"http://www.oasis-open.org/committees/ebxml-cppa/schema/cpp-cpa-2_0.xsd\">"
      + "<KeyName>message_signing_cert-Key</KeyName>" + "<X509Data>" + "<X509Certificate>"
      + "MIIElDCCA3ygAwIBAgIBADANBgkqhkiG9w0BAQQFADCBkjELMAkGA1UEBhMCVVMx"
      + "CzAJBgNVBAgTAk1BMRAwDgYDVQQHEwdCZWRmb3JkMRcwFQYDVQQKEw5Tb25pYyBT"
      + "b2Z0d2FyZTEMMAoGA1UECxMDRGV2MRMwEQYDVQQDEwpKb2UgU2FtcGxlMSgwJgYJ"
      + "KoZIhvcNAQkBFhlqc2FtcGxlQHNvbmljc29mdHdhcmUuY29tMB4XDTA0MTEwMzIz"
      + "MzUwNFoXDTA4MTEwMjIzMzUwNFowgZIxCzAJBgNVBAYTAlVTMQswCQYDVQQIEwJN"
      + "QTEQMA4GA1UEBxMHQmVkZm9yZDEXMBUGA1UEChMOU29uaWMgU29mdHdhcmUxDDAK"
      + "BgNVBAsTA0RldjETMBEGA1UEAxMKSm9lIFNhbXBsZTEoMCYGCSqGSIb3DQEJARYZ"
      + "anNhbXBsZUBzb25pY3NvZnR3YXJlLmNvbTCCASIwDQYJKoZIhvcNAQEBBQADggEP"
      + "ADCCAQoCggEBANg0MJNWN1sxCfLbSGi+WVGBbqEVSExb50VEF4jxgdciJz8By3hb"
      + "6naYdHoWcJl6GyGMEFROZGdOjBGqxcF3DD/bzHLB2Ge60UkJgEFP8eMYOLVYdjb/"
      + "sAvXic+VKmnBp+DDnqeyozTp39/qxqhcP8VGiBHT97G9chTP6ZCLD2tK+t6zvJ+3"
      + "1NexQj0P4OUw4I+uuuBaYmPGqFrpySaHA6BpJ5iY3CB28wqxzIt2xjz/MjqnHWWS"
      + "YwNyjGs0O8+WO7FrQbW9WBGDQrEzANuB6l4O6GRVoMOKnuD407d2DOpIQBmg4m7T"
      + "nTNVol/0lj6XC4leD2cSJg8EIU5pPwBnajUCAwEAAaOB8jCB7zAdBgNVHQ4EFgQU"
      + "vtrk4IPUOLJyQXEA9aysK7l/Ce4wgb8GA1UdIwSBtzCBtIAUvtrk4IPUOLJyQXEA"
      + "9aysK7l/Ce6hgZikgZUwgZIxCzAJBgNVBAYTAlVTMQswCQYDVQQIEwJNQTEQMA4G"
      + "A1UEBxMHQmVkZm9yZDEXMBUGA1UEChMOU29uaWMgU29mdHdhcmUxDDAKBgNVBAsT"
      + "A0RldjETMBEGA1UEAxMKSm9lIFNhbXBsZTEoMCYGCSqGSIb3DQEJARYZanNhbXBs"
      + "ZUBzb25pY3NvZnR3YXJlLmNvbYIBADAMBgNVHRMEBTADAQH/MA0GCSqGSIb3DQEB"
      + "BAUAA4IBAQB7OjBSc18axpRbqOvLq5dO5t8TJtEoRFkopvbAxlSV4lt/c6IptfbO"
      + "a4iRuBzjXufW+AgOV+eRtBaatcChvc5QI7TZuPp4k57bOG4GJCafEWo89SmVsWy/"
      + "9XJU+fmuaTt72i3EEMOsiUWJJqVdcNxAdC2cGSKV1wP4nwhFI+WzdtpJKImEl9LY"
      + "GDiqn9b96Oz2eH8FPA4qzNNkoA/36s/iPl1Zn3VF7mzR4jHe9aKUg/XvNWXpKAvd"
      + "tItpX25JP3RhnzFrwriwUyFshUaF+J05O+6P2WilvUsX7+Q1prU7POnyizhdlvlt" + "6c+G3SjCAdM/oKJB1LSVLuxUzx0vTs2S"
      + "</X509Certificate>" + "</X509Data>" + "</KeyInfo>";

  public EncryptionServiceCase(String name) {
    super(name);
  }

  @Override
  protected void setUp() throws Exception {
    super.setUp();
  }

  public void testEncryptionDecryptionWithLegacyProvider() throws Exception {
    String url = createKeystore();
    CoreSecurityService input = create();
    applyConfigForTests(input, url);
    configureForLegacyProvider(input);
    DecryptionService output = new DecryptionService();
    applyConfigForTests(output, url);
    configureForLegacyProvider(output);
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(EXAMPLE_MSG);
    execute(input, msg);
    execute(output, msg);
    assertEquals("Payload equality", EXAMPLE_MSG, msg.getStringPayload());
  }

  public void testEncryptionDecryptionWithConfiguredProvider() throws Exception {
    String url = createKeystore();
    CoreSecurityService input = create();
    applyConfigForTests(input, url);
    configureForConfiguredPrivateKey(input, PROPERTIES.getProperty(SECURITY_PASSWORD));
    DecryptionService output = new DecryptionService();
    applyConfigForTests(output, url);
    configureForConfiguredPrivateKey(output, PROPERTIES.getProperty(SECURITY_PASSWORD));
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(EXAMPLE_MSG);
    execute(input, msg);
    execute(output, msg);
    assertEquals("Payload equality", EXAMPLE_MSG, msg.getStringPayload());
  }

  public void testEncryptionDecryptionWithMissingPrivateKeyPassword() throws Exception {
    String url = createKeystore();
    CoreSecurityService input = create();
    applyConfigForTests(input, url);
    configureForLegacyProvider(input);
    DecryptionService output = new DecryptionService();
    applyConfigForTests(output, url);
    configureForLegacyProvider(output);
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(EXAMPLE_MSG);
    execute(input, msg);
    execute(output, msg);
    assertEquals("Payload equality", EXAMPLE_MSG, msg.getStringPayload());
  }

  public void testEncryptionDecryptionWithEncryptedPassword() throws Exception {
    String url = createKeystore();
    CoreSecurityService input = create();
    applyConfigForTests(input,
        new ConfiguredUrl(url, Password.encode(PROPERTIES.getProperty(SECURITY_PASSWORD), Password.PORTABLE_PASSWORD)));

    DecryptionService output = new DecryptionService();
    applyConfigForTests(output,
        new ConfiguredUrl(url, Password.encode(PROPERTIES.getProperty(SECURITY_PASSWORD), Password.NON_PORTABLE_PASSWORD)));
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(EXAMPLE_MSG);
    execute(input, msg);
    execute(output, msg);
    assertEquals("Payload equality", EXAMPLE_MSG, msg.getStringPayload());
  }

  public void testFailedEncryption() throws Exception {
    String url = createKeystore();
    CoreSecurityService input = create();
    applyConfigForTests(input, url);
    input.setLocalPartner(NON_EXISTENT_ALIAS);
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(EXAMPLE_MSG);
    try {
      execute(input, msg);
      fail("Expected a failure");
    }
    catch (ServiceException e) {
      ;
    }
  }

  public void testFailedEncryptionWithBranch() throws Exception {
    String url = createKeystore();
    CoreSecurityService input = create();
    applyConfigForTests(input, url);
    input.setFailId(FAIL);
    input.setSuccessId(SUCCESS);
    input.setLocalPartner(NON_EXISTENT_ALIAS);
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(EXAMPLE_MSG);
    execute(input, msg);
    assertEquals(FAIL, msg.getNextServiceId());
    assertTrue(msg.getObjectMetadata().containsKey(CoreConstants.OBJ_METADATA_EXCEPTION));
    assertTrue(msg.getObjectMetadata().get(CoreConstants.OBJ_METADATA_EXCEPTION) instanceof AdaptrisSecurityException);

  }

  public void testSuccessfulEncryptionWithBranch() throws Exception {
    String url = createKeystore();
    CoreSecurityService input = create();
    applyConfigForTests(input, url);
    input.setFailId(FAIL);
    input.setSuccessId(SUCCESS);
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(EXAMPLE_MSG);
    execute(input, msg);
    assertEquals(SUCCESS, msg.getNextServiceId());
    assertTrue(!msg.getObjectMetadata().containsKey(CoreConstants.OBJ_METADATA_EXCEPTION));
    DecryptionService output = new DecryptionService();
    applyConfigForTests(output, url);
    execute(output, msg);
    assertEquals("Payload equality", EXAMPLE_MSG, msg.getStringPayload());
  }

  public void testRoundTripWithRemotePartnerFromDefaultMetadata() throws Exception {
    String url = createKeystore();
    CoreSecurityService input = create();
    applyConfigForTests(input, url);
    input.setRemotePartner(null);
    DecryptionService output = new DecryptionService();
    applyConfigForTests(output, url);
    output.setRemotePartner(null);
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(EXAMPLE_MSG);
    msg.addMetadata(CoreConstants.SECURITY_REMOTE_PARTNER, PROPERTIES.getProperty(SECURITY_ALIAS));
    execute(input, msg);
    execute(output, msg);
    assertEquals("Payload equality", EXAMPLE_MSG, msg.getStringPayload());
  }

  public void testRoundTripWithRemotePartnerFromCustomMetadata() throws Exception {
    String url = createKeystore();
    CoreSecurityService input = create();
    applyConfigForTests(input, url);
    input.setRemotePartner(null);
    input.setRemotePartnerMetadataKey("MyRemotePartner");
    DecryptionService output = new DecryptionService();
    applyConfigForTests(output, url);
    output.setRemotePartner(null);
    output.setRemotePartnerMetadataKey("MyRemotePartner");
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(EXAMPLE_MSG);
    msg.addMetadata("MyRemotePartner", PROPERTIES.getProperty(SECURITY_ALIAS));
    execute(input, msg);
    execute(output, msg);
    assertEquals("Payload equality", EXAMPLE_MSG, msg.getStringPayload());
  }

  public void testRoundTripWithBadMetadata() throws Exception {
    String url = createKeystore();
    CoreSecurityService input = create();
    applyConfigForTests(input, url);
    input.setRemotePartner(null);
    input.setRemotePartnerMetadataKey("MyRemotePartner");
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(EXAMPLE_MSG);
    msg.addMetadata(CoreConstants.SECURITY_REMOTE_PARTNER, PROPERTIES.getProperty(SECURITY_ALIAS));
    try {
      execute(input, msg);
      fail("Expected Exception from doService");
    }
    catch (ServiceException e) {
      ;
    }
  }

  protected void applyConfigForTests(CoreSecurityService service, String url) {
    applyConfigForTests(service, new ConfiguredUrl(url, PROPERTIES.getProperty(SECURITY_PASSWORD)));
  }

  protected void applyConfigForTests(CoreSecurityService service, ConfiguredUrl keystoreUrl) {
    service.addKeystoreUrl(keystoreUrl);
    service.setLocalPartner(PROPERTIES.getProperty(SECURITY_ALIAS));
    service.setRemotePartner(PROPERTIES.getProperty(SECURITY_ALIAS));
    configureForConfiguredPrivateKey(service, PROPERTIES.getProperty(SECURITY_PASSWORD));
  }

  protected void configureForLegacyProvider(CoreSecurityService service) {
    service.setPrivateKeyPasswordProvider(new LegacyPrivateKeyPasswordProvider());
  }

  protected void configureForConfiguredPrivateKey(CoreSecurityService service, String password) {
    ConfiguredPrivateKeyPasswordProvider configured = new ConfiguredPrivateKeyPasswordProvider();
    configured.setEncodedPassword(password);
    service.setPrivateKeyPasswordProvider(configured);
  }

  protected static void applyConfigForSamples(CoreSecurityService service) {
    try {
      service.setLocalPartner("local partner alias in keystore");
      service.setRemotePartner("remote partner alias in keystore");
      service.addKeystoreUrl(new ConfiguredUrl("http://localhost/path/to/JKS/keystore?keystoreType="
          + "JKS&keystorePassword=somePlainTextPassword"));
      service.addKeystoreUrl(new ConfiguredUrl("file://localhost/path/to/a/X509/Certificate?keystoreType="
          + "X509?keystoreAlias=CertificateAlias"));
      service.addKeystoreUrl(new ConfiguredUrl("file://localhost/path/to/another/X509/Certificate?keystoreType="
          + "X509?keystoreAlias=AnotherAlias"));
      service.addKeystoreUrl(new ConfiguredUrl("http://host/path/to/a/PKCS12/Keystore?keystoreType="
          + "PKCS12&keystoreAlias=PKCS12Alias",
          "PW:AAAAEF+zZCbvHeIXDx8HUslqiYwAAAAQ3wi4/BVycX+uzc5zF4F6EQAAABD0hhpr46IrKSu7XxFhEAYN"));
      // service.addKeystoreUrl(new ConfiguredUrl("http://host/path/to/keystore?keystoreType=" + "PKCS12&keystoreAlias=myalias",
      // PROPERTIES.getProperty(SECURITY_PASSWORD)));
      service.addKeystoreUrl(new ConfiguredUrl("http://host/path/to/a/JCE/keystore?keystoreType=JCEKS",
          "ALTPW:AAAAEF+zZCbvHeIXDx8HUslqiYwAAAAQ3wi4/BVycX+uzc5zF4F6EQAAABD0hhpr46IrKSu7XxFhEAYN"));
      // service.addKeystoreUrl(new ConfiguredUrl("http://host/path/to/keystore?keystoreType=" + "JCEKS&keystoreAlias=myalias",
      // Password.encode(PROPERTIES.getProperty(SECURITY_PASSWORD), Password.NON_PORTABLE_PASSWORD)));

      service.addKeystoreUrl(new ConfiguredUrl("file://local/path/to/anoter/JKS/keystore?keystoreType="
          + "JKS&keystoreAlias=myalias",
          "somePlainTextPassword"));
      InlineKeystore inline = new InlineKeystore();
      inline.setCertificate(EXAMPLE_KEYINFO);
      inline.setType(Constants.KEYSTORE_XMLKEYINFO);
      inline.setAlias("myAlias");
      service.addKeystoreUrl(inline);
      addEncryptionAlgsForFactoryType(service);
    }
    catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  private static void addEncryptionAlgsForFactoryType(CoreSecurityService s) {
    if (s.getSecurityFactory() == null) {
      s.setEncryptionAlgorithm(new EncryptionAlgorithm("AES/CBC/PKCS5Padding", 256));
      return;
    }
    return;
  }

  /**
   * @see com.adaptris.core.ExampleConfigCase#retrieveObjectForSampleConfig()
   */
  @Override
  protected Object retrieveObjectForSampleConfig() {
    CoreSecurityService s = create();
    applyConfigForSamples(s);
    return s;
  }

  @Override
  protected abstract CoreSecurityService create();

}
