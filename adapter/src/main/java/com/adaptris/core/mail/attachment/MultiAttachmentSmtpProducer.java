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

package com.adaptris.core.mail.attachment;

import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import com.adaptris.annotation.AdapterComponent;
import com.adaptris.annotation.AdvancedConfig;
import com.adaptris.annotation.ComponentProfile;
import com.adaptris.annotation.DisplayOrder;
import com.adaptris.annotation.InputFieldDefault;
import com.adaptris.annotation.InputFieldHint;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.CoreConstants;
import com.adaptris.core.CoreException;
import com.adaptris.core.NullConnection;
import com.adaptris.core.ProduceDestination;
import com.adaptris.core.ProduceException;
import com.adaptris.core.mail.MailProducer;
import com.adaptris.mail.MailException;
import com.adaptris.mail.SmtpClient;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Implementation of the AdaptrisMessageProducer interface for handling email.
 * <p>
 * Because email is implicitly asynchronous, Request-Reply is invalid, and as such if the request method is used, an
 * <code>UnsupportedOperationException</code> is thrown.
 * <p>
 * Available Content-Encoding schemes that are supported are the same as those specified in RFC2045. They include "base64",
 * "quoted-printable", "7bit", "8bit" and "binary
 * </p>
 * <p>
 * The Content-Type may be any arbitary string such as application/edi-x12, however if no appropriate
 * <code>DataContentHandler</code> is installed, then the results can be undefined
 * </p>
 * <p>
 * The following metadata elements will change behaviour.
 * <ul>
 * <li>emailsubject - Override the configured subject with the value stored against this key.
 * <li>emailcc - If this is set, this this comma separated list will override any configured CC list.</li>
 * </ul>
 * <p>
 * It is possible to control the underlying behaviour of this producer through the use of various properties that will be passed to
 * the <code>javax.mail.Session</code> instance. You need to refer to the javamail documentation to see a list of the available
 * properties and meanings.
 * </p>
 * <p>
 * This implementation differs from {@link com.adaptris.core.mail.DefaultSmtpProducer} as it allows you to construct a mail message
 * with multiple attachments from the same AdaptrisMessage. Additionally, this does not use any configured encode as the MailCreator
 * interface will create the appropriate outputs.
 * </p>
 * 
 * @config multi-attachment-smtp-producer
 * 
 * @see MailProducer
 * @see CoreConstants#EMAIL_SUBJECT
 * @see CoreConstants#EMAIL_CC_LIST
 * @see MailContentCreator
 */
@XStreamAlias("multi-attachment-smtp-producer")
@AdapterComponent
@ComponentProfile(summary = "Send an email for the facility for generating multiple attachments based on the current message",
    tag = "producer,email", recommended = {NullConnection.class})
@DisplayOrder(order = {"smtpUrl", "username", "password", "subject", "from", "ccList", "bccList", "mailCreator"})
public class MultiAttachmentSmtpProducer extends MailProducer {

  @NotNull
  @Valid
  private MailContentCreator mailCreator;
  @AdvancedConfig
  @InputFieldDefault(value = "base64")
  @InputFieldHint(expression = true)
  private String contentEncoding;

  /**
   * @see Object#Object()
   *
   *
   */
  public MultiAttachmentSmtpProducer() {
    super();
  }

  @Override
  public void init() throws CoreException {
    super.init();
    if (mailCreator == null) {
      throw new CoreException("MailCreator implementation missing");
    }
  }

  /**
   * @see com.adaptris.core.AdaptrisMessageProducer #produce(AdaptrisMessage,
   *      ProduceDestination)
   */
  @Override
  public void produce(AdaptrisMessage msg, ProduceDestination destination)
      throws ProduceException {
    try {
      SmtpClient smtp = getClient(msg);
      smtp.addTo(destination.getDestination(msg));
      addBody(smtp, mailCreator.createBody(msg));
      addAttachments(smtp, mailCreator.createAttachments(msg));
      smtp.setEncoding(msg.resolve(contentEncoding()));
      smtp.send();
    }
    catch (Exception e) {
      log.error("Could not produce message because of " + e.getMessage());
      throw new ProduceException(e);
    }
  }

  private void addBody(SmtpClient client, MailContent m) throws Exception {
    if (m == null) {
      throw new Exception("No content available to form email body");
    }
    client.setMessage(m.getBytes(),m.getContentType());
  }

  private void addAttachments(SmtpClient client, List<MailAttachment> l) throws MailException {
    for (MailAttachment m : l) {
      client.addAttachment(m.getBytes(), m.getFilename(), m.getContentType(), m.getContentTransferEncoding());
    }
  }

  /**
   * Set the Content encoding of the email.
   *
   * @param s the content encoding.
   */
  public void setContentEncoding(String s) {
    contentEncoding = s;
  }

  /**
   * Get the encoding of the email.
   *
   * @return the content encoding.
   */
  public String getContentEncoding() {
    return contentEncoding;
  }

  String contentEncoding() {
    return getContentEncoding() != null ? getContentEncoding() : "base64";
  }

  /**
   * @return the mailCreator
   */
  public MailContentCreator getMailCreator() {
    return mailCreator;
  }

  /**
   * @param mailCreator the mailCreator to set
   */
  public void setMailCreator(MailContentCreator mailCreator) {
    this.mailCreator = mailCreator;
  }
}
