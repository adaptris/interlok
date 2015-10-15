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

import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.core.DefaultAdaptrisMessageImp;
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

  /**
   *
   * @see com.adaptris.core.AdaptrisMessage#getInputStream()
   */
  @Override
  public InputStream getInputStream() throws IOException {
    return new ErroringInputStream(super.getInputStream());
  }

  /**
   *
   * @see com.adaptris.core.AdaptrisMessage#getOutputStream()
   */
  @Override
  public OutputStream getOutputStream() throws IOException {
    return new ErroringOutputStream();
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
