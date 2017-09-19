/*
 * Copyright 2015 Adaptris Ltd.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
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
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.URLName;
import javax.mail.internet.MimeMessage;

import org.apache.commons.io.IOUtils;
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
    } catch (Exception e) {
      disconnectLocal();
      throw wrapException(e);
    } finally {
      collectedMessages = new HashMap<>();
      messagesToDelete = new LinkedHashSet<>();
    }
  }

  abstract T createClient() throws MailException;

  abstract void postConnectAction(T client) throws MailException, IOException;


  @Override
  public Iterator<MimeMessage> iterator() {
    if (pop3 == null) {
      throw new IllegalStateException("Not Connected");
    }
    return new MessageCollector();
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
          log.trace("Deleting [{}] (msgNum={})", msgId, msgNum);
          if (msgNum != null) {
            pop3.deleteMessage(msgNum.intValue());
          }
        } catch (Exception e) {
        }
      }
    }
  }

  private void disconnectQuietly() {
    try {
      pop3.logout();
      pop3.disconnect();
    } catch (Exception e) {
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
    } catch (MessagingException e) {
      throw wrapException(e);
    }
  }

  @Override
  public void resetMessage(MimeMessage msg) throws MailException {
    try {
      messagesToDelete.remove(msg.getMessageID());
    } catch (MessagingException e) {
      throw wrapException(e);
    }
  }



  private class MessageCollector implements Iterator<MimeMessage> {

    private transient MimeMessage nextMessage;
    private POP3MessageInfo[] messages;
    private int currentMessage;
    private Session session;

    private MessageCollector() {
      try {
        messages = pop3.listMessages();
        if (messages == null) {
          messages = new POP3MessageInfo[0];
        }
        currentMessage = 0;
        session = Session.getDefaultInstance(new Properties(), null);
      } catch (IOException e) {
        throw new IllegalStateException(e);
      }
    }

    @Override
    public boolean hasNext() {
      if (nextMessage == null) {
        try {
          nextMessage = buildNext();
        } catch (MailException e) {
          log.warn("Could not construct next MimeMessage", e);
          throw new RuntimeException("Could not get next MimeMessage", e);
        }
      }
      return nextMessage != null;
    }

    @Override
    public MimeMessage next() {
      MimeMessage ret = nextMessage;
      nextMessage = null;
      return ret;
    }

    private MimeMessage buildNext() throws MailException {
      MimeMessage result = null;
      if (currentMessage >= messages.length) {
        return null;
      }
      POP3MessageInfo msg = messages[currentMessage++];
      try {
        Reader reader = pop3.retrieveMessage(msg.number);
        if (reader == null) {
          throw new MailException("Could not retrieve message header.");
        }
        result = createMimeMessage(session, reader);
        if (!accept(result)) {
          return buildNext();
        }
        log.trace("Accepted message [{}] (msgNum={})", result.getMessageID(), msg.number);
        collectedMessages.put(result.getMessageID(), msg.number);
      } catch (IOException | MessagingException e) {
        throw wrapException(e);
      }
      return result;
    }
  }
}
