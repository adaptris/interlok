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
