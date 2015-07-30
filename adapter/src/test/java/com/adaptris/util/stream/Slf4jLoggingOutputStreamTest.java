package com.adaptris.util.stream;

import java.io.PrintStream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.adaptris.util.stream.LoggingOutputStreamImpl.LogLevel;

public class Slf4jLoggingOutputStreamTest {

  private static final String TEXT = "The Quick Brown fox jumps over the lazy dog.";

  private static Log logR = LogFactory.getLog(Slf4jLoggingOutputStreamTest.class);

  @Before
  public void setUp() throws Exception {
    logR = LogFactory.getLog(this.getClass());
  }

  @After
  public void tearDown() throws Exception {
  }

  @Test
  public void testLogTrace() throws Exception {
    PrintStream out = new PrintStream(new Slf4jLoggingOutputStream(LogLevel.TRACE));
    out.println(TEXT);
    out.flush();
    out.close();
  }

  @Test
  public void testLogDebug() throws Exception {
    PrintStream out = new PrintStream(new Slf4jLoggingOutputStream(LogLevel.DEBUG));
    out.println(TEXT);
    out.flush();
    out.close();
  }

  @Test
  public void testLogInfo() throws Exception {
    PrintStream out = new PrintStream(new Slf4jLoggingOutputStream(LogLevel.INFO));
    out.println(TEXT);
    out.flush();
    out.close();
  }

  @Test
  public void testLogGreaterThanBuffer() throws Exception {
    PrintStream out = new PrintStream(new Slf4jLoggingOutputStream(LogLevel.INFO));
    StringBuffer sb = new StringBuffer();
    while (sb.length() < 2048) {
      sb.append(TEXT);
    }
    out.println(sb.toString());
    out.flush();
    out.close();
  }

  @Test
  public void testLogWarn() throws Exception {
    PrintStream out = new PrintStream(new Slf4jLoggingOutputStream(LogLevel.WARN));
    out.println(TEXT);
    out.flush();
    out.close();
  }

  @Test
  public void testLogError() throws Exception {
    PrintStream out = new PrintStream(new Slf4jLoggingOutputStream(LogLevel.ERROR));
    out.println(TEXT);
    out.flush();
    out.close();
  }

  @Test
  public void testLogFatal() throws Exception {
    PrintStream out = new PrintStream(new Slf4jLoggingOutputStream(LogLevel.FATAL));
    out.println(TEXT);
    out.flush();
    out.close();
  }
}
