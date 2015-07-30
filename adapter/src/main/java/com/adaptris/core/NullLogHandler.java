/*
 * $RCSfile: NullLogHandler.java,v $
 * $Revision: 1.7 $
 * $Date: 2005/09/23 00:56:54 $
 * $Author: hfraser $
 */
package com.adaptris.core;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import com.adaptris.util.license.License;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Null Implemention of the <code>LogHandler</code>.
 * <p>
 * This implementation merely presents the fixed text <code>No
 * implementation of the Logging Handler configured</code> as the InputStream when <code>retrieveLog()</code> is invoked.
 * </p>
 * 
 * @config null-log-handler
 * @see FileLogHandler
 * @author lchan / $Author: hfraser $
 */
@XStreamAlias("null-log-handler")
public class NullLogHandler extends LogHandlerImp {

  private static final String DEFAULT_STRING =
    "No implementation of the Logging Handler configured";

  /**
   * @see com.adaptris.core.LogHandler#retrieveLog(LogFileType)
   */
  public InputStream retrieveLog(LogFileType type) throws IOException {
    return new ByteArrayInputStream(DEFAULT_STRING.getBytes("UTF-8"));
  }

  /**
   * @see com.adaptris.core.LogHandler#clean()
   */
  public void clean() throws IOException {
    return;
  }

  /**
   *
   * @see com.adaptris.core.LogHandler#isCompressed()
   */
  public boolean isCompressed() {
    return false;
  }

  @Override
  public boolean isEnabled(License license) throws CoreException {
    return true;
  }
}
