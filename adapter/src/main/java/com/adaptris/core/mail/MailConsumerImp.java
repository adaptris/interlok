package com.adaptris.core.mail;

import java.util.List;
import java.util.StringTokenizer;

import javax.mail.internet.MimeMessage;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import com.adaptris.annotation.AdvancedConfig;
import com.adaptris.annotation.AutoPopulated;
import com.adaptris.annotation.InputFieldHint;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisPollingConsumer;
import com.adaptris.core.CoreException;
import com.adaptris.mail.JavamailReceiverFactory;
import com.adaptris.mail.MailException;
import com.adaptris.mail.MailReceiver;
import com.adaptris.mail.MailReceiverFactory;
import com.adaptris.mail.MailboxClient;
import com.adaptris.security.password.Password;
import com.adaptris.util.license.License;
import com.adaptris.util.license.License.LicenseType;

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
 * Because the combination of destination and filter form the uniqueness of the workflow, it is important to configure the
 * filter-expression correctly, if 1 or more workflows contain a filter-expression that evaulates to essentially the same thing,
 * then results are undefined in the context of retries and error handling.
 * </p>
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
  private Boolean deleteOnReceive; // false
  @AdvancedConfig
  private Boolean attemptConnectOnInit;

  @AdvancedConfig
  private String fromFilter;
  @AdvancedConfig
  private String subjectFilter;
  @AdvancedConfig
  private String recipientFilter;
  @AdvancedConfig
  private String regularExpressionStyle;
  @InputFieldHint(style = "PASSWORD")
  private String password;
  private String username;
  @NotNull
  @AutoPopulated
  @Valid
  private MailReceiverFactory mailReceiverFactory;

  protected transient MailReceiver mbox;

  /**
   * <p>
   * Creates a new instance.
   * </p>
   * <ul>
   * <li>regularExpressionStyle is defaulted to "GLOB"</li>
   * <li>preserveHeaders is defaulted to false</li>
   * <li>headerPrefix is defaulted to ""</li>
   * <li>deleteOnReceive is defaulted to false</li>
   * </ul>
   */
  public MailConsumerImp() {
    // null protection...
    regularExpressionStyle = "GLOB";
    mailReceiverFactory = new JavamailReceiverFactory();
  }

  /**
   *
   * @see com.adaptris.core.AdaptrisComponent#isEnabled(License)
   */
  @Override
  public boolean isEnabled(License license) throws CoreException {
    return license.isEnabled(LicenseType.Basic);
  }


  @Override
  protected int processMessages() {
    int count = 0;

    try {
      mbox.connect();

      if (log.isTraceEnabled()) {
        log.trace("there are " + mbox.getMessages().size()
            + " messages to process");
      }

      for (MimeMessage msg : mbox.getMessages()){
        try {
          mbox.setMessageRead(msg);
          List<AdaptrisMessage> msgs = createMessages(msg);
          for (AdaptrisMessage m : msgs) {
            retrieveAdaptrisMessageListener().onAdaptrisMessage(m);
          }
        }
        catch (MailException e) {
          mbox.resetMessage(msg);
          log.debug("Error processing "
              + (msg != null ? msg.getMessageID() : "<null>"), e);
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
      mbox.disconnect();
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
      initFilters(getDestination().getFilterExpression());

      if (log.isTraceEnabled()) {
        log.trace("From filter set to " + fromFilter);
        log.trace("Subject filter set to " + subjectFilter);
        log.trace("Recipient filter set to " + recipientFilter);
      }

      mbox.setFromFilter(fromFilter);
      mbox.setSubjectFilter(subjectFilter);
      mbox.setRecipientFilter(recipientFilter);
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
  private void initFilters(String filterString) {
    if (filterString == null || filterString.equalsIgnoreCase("*")
        || filterString.equals("")) {
      return;
    }
    StringTokenizer st = new StringTokenizer(filterString, ",");

    while (st.hasMoreTokens()) {
      String filter = st.nextToken();
      StringTokenizer fst = new StringTokenizer(filter, "=");
      String key = fst.nextToken().trim();
      String value = fst.nextToken();
      if (key.equalsIgnoreCase(FROM)) {
        fromFilter = value;
      }
      if (key.equalsIgnoreCase(SUBJECT)) {
        subjectFilter = value;
      }
      if (key.equalsIgnoreCase(RECIPIENT)) {
        recipientFilter = value;
      }
    }
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
   * @see MailboxClient#setRegularExpressionCompiler(java.lang.String)
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
   * the password here if you wish to use {@link Password#decode(String)} to decode the password.
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
}
