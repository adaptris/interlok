package com.adaptris.mail;

import javax.mail.URLName;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import com.adaptris.annotation.AutoPopulated;
import com.adaptris.util.KeyValuePairSet;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * {@link MailReceiverFactory} that uses standard Javamail functionality and supports both POP3 + IMAP
 * 
 * @config javamail-receiver-factory
 * @author lchan
 * @see MailboxClient
 */
@XStreamAlias("javamail-receiver-factory")
public class JavamailReceiverFactory implements MailReceiverFactory {
  @NotNull
  @AutoPopulated
  @Valid
  private KeyValuePairSet sessionProperties;

  public JavamailReceiverFactory() {
    setSessionProperties(new KeyValuePairSet());
  }

  @Override
  public MailReceiver createClient(URLName url) {
    return configure(new MailboxClient(url));
  }

  public KeyValuePairSet getSessionProperties() {
    return sessionProperties;
  }

  /**
   * Set the session properties to apply to any {@link MailReceiver} instances.
   * 
   * @param kp the properties
   */
  public void setSessionProperties(KeyValuePairSet kp) {
    if (kp == null) {
      throw new IllegalArgumentException("Illegal Session Propertes [" + kp + "]");
    }
    sessionProperties = kp;
  }

  private MailboxClient configure(MailboxClient mbox) {
    mbox.setSessionProperties(KeyValuePairSet.asProperties(getSessionProperties()));
    return mbox;
  }
}
