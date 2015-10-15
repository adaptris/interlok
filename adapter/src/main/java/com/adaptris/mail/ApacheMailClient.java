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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.URLName;
import javax.mail.internet.MimeMessage;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.input.ReaderInputStream;
import org.apache.commons.net.pop3.POP3Client;
import org.apache.commons.net.pop3.POP3MessageInfo;

abstract class ApacheMailClient<T extends POP3Client> extends MailClientImp {

  private transient URLName mailboxUrl;
  private transient ApacheClientConfig clientConfig;
  private transient T pop3;
  private transient Map<String, Integer> collectedMessages;
  private transient Set<String> messagesToDelete;

  ApacheMailClient() {
    super();
  }

  public ApacheMailClient(URLName url, ApacheClientConfig configurator) {
    this();
    mailboxUrl = url;
    this.clientConfig = configurator;
  }

  @Override
  public void connectLocal() throws MailException {
    try {
      pop3 = createClient();
      clientConfig.preConnectConfigure(pop3);
      pop3.connect(mailboxUrl.getHost(), mailboxUrl.getPort());
      postConnectAction(pop3);
      clientConfig.postConnectConfigure(pop3);
      if (!pop3.login(mailboxUrl.getUsername(), mailboxUrl.getPassword())) {
        throw new Exception("Could not login to server, check username/password.");
      }
    }
    catch (Exception e) {
      disconnectLocal();
      rethrowMailException(e);
    }
    finally {
      collectedMessages = new HashMap<>();
      messagesToDelete = new LinkedHashSet<>();
    }
  }

  abstract T createClient() throws MailException;

  abstract void postConnectAction(T client) throws MailException, IOException;

  @Override
  public ArrayList<MimeMessage> collectMessages() throws MailException {
    ArrayList<MimeMessage> result = new ArrayList<MimeMessage>();

    try {
      POP3MessageInfo[] pop3messages = pop3.listMessages();
      Session session = Session.getDefaultInstance(new Properties(), null);
      // Convert messages to MimeMessages
      if (pop3messages != null && pop3messages.length > 0) {
        for (POP3MessageInfo message : pop3messages) {
          StringWriter writer = new StringWriter();
          BufferedReader bufferedReader = null;
          ReaderInputStream mimeMessageInput = null;
          try {
            Reader reader = pop3.retrieveMessage(message.number);
            if (reader == null) {
              throw new Exception("Could not retrieve message header.");
            }
            MimeMessage mimeMsg = createMimeMessage(session, reader);
            log.trace("Parsing message [{}] (msgNum={})", mimeMsg.getMessageID(), message.number);
            result.add(mimeMsg);
            collectedMessages.put(mimeMsg.getMessageID(), message.number);
          }
          finally {
            IOUtils.closeQuietly(mimeMessageInput);
            IOUtils.closeQuietly(bufferedReader);
            IOUtils.closeQuietly(writer);
          }
        }
      }
    }
    catch (Exception e) {
      throw new MailException(e);
    }

    return result;
  }

  private MimeMessage createMimeMessage(Session session, Reader src) throws IOException, MessagingException {
    MimeMessage result = null;
    StringWriter writer = new StringWriter();
    try (BufferedReader bufferedReader = new BufferedReader(src)) {
      IOUtils.copy(bufferedReader, writer);
    }
    try (InputStream mimeMessageInput = IOUtils.toInputStream(writer.toString())) {
      result = new MimeMessage(session, mimeMessageInput);
    }
    return result;
  }

  @Override
  public void disconnectLocal() {
    if (clientConnected()) {
      deleteQuietly();
      disconnectQuietly();
    }
    collectedMessages = new HashMap<>();
    messagesToDelete = new LinkedHashSet<>();
    pop3 = null;
  }

  private void deleteQuietly() {
    // Delete messages
    if (deleteFlag) {
      for (String msgId : messagesToDelete) {
        try {
          Integer msgNum = collectedMessages.get(msgId);
          log.warn("Deleting [{}] (msgNum={})", msgId, msgNum);
          if (msgNum != null) {
            pop3.deleteMessage(msgNum.intValue());
          }
        }
        catch (Exception e) {
        }
      }
    }
  }

  private void disconnectQuietly() {
    try {
      pop3.logout();
      pop3.disconnect();
    }
    catch (Exception e) {
    }
  }

  private boolean clientConnected() {
    return pop3 != null && pop3.isConnected();
  }

  @Override
  // Messages marked as read (as in they match the filter) will be added to the list of messages to delete.
  public void setMessageRead(MimeMessage msg) throws MailException {
    try {
      messagesToDelete.add(msg.getMessageID());
    }
    catch (MessagingException e) {
      rethrowMailException(e);
    }
  }

  @Override
  public void resetMessage(MimeMessage msg) throws MailException {
    try {
      messagesToDelete.remove(msg.getMessageID());
    }
    catch (MessagingException e) {
      rethrowMailException(e);
    }
  }

  static void rethrowMailException(Throwable e) throws MailException {
    rethrowMailException(e.getMessage(), e);
  }

  static void rethrowMailException(String msg, Throwable e) throws MailException {
    if (e instanceof MailException) {
      throw (MailException) e;
    }
    throw new MailException(msg, e);
  }
}
