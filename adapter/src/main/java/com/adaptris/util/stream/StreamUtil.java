package com.adaptris.util.stream;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Some utility methods associated with input streams.
 */
public final class StreamUtil {

  private static final int BUFSIZE = 1024 * 1024;

  private StreamUtil() {
  }

  /**
   * Read from the inputstream associated with the socket and write the data
   * straight out to a unique file in the temporary (system defined) directory
   *
   * @param input The inputstream to read from
   * @param expected the expected number of bytes
   * @return a File object corresponding to the temporary file that was created
   * @throws IOException if there was an error creating the file
   */
  public static File createFile(InputStream input, int expected)
      throws IOException {
    return createFile(input, expected, null);
  }

  /**
   * Read from the inputstream associated with the socket and write the data
   * straight out to a unique file in the specified directory
   *
   * @param input The inputstream to read from
   * @param expected the expected number of bytes
   * @param dir The directory in which to create the file
   * @return a File object corresponding to the temporary file that was created
   * @throws IOException if there was an creating the file
   */
  public static File createFile(InputStream input, int expected, String dir)
      throws IOException {

    File tempFile = null;
    if (dir == null || dir.equals("")) {
      tempFile = File.createTempFile("tmp", "");
    }
    else {
      tempFile = File.createTempFile("tmp", "", new File(dir));
    }

    OutputStream out = new BufferedOutputStream(new FileOutputStream(tempFile));
    copyStream(input, out, expected);
    out.close();
    return tempFile;
  }

  /**
   * Copy from an InputStream to an OutputStream for expected bytes.
   *
   * @param input the input stream to read from
   * @param output the output stream to write from
   * @param expected the number of bytes to copy
   * @throws IOException if there was an error.
   */
  public static void copyStream(InputStream input, OutputStream output,
                                int expected) throws IOException {
    if (input == null || output == null || expected == 0) {
      return;
    }

    if (expected <= 0) {
      copyStream(input, output);
    }
    else {
      byte[] bytes = new byte[Math.min(expected, BUFSIZE)];

      int totalRead = 0;
      int bytesRead = 0;

      while (totalRead < expected) {
        bytesRead = input.read(bytes, 0, bytes.length);
        output.write(bytes, 0, bytesRead);
        totalRead = totalRead + bytesRead;
      }
      output.flush();
    }
    return;
  }

  /**
   * Copy from an InputStream to an OutputStream for expected bytes.
   *
   * @param input the input stream to read from
   * @param output the output stream to write from
   * @throws IOException if there was an IO error
   */
  public static void copyStream(InputStream input, OutputStream output)
      throws IOException {
    if (input == null || output == null) {
      return;
    }
    byte[] bytes = new byte[BUFSIZE];
    int bytesRead = 0;
    while ((bytesRead = input.read(bytes, 0, bytes.length)) != -1) {
      output.write(bytes, 0, bytesRead);
    }
    output.flush();
  }

  /**
   * Make a copy of an InputStream.
   * <p>
   * This method ensures that the resulting input stream can support a mark(),
   * and reset() call, by copying the contents of the input stream into a
   * ByteArrayInputStream
   *
   * @param input the InputStream to copy
   * @throws IOException if there was an error reading the stream.
   * @return a copy of the input stream
   */
  public static InputStream makeCopy(InputStream input) throws IOException {

    ByteArrayOutputStream out = new ByteArrayOutputStream();
    ByteArrayInputStream copy = null;
    copyStream(input, out);
    copy = new ByteArrayInputStream(out.toByteArray());
    return copy;
  }
}
