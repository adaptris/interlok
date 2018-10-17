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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.activation.DataSource;

/** A Datasource wrapping an arbitary array of bytes.
 * 
 *
 */
public class ByteArrayDataSource implements DataSource {
  private String contentType;
  private String name;
  private byte[] bytes;

  private ByteArrayDataSource() {
    setContentType("application/octet-stream");
  }
  
  public ByteArrayDataSource(byte[] bytes) {
    this(bytes, null);
  }

  public ByteArrayDataSource(byte[] bytes, String contentType) {
    this(bytes, contentType, null);
  }
    
  public ByteArrayDataSource(byte[] b, String ct, String id) {
    this();
    setBytes(b);
    setContentType(ct);
    setName(id);
  }

  public void setBytes(byte[] bytes) {
    this.bytes = bytes;
  }

  public byte[] getBytes() {
    return bytes;
  }

  public void setContentType(String ct) {
    if (ct != null) {
      contentType = ct;
    }
  }

  @Override
  public String getContentType() {
    return contentType;
  }

  @Override
  public InputStream getInputStream() {
    return new ByteArrayInputStream(bytes);
  }

  public void setName(String name) {
    this.name = name;
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public OutputStream getOutputStream() throws IOException {
    throw new UnsupportedOperationException();
  }
}
