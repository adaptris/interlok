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

package com.adaptris.core.http.client.net;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.Enumeration;

import javax.mail.Header;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

import org.apache.commons.lang3.BooleanUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.adaptris.annotation.AdapterComponent;
import com.adaptris.annotation.ComponentProfile;
import com.adaptris.annotation.InputFieldDefault;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.common.MultiPartMessageStreamInputParameter;
import com.adaptris.core.http.client.RequestHeaderProvider;
import com.adaptris.util.text.mime.MimeConstants;
import com.thoughtworks.xstream.annotations.XStreamAlias;

import lombok.Getter;
import lombok.Setter;

/**
 * Implementation of {@link RequestHeaderProvider} that applies multipart MIME message headers as headers to a {@link HttpURLConnection}. It
 * is meant to be used in conjunction with {@link MultiPartMessageStreamInputParameter}
 *
 * @config http-multipart-message-request-headers
 *
 */
@XStreamAlias("http-multipart-message-request-headers")
@AdapterComponent
@ComponentProfile(summary = "Use a multipart message headers and add them as headers to an http connection", tag = "mime,http")
public class MultiPartMessageRequestHeaders extends RequestHeaders implements RequestHeaderProvider<HttpURLConnection> {
  protected transient Logger log = LoggerFactory.getLogger(this.getClass());

  /**
   * If set to true that will stop the multipart mime message Message-ID to be sent as an HTTP header. By default it is set to true.
   *
   * @param excludeMessageId
   * @return excludeMessageId
   */
  @Getter
  @Setter
  @InputFieldDefault("true")
  private Boolean excludeMessageId;

  public MultiPartMessageRequestHeaders() {
  }

  @Override
  public HttpURLConnection addHeaders(AdaptrisMessage msg, HttpURLConnection target) {
    try {
      MimeMessage mimeMessage = new MimeMessage(null, msg.getInputStream());
      Enumeration<Header> headers = mimeMessage.getAllHeaders();

      while (headers.hasMoreElements()) {
        Header header = headers.nextElement();
        String value = unfold(header.getValue());

        if (excludeMessageId() && MimeConstants.HEADER_MESSAGE_ID.equals(header.getName())) {
          log.trace("Ignoring Request Property [{}: {}]", header.getName(), value);
        } else {
          log.trace("Adding Request Property [{}: {}]", header.getName(), value);
          target.addRequestProperty(header.getName(), value);
        }
      }

    } catch (MessagingException | IOException expts) {
      throw new RuntimeException("Invalid multipart MIME message", expts);
    }
    return target;
  }

  private boolean excludeMessageId() {
    return BooleanUtils.toBooleanDefaultIfNull(getExcludeMessageId(), true);
  }

}
