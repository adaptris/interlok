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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

import com.adaptris.annotation.AdapterComponent;
import com.adaptris.annotation.AdvancedConfig;
import com.adaptris.annotation.ComponentProfile;
import com.adaptris.annotation.DisplayOrder;
import com.adaptris.annotation.Removal;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.CoreConstants;
import com.adaptris.core.CoreException;
import com.adaptris.core.NullConnection;
import com.adaptris.mail.Attachment;
import com.adaptris.mail.MailException;
import com.adaptris.mail.MessageParser;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Default Email implementation of the AdaptrisMessageConsumer interface.
 * <p>
 * Each Mime part of the incoming email message will become a separate AdaptrisMessage; attachments are processed separately from
 * the mail body itelf.
 * </p>
 * 
 * 
 * @config default-mail-consumer
 * 
 * 
 * @see MailConsumerImp
 */
@XStreamAlias("default-mail-consumer")
@AdapterComponent
@ComponentProfile(summary = "Pickup messages from a email account parsing the MIME message", tag = "consumer,email", metadata =
{
    "emailmessageid", "emailtotalattachments", "emailattachmentfilename", "emailattachmentcontenttype"
}, 
    recommended = {NullConnection.class})
@DisplayOrder(order = {"poller", "username", "password", "mailReceiverFactory", "partSelector", "headerHandler"})
public class DefaultMailConsumer extends ParsingMailConsumerImpl {

  @AdvancedConfig
  @Deprecated
  @Removal(version = "3.9.0", message = "Use #setHeaderHandler(MailHeaderHandler)")
  private Boolean preserveHeaders;
  @AdvancedConfig
  @Deprecated
  @Removal(version = "3.9.0", message = "Use #setHeaderHandler(MailHeaderHandler)")
  private String headerPrefix;

  /**
   * <p>
   * Creates a new instance.
   * </p>
   */
  public DefaultMailConsumer() {
  }

  @Override
  protected List<AdaptrisMessage> createMessages(MimeMessage mime) throws MailException, CoreException {

    List<AdaptrisMessage> result = new ArrayList<AdaptrisMessage>();
    try {
      MessageParser mp = new MessageParser(mime, getPartSelector());
      log.trace("Start Processing [{}]", mp.getMessageId());
      if (mp.getMessage() != null) {
        AdaptrisMessage msg = decode(mp.getMessage());
        if (mp.getMessageId() != null) {
          msg.addMetadata(CoreConstants.EMAIL_MESSAGE_ID, mp.getMessageId());
        }
        headerHandler().handle(mime, msg);
        if (mp.hasAttachments()) {
          msg.addMetadata(CoreConstants.EMAIL_TOTAL_ATTACHMENTS, String.valueOf(mp.numberOfAttachments()));
        }
        result.add(msg);
      }
      if (mp.hasAttachments()) {
        while (mp.hasMoreAttachments()) {
          Attachment a = mp.nextAttachment();
          AdaptrisMessage msg = decode(a.getBytes());
          msg.addMetadata(CoreConstants.EMAIL_MESSAGE_ID, mp.getMessageId());
          msg.addMetadata(CoreConstants.EMAIL_ATTACH_FILENAME, a.getFilename());
          msg.addMetadata(CoreConstants.EMAIL_ATTACH_CONTENT_TYPE, a.getContentType());
          headerHandler().handle(mime, msg);
          msg.addMetadata(CoreConstants.EMAIL_TOTAL_ATTACHMENTS, String.valueOf(mp.numberOfAttachments()));
          result.add(msg);
        }
      }
    }
    catch (MessagingException | IOException e) {
      throw new MailException(e);
    }
    return result;
  }

  /**
   * @see com.adaptris.core.AdaptrisComponent#init()
   */
  @Override
  protected void initConsumer() throws CoreException {
    if (getPreserveHeaders() != null) {
      log.warn("preserve-headers is deprecated; use header-handler instead");
    }
  }

  /**
   * Get the preserve headers flag.
   *
   * @return the flag.
   * @deprecated since 3.6.5 use {link {@link #setHeaderHandler(MailHeaderHandler)} instead.
   */
  @Deprecated
  @Removal(version = "3.9.0", message = "Use #setHeaderHandler(MailHeaderHandler)")
  public Boolean getPreserveHeaders() {
    return preserveHeaders;
  }

  /**
   * Set the preserve headers flag.
   * <p>
   * If set to true, then an attempt is made to copy all the email headers from the email message as metadata to the AdaptrisMessage
   * object. Each header can optionally be prefixed with the value specfied by <code>
   *  getHeaderPrefix()</code>
   * </p>
   *
   * @param b true or false.
   * @deprecated since 3.6.5 use {link {@link #setHeaderHandler(MailHeaderHandler)} instead.
   */
  @Deprecated
  @Removal(version = "3.9.0", message = "Use #setHeaderHandler(MailHeaderHandler)")
  public void setPreserveHeaders(Boolean b) {
    preserveHeaders = b;
  }


  /**
   * Set the header prefix.
   * <p>
   * The header prefix is used to prefix any headers that are preserved from the email message.
   * </p>
   *
   * @param s the prefix.
   * @deprecated since 3.6.5 use {link {@link #setHeaderHandler(MailHeaderHandler)} instead.
   */
  @Deprecated
  @Removal(version = "3.9.0", message = "Use #setHeaderHandler(MailHeaderHandler)")
  public void setHeaderPrefix(String s) {
    headerPrefix = s;
  }

  /**
   * Get the header prefix.
   *
   * @return the header prefix
   * @deprecated since 3.6.5 use {link {@link #setHeaderHandler(MailHeaderHandler)} instead.
   */
  @Deprecated
  @Removal(version = "3.9.0", message = "Use #setHeaderHandler(MailHeaderHandler)")
  public String getHeaderPrefix() {
    return headerPrefix;
  }

  @Override
  protected MailHeaderHandler headerHandler() {
    MailHeaderHandler result = super.headerHandler();
    if (getPreserveHeaders() != null) {
      result = getPreserveHeaders() == Boolean.TRUE ? new MetadataMailHeaders(getHeaderPrefix()) : new IgnoreMailHeaders();
    }
    return result;
  }

}
