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

package com.adaptris.core.mail;

import static com.adaptris.mail.JunitMailHelper.DEFAULT_RECEIVER;
import static com.adaptris.mail.JunitMailHelper.DEFAULT_SENDER;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import javax.mail.Header;
import javax.mail.internet.MimeMessage;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.core.ConfiguredProduceDestination;
import com.adaptris.core.CoreConstants;
import com.adaptris.core.NullConnection;
import com.adaptris.core.ServiceCase;
import com.adaptris.core.StandaloneProducer;
import com.adaptris.mail.JunitMailHelper;
import com.adaptris.mail.MessageParser;
import com.adaptris.util.KeyValuePair;
import com.icegreen.greenmail.smtp.SmtpServer;
import com.icegreen.greenmail.util.GreenMail;

@SuppressWarnings("deprecation")
public class DefaultMailProducerTest extends MailProducerExample {

  private DefaultSmtpProducer producer;

  public DefaultMailProducerTest(String name) {
    super(name);
  }

  @Override
  protected void setUp() throws Exception {
    producer = new DefaultSmtpProducer();
  }

  @Override
  protected Object retrieveObjectForSampleConfig() {
    return null;
  }

  public void testProduce() throws Exception {
    GreenMail gm = JunitMailHelper.startServer();
    try {
      AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(JunitMailHelper.DEFAULT_PAYLOAD);
      ServiceCase.execute(createProducerForTests(gm), msg);
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

  public void testProduceCC() throws Exception {
    GreenMail gm = JunitMailHelper.startServer();
    try {
      AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(JunitMailHelper.DEFAULT_PAYLOAD);
      StandaloneProducer producer = createProducerForTests(gm);
      MailProducer mailer = (MailProducer) producer.getProducer();
      mailer.setCcList("CarbonCopy@CarbonCopy.com");
      ServiceCase.execute(producer, msg);
      gm.waitForIncomingEmail(1);
      MimeMessage[] msgs = gm.getReceivedMessages();
      assertEquals(2, msgs.length);
      for (MimeMessage mime : msgs) {
        JunitMailHelper.assertFrom(mime, DEFAULT_SENDER);
        JunitMailHelper.assertTo(mime, DEFAULT_RECEIVER);
        JunitMailHelper.assertCC(mime, "CarbonCopy@CarbonCopy.com");
      }
    }
    finally {
      JunitMailHelper.stopServer(gm);
    }
  }

  public void testProduceBCC() throws Exception {
    GreenMail gm = JunitMailHelper.startServer();
    try {
      AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(JunitMailHelper.DEFAULT_PAYLOAD);
      StandaloneProducer producer = createProducerForTests(gm);
      MailProducer mailer = (MailProducer) producer.getProducer();
      mailer.setBccList("BlindCarbonCopy@BlindCarbonCopy.com");
      ServiceCase.execute(producer, msg);
      gm.waitForIncomingEmail(1);
      MimeMessage[] msgs = gm.getReceivedMessages();
      assertEquals(2, msgs.length);
      for (MimeMessage mime : msgs) {
        JunitMailHelper.assertFrom(mime, DEFAULT_SENDER);
        JunitMailHelper.assertTo(mime, DEFAULT_RECEIVER);
        // We never *see* the BCC so we can't check it.
      }
    }
    finally {
      JunitMailHelper.stopServer(gm);
    }
  }

  public void testProduceWithHeaders() throws Exception {
    GreenMail gm = JunitMailHelper.startServer();
    try {
      AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(JunitMailHelper.DEFAULT_PAYLOAD);
      msg.addMetadata("X-Email-Producer", "ABCDEFG");
      StandaloneProducer producer = createProducerForTests(gm);
      MailProducer mailer = (MailProducer) producer.getProducer();
      mailer.setSendMetadataAsHeaders(true);
      mailer.setSendMetadataRegexp("X-Email.*");

      ServiceCase.execute(producer, msg);

      gm.waitForIncomingEmail(1);
      MimeMessage[] msgs = gm.getReceivedMessages();
      assertEquals(1, msgs.length);
      MimeMessage mailmsg = msgs[0];
      JunitMailHelper.assertFrom(mailmsg, DEFAULT_SENDER);
      JunitMailHelper.assertTo(mailmsg, DEFAULT_RECEIVER);
      Enumeration e = mailmsg.getAllHeaders();
      boolean matched = false;
      while (e.hasMoreElements()) {
        Header h = (Header) e.nextElement();
        if (h.getName().equals("X-Email-Producer")) {
          if (h.getValue().equals("ABCDEFG")) {
            matched = true;
            break;
          }
        }
      }
      if (!matched) {
        fail("Additional Metadata Headers were not produced");
      }
    }
    finally {
      JunitMailHelper.stopServer(gm);
    }
  }

  public void testProduceAsAttachment() throws Exception {
    GreenMail gm = JunitMailHelper.startServer();
    try {
      AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(JunitMailHelper.DEFAULT_PAYLOAD);
      StandaloneProducer producer = createProducerForTests(gm);
      DefaultSmtpProducer mailer = (DefaultSmtpProducer) producer.getProducer();
      mailer.setAttachment(true);
      mailer.setAttachmentContentType("text/plain");
      ServiceCase.execute(producer, msg);

      gm.waitForIncomingEmail(1);
      MimeMessage[] msgs = gm.getReceivedMessages();
      assertEquals(1, msgs.length);
      MimeMessage mailmsg = msgs[0];
      JunitMailHelper.assertFrom(mailmsg, DEFAULT_SENDER);
      JunitMailHelper.assertTo(mailmsg, DEFAULT_RECEIVER);
      MessageParser mp = new MessageParser(mailmsg);
      assertTrue(mp.hasAttachments());
    }
    finally {
      JunitMailHelper.stopServer(gm);
    }
  }

  public void testProduceAsAttachmentWithFilename() throws Exception {
    GreenMail gm = JunitMailHelper.startServer();
    try {
      AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(JunitMailHelper.DEFAULT_PAYLOAD);
      StandaloneProducer producer = createProducerForTests(gm);
      DefaultSmtpProducer mailer = (DefaultSmtpProducer) producer.getProducer();
      mailer.setAttachment(true);
      mailer.setAttachmentContentType("text/xml");
      msg.addMetadata(CoreConstants.EMAIL_ATTACH_FILENAME, "filename.txt");
      ServiceCase.execute(producer, msg);

      gm.waitForIncomingEmail(1);
      MimeMessage[] msgs = gm.getReceivedMessages();
      assertEquals(1, msgs.length);
      MimeMessage mailmsg = msgs[0];
      JunitMailHelper.assertFrom(mailmsg, DEFAULT_SENDER);
      JunitMailHelper.assertTo(mailmsg, DEFAULT_RECEIVER);
      MessageParser mp = new MessageParser(mailmsg);
      assertTrue(mp.hasAttachments());
      assertEquals("filename.txt", mp.nextAttachment().getFilename());
    }
    finally {
      JunitMailHelper.stopServer(gm);
    }
  }

  public void testProduceAsAttachmentWithMetadataAttachmentContentType() throws Exception {
    GreenMail gm = JunitMailHelper.startServer();
    try {
      AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(JunitMailHelper.DEFAULT_PAYLOAD);
      StandaloneProducer producer = createProducerForTests(gm);
      DefaultSmtpProducer mailer = (DefaultSmtpProducer) producer.getProducer();
      mailer.setAttachment(true);
      mailer.setAttachmentContentType("text/xml");
      msg.addMetadata(CoreConstants.EMAIL_ATTACH_CONTENT_TYPE, "text/plain");
      ServiceCase.execute(producer, msg);

      gm.waitForIncomingEmail(1);
      MimeMessage[] msgs = gm.getReceivedMessages();
      assertEquals(1, msgs.length);
      MimeMessage mailmsg = msgs[0];
      JunitMailHelper.assertFrom(mailmsg, DEFAULT_SENDER);
      JunitMailHelper.assertTo(mailmsg, DEFAULT_RECEIVER);
      MessageParser mp = new MessageParser(mailmsg);
      assertTrue(mp.hasAttachments());
      assertEquals("text/plain", mp.nextAttachment().getContentType());
    }
    finally {
      JunitMailHelper.stopServer(gm);
    }
  }

  public void testProduceAsAttachmentWithTemplate() throws Exception {
    GreenMail gm = JunitMailHelper.startServer();
    try {
      AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(JunitMailHelper.DEFAULT_PAYLOAD);
      StandaloneProducer producer = createProducerForTests(gm);
      DefaultSmtpProducer mailer = (DefaultSmtpProducer) producer.getProducer();
      mailer.setAttachment(true);
      mailer.setAttachmentContentType("text/plain");
      msg.addMetadata(CoreConstants.EMAIL_TEMPLATE_BODY, "This is the body");
      ServiceCase.execute(producer, msg);

      gm.waitForIncomingEmail(1);
      MimeMessage[] msgs = gm.getReceivedMessages();
      assertEquals(1, msgs.length);
      MimeMessage mailmsg = msgs[0];
      JunitMailHelper.assertFrom(mailmsg, DEFAULT_SENDER);
      JunitMailHelper.assertTo(mailmsg, DEFAULT_RECEIVER);
      MessageParser mp = new MessageParser(mailmsg);
      assertTrue(mp.hasAttachments());
      assertEquals("This is the body", new String(mp.getMessage()));
    }
    finally {
      JunitMailHelper.stopServer(gm);
    }
  }

  public void testProduceAsAttachmentWithCharEncodingForTemplate() throws Exception {
    GreenMail gm = JunitMailHelper.startServer();
    try {
      AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(JunitMailHelper.DEFAULT_PAYLOAD, "UTF-8");
      StandaloneProducer producer = createProducerForTests(gm);
      DefaultSmtpProducer mailer = (DefaultSmtpProducer) producer.getProducer();
      mailer.setAttachment(true);
      mailer.setAttachmentContentType("text/plain");
      msg.addMetadata(CoreConstants.EMAIL_TEMPLATE_BODY, "This is the body");
      ServiceCase.execute(producer, msg);

      gm.waitForIncomingEmail(1);
      MimeMessage[] msgs = gm.getReceivedMessages();
      assertEquals(1, msgs.length);
      MimeMessage mailmsg = msgs[0];
      JunitMailHelper.assertFrom(mailmsg, DEFAULT_SENDER);
      JunitMailHelper.assertTo(mailmsg, DEFAULT_RECEIVER);
      MessageParser mp = new MessageParser(mailmsg);
      assertTrue(mp.hasAttachments());
      assertEquals("This is the body", new String(mp.getMessage()));
    }
    finally {
      JunitMailHelper.stopServer(gm);
    }

  }

  public void testProduceWithContentTypeMetadata() throws Exception {
    GreenMail gm = JunitMailHelper.startServer();
    try {
      AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(JunitMailHelper.DEFAULT_PAYLOAD);
      StandaloneProducer producer = createProducerForTests(gm);
      DefaultSmtpProducer mailer = (DefaultSmtpProducer) producer.getProducer();
      mailer.setContentType("text/xml");
      mailer.setContentTypeKey("MetadataContentType");
      msg.addMetadata("MetadataContentType", "text/plain");
      ServiceCase.execute(producer, msg);

      gm.waitForIncomingEmail(1);
      MimeMessage[] msgs = gm.getReceivedMessages();
      assertEquals(1, msgs.length);
      MimeMessage mailmsg = msgs[0];
      JunitMailHelper.assertFrom(mailmsg, DEFAULT_SENDER);
      JunitMailHelper.assertTo(mailmsg, DEFAULT_RECEIVER);
      JunitMailHelper.assertContentType(mailmsg, "text/plain");
    }
    finally {
      JunitMailHelper.stopServer(gm);
    }
  }

  public void testProduceWithContentTypeMetadataButMissingMetadata() throws Exception {
    GreenMail gm = JunitMailHelper.startServer();
    try {
      AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(JunitMailHelper.DEFAULT_PAYLOAD);
      StandaloneProducer producer = createProducerForTests(gm);
      DefaultSmtpProducer mailer = (DefaultSmtpProducer) producer.getProducer();
      mailer.setContentTypeKey("MetadataContentType");
      mailer.setContentType("text/plain");
      ServiceCase.execute(producer, msg);

      gm.waitForIncomingEmail(1);
      MimeMessage[] msgs = gm.getReceivedMessages();
      assertEquals(1, msgs.length);
      MimeMessage mailmsg = msgs[0];
      JunitMailHelper.assertFrom(mailmsg, DEFAULT_SENDER);
      JunitMailHelper.assertTo(mailmsg, DEFAULT_RECEIVER);
      JunitMailHelper.assertContentType(mailmsg, "text/plain");
    }
    finally {
      JunitMailHelper.stopServer(gm);
    }
  }

  public void testProduceWithContentTypeMetadataButEmptyMetadata() throws Exception {
    GreenMail gm = JunitMailHelper.startServer();
    try {
      AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(JunitMailHelper.DEFAULT_PAYLOAD);
      msg.addMetadata("MetadataContentType", "");
      StandaloneProducer producer = createProducerForTests(gm);
      DefaultSmtpProducer mailer = (DefaultSmtpProducer) producer.getProducer();
      mailer.setContentTypeKey("MetadataContentType");
      mailer.setContentType("text/plain");
      ServiceCase.execute(producer, msg);

      gm.waitForIncomingEmail(1);
      MimeMessage[] msgs = gm.getReceivedMessages();
      assertEquals(1, msgs.length);
      MimeMessage mailmsg = msgs[0];
      JunitMailHelper.assertFrom(mailmsg, DEFAULT_SENDER);
      JunitMailHelper.assertTo(mailmsg, DEFAULT_RECEIVER);
      JunitMailHelper.assertContentType(mailmsg, "text/plain");
    }
    finally {
      JunitMailHelper.stopServer(gm);
    }
  }

  /**
   * @see com.adaptris.core.ExampleConfigCase#retrieveObjectForSampleConfig()
   */
  @Override
  protected List retrieveObjectsForSampleConfig() {
    List<StandaloneProducer> result = new ArrayList<StandaloneProducer>();
    DefaultSmtpProducer smtp = new DefaultSmtpProducer();
    smtp.setDestination(new ConfiguredProduceDestination("user@domain"));
    smtp.getProperties().addKeyValuePair(new KeyValuePair("mail.smtp.starttls.enable", "true"));
    smtp.setSubject("Configured subject");
    smtp.setSmtpUrl("smtp://localhost:25");
    smtp.setCcList("user@domain, user@domain");
    smtp.setSendMetadataAsHeaders(true);
    smtp.setSendMetadataRegexp("X-MyHeaders.*");
    result.add(new StandaloneProducer(smtp));

    DefaultSmtpProducer smtps = new DefaultSmtpProducer();
    smtps.setDestination(new ConfiguredProduceDestination("user@domain"));
    smtps.getProperties().addKeyValuePair(new KeyValuePair("mail.smtp.starttls.enable", "true"));
    smtps.setSubject("Configured subject");
    smtps.setSmtpUrl("smtps://username%40gmail.com:mypassword;@smtp.gmail.com:465");
    smtps.setCcList("user@domain, user@domain");
    smtps.setSendMetadataAsHeaders(true);
    smtps.setSendMetadataRegexp("X-MyHeaders.*");
    result.add(new StandaloneProducer(smtps));

    return result;
  }

  @Override
  protected String createBaseFileName(Object object) {
    String basename = super.createBaseFileName(object);
    StandaloneProducer c = (StandaloneProducer) object;
    String s = ((DefaultSmtpProducer) c.getProducer()).getSmtpUrl();
    int pos = s.indexOf(":");
    if (pos > 0) {
      basename = basename + "-" + s.substring(0, pos).toUpperCase();
    }
    return basename;
  }

  protected StandaloneProducer createProducerForTests(GreenMail gm) {
    DefaultSmtpProducer smtp = new DefaultSmtpProducer();
    SmtpServer server = gm.getSmtp();
    String smtpUrl = server.getProtocol() + "://localhost:" + server.getPort();
    smtp.setSmtpUrl(smtpUrl);
    smtp.setSubject("Junit Test for com.adaptris.core.mail");
    smtp.setFrom(JunitMailHelper.DEFAULT_SENDER);
    smtp.setContentType("plain/text");
    smtp.setDestination(new ConfiguredProduceDestination(JunitMailHelper.DEFAULT_RECEIVER));
    return new StandaloneProducer(new NullConnection(), smtp);
  }

}
