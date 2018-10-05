/*
 * Copyright 2018 Adaptris Ltd.
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

import java.io.BufferedInputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import javax.activation.DataSource;
import javax.mail.MessagingException;
import javax.mail.internet.InternetHeaders;
import javax.mail.internet.SharedInputStream;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;

import com.adaptris.core.util.Args;

class ReadonlyFileDataSource implements DataSource, Closeable, MimeConstants, MimeHeaders {

  private transient File wrappedFile;
  private transient List<SharedFileInputStream> children;
  private transient InternetHeaders headers;
  private transient String contentType = null;
  private transient String messageId = null;

  ReadonlyFileDataSource(File f) throws IOException, MessagingException {
    wrappedFile = f;
    headers = readHeaders(wrappedFile);
    children = new ArrayList<>();

  }

  private static InternetHeaders readHeaders(File f) throws IOException, MessagingException {
    InternetHeaders hdrs = null;
    try (InputStream in = new FileInputStream(f)) {
      hdrs = new InternetHeaders(in);
    }
    return hdrs;
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
    SharedFileInputStream child = new SharedFileInputStream();
    children.add(child);
    return child;
  }

  @Override
  public String getName() {
    if (messageId == null) {
      messageId = get(HEADER_MESSAGE_ID);
    }
    return messageId;
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

  @Override
  public OutputStream getOutputStream() throws IOException {
    throw new UnsupportedOperationException();
  }

  @Override
  @SuppressWarnings("deprecation")
  public void close() throws IOException {
    for (SharedFileInputStream child : children) {
      IOUtils.closeQuietly(child);
    }
  }

  @Override
  public InternetHeaders getHeaders() {
    return headers;
  }

  private class SharedFileInputStream extends FilterInputStream implements SharedInputStream {
    private final long myStart;
    private final long myLength;

    private long currentPos;
    private long markedPos;

    public SharedFileInputStream() throws IOException {
      this(0, wrappedFile.length());
    }

    private SharedFileInputStream(long start, long length) throws IOException {
      super(new BufferedInputStream(new FileInputStream(wrappedFile)));
      myStart = start;
      myLength = length;
      in.skip(start);
    }

    public long getPosition() {
      return currentPos;
    }

    public InputStream newStream(long start, long finish) {
      SharedFileInputStream stream = null;

      try {
        if (finish < 0) {
          if (myLength > 0) {
            stream = new SharedFileInputStream(myStart + start, myLength - start);
          } else if (myLength == 0) {
            stream = new SharedFileInputStream(myStart + start, 0);
          } else {
            stream = new SharedFileInputStream(myStart + start, -1);
          }
        } else {
          stream = new SharedFileInputStream(myStart + start, finish - start);
        }
        children.add(stream);
      } catch (IOException e) {
        throw new IllegalStateException(e);
      }
      return stream;

    }

    public int read(byte[] buf) throws IOException {
      return this.read(buf, 0, buf.length);
    }

    public int read(byte[] buf, int off, int len) throws IOException {
      if ((currentPos + len) > myLength) {
        // we would read past the end of what we're allowed to.
        // so just read the rest of it.
        return read(buf, off, Long.valueOf(myLength - currentPos).intValue());
      }
      int bytesRead = in.read(buf, off, len);
      if (bytesRead == 0) {
        return -1;
      }
      currentPos += bytesRead;
      return bytesRead;
    }

    public int read() throws IOException {
      if (currentPos == myLength) {
        return -1;
      }
      currentPos++;
      return in.read();
    }

    public boolean markSupported() {
      return true;
    }

    public long skip(long n) throws IOException {
      long count;
      for (count = 0; count != n; count++) {
        if (this.read() < 0) {
          break;
        }
      }
      return count;
    }

    public void mark(int readLimit) {
      markedPos = currentPos;
      in.mark(readLimit);
    }

    public void reset() throws IOException {
      currentPos = markedPos;
      in.reset();
    }
  }

}
