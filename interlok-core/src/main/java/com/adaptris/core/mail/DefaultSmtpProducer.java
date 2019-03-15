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

import static org.apache.commons.lang.StringUtils.isEmpty;

import org.apache.commons.lang3.BooleanUtils;

import com.adaptris.annotation.AdapterComponent;
import com.adaptris.annotation.AdvancedConfig;
import com.adaptris.annotation.ComponentProfile;
import com.adaptris.annotation.DisplayOrder;
import com.adaptris.annotation.InputFieldDefault;
import com.adaptris.annotation.InputFieldHint;
import com.adaptris.annotation.Removal;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.CoreConstants;
import com.adaptris.core.CoreException;
import com.adaptris.core.NullConnection;
import com.adaptris.core.ProduceDestination;
import com.adaptris.core.ProduceException;
import com.adaptris.mail.SmtpClient;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Email implementation of the AdaptrisMessageProducer interface.
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
 * <li>emailtemplatebody - If the producer is configured to send the payload as an attachment, the value stored against this key
 * will be used as the message body.</li>
 * <li>emailattachmentfilename - If this is set, and the message is to be sent as an attachment, then this will be used as the
 * filename, otherwise the messages unique id will be used.</li>
 * <li>emailattachmentcontenttype - If this is set, and the message is to be sent as an attachment, then this will be used as the
 * attachment content-type, otherwise the default setting (or configured setting) will be used.</li>
 * <li>emailcc - If this is set, this this comma separated list will override any configured CC list.</li>
 * </ul>
 * <p>
 * It is possible to control the underlying behaviour of this producer through the use of various properties that will be passed to
 * the <code>javax.mail.Session</code> instance. You need to refer to the javamail documentation to see a list of the available
 * properties and meanings.
 * </p>
 * 
 * @config default-smtp-producer
 * 
 * @see MailProducer
 * @see CoreConstants#EMAIL_SUBJECT
 * @see CoreConstants#EMAIL_ATTACH_FILENAME
 * @see CoreConstants#EMAIL_ATTACH_CONTENT_TYPE
 * @see CoreConstants#EMAIL_TEMPLATE_BODY
 * @see CoreConstants#EMAIL_CC_LIST
 */
@XStreamAlias("default-smtp-producer")
@AdapterComponent
@ComponentProfile(summary = "Send an email", tag = "producer,email", recommended = {NullConnection.class})
@DisplayOrder(order = {"smtpUrl", "username", "password", "subject", "from", "ccList", "bccList"})
public class DefaultSmtpProducer extends MailProducer {

  @AdvancedConfig
  @InputFieldDefault(value = "false")
  private Boolean isAttachment;
  @AdvancedConfig
  @InputFieldHint(expression = true)
  @InputFieldDefault(value = "text/plain")
  private String contentType;
  @AdvancedConfig
  @InputFieldHint(expression = true)
  @InputFieldDefault(value = "base64")
  private String contentEncoding = null;
  @AdvancedConfig
  @InputFieldDefault(value = "application/octet-stream")
  @InputFieldHint(expression = true)
  private String attachmentContentType = null;
  @AdvancedConfig
  @InputFieldDefault(value = "base64")
  @InputFieldHint(expression = true)
  private String attachmentContentEncoding = null;
  @AdvancedConfig
  @Deprecated
  @Removal(version = "3.9.0", message = "Use #setContentType(String) with an expression")
  private String contentTypeKey = null;

  /**
   * @see Object#Object()
   *
   *
   */
  public DefaultSmtpProducer() {
    super();
  }

  @Override
  public void init() throws CoreException {
    super.init();
    if (!isEmpty(getContentTypeKey())) {
      log.warn("Use of deprecated content-type-key; use %message{metadata} in content-type instead");
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
      smtp.setEncoding(msg.resolve(contentEncoding()));
      byte[] encodedPayload = encode(msg);
      smtp.addTo(destination.getDestination(msg));
      String ctype = getContentType(msg);

      if (isAttachment()) {
        String template = msg
            .getMetadataValue(CoreConstants.EMAIL_TEMPLATE_BODY);
        if (template != null) {
          if (msg.getContentEncoding() != null) {
            smtp.setMessage(template.getBytes(msg.getContentEncoding()), ctype);
          } else {
            smtp.setMessage(template.getBytes(), ctype);
          }
        }
        String fname = resolve(msg, CoreConstants.EMAIL_ATTACH_FILENAME, msg.getUniqueId());
        String type = resolve(msg, CoreConstants.EMAIL_ATTACH_CONTENT_TYPE, msg.resolve(attachmentContentType()));
        smtp.addAttachment(encodedPayload, fname, type, msg.resolve(attachmentContentEncoding()));
      }
      else {
        smtp.setMessage(encodedPayload, ctype);
      }
      smtp.send();
    }
    catch (Exception e) {
      log.error("Could not produce message because of " + e.getMessage());
      throw new ProduceException(e);
    }
  }

