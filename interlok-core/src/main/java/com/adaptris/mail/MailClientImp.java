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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.mail.Flags;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class MailClientImp implements MailReceiver {

  private static final String DEFAULT_REGEX_STYLE = "REGEX";

  protected transient Logger log = LoggerFactory.getLogger(this.getClass());
  protected boolean deleteFlag = false;
  private boolean connected = false;
  private List<MessageFilter> filters;
  private String fromFilter, recipientFilter, subjectFilter;
  private List<String[]> customFilters;
  private String regExpHandler;
  private List<MimeMessage> messages;

  protected MailClientImp() {
    filters = new ArrayList<MessageFilter>();
    customFilters = new ArrayList<String[]>();
    setRegularExpressionCompiler(DEFAULT_REGEX_STYLE);
  }

  @Override
  public void connect() throws MailException {
    if (connected) {
      return;
    }
    setupFilters();
    connectLocal();
    // List<MimeMessage> msgs = collectMessages();
    // messages = filterMessages(msgs);
    connected = true;
  }

  /**
   * Connect to the mailbox.
   * <p>
   * This will get and open the inbox folder
   * </p>
   * 
   * @throws MailException if the connection failed.
   */
  protected abstract void connectLocal() throws MailException;

  private ArrayList<MimeMessage> collectMessages() {
    ArrayList<MimeMessage> msgs = new ArrayList<MimeMessage>();
    for (Iterator<MimeMessage> i = iterator(); i.hasNext();) {
      msgs.add(i.next());
    }
    return msgs;
  }

  @Override
  public void disconnect() {
    close();
  }

  @Override
  public void close() {
    connected = false;
    disconnectLocal();
    messages = null;
  }

  /**
   * Disconnect from the mail server
   * <p>
   * This will purge messages if necessary and close folders & connections
   * </p>
   * 
   */
  protected abstract void disconnectLocal();

  @Override
  public void setSubjectFilter(String filter) {
    subjectFilter = filter;
  }

  @Override
  public void setFromFilter(String filter) {
    fromFilter = filter;
  }

  @Override
  public void setRecipientFilter(String filter) {
    recipientFilter = filter;
  }

  @Override
  public void addCustomFilter(String headerValue, String filter) {
    if (StringUtils.isNotEmpty(headerValue)) {
      customFilters.add(new String[] {headerValue, filter});
    }
  }

  /**
   * Set the handler for regular expressions.
   * 
   * @param type a regular expression compiler, default is "REGEX" which uses java.util
   *        regular expressions.
   */
  @Override
  public void setRegularExpressionCompiler(String type) {
    filters.clear();
    regExpHandler = type;
  }

  /**
   * Specify whether to delete messages on disconnect.
   * <p>
   * By default all messages that are retrieved have the <b>Flags.Flag.DELETED</b> flag set, which means that if this is set to true
   * then the messages will be deleted when disconnect is called
   * </p>
   * 
   * @param delFlag true or false
   */
  @Override
  public void purge(boolean delFlag) {
    deleteFlag = delFlag;
  }

  @Override
  public void setMessageRead(MimeMessage msg) throws MailException {
    try {
      if (msg.isSet(Flags.Flag.SEEN) || msg.isSet(Flags.Flag.DELETED)) {
        return;
      }
      msg.setFlag(Flags.Flag.SEEN, true);
      msg.setFlag(Flags.Flag.DELETED, deleteFlag);
      try {
        // When accessing a POP3 mailbox the sun provider
        // doesn't allow you to save changes to the message
        // status (curious)
        // To delete the messages, you should do setPurge(true);
        // This will work with imap mailboxes

        // Note that this does nothing for ComsNetClient
        msg.saveChanges();
      } catch (Exception e) {
        ;
      }
    } catch (Exception e) {
      throw new MailException(e);
    }
  }

  /**
   * Reset the state of the message so that it is no longer marked as seen or deleted.
   * 
   * @param msg the Message to reset.
   * @throws Exception on error.
   */
  @Override
  public void resetMessage(MimeMessage msg) throws Exception {
    msg.setFlag(Flags.Flag.SEEN, false);
    msg.setFlag(Flags.Flag.DELETED, false);
    msg.saveChanges();
  }

  /**
   * Get list of locally held messages
   * 
   * @return messages
   */
  @Override
  public List<MimeMessage> getMessages() {
    if (!connected) {
      throw new IllegalStateException("Not Connected");
    }
    if (messages == null) {
      messages = collectMessages();
    }
    return messages;
  }

  private void setupFilters() throws MailException {
    if (filters.size() > 0) {
      return;
    }
    try {
      filters.add(new FromFilter(MatchProxyFactory.create(regExpHandler, fromFilter)));
      filters.add(new SubjectFilter(MatchProxyFactory.create(regExpHandler, subjectFilter)));
      filters.add(new RecipientFilter(MatchProxyFactory.create(regExpHandler, recipientFilter)));
      for (String[] customFilter : customFilters) {
        filters.add(new CustomHeaderFilter(MatchProxyFactory.create(regExpHandler, customFilter[1]), customFilter[0]));
      }
    }
    catch (Exception e) {
      throw new MailException(e.getMessage(), e);
    }
  }

  protected boolean accept(MimeMessage m) throws MessagingException {
    boolean accept = false;
    if (m.isSet(Flags.Flag.SEEN) || m.isSet(Flags.Flag.DELETED)) {
      return accept;
    }
    int matches = 0;
    for (MessageFilter mf : filters) {
      if (mf.accept(m)) {
        matches++;
      }
    }
    if (matches == filters.size()) {
      log.trace("message [{}] matches filters", m.getMessageID());
      accept = true;
    } else {
      log.trace("Ignoring message [{}] filters not matched", m.getMessageID());
    }
    return accept;
  }


  protected static MailException wrapException(Throwable e) {
    return wrapException(e.getMessage(), e);
  }

  private static MailException wrapException(String msg, Throwable e) {
    if (e instanceof MailException) {
      return (MailException) e;
    }
    return new MailException(msg, e);
  }
}
