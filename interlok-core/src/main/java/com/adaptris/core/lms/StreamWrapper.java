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
package com.adaptris.core.lms;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

abstract class StreamWrapper {

  protected transient Logger log = LoggerFactory.getLogger(this.getClass());
  protected transient boolean extendedLogging = false;

  public StreamWrapper(boolean b) {
    this.extendedLogging = b;
  }

  protected final OutputStream openOutputStream(File f, Callback c) throws IOException {
    return new FileFilterOutputStream(f, c);
  }

  protected final InputStream openInputStream(File f, Callback c) throws IOException {
    return new FileFilterInputStream(f, c);
  }

  protected abstract InputStream openInputStream(File f) throws IOException;

  protected abstract OutputStream openOutputStream(File f) throws IOException;

  @FunctionalInterface
  public interface Callback {
    void nowClosed();
  }

  protected class FileFilterOutputStream extends OutputStream {
    private OutputStream wrapped;
    private File myFile;
    private Callback onClose;

    FileFilterOutputStream(File f, Callback c) throws IOException {
      super();
      wrapped = openOutputStream(f);
      myFile = f;
      if (extendedLogging) {
        log.trace("open() on [{}] ", myFile.getCanonicalFile());
      }
      onClose = c;
    }

    // Override so that we don't do the range checks.
    @Override
    public void write(byte b[]) throws IOException {
      wrapped.write(b);
    }

    @Override
    public void write(byte b[], int off, int len) throws IOException {
      wrapped.write(b, off, len);
    }

    @Override
    public void write(int b) throws IOException {
      wrapped.write(b);
    }

    @Override
    public void flush() throws IOException {
      wrapped.flush();
    }

    @Override
    @SuppressWarnings("deprecation")
    public void close() throws IOException {
      super.close();
      IOUtils.closeQuietly(wrapped);
      if (null != wrapped) {
        if (extendedLogging) {
          log.trace("close() on [{}] ", myFile.getCanonicalFile());
        }
        onClose.nowClosed();
        wrapped = null;
      }
    }
  }

  private class FileFilterInputStream extends InputStream {
    private File myFile = null;
    private Callback onClose;
    private InputStream wrapped;

    FileFilterInputStream(File f, Callback c) throws IOException {
      super();
      wrapped = openInputStream(f);
      myFile = f;
      if (extendedLogging) {
        log.trace("open() on [{}] ", myFile.getCanonicalFile());
      }
      onClose = c;
    }

    @Override
    public int available() throws IOException {
      return wrapped.available();
    }

    @Override
    @SuppressWarnings("deprecation")
    public void close() throws IOException {
      super.close();
      IOUtils.closeQuietly(wrapped);
      if (null != wrapped) {
        if (extendedLogging) {
          log.trace("close() on [{}] ", myFile.getCanonicalFile());
        }
        onClose.nowClosed();
        wrapped = null;
      }
    }

    // Use the parent super-class mark methods.
    // Which is basically not supported, as Files don't support mark/reset.
    // @Override
    // public synchronized void mark(int readlimit) {
    // wrapped.mark(readlimit);
    // }
    //
    // @Override
    // public boolean markSupported() {
    // return wrapped.markSupported();
    // }
    //
    // @Override
    // public synchronized void reset() throws IOException {
    // wrapped.reset();
    // }

    @Override
    public int read() throws IOException {
      return wrapped.read();
    }

    @Override
    public int read(byte b[]) throws IOException {
      return wrapped.read(b);
    }

    @Override
    public int read(byte b[], int off, int len) throws IOException {
      return wrapped.read(b, off, len);
    }

    @Override
    public long skip(long n) throws IOException {
      return wrapped.skip(n);
    }


  }

}
