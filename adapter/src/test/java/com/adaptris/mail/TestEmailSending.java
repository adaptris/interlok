package com.adaptris.mail;

import static com.adaptris.mail.JunitMailHelper.DEFAULT_PAYLOAD_HTML;
import static com.adaptris.mail.JunitMailHelper.DEFAULT_RECEIVER;
import static com.adaptris.mail.JunitMailHelper.DEFAULT_SENDER;
import static com.adaptris.mail.JunitMailHelper.XML_DOCUMENT;
import static com.adaptris.mail.JunitMailHelper.createClient;
import static org.junit.Assert.assertEquals;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import javax.mail.internet.MimeMessage;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Test;

import com.icegreen.greenmail.util.GreenMail;

public class TestEmailSending {



  private Log logR = LogFactory.getLog(this.getClass());

  public TestEmailSending() {

  }

  @Test
  public void testSmtpSend() throws Exception {
    GreenMail gm = JunitMailHelper.startServer();
    try {
      SmtpClient smtp = createClient(gm);
      smtp.send();
      gm.waitForIncomingEmail(1);
      MimeMessage[] msgs = gm.getReceivedMessages();
      assertEquals(1, msgs.length);
      JunitMailHelper.assertFrom(msgs[0], DEFAULT_SENDER);
      JunitMailHelper.assertTo(msgs[0], DEFAULT_RECEIVER);
    }
    finally {
      JunitMailHelper.stopServer(gm);
    }
  }

  @Test
  public void testSmtpSendBase64Encoded() throws Exception {
    GreenMail gm = JunitMailHelper.startServer();
    try {
      SmtpClient smtp = createClient(gm);
      smtp.setEncoding("base64");
      smtp.send();
      gm.waitForIncomingEmail(1);
      MimeMessage[] msgs = gm.getReceivedMessages();
      assertEquals(1, msgs.length);
      JunitMailHelper.assertFrom(msgs[0], DEFAULT_SENDER);
      JunitMailHelper.assertTo(msgs[0], DEFAULT_RECEIVER);
    }
    finally {
      JunitMailHelper.stopServer(gm);
    }
  }

  @Test
  public void testSmtpSendWithContentType() throws Exception {
    GreenMail gm = JunitMailHelper.startServer();
    try {
      SmtpClient smtp = createClient(gm);
      smtp.setMessage(DEFAULT_PAYLOAD_HTML.getBytes(), "text/html");
      smtp.send();
      gm.waitForIncomingEmail(1);
      MimeMessage[] msgs = gm.getReceivedMessages();
      assertEquals(1, msgs.length);
      JunitMailHelper.assertFrom(msgs[0], DEFAULT_SENDER);
      JunitMailHelper.assertTo(msgs[0], DEFAULT_RECEIVER);
    }
    finally {
      JunitMailHelper.stopServer(gm);
    }
  }

  @Test
  public void testSmtpSendAttachment() throws Exception {
    GreenMail gm = JunitMailHelper.startServer();
    try {
      SmtpClient smtp = createClient(gm);
      ByteArrayOutputStream out = new ByteArrayOutputStream();
      PrintStream print = new PrintStream(out);
      print.print(XML_DOCUMENT);
      print.close();
      out.close();
      smtp.addAttachment(out.toByteArray(), "filename.xml", null);
      smtp.send();
      gm.waitForIncomingEmail(1);
      MimeMessage[] msgs = gm.getReceivedMessages();
      assertEquals(1, msgs.length);
      JunitMailHelper.assertFrom(msgs[0], DEFAULT_SENDER);
      JunitMailHelper.assertTo(msgs[0], DEFAULT_RECEIVER);
    }
    finally {
      JunitMailHelper.stopServer(gm);
    }
  }

  @Test
  public void testSmtpSendAttachmentBase64Encoded() throws Exception {

    GreenMail gm = JunitMailHelper.startServer();
    try {
      SmtpClient smtp = createClient(gm);
      ByteArrayOutputStream out = new ByteArrayOutputStream();
      PrintStream print = new PrintStream(out);
      print.print(XML_DOCUMENT);
      print.close();
      out.close();
      smtp.addAttachment(out.toByteArray(), "filename.xml", "text/plain");
      smtp.setEncoding("base64");
      smtp.send();
      gm.waitForIncomingEmail(1);
      MimeMessage[] msgs = gm.getReceivedMessages();
      assertEquals(1, msgs.length);
      JunitMailHelper.assertFrom(msgs[0], DEFAULT_SENDER);
      JunitMailHelper.assertTo(msgs[0], DEFAULT_RECEIVER);
    }
    finally {
      JunitMailHelper.stopServer(gm);
    }
  }

  @Test
  public void testSmtpSendAttachmentUUEncoded() throws Exception {

    GreenMail gm = JunitMailHelper.startServer();
    try {
      SmtpClient smtp = createClient(gm);
      ByteArrayOutputStream out = new ByteArrayOutputStream();
      PrintStream print = new PrintStream(out);
      print.print(XML_DOCUMENT);
      print.close();
      out.close();
      smtp.addAttachment(out.toByteArray(), "filename.xml", null);
      smtp.setEncoding("uuencode");
      smtp.send();
      gm.waitForIncomingEmail(1);
      MimeMessage[] msgs = gm.getReceivedMessages();
      assertEquals(1, msgs.length);
      JunitMailHelper.assertFrom(msgs[0], DEFAULT_SENDER);
      JunitMailHelper.assertTo(msgs[0], DEFAULT_RECEIVER);
    }
    finally {
      JunitMailHelper.stopServer(gm);
    }
  }
}