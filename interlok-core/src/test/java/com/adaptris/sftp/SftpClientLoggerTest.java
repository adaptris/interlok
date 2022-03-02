package com.adaptris.sftp;

import static org.junit.Assert.assertEquals;
import org.junit.Test;
import org.slf4j.LoggerFactory;

public class SftpClientLoggerTest  {

  @Test
  public void testEnabled() {
    org.slf4j.Logger logger = LoggerFactory.getLogger(SftpClientLogger.class);
    SftpClientLogger clientLogger = new SftpClientLogger();
    assertEquals(logger.isDebugEnabled(), clientLogger.isEnabled(com.jcraft.jsch.Logger.DEBUG));
    assertEquals(logger.isInfoEnabled(), clientLogger.isEnabled(com.jcraft.jsch.Logger.INFO));
    assertEquals(logger.isWarnEnabled(), clientLogger.isEnabled(com.jcraft.jsch.Logger.WARN));
    assertEquals(logger.isErrorEnabled(), clientLogger.isEnabled(com.jcraft.jsch.Logger.ERROR));
    assertEquals(true, clientLogger.isEnabled(com.jcraft.jsch.Logger.FATAL));
  }

  @Test
  public void testLogging() {
    org.slf4j.Logger logger = LoggerFactory.getLogger(SftpClientLogger.class);
    SftpClientLogger clientLogger = new SftpClientLogger();
    clientLogger.log(com.jcraft.jsch.Logger.DEBUG, "hello");
    clientLogger.log(com.jcraft.jsch.Logger.INFO, "hello");
    clientLogger.log(com.jcraft.jsch.Logger.WARN, "hello");
    clientLogger.log(com.jcraft.jsch.Logger.ERROR, "hello");
    clientLogger.log(com.jcraft.jsch.Logger.FATAL, "hello");
  }

}
