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
package com.adaptris.core.ftp;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageEncoder;


/**
 * OutputStream wrapper that manages the {@link AdaptrisMessageEncoder} for us.
 * 
 * <p>
 * Only fit for purpose where the associated encoder handles an InputStream, so really, only MimeEncoder, but then that's the only
 * encoder; (should that interface just change).
 * </p>
 */
class EncoderWrapper extends OutputStream {

  private transient OutputStream wrapped;
  private transient AdaptrisMessage wrappedMsg;
  private transient AdaptrisMessageEncoder encoder;
  private transient Logger log = LoggerFactory.getLogger(this.getClass());

  public EncoderWrapper(AdaptrisMessage msg, AdaptrisMessageEncoder encoder) throws Exception {
    super();
    initWrapping(msg, encoder);
  }

  private void initWrapping(AdaptrisMessage msg, AdaptrisMessageEncoder enc) throws Exception {
    this.encoder = enc;
    if (encoder == null) {
      wrappedMsg = msg;
      wrapped = msg.getOutputStream();
    }
    else {
      wrappedMsg = msg.getFactory().newMessage();
      wrapped = wrappedMsg.getOutputStream();
    }
  }

  @Override
  public void write(int b) throws IOException {
    wrapped.write(b);
  }

  @Override
  public void write(byte b[]) throws IOException {
    write(b, 0, b.length);
  }

  @Override
  public void write(byte b[], int off, int len) throws IOException {
    wrapped.write(b, off, len);
  }

  @Override
  public void flush() throws IOException {
    wrapped.flush();
  }

  @Override
  public void close() throws IOException {
    try (OutputStream ostream = wrapped) {
      flush();
    }
  }

  @SuppressWarnings("unchecked")
  public AdaptrisMessage build() throws Exception {
    IOUtils.closeQuietly(wrapped);
    AdaptrisMessage result = null;
    if (encoder == null) {
      result = wrappedMsg;
    }
    else {
      log.trace("Using {} to decode", encoder.getClass().getName());
      encoder.registerMessageFactory(wrappedMsg.getFactory());
      try (InputStream in = wrappedMsg.getInputStream()) {
        result = encoder.readMessage(in);
      }
    }
    return result;
  }
}