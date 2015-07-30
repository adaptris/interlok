package com.adaptris.core.mail;

import static com.adaptris.core.AdaptrisMessageFactory.defaultIfNull;
import static org.apache.commons.lang.StringUtils.isEmpty;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

import org.apache.commons.io.IOUtils;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.CoreException;
import com.adaptris.mail.MailException;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Email implementation of the AdaptrisMessageConsumer interface.
 * <p>
 * The raw MimeMessage will not be parsed, and the contents of the entire MimeMessage will be used to create a single
 * AdaptrisMessage instance for processing. Additionally, any configured encoder will be ignored.
 * </p>
 * 
 * @config raw-mail-consumer
 * @license BASIC
 * 
 * @see MailConsumerImp
 */
@XStreamAlias("raw-mail-consumer")
public class RawMailConsumer extends MailConsumerImp {

  private Boolean useEmailMessageIdAsUniqueId;

  public RawMailConsumer() {

  }

  @Override
  protected List<AdaptrisMessage> createMessages(MimeMessage mime)
      throws MailException, CoreException {

    List<AdaptrisMessage> result = new ArrayList<AdaptrisMessage>();
    OutputStream out = null;
    try {
      if (log.isTraceEnabled()) {
        log.trace("Start Processing [" + mime.getMessageID() + "]");
      }
      AdaptrisMessage msg = defaultIfNull(getMessageFactory()).newMessage();
      out = msg.getOutputStream();
      mime.writeTo(out);
      if (useEmailMessageIdAsUniqueId() && !isEmpty(mime.getMessageID())) {
        msg.setUniqueId(mime.getMessageID());
      }
      result.add(msg);
    }
    catch (MessagingException e) {
      throw new MailException(e.getMessage(), e);
    }
    catch (IOException e) {
      throw new MailException(e.getMessage(), e);
    }
    finally {
      IOUtils.closeQuietly(out);
    }
    return result;
  }

  /**
   * @see com.adaptris.core.AdaptrisComponent#init()
   */
  @Override
  protected void initConsumer() throws CoreException {
  }

  /**
   * @return the useEmailMessageIdAdUniqueId
   */
  public Boolean getUseEmailMessageIdAsUniqueId() {
    return useEmailMessageIdAsUniqueId;
  }

  /**
   * Specify whether to use the email unique id as the AdaptrisMessage unique
   * ID.
   *
   * @param b true to use the email message id as the uniqueid
   */
  public void setUseEmailMessageIdAsUniqueId(Boolean b) {
    useEmailMessageIdAsUniqueId = b;
  }

  boolean useEmailMessageIdAsUniqueId() {
    return getUseEmailMessageIdAsUniqueId() != null ? getUseEmailMessageIdAsUniqueId().booleanValue() : false;
  }
}
