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

import static com.adaptris.core.AdaptrisMessageFactory.defaultIfNull;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;

import com.adaptris.annotation.AdapterComponent;
import com.adaptris.annotation.AdvancedConfig;
import com.adaptris.annotation.ComponentProfile;
import com.adaptris.annotation.DisplayOrder;
import com.adaptris.annotation.InputFieldDefault;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.CoreException;
import com.adaptris.core.NullConnection;
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
 * 
 * 
 * @see MailConsumerImp
 */
@XStreamAlias("raw-mail-consumer")
@AdapterComponent
@ComponentProfile(summary = "Pickup messages from a email account without trying to parse the MIME message.",
    tag = "consumer,email", recommended = {NullConnection.class})
@DisplayOrder(order = {"poller", "username", "password", "mailReceiverFactory", "headerHandler"})
public class RawMailConsumer extends MailConsumerImp {

  @AdvancedConfig
  @InputFieldDefault(value = "false")
  private Boolean useEmailMessageIdAsUniqueId;

  public RawMailConsumer() {

  }

  @Override
  protected List<AdaptrisMessage> createMessages(MimeMessage mime)
      throws MailException, CoreException {

    List<AdaptrisMessage> result = new ArrayList<AdaptrisMessage>();
    OutputStream out = null;
    try {
      log.trace("Start Processing [{}]", mime.getMessageID());
      AdaptrisMessage msg = defaultIfNull(getMessageFactory()).newMessage();
      String uuid = msg.getUniqueId();
      out = msg.getOutputStream();
      mime.writeTo(out);
      if (useEmailMessageIdAsUniqueId()) {
        msg.setUniqueId(StringUtils.defaultIfBlank(mime.getMessageID(), uuid));
      }
      headerHandler().handle(mime, msg);
      result.add(msg);
    }
    catch (MessagingException | IOException e) {
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
