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

import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.core.GeneralServiceExample;
import com.adaptris.core.services.LogMessageService.LoggingLevel;

@SuppressWarnings("deprecation")
public class LogMessageServiceTest extends GeneralServiceExample {

  public LogMessageServiceTest(String name) {
    super(name);
  }

  @Override
  protected Object retrieveObjectForSampleConfig() {
    return new LogMessageService(LoggingLevel.DEBUG);
  }

  public void testSetPrefix() {
    LogMessageService srv = new LogMessageService();
    srv.setLogPrefix("HELLO");
    assertEquals("HELLO", srv.getLogPrefix());
  }

  public void testSetLogPayload() {
    LogMessageService srv = new LogMessageService();
    assertNull(srv.getIncludePayload());
    assertEquals(true, srv.includePayload());
    srv.setIncludePayload(Boolean.FALSE);
    assertEquals(Boolean.FALSE, srv.getIncludePayload());
    assertEquals(false, srv.includePayload());
    srv.setIncludePayload(null);
    assertNull(srv.getIncludePayload());
    assertEquals(true, srv.includePayload());
  }


  public void testSetIncludeEvents() {
    LogMessageService srv = new LogMessageService();
    assertNull(srv.getIncludeEvents());
    assertEquals(false, srv.includeEvents());
    srv.setIncludeEvents(Boolean.TRUE);
    assertEquals(Boolean.TRUE, srv.getIncludeEvents());
    assertEquals(true, srv.includeEvents());
    srv.setIncludeEvents(null);
    assertNull(srv.getIncludeEvents());
    assertEquals(false, srv.includeEvents());
  }

  public void testSetLogLevel() {
    LogMessageService srv = new LogMessageService();
    assertEquals(LoggingLevel.DEBUG, srv.getLogLevel());
    srv.setLogLevel(LoggingLevel.TRACE);
    assertEquals(LoggingLevel.TRACE, srv.getLogLevel());
    srv.setLogLevel(null);
    assertNull(srv.getLogLevel());
  }

  public void testLoggingAtFatal() throws Exception {
    LogMessageService srv = new LogMessageService();
    srv.setLogLevel(LoggingLevel.FATAL);
    execute(srv, AdaptrisMessageFactory.getDefaultInstance().newMessage("Hello"));
  }

  public void testLoggingAtError() throws Exception {
    LogMessageService srv = new LogMessageService();
    srv.setLogLevel(LoggingLevel.ERROR);
    execute(srv, AdaptrisMessageFactory.getDefaultInstance().newMessage("Hello"));
  }

  public void testLoggingAtWarn() throws Exception {
    LogMessageService srv = new LogMessageService();
    srv.setLogLevel(LoggingLevel.WARN);
    execute(srv, AdaptrisMessageFactory.getDefaultInstance().newMessage("Hello"));
  }

  public void testLoggingAtInfo() throws Exception {
    LogMessageService srv = new LogMessageService();
    srv.setLogLevel(LoggingLevel.INFO);
    execute(srv, AdaptrisMessageFactory.getDefaultInstance().newMessage("Hello"));
  }

  public void testLoggingAtDebug() throws Exception {
    LogMessageService srv = new LogMessageService();
    srv.setLogLevel(LoggingLevel.DEBUG);
    execute(srv, AdaptrisMessageFactory.getDefaultInstance().newMessage("Hello"));
  }

  public void testLoggingAtTrace() throws Exception {
    LogMessageService srv = new LogMessageService();
    srv.setLogLevel(LoggingLevel.TRACE);
    execute(srv, AdaptrisMessageFactory.getDefaultInstance().newMessage("Hello"));
  }

  public void testDefaultLogging() throws Exception {
    LogMessageService srv = new LogMessageService();
    srv.setLogLevel(null);
    execute(srv, AdaptrisMessageFactory.getDefaultInstance().newMessage("Hello"));
  }

}
