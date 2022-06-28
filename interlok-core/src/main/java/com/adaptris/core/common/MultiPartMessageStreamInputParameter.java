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

package com.adaptris.core.common;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import javax.mail.Header;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

import com.adaptris.annotation.AdapterComponent;
import com.adaptris.annotation.ComponentProfile;
import com.adaptris.core.http.client.net.MultiPartMessageRequestHeaders;
import com.adaptris.core.util.ExceptionHelper;
import com.adaptris.interlok.InterlokException;
import com.adaptris.interlok.config.DataInputParameter;
import com.adaptris.interlok.types.InterlokMessage;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * This {@code DataInputParameter} is used when you want to source data from the {@link com.adaptris.core.AdaptrisMessage} multipart MIME
 * payload. This will remove the multipart payload headers and only keep the content (All the parts). It is meant to be used in conjunction
 * with {@link MultiPartMessageRequestHeaders}
 *
 * @config multipart-message-stream-input-parameter
 *
 */
@XStreamAlias("multipart-message-stream-input-parameter")
@AdapterComponent
@ComponentProfile(summary = "Use a multipart message payload and remove its headers", tag = "mime,http")
public class MultiPartMessageStreamInputParameter implements DataInputParameter<InputStream> {

  public MultiPartMessageStreamInputParameter() {
  }

  @Override
  public InputStream extract(InterlokMessage message) throws InterlokException {
    InputStream result = null;

    try {
      MimeMessage mimeMessage = new MimeMessage(null, message.getInputStream());

      List<String> headerNames = headerNames(mimeMessage);

      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      mimeMessage.writeTo(baos, headerNames.toArray(String[]::new));

      result = new ByteArrayInputStream(baos.toByteArray());
    } catch (MessagingException | IOException expts) {
      throw ExceptionHelper.wrapCoreException(expts);
    }

    return result;
  }

  private List<String> headerNames(MimeMessage mimeMessage) throws MessagingException {
    List<String> headerNames = new ArrayList<>();
    Enumeration<Header> headers = mimeMessage.getAllHeaders();
    while (headers.hasMoreElements()) {
      Header header = headers.nextElement();
      headerNames.add(header.getName());
    }
    return headerNames;
  }

}
