/*
 * Copyright 2018 Adaptris Ltd.
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
package com.adaptris.core.ftp;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPSClient;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.adaptris.ftp.ClientSettings;

public class ClientSettingsTest extends ClientSettings {

  @Before
  public void setUp() throws Exception {
  }

  @After
  public void tearDown() throws Exception {
  }

  @Test
  public void testPreConnectSettings_FTP() {
    FTPClient client = new FTPClient();
    preConnectSettings(client, FTP.values(), createFtpSettings());
    assertTrue(client.isUseEPSVwithIPv4());
    assertTrue(client.isStrictReplyParsing());
    assertTrue(client.isStrictMultilineParsing());
    assertFalse(client.isRemoteVerificationEnabled());
    assertTrue(client.getListHiddenFiles());
    assertTrue(client.getAutodetectUTF8());
  }

  @Test(expected = RuntimeException.class)
  public void testPreConnectSettings_FTP_Error() {
    FTPClient client = new FTPClient();
    Map<String, String> settings = createFtpSettings();
    // Should throw a NumberFormatException, which is then wrapped
    settings.put(FTP.SendDataSocketBufferSize.name(), "XXX");
    preConnectSettings(client, FTP.values(), settings);
  }

  @Test
  public void testPreConnectSettings_FTPS() {
    FTPSClient client = new FTPSClient();
    preConnectSettings(client, FTPS.values(), createFtpsSettings());
    assertTrue(client.isEndpointCheckingEnabled());
    // Not Connected yet, so these will not be the same.
    assertFalse(client.getNeedClientAuth());
    assertFalse(client.getUseClientMode());
    assertFalse(client.getWantClientAuth());
    assertNull(client.getEnabledCipherSuites());
    assertNull(client.getEnabledProtocols());
  }

  private Map<String, String> createFtpSettings() {
    Map<String, String> settings = new HashMap<>();
    settings.put(FTP.ActiveExternalIPAddress.name(), "localhost");
    settings.put(FTP.AutodetectUTF8.name(), "true");
    settings.put(FTP.ControlEncoding.name(), "UTF-8");
    settings.put(FTP.ListHiddenFiles.name(), "true");
    settings.put(FTP.PassiveLocalIPAddress.name(), "localhost");
    settings.put(FTP.PassiveNatWorkaround.name(), "true");
    settings.put(FTP.ReceiveDataSocketBufferSize.name(), "1024");
    settings.put(FTP.RemoteVerificationEnabled.name(), "false");
    settings.put(FTP.ReportActiveExternalIPAddress.name(), "localhost");
    settings.put(FTP.SendDataSocketBufferSize.name(), "1024");
    settings.put(FTP.StrictReplyParsing.name(), "true");
    settings.put(FTP.StrictMultilineParsing.name(), "true");
    settings.put(FTP.UseEPSVwithIPv4.name(), "true");
    return settings;
  }

  private Map<String, String> createFtpsSettings() {
    Map<String, String> settings = new HashMap<>();
    settings.put(FTPS.AuthValue.name(), "localhost");
    settings.put(FTPS.EnabledCipherSuites.name(), "TLS_ECDHE_ECDSA_WITH_AES_256_CBC_SHA384,TLS_ECDHE_RSA_WITH_AES_256_CBC_SHA384");
    settings.put(FTPS.EnabledProtocols.name(), "SSLv3,SSLv2");
    settings.put(FTPS.EnabledSessionCreation.name(), "true");
    settings.put(FTPS.EndpointCheckingEnabled.name(), "true");
    settings.put(FTPS.NeedClientAuth.name(), "true");
    settings.put(FTPS.UseClientMode.name(), "true");
    settings.put(FTPS.WantClientAuth.name(), "true");
    return settings;
  }
}
