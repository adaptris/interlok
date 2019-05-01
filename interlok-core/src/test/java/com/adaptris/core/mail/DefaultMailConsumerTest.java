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

import static com.adaptris.mail.JunitMailHelper.testsEnabled;

import java.io.FileInputStream;
import java.util.List;
import java.util.Properties;

import javax.mail.Session;
import javax.mail.internet.MimeMessage;

import com.adaptris.core.Adapter;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.ConfiguredConsumeDestination;
import com.adaptris.core.CoreException;
import com.adaptris.core.StandaloneConsumer;
import com.adaptris.core.metadata.NoOpMetadataFilter;
import com.adaptris.core.stubs.MockMessageListener;
import com.adaptris.core.stubs.MockMessageProducer;
import com.adaptris.core.util.LifecycleHelper;
import com.adaptris.mail.JunitMailHelper;
import com.adaptris.mail.Pop3ReceiverFactory;
import com.adaptris.mail.Pop3sReceiverFactory;
import com.adaptris.util.text.mime.NullPartSelector;
import com.adaptris.util.text.mime.SelectByPosition;
import com.icegreen.greenmail.util.GreenMail;

public class DefaultMailConsumerTest extends MailConsumerCase {

  private static final String EMAIL_WITH_CD = "mail.email.with.cd";
  private static final String EMAIL_WITHOUT_CD = "mail.email.without.cd";

  public DefaultMailConsumerTest(String name) {
    super(name);
  }

  public void testConsume_NoHandler() throws Exception {
    if (!testsEnabled()) return;
    GreenMail gm = JunitMailHelper.startServer(JunitMailHelper.DEFAULT_RECEIVER, DEFAULT_POP3_USER, DEFAULT_POP3_PASSWORD);
    try {
      sendMessage(gm);
      MockMessageListener mockListener = new MockMessageListener();
      MailConsumerImp imp = createConsumerForTests(gm);
      imp.getDestination().setFilterExpression("*");
      StandaloneConsumer c = new StandaloneConsumer(imp);
      c.registerAdaptrisMessageListener(mockListener);
      LifecycleHelper.initAndStart(c);
      waitForMessages(mockListener, 1);
      LifecycleHelper.stopAndClose(c);
      AdaptrisMessage prdMsg = mockListener.getMessages().get(0);
      assertEquals(TEXT_PAYLOADS[0], prdMsg.getContent());
      assertFalse(prdMsg.headersContainsKey("To"));
      assertFalse(prdMsg.headersContainsKey("From"));
    }
    finally {
      JunitMailHelper.stopServer(gm);
    }
  }

  public void testConsume_MetadataHandler() throws Exception {
    if (!testsEnabled()) return;
    GreenMail gm = JunitMailHelper.startServer(JunitMailHelper.DEFAULT_RECEIVER, DEFAULT_POP3_USER, DEFAULT_POP3_PASSWORD);
    try {

      sendMessage(gm);
      MockMessageListener mockListener = new MockMessageListener();
      DefaultMailConsumer imp = (DefaultMailConsumer) createConsumerForTests(gm);
      imp.setRegularExpressionStyle(REGEX_STYLE);
      imp.getDestination().setFilterExpression("SUBJECT=.*");
      imp.setHeaderHandler(new MetadataMailHeaders().withHeaderFilter(new NoOpMetadataFilter()));

      StandaloneConsumer c = new StandaloneConsumer(imp);
      c.registerAdaptrisMessageListener(mockListener);
      LifecycleHelper.initAndStart(c);
      waitForMessages(mockListener, 1);
      LifecycleHelper.stopAndClose(c);
      AdaptrisMessage prdMsg = mockListener.getMessages().get(0);
      assertEquals(TEXT_PAYLOADS[0], prdMsg.getContent());
      assertEquals(JunitMailHelper.DEFAULT_RECEIVER, prdMsg.getMetadataValue("To"));
    }
    finally {
      JunitMailHelper.stopServer(gm);
    }
  }

  public void testConsume_CommonsNetPop3() throws Exception {
    if (!testsEnabled()) return;
    GreenMail gm = JunitMailHelper.startServer(JunitMailHelper.DEFAULT_RECEIVER, DEFAULT_POP3_USER, DEFAULT_POP3_PASSWORD);
    try {
      sendMessage(gm);
      MockMessageProducer mockProducer = new MockMessageProducer();
      Adapter a = createAdapter(createConsumerForTests(gm, new Pop3ReceiverFactory()), mockProducer);
      a.requestStart();
      waitForMessages(mockProducer, 1);
      a.requestClose();
      // assertEquals(1, mockProducer.getMessages().size());
      AdaptrisMessage prdMsg = mockProducer.getMessages().get(0);
      assertEquals("Consumed Payload", TEXT_PAYLOADS[0], prdMsg.getContent());
    }
    finally {
      JunitMailHelper.stopServer(gm);
    }
  }

