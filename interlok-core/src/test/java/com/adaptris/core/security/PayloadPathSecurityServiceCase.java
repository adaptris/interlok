package com.adaptris.core.security;

import static com.adaptris.core.security.JunitSecurityHelper.SECURITY_ALIAS;
import static com.adaptris.core.security.JunitSecurityHelper.SECURITY_PASSWORD;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.security.EncryptionAlgorithm;
import com.adaptris.security.keystore.ConfiguredUrl;
import com.adaptris.security.keystore.InlineKeystore;
import com.adaptris.security.util.Constants;

public abstract class PayloadPathSecurityServiceCase extends SecurityServiceCase{
  
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
  
  protected static final String XML = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\r\n"
      + "<root>\r\n"
      + "   <xpath1>xpath1_result</xpath1>\r\n"
      + "   <xpath2>xpath2_result</xpath2>\r\n"
      + "   <xpath3>xpath3_result</xpath3>\r\n"
      + "   <parent>\r\n"
      + "      <child1>child1</child1>\r\n"
      + "      <child2>child2</child2>\r\n"
      + "   </parent>\r\n"
      + "</root>\r\n";
  
  protected static final String JSON = "{\r\n"
      + "    \"firstName\": \"John\",\r\n"
      + "    \"lastName\": \"doe\",\r\n"
      + "    \"age\": 26,\r\n"
      + "    \"address\": {\r\n"
      + "        \"streetAddress\": \"street\",\r\n"
      + "        \"city\": \"city\",\r\n"
      + "        \"postalCode\": \"TW12 3RR\"\r\n"
      + "    },\r\n"
      + "    \"phoneNumbers\": [\r\n"
      + "        {\r\n"
      + "            \"type\": \"mobile\",\r\n"
      + "            \"number\": \"000-000-000\"\r\n"
      + "        },\r\n"
      + "        {\r\n"
      + "            \"type\": \"home\",\r\n"
      + "            \"number\": \"111-111-111\"\r\n"
      + "        }\r\n"
      + "    ]\r\n"
      + "}";
  
  protected static final String XPATH_1 = "//xpath1";
  protected static final String XPATH_2 = "//xpath2";
  protected static final String XPATH_3 = "//xpath3";
  
  protected static final String JSON_SINGLE_OBJECT_PATH = "$.firstName";
  protected static final String JSON_MULTI_OBJECT_PATH = "$.address";
  protected static final String JSON_ARRAY_PATH = "$.phoneNumbers";
  protected static final String JSON_EXPRESSION_PATH = "$.phoneNumbers[:1].type";
  protected static final String JSON_INVALID_PATH = "invalid json path";
  protected static final String JSON_NON_EXISTENT_PATH = "$.thirdName";
  
  @Test
  public void testEncryptingDecryptingMultiplePathsWithConfiguredProvider() throws Exception {
    List<String> path = new ArrayList<String>();
    path.add(XPATH_1);
    path.add(XPATH_2);
    path.add(XPATH_3);
    XpathBuilder xpathBuilder = new XpathBuilder();
    xpathBuilder.setPaths(path);
    PayloadPathEncryptionService payloadPathEncryptionService = new PayloadPathEncryptionService();
    payloadPathEncryptionService.setPathBuilder(xpathBuilder);
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(XML);
    String url = createKeystore();
    applyConfigForTests(payloadPathEncryptionService, url);
    configureForConfiguredPrivateKey(payloadPathEncryptionService, PROPERTIES.getProperty(SECURITY_PASSWORD));
    execute(payloadPathEncryptionService, msg);
    PayloadPathDecryptionService payloadPathDecryptionService = new PayloadPathDecryptionService();
    payloadPathDecryptionService.setPathBuilder(xpathBuilder);
    applyConfigForTests(payloadPathDecryptionService, url);
    configureForConfiguredPrivateKey(payloadPathDecryptionService, PROPERTIES.getProperty(SECURITY_PASSWORD));
    execute(payloadPathDecryptionService, msg);
    assertEquals(XML, msg.getContent());
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
  
}