  /**
   * Specify if this message should be sent as an attachment.
   *
   * @param b true or false.
   */
  public void setIsAttachment(Boolean b) {
    isAttachment = b;
  }

  /**
   * Get the attachment flag.
   *
   * @return true or false.
   */
  public Boolean getIsAttachment() {
    return isAttachment;
  }

  boolean isAttachment() {
    return BooleanUtils.toBooleanDefaultIfNull(getIsAttachment(), false);
  }

  /**
   * Set the content type of the email.
   *
   * @param s the content type
   */
  public void setContentType(String s) {
    contentType = s;
  }

  String contentType() {
    return getContentType() != null ? getContentType() : "text/plain";
  }

  /**
   * Get the content type of the email.
   *
   * @return the content type.
   */
  public String getContentType() {
    return contentType;
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
   * Set the content type associated with the attachement.
   * <p>
   * The default content-type for attachments is
   * <code>application/octet-stream</code>
   * </p>
   *
   * @param s the content type
   *
   * @see #setContentType(String)
   * @see #setIsAttachment(Boolean)
   * @see SmtpClient#addAttachment(byte[], java.lang.String, java.lang.String)
   */
  public void setAttachmentContentType(String s) {
    attachmentContentType = s;
  }

  /**
   * Get the content type associated with the attachement.
   *
   * @see SmtpClient#addAttachment(byte[], java.lang.String, java.lang.String)
   * @return the attachment content type.
   */
  public String getAttachmentContentType() {
    return attachmentContentType;
  }

  String attachmentContentType() {
    return getAttachmentContentType() != null ? getAttachmentContentType() : "application/octet-stream";
  }

  /**
   * Get the metadata key from which to extract the metadata.
   *
   * @return the contentTypeKey
   * @deprecated since 3.6.6 {@link #setContentType(String)} supports expressions
   */
  @Deprecated
  @Removal(version = "3.9.0", message = "Use #setContentType(String) with an expression")
  public String getContentTypeKey() {
    return contentTypeKey;
  }

  /**
   * Set the content type metadata key that will be used to extract the Content Type.
   * <p>
   * In the event that this metadata key exists, it will be used in preference to the configured content-type.
   * </p>
   *
   * @param s the contentTypeKey to set
   * @deprecated since 3.6.6 {@link #setContentType(String)} supports expressions
   */
  @Deprecated
  @Removal(version = "3.9.0", message = "Use #setContentType(String) with an expression")
  public void setContentTypeKey(String s) {
    contentTypeKey = s;
  }

  public String getAttachmentContentEncoding() {
    return attachmentContentEncoding;
  }

  /**
   * Set the attachment content encoding.
   * 
   * @param enc the encoding for the attachment.
   */
  public void setAttachmentContentEncoding(String enc) {
    this.attachmentContentEncoding = enc;
  }

  String attachmentContentEncoding() {
    return getAttachmentContentEncoding() != null ? getAttachmentContentEncoding() : "base64";
  }

  private String getContentType(AdaptrisMessage msg) {
    String type = msg.resolve(contentType());
    if (getContentTypeKey() != null) {
      String s = msg.getMetadataValue(getContentTypeKey());
      if (!isEmpty(s)) {
        log.trace("{} metadata overrides configured content type", getContentTypeKey());
        type = s;
      }
    }
    log.trace("Content-Type set to {}", type);
    return type;
  }

  private static String resolve(AdaptrisMessage msg, String metadataKey, String defaultValue) {
    String result = defaultValue;
    if (msg.headersContainsKey(metadataKey)) {
      result = msg.getMetadataValue(metadataKey);
    }
    return result;
  }
}