  public void testConsume_CommonsNetPop3_ImapProtocol() throws Exception {
    if (!testsEnabled()) return;
    GreenMail gm = JunitMailHelper.startServer(JunitMailHelper.DEFAULT_RECEIVER, DEFAULT_POP3_USER, DEFAULT_POP3_PASSWORD);
    try {
      MockMessageProducer mockProducer = new MockMessageProducer();
      MailConsumerImp consumer = createConsumerForTests(gm, new Pop3ReceiverFactory());
      consumer.setDestination(new ConfiguredConsumeDestination("imap://localhost:" + gm.getPop3().getPort() + "/INBOX"));
      Adapter a = createAdapter(consumer, mockProducer);
      a.requestStart();
      fail();
    } catch (CoreException expected) {
      
    }
    finally {
      JunitMailHelper.stopServer(gm);
    }

  }

  public void testConsume_CommonsNetPop3S() throws Exception {
    if (!testsEnabled()) return;
    GreenMail gm = JunitMailHelper.startServer(JunitMailHelper.DEFAULT_RECEIVER, DEFAULT_POP3_USER, DEFAULT_POP3_PASSWORD);
    try {
      sendMessage(gm);
      MockMessageProducer mockProducer = new MockMessageProducer();
      Pop3sReceiverFactory pop3s = new Pop3sReceiverFactory();
      pop3s.setAlwaysTrust(true);
      pop3s.setImplicitTls(true);
      Adapter a = createAdapter(createConsumerForTests(gm.getPop3s(), pop3s), mockProducer);
      a.requestStart();
      waitForMessages(mockProducer, 1);
      a.requestClose();
      // assertEquals(1, mockProducer.getMessages().size());
      AdaptrisMessage prdMsg = mockProducer.getMessages().get(0);
      assertEquals("Consumed Payload", TEXT_PAYLOADS[0], prdMsg.getContent());
    }
    finally {
      JunitMailHelper.stopServer(gm);
    }
  }

  public void testPartSelector() throws Exception {
    if (!testsEnabled()) return;
    GreenMail gm = JunitMailHelper.startServer(JunitMailHelper.DEFAULT_RECEIVER, DEFAULT_POP3_USER, DEFAULT_POP3_PASSWORD);
    try {
      sendMessage(gm, 3);
      MockMessageProducer mockProducer = new MockMessageProducer();
      DefaultMailConsumer consumer = (DefaultMailConsumer) createConsumerForTests(gm);
      consumer.setPartSelector(new SelectByPosition(1));
      Adapter a = createAdapter(consumer, mockProducer);
      a.requestStart();
      waitForMessages(mockProducer, 1);
      a.requestClose();
      AdaptrisMessage prdMsg = mockProducer.getMessages().get(0);
      assertEquals("Consumed Payload", TEXT_PAYLOADS[1], prdMsg.getContent());
    }
    finally {
      JunitMailHelper.stopServer(gm);
    }
  }

  public void testDefaultProducerWithDefaultConsumerWithContentDispositionRedmine727() throws Exception {
    try (FileInputStream fileInputStream = new FileInputStream(PROPERTIES.getProperty(EMAIL_WITH_CD))) {
      Session session = Session.getDefaultInstance(new Properties());
      MimeMessage mimeMessage = new MimeMessage(session, fileInputStream);
      DefaultMailConsumer mailConsumer = new DefaultMailConsumer();
      List<AdaptrisMessage> messages = mailConsumer.createMessages(mimeMessage);
      assertEquals(2, messages.size());
    }
  }

  public void testDefaultProducerWithDefaultConsumerWithoutContentDispositionRedmine727() throws Exception {
    try (FileInputStream fileInputStream = new FileInputStream(PROPERTIES.getProperty(EMAIL_WITHOUT_CD))) {
      Session session = Session.getDefaultInstance(new Properties());
      MimeMessage mimeMessage = new MimeMessage(session, fileInputStream);
      DefaultMailConsumer mailConsumer = new DefaultMailConsumer();
      List<AdaptrisMessage> messages = mailConsumer.createMessages(mimeMessage);
      assertEquals(2, messages.size());
    }
  }

  @Override
  protected DefaultMailConsumer create() {
    DefaultMailConsumer c = new DefaultMailConsumer();
    c.setPartSelector(new NullPartSelector());
    return c;
  }

}
