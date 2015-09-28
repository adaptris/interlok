package com.adaptris.core;

import java.io.IOException;
import java.io.InputStream;

/**
 * Defines methods for handling <code>Adapter </code> log files.
 *
 * @author lchan / $Author: hfraser $
 */
public interface LogHandler extends AdaptrisComponent {

  /**
   * Standard types of logfile.
   *
   */
  public static enum LogFileType {
    /** Standard log file */
    Standard,
    /** Statistics */
    Statistics,
    /** Google graph API log file. */
    Graphing
  }

  /**
   * Retrieve the log file and present it as an InputStream.
   * 
   * @return the log file.
   * @param type the Logfile type
   * @throws IOException if the input stream could not be returned.
   * @deprecated since 3.0.6; there is no replacement.
   */
  @Deprecated
  InputStream retrieveLog(LogFileType type) throws IOException;

  /**
   * Clean up any logfiles.
   *
   * @throws IOException if there was an error.
   */
  void clean() throws IOException;

  /**
   * Get the compression status for the log file.
   *
   * @return the compression flag.
   * @deprecated since 3.0.6; there is no replacement.
   */
  @Deprecated
  boolean isCompressed();
}
