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

import static org.apache.commons.lang.StringUtils.defaultIfEmpty;
import static org.apache.commons.lang.StringUtils.isBlank;
import static org.apache.commons.lang.StringUtils.split;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.mail.internet.MimeMessage;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.apache.commons.io.IOUtils;

import com.adaptris.annotation.AdvancedConfig;
import com.adaptris.annotation.AutoPopulated;
import com.adaptris.annotation.InputFieldDefault;
import com.adaptris.annotation.InputFieldHint;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisPollingConsumer;
import com.adaptris.core.CoreException;
import com.adaptris.core.util.Args;
import com.adaptris.mail.JavamailReceiverFactory;
import com.adaptris.mail.MailException;
import com.adaptris.mail.MailReceiver;
import com.adaptris.mail.MailReceiverFactory;

/**
 * Email implementation of the AdaptrisMessageConsumer interface.
 * <p>
 * The consume destination configuration is the mailbox url in the form {@code  pop3|imap://user:password@host:port/mailbox} where
 * mailbox follows the rule {@code url.getProtocol() == "pop3" ? "INBOX" : "any arbitary folder"}
 * </p>
 * <p>
 * Possible filter expressions could be :-
 * <ul>
 * <li>&lt;filter-expression&gt;*&lt;filter-expression&gt;</li>
 * <li>
 * &lt;filter-expression&gt;FROM=somevalue,RECIPIENT=somevalue,SUBJECT=somevalue &lt;filter-expression&gt;</li>
 * <li>&lt;filter-expression&gt;FROM=somevalue,SUBJECT=somevalue &lt;filter-expression&gt;</li>
 * <li>&lt;filter-expression&gt;FROM=somevalue&lt;filter-expression&gt;</li>
 * <li>&lt;filter-expression&gt;SUBJECT=somevalue&lt;filter-expression&gt;</li>
 * <li>&lt;filter-expression&gt;RECIPIENT=somevalue&lt;filter-expression&gt;</li>
 * </ul>
 * A missing filter-expression implicitly means all. If you are performing filtering on the recipient, then an attempt will be made
 * to match the filter expression against all the recipients for the email.
 * </p>
 * <p>
 * The default filter-expression syntax is based on Unix shell glob expressions. It can be changed by using the
 * {@link #setRegularExpressionStyle(String)} method.
 * <p>
 * It is possible to control the underlying behaviour of this consumer through the use of various properties that will be passed to
 * the <code>javax.mail.Session</code> instance. You need to refer to the javamail documentation to see a list of the available
 * properties and meanings.
 * </p>
 */

public abstract class MailConsumerImp extends AdaptrisPollingConsumer{

  private static final String RECIPIENT = "RECIPIENT";
  private static final String SUBJECT = "SUBJECT";
  private static final String FROM = "FROM";


  // marshalled
  @AdvancedConfig
  @InputFieldDefault(value = "false")
  private Boolean deleteOnReceive; // false
  @AdvancedConfig
  @InputFieldDefault(value = "true")
  private Boolean attemptConnectOnInit;
  @AdvancedConfig
  private String regularExpressionStyle;
  @InputFieldHint(style = "PASSWORD")
  private String password;
  private String username;
  @NotNull
  @AutoPopulated
  @Valid
  private MailReceiverFactory mailReceiverFactory;
  @AdvancedConfig
  @Valid
  @InputFieldDefault(value = "ignore-mail-headers")
  private MailHeaderHandler headerHandler;

  protected transient MailReceiver mbox;

  /**
   * <p>
   * Creates a new instance.
   * </p>
   * <ul>
   * <li>regularExpressionStyle is defaulted to "GLOB"</li>
   * <li>deleteOnReceive is defaulted to false</li>
   * </ul>
   */
  public MailConsumerImp() {
    // null protection...
    regularExpressionStyle = "GLOB";
    mailReceiverFactory = new JavamailReceiverFactory();
    setHeaderHandler(new IgnoreMailHeaders());
  }

  @Override
  protected void prepareConsumer() throws CoreException {
  }



  @Override
  protected int processMessages() {
    int count = 0;

    try {
      mbox.connect();
      // log.trace("there are {} messages to process", mbox.getMessages().size());
      for (MimeMessage msg : mbox) {
        try {
          mbox.setMessageRead(msg);
          List<AdaptrisMessage> msgs = createMessages(msg);
          for (AdaptrisMessage m : msgs) {
            retrieveAdaptrisMessageListener().onAdaptrisMessage(m);
          }
        }
        catch (MailException e) {
          mbox.resetMessage(msg);
          log.debug("Error processing {}",(msg != null ? msg.getMessageID() : "<null>"), e);
        }
        count++;
        if (!continueProcessingMessages()) {
          break;
        }
      }
    }
    catch (Exception e) {
      log.trace("Error reading mailbox", e);
    }
    finally {
      IOUtils.closeQuietly(mbox);
    }
    return count;
  }

  protected abstract List<AdaptrisMessage> createMessages(MimeMessage mime)
      throws MailException, CoreException;

