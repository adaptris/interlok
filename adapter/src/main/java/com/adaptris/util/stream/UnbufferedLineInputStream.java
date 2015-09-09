package com.adaptris.util.stream;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

/** Class UnbufferedLineInputStream.
 *  <p>
 *  This class is to provide a way to read an inputstream line by line,
 *  without buffering.
 */
public class UnbufferedLineInputStream extends FilterInputStream {

  private static final int CR = 13;
  private static final int LF = 10;
  private int lastChar = -1; // The last char we've read
  private int currentChar = -1; // currently read char

  /** Constructor.
   *  @param input The underlying inputstream.
   */
  public UnbufferedLineInputStream(InputStream input) {
    super(input);
  }

  /** Read a line of data from the underlying inputstream.
   *  @return a line stripped of line terminators
   *  @throws IOException if there was an error reading the wrapped stream.
   */
  public String readLine() throws IOException {

    StringBuffer sb = new StringBuffer("");

    if (lastChar != -1) {
      sb.append((char) lastChar);
    }
    currentChar = in.read();
    if (currentChar == -1) {
      throw new IOException("End of input reached unexpectedly");
    }
    while (currentChar != CR && currentChar != LF && currentChar != -1) {
      sb.append((char) currentChar);
      currentChar = in.read();
    }

    // Read the next byte and check if it's a LF
    try {
      lastChar = in.read();
    } catch (IOException e) {
      // End of stream doesn't mean that we fail, as we're trying to read
      // ahead.
      lastChar = -1;
    }
    if (lastChar == LF) {
      lastChar = -1;
    }
    return sb.toString();
  }
}
