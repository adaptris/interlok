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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.activation.DataSource;
import javax.mail.MessagingException;
import javax.mail.internet.InternetHeaders;


class InputStreamDataSource implements DataSource, MimeConstants {

  private InputStream in = null;
  private InternetHeaders header = null;
  private String contentType = null;
  private String messageId = null;

  private InputStreamDataSource() {
  }

  /** Constructor.
   *  @param input the input stream.
   *  @throws IOException if there was an error reading the stream.
   *  @throws MessagingException if there was an error initialising the
   *  datasource.
   */
  public InputStreamDataSource(InputStream input)
  throws IOException, MessagingException {
    this();
    initialise(input);
  }

  private void initialise(InputStream input)
  throws IOException, MessagingException {
    in = input;
    header = new InternetHeaders(in);
  }

  @Override
  public String getContentType() {
    if (contentType == null) {
      String[] s = header.getHeader(HEADER_CONTENT_TYPE);
      contentType = s[0];
    }
    return contentType;
  }

  @Override
  public InputStream getInputStream() throws IOException {
    return in;
  }

  @Override
  public String getName() {
    if (messageId == null) {

      String[] s = header.getHeader(HEADER_MESSAGE_ID);
      messageId = s[0];
    }
    return messageId;
  }

  @Override
  public OutputStream getOutputStream() throws IOException {
    throw new UnsupportedOperationException();
  }

}
