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

package com.adaptris.mail;

import static com.adaptris.mail.JunitMailHelper.testsEnabled;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import javax.mail.URLName;
import javax.mail.internet.MimeMessage;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Test;

import com.icegreen.greenmail.smtp.SmtpServer;
import com.icegreen.greenmail.util.GreenMail;

public class TestSmtpClient {

  private static final String INVALID_EMAIL_ADDR = "a\"b(c)d,e:f;g<h>i[j\\k]l@example.com";
  private static final String DUMMY_VALUE = "HELO";
  private static final String DUMMY_KEY = "EHLO";

  private Log logR = LogFactory.getLog(this.getClass());

  public TestSmtpClient() {

  }

  @Test
  public void testConstructors() throws Exception {
    if (!testsEnabled()) return;
    GreenMail gm = JunitMailHelper.startServer();
    SmtpServer server = gm.getSmtp();
    String smtpUrl = server.getProtocol() + "://localhost:" + server.getPort();
    try {
      SmtpClient client = new SmtpClient(smtpUrl);
      client = new SmtpClient(new URLName(smtpUrl));
      client = new SmtpClient("localhost", server.getPort(), "", "");
    }
    finally {
        JunitMailHelper.stopServer(gm);
      }
  }

  @Test
  public void testAddSessionProperties() throws Exception {
    if (!testsEnabled()) return;
    GreenMail gm = JunitMailHelper.startServer();
    try {
      SmtpClient smtp = createClient(gm);
      smtp.addSessionProperty(DUMMY_KEY, DUMMY_VALUE);
      assertTrue(smtp.getSessionProperties().containsKey(DUMMY_KEY));
      assertEquals(DUMMY_VALUE, smtp.getSessionProperties().getProperty(DUMMY_KEY));
      smtp.removeSessionProperty(DUMMY_KEY);
      assertFalse(smtp.getSessionProperties().containsKey(DUMMY_KEY));
      smtp.removeSessionProperty(DUMMY_KEY);
      assertFalse(smtp.getSessionProperties().containsKey(DUMMY_KEY));
    }
    finally {
      JunitMailHelper.stopServer(gm);
    }
  }

  @Test
  public void testStartSession() throws Exception {
    if (!testsEnabled()) return;
    GreenMail gm = JunitMailHelper.startServer();
    try {
      SmtpClient smtp = createClient(gm);
      assertNull(smtp.session);
      smtp.startSession();
      assertNotNull(smtp.session);
      assertNotNull(smtp.message);
    }
    finally {
      JunitMailHelper.stopServer(gm);
    }
  }

  @Test
  public void testNewMessage_WithoutStartSession() throws Exception {
    if (!testsEnabled()) return;
    GreenMail gm = JunitMailHelper.startServer();
    try {
      SmtpClient smtp = createClient(gm);
      smtp.newMessage();
      fail();
    }
    catch (MailException expected) {
      assertEquals("Session not started", expected.getMessage());
    }
    finally {
      JunitMailHelper.stopServer(gm);
    }
  }

  @Test
  public void testAddMailHeader() throws Exception {
    if (!testsEnabled()) return;
    GreenMail gm = JunitMailHelper.startServer();
    try {
      SmtpClient smtp = createClient(gm);
      smtp.startSession();
      smtp.addMailHeader(DUMMY_KEY, DUMMY_VALUE);
      assertNotNull(smtp.message.getHeader(DUMMY_KEY));
      assertEquals(DUMMY_VALUE, smtp.message.getHeader(DUMMY_KEY)[0]);
    }
    finally {
      JunitMailHelper.stopServer(gm);
    }
  }

  @Test
  public void testRemoveMailHeader() throws Exception {
    if (!testsEnabled()) return;
    GreenMail gm = JunitMailHelper.startServer();
    try {
      SmtpClient smtp = createClient(gm);
      smtp.startSession();
      smtp.addMailHeader(DUMMY_KEY, DUMMY_VALUE);
      assertNotNull(smtp.message.getHeader(DUMMY_KEY));
      assertEquals(DUMMY_VALUE, smtp.message.getHeader(DUMMY_KEY)[0]);
      smtp.removeMailHeader(DUMMY_KEY);
      assertNull(smtp.message.getHeader(DUMMY_KEY));
    }
    finally {
      JunitMailHelper.stopServer(gm);
    }
  }

