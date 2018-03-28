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

package com.adaptris.util.text.mime;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.activation.DataSource;
import javax.mail.MessagingException;
import javax.mail.internet.InternetHeaders;
import javax.mail.util.SharedByteArrayInputStream;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;

import com.adaptris.core.util.Args;


public class InputStreamDataSource implements DataSource, MimeConstants {

  private InternetHeaders headers = null;
  private String contentType = null;
  private String messageId = null;
  private byte[] wrappedBytes;

  private InputStreamDataSource() {
  }

  /** Constructor.
   *  @param input the input stream.
   *  @throws IOException if there was an error reading the stream.
   *  @throws MessagingException if there was an error initialising the
   *  datasource.
   */
  public InputStreamDataSource(InputStream input) throws IOException, MessagingException {
    this();
    initialise(input);
  }

  private void initialise(InputStream input) throws IOException, MessagingException {
    headers = new InternetHeaders(input);
    initContent(input);
  }

  @Override
  public String getContentType() {
    if (contentType == null) {
      contentType = get(HEADER_CONTENT_TYPE);
    }
    return contentType;
  }

  @Override
  public InputStream getInputStream() throws IOException {
    return new SharedByteArrayInputStream(wrappedBytes);
  }

  @Override
  public String getName() {
    if (messageId == null) {
      messageId = get(HEADER_MESSAGE_ID);
    }
    return messageId;
  }

  @Override
  public OutputStream getOutputStream() throws IOException {
    throw new UnsupportedOperationException();
  }

  private void initContent(InputStream in) throws IOException {
    try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
      IOUtils.copy(in, out);
      wrappedBytes = out.toByteArray();
    }
  }

  public InternetHeaders getHeaders() {
    return headers;
  }

  private String get(String headerName) {
    String result = null;
    try {
      String[] s = Args.notNull(headers.getHeader(headerName), headerName);
      result = s[0];
    } catch (IllegalArgumentException e) {

    }
    return StringUtils.defaultIfEmpty(result, "");
  }
}
