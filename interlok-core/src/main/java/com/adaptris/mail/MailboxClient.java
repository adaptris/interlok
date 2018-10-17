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

import java.util.Iterator;
import java.util.Properties;

import javax.mail.FetchProfile;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.URLName;
import javax.mail.internet.MimeMessage;

import com.adaptris.core.util.Args;

/**
 * Wrapper around the JavaMail API supporting all the standard protocols that are available.
 * <p>
 * POP3 folders also differ from IMAP folders in how they are manipulated.<br>
 * POP3 supports only a single folder named "INBOX".
 * <p>
 * POP3 supports <strong>no</strong> permanent flags ( <code>Folder.getPermanentFlags()</code>).
 * <p>
 * In this implementation all messages in a POP3 mailbox are considered new. Therefore in order to avoid re-processing messages,
 * <code>purge(true)</code> should be called prior to disconnection
 * <p>
 * If getNextMessage() is invoked, then that message is immediatedly marked as "SEEN" and "DELETED" to avoid processing by other
 * connections.
 * <p>
 * Message.saveChanges() is invoked to commit the change to an IMAP folder, if <code>purge(true)</code> is called prior to
 * disconnection, then those messages are deleted.
 * <p>
 * It is possible to use this mailbox client to filter messages based on the subject, sender and a pre-defined customHeader field.
 * </p>
 * <p>
 * Subject / Sender / recipient / custom Filtering is supported via the use of the org.apache.oro suite, specify your preferred
 * regular expression type using the <code>setRegularExpressionCompiler()</code> method and then you can specify the filter using
 * your preferred regular expression using the various <code>setXXXFilter</code> methods.
 * </p>
 * 
 * <pre>
 * {@code 
 *  MailboxClient pop3 = new MailboxClient
 *                    ("pop3://username:password@mymailhost:110/INBOX");
 *  pop3.setSenderFilter("FromMe*");
 *  pop3.setSubjectFilter("Test*");
 *  pop3.connect();
 *  System.out.println(pop3.getMessageCount() + " new matched messages");
 *  Message payload;
 *  while ((payload = pop3.getNextMessage()) != null) {
 *    payload.writeTo(System.out);
 *  }
 *  pop3.disconnect();
 * }
 * </pre>
 * 
 * @author lchan
 * @author $Author: lchan $
 */
public final class MailboxClient extends MailClientImp{

  private static final String POP3_PROVIDER = "pop3";
  private static final String POP3_MAILBOX = "INBOX";

  private transient Folder inbox;
  private transient Store store;

  private URLName url;
  private Properties sessionProperties;

  /** Constructor */
  private MailboxClient() {
    super();
    sessionProperties = new Properties();
  }

  /**
   * Constructor.
   * 
   * @param name the host to poll for messages
   *          pop3://user:pw@hostname:port/INBOX or
   *          imap://user:pw@hostname:port/<folder>
   */
  public MailboxClient(URLName name) {
    this();
    url = name;
  }

  /**
   * Constructor.
   * 
   * @param mailboxUrl the POP3 host to poll for messages
   *          pop3://user:pw@hostname:port/INBOX or
   *          imap://user:pw@hostname:port/<folder>
   */
  public MailboxClient(String mailboxUrl) {
    this(new URLName(mailboxUrl));
  }

  /**
   * Constructor.
   * <p>
   * If this constructor is used, then we assume that a POP3 connection is
   * required.
   * </p>
   * 
   * @param host the host
   * @param username the username
   * @param password the password.
   */
  public MailboxClient(String host, String username, String password) {
    this(new URLName(POP3_PROVIDER, host, 110, POP3_MAILBOX, username, password));
  }

  /**
   * Constructor.
   * <p>
   * If this constructor is used, then we assume that a POP3 connection is
   * required.
   * 
   * @param host the host
   * @param username the username
   * @param password the password.
   * @param port the port
   */
  public MailboxClient(String host, int port, String username, String password) {
    this(new URLName(POP3_PROVIDER, host, port, POP3_MAILBOX, username,
        password));
  }

  public void setSessionProperties(Properties p) {   
    sessionProperties = Args.notNull(p, "sessionProperties");
  }

  @Override
  protected void connectLocal() throws MailException {
    try {
      Session session = Session.getInstance(sessionProperties, null);
      store = session.getStore(url);
      store.connect();

      // store.connect(host, port, username, password);
      // Open the folder
      inbox = store.getFolder(url.getFile());
      if (inbox == null) {
        throw new MailException("No Inbox available");
      }
      inbox.open(Folder.READ_WRITE);
    }
    catch (Exception e) {
      throw new MailException(e);
    }
  }

  @Override
  public Iterator<MimeMessage> iterator() {
    if (inbox == null) {
      throw new IllegalStateException("Not connected to mail server");
    }
    return new MessageCollector();
  }

  /**
   * Disconnect from the mail server.
   */
  @Override
  protected void disconnectLocal() {
    try {
      inbox.close(deleteFlag);
      store.close();

    }
    catch (Exception e) {
    } finally {
      inbox = null;
      store = null;
    }
  }

  private class MessageCollector implements Iterator<MimeMessage> {

    private transient MimeMessage nextMessage;
    private int messageCount;
    private int currentMessage;

    private MessageCollector() {
      try {
        messageCount = inbox.getMessageCount();
        currentMessage = 1;
      } catch (MessagingException e) {
        throw new IllegalStateException(e);
      }
    }

    @Override
    public boolean hasNext() {
      if (nextMessage == null) {
        try {
          nextMessage = buildNext();
        } catch (MessagingException e) {
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

    private MimeMessage buildNext() throws MessagingException {
      if (currentMessage > messageCount) {
        return null;
      }
      MimeMessage m = (MimeMessage) inbox.getMessage(currentMessage++);
      if (!accept(m)) {
        return buildNext();
      } else {
        FetchProfile fp = new FetchProfile();
        fp.add(FetchProfile.Item.ENVELOPE);
        inbox.fetch(new Message[] {m}, fp);
      }
      log.trace("Accepted message [{}]", m.getMessageID());
      return m;
    }

  }

}
