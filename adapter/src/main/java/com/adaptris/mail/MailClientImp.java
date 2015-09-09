package com.adaptris.mail;

import java.util.ArrayList;
import java.util.List;

import javax.mail.Flags;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

import org.apache.oro.text.GlobCompiler;
import org.apache.oro.text.regex.MalformedPatternException;
import org.apache.oro.text.regex.PatternCompiler;
import org.apache.oro.text.regex.PatternMatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class MailClientImp implements MailReceiver {

  private static final Class DEFAULT_COMPILER = GlobCompiler.class;
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
    messages = new ArrayList<MimeMessage>();
    setRegularExpressionCompiler(DEFAULT_COMPILER.getName());
  }

  /**
   * Connect to the mailbox.
   * <p>
   * This will read the inbox and build a local list of messages filtering out the ones that don't fit the supplied patterns.
   * </p>
   * 
   * @throws MailException if the connection failed.
   */
  @Override
  public void connect() throws MailException {
    if (connected) {
      return;
    }
    setupFilters();
    connectLocal();
    List<MimeMessage> msgs = collectMessages();
    messages = filterMessages(msgs);
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

  /**
   * Retrieve messages from the server into this client's memory.
   */
  protected abstract List<MimeMessage> collectMessages() throws MailException;

  /**
   * Disconnect from the mail server
   */
  @Override
  public void disconnect() {
    connected = false;
    disconnectLocal();
    messages = new ArrayList<MimeMessage>();
  }

  /**
   * Disconnect from the mail server
   * <p>
   * This will purge messages if necessary and close folders & connections
   * </p>
   * 
   */
  protected abstract void disconnectLocal();

  /**
   * Set the subject filter.
   * <p>
   * This filters the subject that is present in the message
   * 
   * @param filter the filter.
   */
  @Override
  public void setSubjectFilter(String filter) {
    subjectFilter = filter;
  }

  /**
   * Set the sender filter.
   * <p>
   * This filters the sender that is present in the message
   * 
   * @param filter the filter.
   */
  @Override
  public void setFromFilter(String filter) {
    fromFilter = filter;
  }

  /**
   * Set the sender filter.
   * <p>
   * This filters the sender that is present in the message
   * 
   * @param filter the filter.
   */
  @Override
  public void setRecipientFilter(String filter) {
    recipientFilter = filter;
  }

  /**
   * Add a custom filter
   * <p>
   * This filters any specific user header that is present in the message
   * 
   * @param filter the filter.
   * @param headerValue the header value
   */
  @Override
  public void addCustomFilter(String headerValue, String filter) {
    if (headerValue == null || headerValue.equals("")) {
      return;
    }
    customFilters.add(new String[]
    {
        headerValue, filter
    });
  }

  /**
   * Set the handler for regular expressions.
   * 
   * @param type one of "GLOB", "AWK", "PERL5" or their respective compiler classnames
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
      }
      catch (Exception e) {
        ;
      }
    }
    catch (Exception e) {
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
    return messages;
  }

  private void setupFilters() throws MailException {
    if (filters.size() > 0) {
      return;
    }

    try {
      PatternCompiler compiler = RegExpFactory.getCompiler(regExpHandler);
      PatternMatcher matcher = RegExpFactory.getMatcher(compiler);
      if (fromFilter != null) {
        filters.add(new FromFilter(matcher, compiler.compile(fromFilter)));
      }
      if (subjectFilter != null) {
        filters.add(new SubjectFilter(matcher, compiler.compile(subjectFilter)));
      }
      if (recipientFilter != null) {
        filters.add(new RecipientFilter(matcher, compiler.compile(recipientFilter)));
      }
      for (String[] customFilter : customFilters) {
        filters.add(new CustomHeaderFilter(matcher, compiler.compile(customFilter[1]), customFilter[0]));
      }
    }
    catch (MalformedPatternException e) {
      throw new MailException(e.getMessage(), e);
    }
  }

  private List<MimeMessage> filterMessages(List<MimeMessage> mimeMessages) throws MailException {
    List<MimeMessage> filtered = new ArrayList<MimeMessage>();
    for (MimeMessage m : mimeMessages) {
      try {
        if (m.isSet(Flags.Flag.SEEN) || m.isSet(Flags.Flag.DELETED)) {
          continue;
        }

        int matches = 0;
        for (MessageFilter mf : filters) {
          if (mf.accept(m)) {
            matches++;
          }
        }
        if (matches == filters.size()) {
          log.trace("message [{}] matches filters", m.getMessageID());
          filtered.add(m);
        }
        else {
          log.trace("Ignoring message [{}] filters not matched", m.getMessageID());
        }
      }
      catch (MessagingException e) {
        throw new MailException(e);
      }
    }
    return filtered;
  }

}