  @Test
  public void testAddTo_NoSession() throws Exception {
    if (!testsEnabled()) return;
    GreenMail gm = JunitMailHelper.startServer();
    try {
      SmtpClient smtp = createClient(gm);
      smtp.addTo("abc@adaptris.com, bcd@adaptris.com, def@adaptris.com");
      fail();
    }
    catch (MailException expected) {
      assertEquals("Session not started", expected.getMessage());
    }
    finally {
      JunitMailHelper.stopServer(gm);
    }
  }

  @Test
  public void testAddTo() throws Exception {
    if (!testsEnabled()) return;
    GreenMail gm = JunitMailHelper.startServer();
    try {
      SmtpClient smtp = createClient(gm);
      smtp.startSession();
      smtp.addTo("abc@adaptris.com, bcd@adaptris.com, def@adaptris.com");
    }
    finally {
      JunitMailHelper.stopServer(gm);
    }

  }

  @Test
  public void testAddTo_Invalid() throws Exception {
    if (!testsEnabled()) return;
    GreenMail gm = JunitMailHelper.startServer();
    try {
      SmtpClient smtp = createClient(gm);
      smtp.startSession();
      smtp.addTo(INVALID_EMAIL_ADDR);
      fail();
    }
    catch (Exception expected) {

    }
    finally {
      JunitMailHelper.stopServer(gm);
    }
  }

  @Test
  public void testAddCc_NoSession() throws Exception {
    if (!testsEnabled()) return;
    GreenMail gm = JunitMailHelper.startServer();
    try {
      SmtpClient smtp = createClient(gm);
      smtp.addCarbonCopy("abc@adaptris.com, bcd@adaptris.com, def@adaptris.com");
      fail();
    }
    catch (MailException expected) {
      assertEquals("Session not started", expected.getMessage());
    }
    finally {
      JunitMailHelper.stopServer(gm);
    }
  }

  @Test
  public void testAddCc() throws Exception {
    if (!testsEnabled()) return;
    GreenMail gm = JunitMailHelper.startServer();
    try {
      SmtpClient smtp = createClient(gm);
      smtp.startSession();
      smtp.addCarbonCopy("abc@adaptris.com, bcd@adaptris.com, def@adaptris.com");
    }
    finally {
      JunitMailHelper.stopServer(gm);
    }
  }

  @Test
  public void testAddCc_Invalid() throws Exception {
    if (!testsEnabled()) return;
    GreenMail gm = JunitMailHelper.startServer();
    try {
      SmtpClient smtp = createClient(gm);
      smtp.startSession();
      smtp.addCarbonCopy(INVALID_EMAIL_ADDR);
      fail();
    }
    catch (Exception expected) {

    }
    finally {
      JunitMailHelper.stopServer(gm);
    }
  }

  @Test
  public void testAddBcc_NoSession() throws Exception {
    if (!testsEnabled()) return;
    GreenMail gm = JunitMailHelper.startServer();
    try {
      SmtpClient smtp = createClient(gm);
      smtp.addBlindCarbonCopy("abc@adaptris.com, bcd@adaptris.com, def@adaptris.com");
      fail();
    }
    catch (MailException expected) {
      assertEquals("Session not started", expected.getMessage());
    }
    finally {
      JunitMailHelper.stopServer(gm);
    }
  }

  @Test
  public void testAddBcc() throws Exception {
    if (!testsEnabled()) return;
    GreenMail gm = JunitMailHelper.startServer();
    try {
      SmtpClient smtp = createClient(gm);
      smtp.startSession();
      smtp.addBlindCarbonCopy("abc@adaptris.com, bcd@adaptris.com, def@adaptris.com");
    }
    finally {
      JunitMailHelper.stopServer(gm);
    }
  }

  @Test
  public void testAddBcc_Invalid() throws Exception {
    if (!testsEnabled()) return;
    GreenMail gm = JunitMailHelper.startServer();
    try {
      SmtpClient smtp = createClient(gm);
      smtp.startSession();
      smtp.addBlindCarbonCopy(INVALID_EMAIL_ADDR);
      fail();
    }
    catch (Exception expected) {

    }
    finally {
      JunitMailHelper.stopServer(gm);
    }
  }

  @Test
  public void testSetFrom_NoSession() throws Exception {
    if (!testsEnabled()) return;
    GreenMail gm = JunitMailHelper.startServer();
    try {
      SmtpClient smtp = createClient(gm);
      smtp.setFrom("abc@adaptris.com");
    }
    catch (MailException expected) {
      assertEquals("Session not started", expected.getMessage());
    }
    finally {
      JunitMailHelper.stopServer(gm);
    }
  }

