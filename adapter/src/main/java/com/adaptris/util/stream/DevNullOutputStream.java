/*
 * $RCSfile: LoggingOutputStream.java,v $
 * $Revision: 1.1 $
 * $Date: 2008/05/16 14:29:26 $
 * $Author: lchan $
 */
package com.adaptris.util.stream;

import java.io.IOException;
import java.io.OutputStream;

/**
 * An OutputStream that goes nowhere.
 */
public class DevNullOutputStream extends OutputStream {

  private boolean hasBeenClosed = false;
  public DevNullOutputStream() {
  }

  @Override
  public void close() {
    hasBeenClosed = true;
  }

  @Override
  public void write(final int b) throws IOException {
    if (hasBeenClosed) {
      throw new IOException("The stream has been closed.");
    }
  }
}
