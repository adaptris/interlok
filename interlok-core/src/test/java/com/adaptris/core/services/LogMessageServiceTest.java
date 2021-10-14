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

package com.adaptris.core.services;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import org.junit.Test;
import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.core.GeneralServiceExample;
import com.adaptris.core.services.LoggingServiceImpl.LoggingLevel;
import com.adaptris.core.util.MinimalMessageLogger;
import com.adaptris.core.util.PayloadMessageLogger;

public class LogMessageServiceTest extends GeneralServiceExample {

  @Override
  protected Object retrieveObjectForSampleConfig() {
    return new LogMessageService(LoggingLevel.DEBUG);
  }

  @Test
  public void testSetPrefix() {
    LogMessageService srv = new LogMessageService();
    srv.setLogPrefix("HELLO");
    assertEquals("HELLO", srv.getLogPrefix());
  }

  @Test
  public void testSetLoggingFormat() throws Exception {
    LogMessageService srv = new LogMessageService(LoggingLevel.ERROR, "testSetLoggingFormat: ");
    assertNull(srv.getLoggingFormat());
    assertEquals(PayloadMessageLogger.class, srv.loggingFormat().getClass());

    srv.setLoggingFormat(new MinimalMessageLogger());
    assertEquals(MinimalMessageLogger.class, srv.loggingFormat().getClass());

  }

  @Test
  public void testSetLogLevel() {
    LogMessageService srv = new LogMessageService();
    assertEquals(LoggingLevel.DEBUG, srv.getLogLevel());
    srv.setLogLevel(LoggingLevel.TRACE);
    assertEquals(LoggingLevel.TRACE, srv.getLogLevel());
    srv.setLogLevel(null);
    assertNull(srv.getLogLevel());
  }


  @Test
  public void testLoggingAtError() throws Exception {
    LogMessageService srv = new LogMessageService();
    srv.setLogLevel(LoggingLevel.ERROR);
    execute(srv, AdaptrisMessageFactory.getDefaultInstance().newMessage("Hello"));
  }

  @Test
  public void testLoggingAtWarn() throws Exception {
    LogMessageService srv = new LogMessageService();
    srv.setLogLevel(LoggingLevel.WARN);
    execute(srv, AdaptrisMessageFactory.getDefaultInstance().newMessage("Hello"));
  }

  @Test
  public void testLoggingAtInfo() throws Exception {
    LogMessageService srv = new LogMessageService();
    srv.setLogLevel(LoggingLevel.INFO);
    execute(srv, AdaptrisMessageFactory.getDefaultInstance().newMessage("Hello"));
  }

  @Test
  public void testLoggingAtDebug() throws Exception {
    LogMessageService srv = new LogMessageService();
    srv.setLogLevel(LoggingLevel.DEBUG);
    execute(srv, AdaptrisMessageFactory.getDefaultInstance().newMessage("Hello"));
  }

  @Test
  public void testLoggingAtTrace() throws Exception {
    LogMessageService srv = new LogMessageService();
    srv.setLogLevel(LoggingLevel.TRACE);
    execute(srv, AdaptrisMessageFactory.getDefaultInstance().newMessage("Hello"));
  }

  @Test
  public void testDefaultLogging() throws Exception {
    LogMessageService srv = new LogMessageService();
    srv.setLogLevel(null);
    execute(srv, AdaptrisMessageFactory.getDefaultInstance().newMessage("Hello"));
  }

}
