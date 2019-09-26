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

package com.adaptris.core.stubs;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;
import java.util.Set;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.core.DefaultAdaptrisMessageImp;
import com.adaptris.core.MetadataElement;
import com.adaptris.util.IdGenerator;

/**
 * Stub of AdaptrisMessage for testing custom message factory.
 *
 * @author hfraser
 * @author $Author: lchan $
 */
public class DefectiveAdaptrisMessage extends DefaultAdaptrisMessageImp {

  protected DefectiveAdaptrisMessage(IdGenerator guid, AdaptrisMessageFactory amf) throws RuntimeException {
    super(guid, amf);
    setPayload(new byte[0]);
  }

  @Override
  public InputStream getInputStream() throws IOException {
    if (getFactory().brokenInput()) {
      return new ErroringInputStream(super.getInputStream());
    }
    return super.getInputStream();
  }

  @Override
  public OutputStream getOutputStream() throws IOException {
    if (getFactory().brokenOutput()) {
      return new ErroringOutputStream();
    }
    return super.getOutputStream();
  }

  @Override
  public boolean containsKey(String key) {
    return headersContainsKey(key);
  }

  /** @see AdaptrisMessage#headersContainsKey(String) */
  @Override
  public boolean headersContainsKey(String key) {
    if (getFactory().brokenMetadataGet()) {
      throw new RuntimeException();
    }
    return super.headersContainsKey(key);
  }

  /** @see AdaptrisMessage#addMetadata(MetadataElement) */
  @Override
  public synchronized void addMetadata(MetadataElement e) {
    if (getFactory().brokenMetadataSet()) {
      throw new RuntimeException();
    }
    super.addMetadata(e);
  }

  /** @see AdaptrisMessage#removeMetadata(MetadataElement) */
  @Override
  public void removeMetadata(MetadataElement element) {
    if (getFactory().brokenMetadataSet()) {
      throw new RuntimeException();
    }
    super.removeMetadata(element);
  }

  /** @see AdaptrisMessage#removeMessageHeader(String) */
  @Override
  public void removeMessageHeader(String key) {
    if (getFactory().brokenMetadataSet()) {
      throw new RuntimeException();
    }
    super.removeMessageHeader(key);
  }

  @Override
  public synchronized void clearMetadata() {
    if (getFactory().brokenMetadataSet()) {
      throw new RuntimeException();
    }
    super.clearMetadata();
  }

  @Override
  public Map<String, String> getMessageHeaders() {
    if (getFactory().brokenMetadataGet()) {
      throw new RuntimeException();
    }
    return super.getMessageHeaders();
  }

  @Override
  public Set<MetadataElement> getMetadata() { // lgtm [java/unsynchronized-getter]
    if (getFactory().brokenMetadataGet()) {
      throw new RuntimeException();
    }
    return super.getMetadata();
  }

  @Override
  public String getMetadataValue(String key) { // is case-sensitive
    if (getFactory().brokenMetadataGet()) {
      throw new RuntimeException();
    }
    return super.getMetadataValue(key);
  }

  @Override
  public String getMetadataValueIgnoreKeyCase(String key) {
    if (getFactory().brokenMetadataGet()) {
      throw new RuntimeException();
    }
    return super.getMetadataValueIgnoreKeyCase(key);
  }

  @Override
  public DefectiveMessageFactory getFactory() {
    return (DefectiveMessageFactory) super.getFactory();
  }


  private class ErroringOutputStream extends OutputStream {

    protected ErroringOutputStream() {
      super();
    }

    @Override
    public void write(int b) throws IOException {
      throw new IOException("Failed to write");
    }

  }

  private class ErroringInputStream extends FilterInputStream {

    protected ErroringInputStream(InputStream in) {
      super(in);
    }

    @Override
    public int read() throws IOException {
      throw new IOException("Failed to read");
    }

    @Override
    public int read(byte[] b) throws IOException {
      throw new IOException("Failed to read");
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
      throw new IOException("Failed to read");
    }

  }
}