  @Test
  public void testSetFrom() throws Exception {
    if (!testsEnabled()) return;
    GreenMail gm = JunitMailHelper.startServer();
    try {
      SmtpClient smtp = createClient(gm);
      smtp.startSession();
      smtp.setFrom("abc@adaptris.com");
    }
    finally {
      JunitMailHelper.stopServer(gm);
    }
  }

  @Test
  public void testAddFrom_Invalid() throws Exception {
    if (!testsEnabled()) return;
    GreenMail gm = JunitMailHelper.startServer();
    try {
      SmtpClient smtp = createClient(gm);
      smtp.startSession();
      smtp.setFrom(INVALID_EMAIL_ADDR);
      fail();
    }
    catch (Exception expected) {

    }
    finally {
      JunitMailHelper.stopServer(gm);
    }
  }

  @Test
  public void testSetSubject_NoSession() throws Exception {
    if (!testsEnabled()) return;
    GreenMail gm = JunitMailHelper.startServer();
    try {
      SmtpClient smtp = createClient(gm);
      smtp.setSubject("abc@adaptris.com");
      fail();
    }
    catch (MailException expected) {
      assertEquals("Session not started", expected.getMessage());
    }
    finally {
      JunitMailHelper.stopServer(gm);
    }
  }

  @Test
  public void testSetSubject() throws Exception {
    if (!testsEnabled()) return;
    GreenMail gm = JunitMailHelper.startServer();
    try {
      SmtpClient smtp = createClient(gm);
      smtp.startSession();
      smtp.setSubject("abc@adaptris.com");
    }
    finally {
      JunitMailHelper.stopServer(gm);
    }
  }

  @Test
  public void testSend_WithoutStartSession() throws Exception {
    if (!testsEnabled()) return;
    GreenMail gm = JunitMailHelper.startServer();
    try {
      SmtpClient smtp = createClient(gm);
      smtp.setMessage(JunitMailHelper.DEFAULT_PAYLOAD.getBytes());
      smtp.setSubject(JunitMailHelper.DEFAULT_SUBJECT);
      smtp.addTo(JunitMailHelper.DEFAULT_RECEIVER);
      smtp.send();
      gm.waitForIncomingEmail(1);
      MimeMessage[] msgs = gm.getReceivedMessages();
      assertEquals(1, msgs.length);
      assertNotNull(msgs[0].getFrom());
    }
    catch (MailException expected) {
      assertEquals("Session not started", expected.getMessage());
    }
    finally {
      JunitMailHelper.stopServer(gm);
    }
  }

  @Test
  public void testSend_NoFromAddress() throws Exception {
    if (!testsEnabled()) return;
    GreenMail gm = JunitMailHelper.startServer();
    try {
      SmtpClient smtp = createClient(gm);
      smtp.startSession();
      smtp.setMessage(JunitMailHelper.DEFAULT_PAYLOAD.getBytes());
      smtp.setSubject(JunitMailHelper.DEFAULT_SUBJECT);
      smtp.addTo(JunitMailHelper.DEFAULT_RECEIVER);
      smtp.send();
      gm.waitForIncomingEmail(1);
      MimeMessage[] msgs = gm.getReceivedMessages();
      assertEquals(1, msgs.length);
      assertNotNull(msgs[0].getFrom());
    }
    finally {
      JunitMailHelper.stopServer(gm);
    }
  }

  @Test
  public void testSend_NoRecipients() throws Exception {
    if (!testsEnabled()) return;
    GreenMail gm = JunitMailHelper.startServer();
    try {
      SmtpClient smtp = createClient(gm);
      smtp.startSession();
      smtp.setMessage(JunitMailHelper.DEFAULT_PAYLOAD.getBytes());
      smtp.setSubject(JunitMailHelper.DEFAULT_SUBJECT);
      smtp.send();
      fail();
    }
    catch (MailException expected) {
      expected.printStackTrace();
      assertEquals("Mail message has no recipients", expected.getMessage());
    }
    finally {
      JunitMailHelper.stopServer(gm);
    }
  }

  private static SmtpClient createClient(GreenMail gm) throws Exception {
    SmtpServer server = gm.getSmtp();
    String smtpUrl = server.getProtocol() + "://localhost:" + server.getPort();
    SmtpClient client = new SmtpClient(smtpUrl);
    return client;
  }

}
