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

package com.adaptris.util.stream;

import java.io.IOException;
import java.io.PrintStream;

import org.junit.Test;

import com.adaptris.util.stream.LoggingOutputStreamImpl.LogLevel;

public class Slf4jLoggingOutputStreamTest {

  private static final String TEXT = "The Quick Brown fox jumps over the lazy dog.";

  @Test
  public void testLogTrace() throws Exception {
    PrintStream out = new PrintStream(new Slf4jLoggingOutputStream("TRACE"));
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

  @Test(expected = IOException.class)
  public void testLogPostClose() throws Exception {
    Slf4jLoggingOutputStream out = new Slf4jLoggingOutputStream(LogLevel.INFO);
    for (int i = 0; i < TEXT.length(); i++) {
      out.write(TEXT.charAt(i));
    }
    out.close();
    out.write(TEXT.charAt(0));
  }

  @Test
  public void testLogFlush_CR() throws Exception {
    Slf4jLoggingOutputStream out = new Slf4jLoggingOutputStream(LogLevel.INFO);
    out.write('\r');
    out.flush();
    out.write('\t');
    out.flush();
    out.close();
  }

  @Test
  public void testLogFlush_LF() throws Exception {
    Slf4jLoggingOutputStream out = new Slf4jLoggingOutputStream(LogLevel.INFO);
    out.write('\n');
    out.flush();
    out.write('\t');
    out.flush();
    out.close();
  }

  @Test
  public void testLogFlush_CRLF() throws Exception {
    Slf4jLoggingOutputStream out = new Slf4jLoggingOutputStream(LogLevel.INFO);
    out.write('\r');
    out.write('\n');
    out.flush();
    out.write('A');
    out.write('B');
    out.flush();
    out.write('A');
    out.write('\r');
    out.flush();
    out.write('A');
    out.write('\n');
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
