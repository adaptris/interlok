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
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

class StandardStreamWrapper extends StreamWrapper {

  private transient boolean extendedLogging;

  StandardStreamWrapper(boolean logging) {
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

  private class FileFilterOutputStream extends FileOutputStream {
    private boolean alreadyClosed;
    private File myFile;
    private Callback onClose;

    FileFilterOutputStream(File out, Callback c) throws IOException {
      super(out);
      myFile = out;
      if (extendedLogging) {
        log.trace("open() on FileOutputStream [{}] ", myFile.getCanonicalFile());
      }
      alreadyClosed = false;
      onClose = c;
    }

    @Override
    public void close() throws IOException {
      super.close();
      if (!alreadyClosed) {
        if (extendedLogging) {
          log.trace("close() on FileOutputStream [{}] ", myFile.getCanonicalFile());
        }
        onClose.nowClosed();
        alreadyClosed = true;
      }
    }
  }

  private class FileFilterInputStream extends FileInputStream {
    private File myFile = null;
    private Callback onClose;
    private boolean alreadyClosed;

    FileFilterInputStream(File in, Callback c) throws IOException {
      super(in);
      myFile = in;
      if (extendedLogging) {
        log.trace("open() on FileInputStream [{}] ", myFile.getCanonicalFile());
      }
      onClose = c;
      alreadyClosed = false;
    }

    @Override
    public void close() throws IOException {
      super.close();
      if (!alreadyClosed) {
        if (extendedLogging) {
          log.trace("close() on FileInputStream [{}] ", myFile.getCanonicalFile());
        }
        onClose.nowClosed();
        alreadyClosed = true;
      }
    }
  }

}
