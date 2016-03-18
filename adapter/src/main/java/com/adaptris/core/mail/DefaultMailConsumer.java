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

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import javax.mail.Header;
import javax.mail.internet.MimeMessage;

import com.adaptris.annotation.AdapterComponent;
import com.adaptris.annotation.AdvancedConfig;
import com.adaptris.annotation.ComponentProfile;
import com.adaptris.annotation.DisplayOrder;
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
@ComponentProfile(summary = "Pickup messages from a email account parsing the MIME message", tag = "consumer,email",
    recommended = {NullConnection.class})
@DisplayOrder(order = {"poller", "username", "password", "mailReceiverFactory", "partSelector"})
public class DefaultMailConsumer extends ParsingMailConsumerImpl {

  @AdvancedConfig
  private boolean preserveHeaders;
  @AdvancedConfig
  private String headerPrefix = "";

  /**
   * <p>
   * Creates a new instance.
   * </p>
   */
  public DefaultMailConsumer() {
  }

  @Override
  protected List<AdaptrisMessage> createMessages(MimeMessage mime)
      throws MailException, CoreException {

    List<AdaptrisMessage> result = new ArrayList<AdaptrisMessage>();
    MessageParser mp = new MessageParser(mime, getPartSelector());
    if (log.isTraceEnabled()) {
      log.trace("Start Processing [" + mp.getMessageId() + "]");
    }
    if (mp.getMessage() != null) {
      AdaptrisMessage msg = decode(mp.getMessage());
      if (mp.getMessageId() != null) {
        msg.addMetadata(CoreConstants.EMAIL_MESSAGE_ID, mp.getMessageId());
      }
      copyEmailHeaders(mime, msg);
      if (mp.hasAttachments()) {
        msg.addMetadata(CoreConstants.EMAIL_TOTAL_ATTACHMENTS, String
            .valueOf(mp.numberOfAttachments()));
      }
      result.add(msg);
    }
    if (mp.hasAttachments()) {
      while (mp.hasMoreAttachments()) {
        Attachment a = mp.nextAttachment();
        AdaptrisMessage msg = decode(a.getBytes());
        msg.addMetadata(CoreConstants.EMAIL_MESSAGE_ID, mp.getMessageId());
        msg.addMetadata(CoreConstants.EMAIL_ATTACH_FILENAME, a.getFilename());
        msg.addMetadata(CoreConstants.EMAIL_ATTACH_CONTENT_TYPE, a
            .getContentType());
        copyEmailHeaders(mime, msg);
        msg.addMetadata(CoreConstants.EMAIL_TOTAL_ATTACHMENTS, String
            .valueOf(mp.numberOfAttachments()));
        result.add(msg);
      }
    }
    return result;
  }

  private void copyEmailHeaders(MimeMessage src, AdaptrisMessage dest)
      throws MailException {
    try {
      if (getPreserveHeaders()) {
        Enumeration e = src.getAllHeaders();
        while (e.hasMoreElements()) {
          Header h = (Header) e.nextElement();
          dest.addMetadata(getHeaderPrefix() + h.getName(), h.getValue());
        }
      }
    }
    catch (Exception e) {
      throw new MailException(e.getMessage(), e);
    }
    return;
  }

  /**
   * @see com.adaptris.core.AdaptrisComponent#init()
   */
  @Override
  protected void initConsumer() throws CoreException {
  }

  /**
   * Get the preserve headers flag.
   *
   * @return the flag.
   */
  public boolean getPreserveHeaders() {
    return preserveHeaders;
  }

  /**
   * Set the preserve headers flag.
   * <p>
   * If set to true, then an attempt is made to copy all the email headers from
   * the email message as metadata to the AdaptrisMessage object. Each header
   * can optionally be prefixed with the value specfied by <code>
   *  getHeaderPrefix()</code>
   * </p>
   *
   * @param b true or false.
   */
  public void setPreserveHeaders(boolean b) {
    preserveHeaders = b;
  }

  /**
   * Set the header prefix.
   * <p>
   * The header prefix is used to prefix any headers that are preserved from the
   * email message.
   * </p>
   *
   * @param s the prefix.
   */
  public void setHeaderPrefix(String s) {
    headerPrefix = s;
  }

  /**
   * Get the header prefix.
   *
   * @return the header prefix
   */
  public String getHeaderPrefix() {
    return headerPrefix;
  }
}
