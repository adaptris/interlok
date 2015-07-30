package com.adaptris.mail;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.Date;

import javax.mail.Address;
import javax.mail.Message;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import com.icegreen.greenmail.smtp.SmtpServer;
import com.icegreen.greenmail.util.GreenMail;

public class JunitMailHelper {

  public static final String DEFAULT_RECEIVER_DOMAIN = "TheReceivingCompany.com";
  public static final String DEFAULT_RECEIVER = "TheReceiver@" + DEFAULT_RECEIVER_DOMAIN;
  public static final String DEFAULT_SENDER_DOMAIN = "TheSendingCompany.com";
  public static final String DEFAULT_SENDER = "TheSender@" + DEFAULT_SENDER_DOMAIN;
  public static final String DEFAULT_PAYLOAD = "The quick brown fox jumps over the lazy dog";
  public static final String DEFAULT_PAYLOAD_HTML = "<html><body>The quick brown fox jumps over the lazy dog</body></html>";
  public static final String DEFAULT_SUBJECT = "Junit Mail Test : " + new Date();

  private static final String LF = System.getProperty("line.separator");
  public static final String XML_DOCUMENT = "<?xml version=\"1.0\"?>" + LF + "<document>" + LF
      + "<subject>an email with attachemnts perhaps</subject>" + LF + "<content>Quick zephyrs blow, vexing daft Jim</content>" + LF
      + "<!-- This is ADP-01 MD5 Base64 -->" + LF
      + "<attachment encoding=\"base64\" filename=\"attachment1.txt\">dp/HSJfonUsSMM7QRBSRfg==</attachment>" + LF
      + "<!-- This is PENRY MD5 Base64 -->" + LF
      + "<attachment encoding=\"base64\" filename=\"attachment2.txt\">OdjozpCZB9PbCCLZlKregQ==</attachment>" + LF + "</document>";

  // Just starts a vanilla server with no settings...
  public static GreenMail startServer() throws Exception {
    return startServer(new GreenMail());
  }

  // Starts a server whereby emails for "emailAddr" are picked up by logging in as "user" and "password"
  public static GreenMail startServer(String emailAddr, String user, String password) throws Exception {
    GreenMail gm = new GreenMail();
    gm.setUser(emailAddr, user, password);
    return startServer(gm);
  }

  private static GreenMail startServer(GreenMail gm) {
    gm.start();
    return gm;
  }

  public static void stopServer(GreenMail gm) throws Exception {
    if (gm != null) {
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
}
