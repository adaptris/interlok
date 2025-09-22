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

package com.adaptris.core.services;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.core.CoreException;
import com.adaptris.core.GeneralServiceExample;
import com.adaptris.security.exc.PasswordException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.spy;

public class EncodePasswordServiceTest extends GeneralServiceExample {

  private EncodePasswordService service;
  private AdaptrisMessage msg;

  private static final String ADAPTER_CONFIG_FILE = "build/resources/test/validAdapter.xml";
  private static final String MESSAGE_HANDLER_FILE = "build/resources/test/message-handler.xml";
  private static final String ADAPTER_PROPERTIES_FILE = "build/resources/test/valid-local-vars.properties";
  private static final String ADAPTER_DTD_FILE = "build/resources/test/valid-local-vars.dtd";
  private static final String XML_WITHOUT_ROOT_FILE = "build/resources/test/services.xml";
  private static final String PASSWORD_PARAPHRASE = "PASSWORD, PASSPHRASE, SECRET, ROLEEXTERNALID";
  private static final String METADATA_KEY_PASSWORD_TOKENS = "passwordtokens";
  private static final String PREFIX_PORTBALE_PASSWORD_2 = "AES_GCM:";

  @BeforeEach
  public void setUp() throws Exception {
    service = new EncodePasswordService();
    msg = AdaptrisMessageFactory.getDefaultInstance().newMessage("Hello");
    msg.addMetadata(METADATA_KEY_PASSWORD_TOKENS, PASSWORD_PARAPHRASE);
  }

  @Override
  protected Object retrieveObjectForSampleConfig() {
    return new EncodePasswordService();
  }

  @Test
  public void testEncodePasswordInXML() throws Exception {
    service.setFilePath(ADAPTER_CONFIG_FILE);
    execute(service, msg);
    String xmlString = Files.readString(new File(service.getFilePath()).toPath());
    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
    DocumentBuilder builder = factory.newDocumentBuilder();
    Document document = builder.parse(new InputSource(new StringReader(xmlString)));

    XPath xPath = XPathFactory.newInstance().newXPath();
    String jmsPassword = (String) xPath.evaluate("/adapter/shared-components/connections/jms-connection/password/text()", document, XPathConstants.STRING);
    assertTrue(jmsPassword.startsWith(PREFIX_PORTBALE_PASSWORD_2));
    String jdbcPassword = (String) xPath.evaluate("/adapter/shared-components/connections/jdbc-pooled-connection/password/text()", document, XPathConstants.STRING);
    assertFalse(jdbcPassword.startsWith(PREFIX_PORTBALE_PASSWORD_2));
    String sftpPassword = (String) xPath.evaluate("/adapter/shared-components/connections/standard-sftp-connection/authentication/default-password/text()", document, XPathConstants.STRING);
    assertFalse(sftpPassword.startsWith(PREFIX_PORTBALE_PASSWORD_2));
  }

  @Test
  public void testEncodePasswordInProperties() throws Exception {
    service.setFilePath(ADAPTER_PROPERTIES_FILE);
    execute(service, msg);
    List<String> lines = Files.readAllLines(Paths.get(service.getFilePath()));
    String pwdLine = lines.stream().filter(line -> line.startsWith("cirrus.broker.password")).collect(Collectors.toList()).get(0);
    assertTrue(pwdLine.substring(pwdLine.indexOf('=')+1).startsWith(PREFIX_PORTBALE_PASSWORD_2));
  }

  @Test
  public void testEncodePasswordInDtd() throws Exception {
    service.setFilePath(ADAPTER_DTD_FILE);
    execute(service, msg);
    List<String> lines = Files.readAllLines(Paths.get(service.getFilePath()));
    String pwdLine = lines.stream().filter(line -> line.startsWith("<!ENTITY BROKER_PASSWORD")).collect(Collectors.toList()).get(0);
    assertTrue(pwdLine.substring(("<!ENTITY BROKER_PASSWORD").length()+19).startsWith(PREFIX_PORTBALE_PASSWORD_2));
    assertTrue(pwdLine.substring(("<!ENTITY HUB_PASSWORD").length()+22).startsWith(PREFIX_PORTBALE_PASSWORD_2));
  }

  @Test
  public void testDoServiceException() throws Exception{
    EncodePasswordService service = spy(new EncodePasswordService());
    doThrow(new IOException()).when(service).encodeValuesInPropertiesFile(any());
    assertThrows(CoreException.class, () -> {
      service.doService(msg);
    });
  }

  @Test
  public void testDoEncodePasswordException() throws Exception {
    EncodePasswordService service = spy(new EncodePasswordService());
    doThrow(new PasswordException()).when(service).doEncodePassword(any());
    assertThrows(CoreException.class, () -> {
      service.doService(msg);
    });
  }

    @Test
    public void testEncodePasswordInWithoutRootXML() throws Exception {
        service.setFilePath(XML_WITHOUT_ROOT_FILE);
        execute(service, msg);
        List<String> lines = Files.readAllLines(Paths.get(service.getFilePath()));
        List<String> pwdLines = lines.stream().filter(line -> line.trim().startsWith("<service")).collect(Collectors.toList());
        assertEquals(pwdLines.size(),9);
    }

    @Test
    public void testEncodePasswordParseErrorXML() throws Exception {
        service.setFilePath(MESSAGE_HANDLER_FILE);
        execute(service, msg);
        List<String> lines = Files.readAllLines(Paths.get(service.getFilePath()));
        List<String> pwdLines = lines.stream().filter(line -> line.trim().startsWith("<service")).collect(Collectors.toList());
        assertEquals(pwdLines.size(),1);
    }

    @Test
    public void testXIncludesInXML() throws Exception {
        service.setFilePath(ADAPTER_CONFIG_FILE);
        execute(service, msg);
        List<String> lines = Files.readAllLines(Paths.get(service.getFilePath()));
        List<String> eventHandlerLines = lines.stream().filter(line -> line.trim().contains("xinclude.event-handler")).collect(Collectors.toList());
        assertEquals(eventHandlerLines.size(),1);
        assertEquals(eventHandlerLines.get(0).trim(),"<xi:include href=\"${xinclude.event-handler}\"/>");
    }
}
