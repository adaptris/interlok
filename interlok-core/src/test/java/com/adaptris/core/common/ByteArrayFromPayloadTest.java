/*
 * Copyright 2016 Adaptris Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.adaptris.core.common;

import static org.junit.Assert.assertTrue;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.security.MessageDigest;
import java.util.Map;
import org.junit.Test;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.interlok.types.InterlokMessage;

public class ByteArrayFromPayloadTest {

  private static final byte[] BYTE_ARRAY = "Hello World".getBytes();

  @Test
  public void testWrapAdaptrisMessage() throws Exception {
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(BYTE_ARRAY);
    byte[] wrapped = new ByteArrayFromPayload().wrap(msg);
    assertTrue(MessageDigest.isEqual(BYTE_ARRAY, wrapped));
  }

  @Test
  public void testWrapInterlokMessage() throws Exception {
    InterlokMessage msg = new MyInterlokMessage();
    byte[] wrapped = new ByteArrayFromPayload().wrap(msg);
    assertTrue(MessageDigest.isEqual(BYTE_ARRAY, wrapped));
  }

  private class MyInterlokMessage implements InterlokMessage {

    @Override
    public String getUniqueId() {
      throw new UnsupportedOperationException();
    }

    @Override
    public void setUniqueId(String uniqueId) {
      throw new UnsupportedOperationException();
    }

    @Override
    public String getContent() {
      throw new UnsupportedOperationException();
    }

    @Override
    public void setContent(String payload, String encoding) {
      throw new UnsupportedOperationException();
    }

    @Override
    public Map<String, String> getMessageHeaders() {
      throw new UnsupportedOperationException();
    }

    @Override
    public void setMessageHeaders(Map<String, String> metadata) {
      throw new UnsupportedOperationException();
    }

    @Override
    public void clearMessageHeaders() {
      throw new UnsupportedOperationException();
    }

    @Override
    public void addMessageHeader(String key, String value) {
      throw new UnsupportedOperationException();
    }

    @Override
    public void removeMessageHeader(String key) {
      throw new UnsupportedOperationException();
    }

    @Override
    public String getContentEncoding() {
      throw new UnsupportedOperationException();
    }

    @Override
    public void setContentEncoding(String payloadEncoding) {
      throw new UnsupportedOperationException();
    }

    @Override
    public Reader getReader() throws IOException {
      throw new UnsupportedOperationException();
    }

    @Override
    public Writer getWriter() throws IOException {
      throw new UnsupportedOperationException();
    }

    @Override
    public Writer getWriter(String encoding) throws IOException {
      throw new UnsupportedOperationException();
    }

    @Override
    public InputStream getInputStream() throws IOException {
      return new ByteArrayInputStream(BYTE_ARRAY);
    }

    @Override
    public OutputStream getOutputStream() throws IOException {
      throw new UnsupportedOperationException();
    }

    @Override
    public void addObjectHeader(Object key, Object object) {
      throw new UnsupportedOperationException();
    }

    @Override
    public Map<Object, Object> getObjectHeaders() {
      throw new UnsupportedOperationException();
    }

    @Override
    public boolean headersContainsKey(String key) {
      throw new UnsupportedOperationException();
    }

    @Override
    public String resolve(String s, boolean multiline) {
      throw new UnsupportedOperationException();
    }

    @Override
    public Object resolveObject(String s) {
      throw new UnsupportedOperationException();
    }

  }
}
