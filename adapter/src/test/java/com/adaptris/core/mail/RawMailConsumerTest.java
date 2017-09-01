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
import java.io.InputStream;
import java.io.OutputStream;

import javax.mail.internet.MimeBodyPart;

import org.apache.commons.io.IOUtils;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.StandaloneConsumer;
import com.adaptris.core.stubs.MockMessageListener;
import com.adaptris.core.util.LifecycleHelper;
import com.adaptris.mail.JunitMailHelper;
import com.adaptris.mail.Pop3ReceiverFactory;
import com.adaptris.util.text.mime.MultiPartInput;
import com.icegreen.greenmail.util.GreenMail;

public class RawMailConsumerTest extends MailConsumerCase {

  public RawMailConsumerTest(String name) {
    super(name);
  }

  @Override
  protected void setUp() throws Exception {
  }

  public void testConsumer() throws Exception {
    if (!testsEnabled()) return;
    GreenMail gm = JunitMailHelper.startServer(JunitMailHelper.DEFAULT_RECEIVER, DEFAULT_POP3_USER, DEFAULT_POP3_PASSWORD);
    try {
      sendMessage(gm);
      MockMessageListener mockListener = new MockMessageListener();
      StandaloneConsumer c = new StandaloneConsumer(createConsumerForTests(gm));
      c.registerAdaptrisMessageListener(mockListener);
      LifecycleHelper.initAndStart(c);
      waitForMessages(mockListener, 1);
      LifecycleHelper.stopAndClose(c);
      assertTrue(mockListener.getMessages().size() >= 1);
      compare(mockListener.getMessages().get(0), TEXT_PAYLOADS[0]);
    }
    finally {
      JunitMailHelper.stopServer(gm);
    }
  }

  public void testConsumer_MetadataHeaders() throws Exception {
    if (!testsEnabled()) return;
    GreenMail gm = JunitMailHelper.startServer(JunitMailHelper.DEFAULT_RECEIVER, DEFAULT_POP3_USER, DEFAULT_POP3_PASSWORD);
    try {
      sendMessage(gm);
      MockMessageListener mockListener = new MockMessageListener();
      MailConsumerImp imp = createConsumerForTests(gm);
      imp.setHeaderHandler(new MetadataMailHeaders());
      StandaloneConsumer c = new StandaloneConsumer(imp);
      c.registerAdaptrisMessageListener(mockListener);
      LifecycleHelper.initAndStart(c);
      waitForMessages(mockListener, 1);
      LifecycleHelper.stopAndClose(c);
      AdaptrisMessage prdMsg = mockListener.getMessages().get(0);
      compare(prdMsg, TEXT_PAYLOADS[0]);
      assertEquals(JunitMailHelper.DEFAULT_RECEIVER, prdMsg.getMetadataValue("To"));
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
      MockMessageListener mockListener = new MockMessageListener();
      StandaloneConsumer c = new StandaloneConsumer(createConsumerForTests(gm, new Pop3ReceiverFactory()));
      c.registerAdaptrisMessageListener(mockListener);
      LifecycleHelper.initAndStart(c);
      waitForMessages(mockListener, 1);
      LifecycleHelper.stopAndClose(c);

      assertTrue(mockListener.getMessages().size() >= 1);
      compare(mockListener.getMessages().get(0), TEXT_PAYLOADS[0]);
    }
    finally {
      JunitMailHelper.stopServer(gm);
    }
  }

  @Override
  protected RawMailConsumer create() {
    return new RawMailConsumer();
  }

  private void compare(AdaptrisMessage msg, String expected) throws Exception {
    try (InputStream msgIn = msg.getInputStream(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
      MultiPartInput mime = new MultiPartInput(msgIn, false);
      MimeBodyPart part = (MimeBodyPart) mime.next();
      try (InputStream partIn = part.getInputStream(); OutputStream bout = out) {
        IOUtils.copy(partIn, bout);
      }
      assertEquals(expected, out.toString());
    }

  }
}