  protected abstract void initConsumer() throws CoreException;

  /**
   * @see com.adaptris.core.AdaptrisComponent#init()
   */
  @Override
  public final void init() throws CoreException {

    try {
      mbox = getMailReceiverFactory().createClient(
          MailHelper.createURLName(getDestination().getDestination(), getUsername(), getPassword()));
      //mbox = new MailComNetClient("localhost", 3110, getUsername(), Password.decode(getPassword()));
      mbox.setRegularExpressionCompiler(getRegularExpressionStyle());
      Map<String, String> filters = initFilters(getDestination().getFilterExpression());

      log.trace("From filter set to [{}]", filters.get(FROM));
      log.trace("Subject filter set to [{}]", filters.get(SUBJECT));
      log.trace("Recipient filter set to [{}]", filters.get(RECIPIENT));

      mbox.setFromFilter(filters.get(FROM));
      mbox.setSubjectFilter(filters.get(SUBJECT));
      mbox.setRecipientFilter(filters.get(RECIPIENT));
      // Make an attempt to connect just to be sure that we can
      if (attemptConnectOnInit()) {
        mbox.connect();
        mbox.disconnect();
      }
      mbox.purge(deleteOnReceive());
    }
    catch (Exception e) {
      throw new CoreException(e);
    }
    initConsumer();
    super.init();
  }


  /**
   * Set the filtering on the consume destination.
   */
  private Map<String, String> initFilters(String filterString) {
    Map<String, String> result = new HashMap<String, String>();
    if (isBlank(filterString) || filterString.equalsIgnoreCase("*")) {
      return result;
    }
    String[] filters = split(filterString, ",");
    for (String f : filters) {
      addFilter(result, split(f, "=", 2));
    }
    return result;
  }

  private void addFilter(Map<String, String> filters, String[] nv) {
    if (nv.length < 2) {
      return;
    }
    filters.put(defaultIfEmpty(nv[0], ""), defaultIfEmpty(nv[1], ""));
  }

  /**
   * Specify whether messages should be deleted after receipt.
   *
   * @param b true or false.
   */
  public void setDeleteOnReceive(Boolean b) {
    deleteOnReceive = b;
  }

  /**
   * Get the flag.
   *
   * @return true or false.
   */
  public Boolean getDeleteOnReceive() {
    return deleteOnReceive;
  }

  boolean deleteOnReceive() {
    return getDeleteOnReceive() != null ? getDeleteOnReceive().booleanValue() : false;
  }
  /**
   * returns the regularExpressionSyntax.
   *
   * @return returns the regularExpressionSyntax.
   * @see #setRegularExpressionStyle(String)
   */
  public String getRegularExpressionStyle() {
    return regularExpressionStyle;
  }

  /**
   * Set the regular expression syntax.
   *
   * @param s The regularExpressionSyntax to set, it should be one of "PERL5",
   *          "GLOB", or "AWK"
   */
  public void setRegularExpressionStyle(String s) {
    regularExpressionStyle = s;
  }

  public String getPassword() {
    return password;
  }

  /**
   * Set the password to be used with this consumer implementation.
   * <p>
   * If you specify the username and password in the URL for the SMTP server then does not lend itself to being encrypted. Specify
   * the password here if you wish to use {@link com.adaptris.security.password.Password#decode(String)} to decode the password.
   * </p>
   *
   * @param pw the password.
   */
  public void setPassword(String pw) {
    password = pw;
  }

  public String getUsername() {
    return username;
  }

  /**
   * Set the username to be used with this consumer implementation.
   *
   * @param name the username.
   */
  public void setUsername(String name) {
    username = name;
  }

  public Boolean getAttemptConnectOnInit() {
    return attemptConnectOnInit;
  }

  /**
   * Specify whether or not to attempt a connection upon {@link #init()}.
   * 
   * @param b true to attempt a connection on init, false otherwise, default is true if unspecified.
   */
  public void setAttemptConnectOnInit(Boolean b) {
    this.attemptConnectOnInit = b;
  }

  boolean attemptConnectOnInit() {
    return getAttemptConnectOnInit() != null ? getAttemptConnectOnInit().booleanValue() : true;
  }

  public MailReceiverFactory getMailReceiverFactory() {
    return mailReceiverFactory;
  }

  /**
   * Set the type of client to use to connect to the mailbox.
   * 
   * @param f the {@link MailReceiverFactory}, default is a {@link JavamailReceiverFactory}
   */
  public void setMailReceiverFactory(MailReceiverFactory f) {
    this.mailReceiverFactory = f;
  }

  public MailHeaderHandler getHeaderHandler() {
    return headerHandler;
  }

  /**
   * Specify how to handle mails headers
   * 
   * @param mh the handler, defaults to {@link IgnoreMailHeaders}.
   */
  public void setHeaderHandler(MailHeaderHandler mh) {
    this.headerHandler = Args.notNull(mh, "headerHandler");
  }

  protected MailHeaderHandler headerHandler() {
    return getHeaderHandler();
  }
}
