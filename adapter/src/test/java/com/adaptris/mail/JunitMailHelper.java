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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.Date;

import javax.mail.Address;
import javax.mail.Message;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import com.adaptris.core.PortManager;
import com.icegreen.greenmail.smtp.SmtpServer;
import com.icegreen.greenmail.util.GreenMail;
import com.icegreen.greenmail.util.ServerSetup;

public class JunitMailHelper {

  public static final String DEFAULT_RECEIVER_DOMAIN = "TheReceivingCompany.com";
  public static final String DEFAULT_RECEIVER = "TheReceiver@" + DEFAULT_RECEIVER_DOMAIN;
  public static final String DEFAULT_SENDER_DOMAIN = "TheSendingCompany.com";
  public static final String DEFAULT_SENDER = "TheSender@" + DEFAULT_SENDER_DOMAIN;
  public static final String DEFAULT_PAYLOAD = "The quick brown fox jumps over the lazy dog";
  public static final String DEFAULT_PAYLOAD_HTML = "<html><body>The quick brown fox jumps over the lazy dog</body></html>";
  public static final String DEFAULT_SUBJECT = "Junit Mail Test : " + new Date();

  private static final String LF = System.lineSeparator();
  public static final String XML_DOCUMENT = "<?xml version=\"1.0\"?>" + LF + "<document>" + LF
      + "<subject>an email with attachemnts perhaps</subject>" + LF + "<content>Quick zephyrs blow, vexing daft Jim</content>" + LF
      + "<!-- This is ADP-01 MD5 Base64 -->" + LF
      + "<attachment encoding=\"base64\" filename=\"attachment1.txt\">dp/HSJfonUsSMM7QRBSRfg==</attachment>" + LF
      + "<!-- This is PENRY MD5 Base64 -->" + LF
      + "<attachment encoding=\"base64\" filename=\"attachment2.txt\">OdjozpCZB9PbCCLZlKregQ==</attachment>" + LF + "</document>";

  // Just starts a vanilla server with no settings...
  public static GreenMail startServer() throws Exception {
    return startServer(new GreenMail(createServerSetups()));
  }

  // Starts a server whereby emails for "emailAddr" are picked up by logging in as "user" and "password"
  public static GreenMail startServer(String emailAddr, String user, String password) throws Exception {
    GreenMail gm = new GreenMail(createServerSetups());
    gm.setUser(emailAddr, user, password);
    return startServer(gm);
  }

  private static GreenMail startServer(GreenMail gm) {
    gm.start();
    return gm;
  }

  public static void stopServer(GreenMail gm) throws Exception {
    if (gm != null) {
      releasePorts(gm);
      gm.stop();
    }
  }

  public static void assertFrom(MimeMessage m, String from) throws Exception {
    Address[] a = m.getFrom();
    if (!find(a, from)) {
      fail("Could not find " + from + " in FROM list");
    }
  }

  public static void assertTo(MimeMessage m, String email) throws Exception {
    boolean matched = false;
    Address[] addresses = m.getRecipients(Message.RecipientType.TO);
    if (!find(addresses, email)) {
      fail("Could not find " + email + " in TO list");
    }
  }

  public static void assertCC(MimeMessage m, String email) throws Exception {
    boolean matched = false;
    Address[] addresses = m.getRecipients(Message.RecipientType.CC);
    if (!find(addresses, email)) {
      fail("Could not find " + email + " in CC list");
    }
  }

  private static boolean find(Address[] addresses, String email) {
    boolean matched = false;
    for (Address a : addresses) {
      if (email.equals(a.toString())) {
        matched = true;
        break;
      }
    }
    return matched;
  }

  public static void assertBCC(MimeMessage m, String email) throws Exception {
    Address[] addresses = m.getRecipients(Message.RecipientType.BCC);
    if (!find(addresses, email)) {
      fail("Could not find " + email + " in BCC list");
    }
  }


  public static void assertContentType(MimeMessage msg, String contentType) throws Exception {
    Object o = msg.getContent();
    if (o instanceof MimeMultipart) {
      assertContentType((MimeMultipart) o, contentType);
    }
    else {
      assertEquals(contentType, msg.getContentType());
    }
  }

  private static void assertContentType(MimeMultipart m, String contentType) throws Exception {
    for (int i = 0; i < m.getCount(); i++) {
      MimeBodyPart part = (MimeBodyPart) m.getBodyPart(i);
      assertEquals(contentType, part.getContentType());
    }
  }

  public static SmtpClient createClient(GreenMail gm) throws Exception {
    SmtpClient client = createPlainClient(gm);
    client.addTo(DEFAULT_RECEIVER);
    client.setFrom(DEFAULT_SENDER);
    client.setMessage(DEFAULT_PAYLOAD.getBytes());
    client.setSubject(DEFAULT_SUBJECT);
    return client;
  }

  public static SmtpClient createPlainClient(GreenMail gm) throws Exception {
    SmtpServer server = gm.getSmtp();
    String smtpUrl = server.getProtocol() + "://localhost:" + server.getPort();
    SmtpClient client = new SmtpClient(smtpUrl);
    client.startSession();
    return client;
  }

  private static void releasePorts(GreenMail gm) {
    if (gm.getPop3() != null) {
      PortManager.release(gm.getPop3().getPort());
    }
    if (gm.getPop3s() != null) {
      PortManager.release(gm.getPop3s().getPort());
    }
    if (gm.getImap() != null) {
      PortManager.release(gm.getImap().getPort());
    }
    if (gm.getImaps() != null) {
      PortManager.release(gm.getImaps().getPort());
    }
    if (gm.getSmtp() != null) {
      PortManager.release(gm.getSmtp().getPort());
    }
    if (gm.getSmtps() != null) {
      PortManager.release(gm.getSmtps().getPort());
    }
  }

  private static ServerSetup[] createServerSetups() {
    int basePort = 12500;
    return new ServerSetup[] {
        new ServerSetup(PortManager.nextUnusedPort(basePort), null, ServerSetup.PROTOCOL_SMTP),
        new ServerSetup(PortManager.nextUnusedPort(basePort), null, ServerSetup.PROTOCOL_SMTPS),
        new ServerSetup(PortManager.nextUnusedPort(basePort), null, ServerSetup.PROTOCOL_POP3),
        new ServerSetup(PortManager.nextUnusedPort(basePort), null, ServerSetup.PROTOCOL_POP3S),
        new ServerSetup(PortManager.nextUnusedPort(basePort), null, ServerSetup.PROTOCOL_IMAP),
        new ServerSetup(PortManager.nextUnusedPort(basePort), null, ServerSetup.PROTOCOL_IMAPS)
    };
  }
}
