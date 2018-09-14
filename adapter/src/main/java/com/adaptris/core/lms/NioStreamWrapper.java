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
import java.io.FilterInputStream;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;

class NioStreamWrapper extends StreamWrapper {

  private transient boolean extendedLogging;

  NioStreamWrapper(boolean logging) {
    extendedLogging = logging;
  }

  @Override
  protected InputStream asInputStream(File f, Callback c) throws IOException {
    return new FileFilterInputStream(f, c);
  }

  @Override
  protected OutputStream asOutputStream(File f, Callback c) throws IOException {
    return new FileFilterOutputStream(f, c);
  }

  private class FileFilterOutputStream extends FilterOutputStream {
    private boolean alreadyClosed;
    private File myFile;
    private Callback onClose;

    FileFilterOutputStream(File out, Callback c) throws IOException {
      super(Files.newOutputStream(out.toPath()));
      myFile = out;
      if (extendedLogging) {
        log.trace("open() on Files#newOutputStream [{}] ", myFile.getCanonicalFile());
      }
      onClose = c;
      alreadyClosed = false;
    }

    // Override so that we don't do the range checks.
    @Override
    public void write(byte b[]) throws IOException {
      out.write(b);
    }

    @Override
    public void write(byte b[], int off, int len) throws IOException {
      out.write(b, off, len);
    }

    @Override
    public void write(int b) throws IOException {
      out.write(b);
    }

    @Override
    public void close() throws IOException {
      super.close();
      if (!alreadyClosed) {
        if (extendedLogging) {
          log.trace("close() on Files#newOutputStream [{}] ", myFile.getCanonicalFile());
        }
        onClose.nowClosed();
        alreadyClosed = true;
      }
    }
  }

  private class FileFilterInputStream extends FilterInputStream {
    private File myFile = null;
    private Callback onClose;
    private boolean alreadyClosed;

    FileFilterInputStream(File in, Callback c) throws IOException {
      super(Files.newInputStream(in.toPath()));
      myFile = in;
      if (extendedLogging) {
        log.trace("open() on Files#newInputStream [{}] ", myFile.getCanonicalFile());
      }
      onClose = c;
      alreadyClosed = false;
    }

    @Override
    public int read() throws IOException {
      return in.read();
    }

    @Override
    public int read(byte b[]) throws IOException {
      return in.read(b);
    }

    @Override
    public int read(byte b[], int off, int len) throws IOException {
      return in.read(b, off, len);
    }

    @Override
    public void close() throws IOException {
      super.close();
      if (!alreadyClosed) {
        if (extendedLogging) {
          log.trace("close() on Files#newInputStream [{}] ", myFile.getCanonicalFile());
        }
        onClose.nowClosed();
        alreadyClosed = true;
      }
    }
  }

}
