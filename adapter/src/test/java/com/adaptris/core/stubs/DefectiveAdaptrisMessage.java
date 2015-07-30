/*
 * $RCSfile: AdaptrisMessageStub.java,v $
 * $Revision: 1.4 $
 * $Date: 2009/03/31 11:09:40 $
 * $Author: lchan $
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
