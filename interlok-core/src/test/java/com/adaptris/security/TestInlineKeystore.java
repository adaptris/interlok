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

import com.adaptris.security.keystore.InlineKeystore;
import com.adaptris.security.util.Constants;

import junit.framework.TestCase;

/**
 * Test Inline Keystore Functionality wrapping a single KEYSTORE_X509
 * certificate
 *
 * @author $Author: lchan $
 */
public class TestInlineKeystore extends SingleEntryKeystoreBase {
  private static final String EXAMPLE_KEYINFO = "<KeyInfo xmlns=\"http://www.w3.org/2000/09/xmldsig#\""
      + " xmlns:tns=\"http://www.oasis-open.org/committees/ebxml-cppa/schema/cpp-cpa-2_0.xsd\">"
      + "<KeyName>message_signing_cert-Key</KeyName>"
      + "<X509Data>"
      + "<X509Certificate>"
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
      + "tItpX25JP3RhnzFrwriwUyFshUaF+J05O+6P2WilvUsX7+Q1prU7POnyizhdlvlt"
      + "6c+G3SjCAdM/oKJB1LSVLuxUzx0vTs2S"
      + "</X509Certificate>"
      + "</X509Data>" + "</KeyInfo>";

  private InlineKeystore inline, copy;

  /** @see TestCase */
  public TestInlineKeystore(String testName) {
    super(testName);
  }

  /**
   * @see TestCase#setUp()
   */
  @Override
  public void setUp() throws Exception {
    super.setUp();
    config = Config.getInstance();
    cfg = config.getProperties();

    if (cfg == null) {
      fail("No Configuration(!) available");
    }
    inline = new InlineKeystore();
    inline.setCertificate(EXAMPLE_KEYINFO);
    inline.setType(Constants.KEYSTORE_XMLKEYINFO);
    inline.setAlias("MyAlias");
    copy = new InlineKeystore();
    copy.setCertificate(EXAMPLE_KEYINFO);
    copy.setType(Constants.KEYSTORE_XMLKEYINFO);
    copy.setAlias("MyAlias");

    kloc = inline.asKeystoreLocation();
  }

  /**
   * @see TestCase#tearDown()
   */
  @Override
  public void tearDown() throws Exception {
    super.tearDown();
  }

  public void testEquality() throws Exception {
    assertNotSame(inline, new Object());
    assertEquals(inline, copy);
    assertEquals(inline.hashCode(), copy.hashCode());
    assertEquals(0, new InlineKeystore().hashCode());
    assertNotNull(inline.toString());
  }

  public void testAsKeystoreLocation() throws Exception {
    assertNotNull(inline.asKeystoreLocation());
    assertEquals(inline.asKeystoreLocation(), copy.asKeystoreLocation());
    assertEquals(inline.asKeystoreLocation().hashCode(), copy.asKeystoreLocation().hashCode());
  }

  public void testAsKeystoreProxy() throws Exception {
    assertNotNull(inline.asKeystoreProxy());
  }

  public void testWriteable() throws Exception {
    assertTrue(!inline.asKeystoreLocation().isWriteable());
    try {
      inline.asKeystoreLocation().openOutput();
      fail("openOutput was successful");
    }
    catch (Exception e) {
      // expected;
    }
  }

}
