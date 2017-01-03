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

import java.io.ByteArrayOutputStream;

import javax.mail.internet.MimeBodyPart;

import com.adaptris.core.Adapter;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.stubs.MockMessageProducer;
import com.adaptris.mail.JunitMailHelper;
import com.adaptris.mail.Pop3ReceiverFactory;
import com.adaptris.util.stream.StreamUtil;
import com.adaptris.util.text.mime.MultiPartInput;
import com.icegreen.greenmail.util.GreenMail;

public class RawMailConsumerTest extends MailConsumerCase {

  public RawMailConsumerTest(String name) {
    super(name);
  }

  /** @see junit.framework.TestCase#setUp() */
  @Override
  protected void setUp() throws Exception {
  }

  public void testConsumer() throws Exception {
    if (!testsEnabled()) return;
    GreenMail gm = JunitMailHelper.startServer(JunitMailHelper.DEFAULT_RECEIVER, DEFAULT_POP3_USER, DEFAULT_POP3_PASSWORD);
    try {
      sendMessage(gm);
      MockMessageProducer mockProducer = new MockMessageProducer();
      Adapter a = createAdapter(createConsumerForTests(gm), mockProducer);
      a.requestStart();
      Thread.sleep(3000);
      a.requestClose();
      assertTrue(mockProducer.getMessages().size() >= 1);
      AdaptrisMessage prdMsg = mockProducer.getMessages().get(0);
      MultiPartInput mime = new MultiPartInput(prdMsg.getInputStream(), false);
      MimeBodyPart part = (MimeBodyPart) mime.next();
      ByteArrayOutputStream out = new ByteArrayOutputStream();
      StreamUtil.copyStream(part.getInputStream(), out);
      assertEquals("Text Payload", TEXT_PAYLOADS[0], out.toString());
    }
    finally {
      JunitMailHelper.stopServer(gm);
    }
  }

  public void testConsumer_CommonsNet() throws Exception {
    if (!testsEnabled()) return;
    GreenMail gm = JunitMailHelper.startServer(JunitMailHelper.DEFAULT_RECEIVER, DEFAULT_POP3_USER, DEFAULT_POP3_PASSWORD);
    try {
      sendMessage(gm);
      MockMessageProducer mockProducer = new MockMessageProducer();
      Adapter a = createAdapter(createConsumerForTests(gm, new Pop3ReceiverFactory()), mockProducer);
      a.requestStart();
      Thread.sleep(3000);
      a.requestClose();
      assertTrue(mockProducer.getMessages().size() >= 1);
      AdaptrisMessage prdMsg = mockProducer.getMessages().get(0);
      MultiPartInput mime = new MultiPartInput(prdMsg.getInputStream(), false);
      MimeBodyPart part = (MimeBodyPart) mime.next();
      ByteArrayOutputStream out = new ByteArrayOutputStream();
      StreamUtil.copyStream(part.getInputStream(), out);
      assertEquals("Text Payload", TEXT_PAYLOADS[0], out.toString());
    }
    finally {
      JunitMailHelper.stopServer(gm);
    }
  }

  @Override
  protected MailConsumerImp create() {
    return new RawMailConsumer();
  }
}
