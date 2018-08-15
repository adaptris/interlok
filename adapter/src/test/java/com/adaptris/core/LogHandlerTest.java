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

package com.adaptris.core;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class LogHandlerTest {

  public static final String LOG_ENTRY = "TRACE [main] [PingEvent.handleRequest()] ping event handled\n"
      + "DEBUG [main] [ClosedState.requestInit()] completed init [DefaultEventHandler]\n"
      + "DEBUG [main] [ClosedState.requestStart()] started [DefaultEventHandler]\n"
      + "DEBUG [main] [Adapter.setWorkflowsInFailedMessageRetrier()] FailedMessageRetrier []\n"
      + "DEBUG [testLifecycleWithBlockingLifecycleStrategy_channel1 Init] [ClosedState.requestInit()] completed init [DefaultEventHandler]\n"
      + "DEBUG [testLifecycleWithBlockingLifecycleStrategy_channel1 Init] [ClosedStat...[truncated 111194 chars]...] closed [testHeartbeatTimerTask_channel1]\n"
      + "TRACE [EventProducerThread] [MockMessageProducer.produce()] Produced [a9fbc945-0000-000a-6564-dbd5cbf15978]\n"
      + "TRACE [EventProducerThread] [MockMessageProducer.produce()] Produced [a9fbc948-0000-000a-6564-dbd545e68616]\n"
      + "DEBUG [main] [StartedState.requestStop()] stopped [SeparateConnectionEventHandler]\n"
      + "DEBUG [main] [StartedState.requestClose()] closed [SeparateConnectionEventHandler]\n"
      + "DEBUG [main] [StartedState.requestClose()] closed [testHeartbeatTimerTask]\n";

  private static final File LOG_DIRECTORY;
  static {
    try {
      LOG_DIRECTORY = File.createTempFile(LogHandlerTest.class.getSimpleName(), null);
      LOG_DIRECTORY.delete();
    }
    catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Before
  public void setUp() throws Exception {
    ensureDirectory(LOG_DIRECTORY);
  }

  @After
  public void tearDown() throws Exception {
    FileUtils.deleteQuietly(LOG_DIRECTORY);
    FileUtils.deleteDirectory(LOG_DIRECTORY);
  }

  @Test
  public void testNullLogHandler() throws Exception {
    NullLogHandler lh = new NullLogHandler();
    lh.clean();
  }

  @Test
  public void testFileLogHandlerSetLogDirectory() throws Exception {
    FileLogHandler fh = new FileLogHandler();
    fh.setLogDirectory(System.getProperty("java.io.tmpdir"));
    assertEquals(System.getProperty("java.io.tmpdir"), fh.getLogDirectory());
  }

  @Test
  public void testFileLogHandlerSetLogFile() throws Exception {
    FileLogHandler fh = new FileLogHandler();
    fh.setLogFile("adapter.log");
    assertEquals("adapter.log", fh.getLogFile());
    try {
      fh.setLogFile("");
      fail();
    }
    catch (IllegalArgumentException expected) {

    }
    assertEquals("adapter.log", fh.getLogFile());
    try {
      fh.setLogFile(null);
      fail();
    }
    catch (IllegalArgumentException expected) {
    }
    assertEquals("adapter.log", fh.getLogFile());
  }

  @Test
  public void testFileLogHandlerSetPeriod() throws Exception {
    FileLogHandler fh = new FileLogHandler();
    fh.setPeriod(1);
    assertEquals(1, fh.period());
    try {
      fh.setPeriod(-1);
      fail();
    }
    catch (IllegalArgumentException expected) {

    }
    assertEquals(1, fh.period());
  }

  @Test
  public void testFileLogHandlerClean() throws Exception {
    createLogFiles(LOG_DIRECTORY, "adapter.log.", 10);
    FileLogHandler fh = new FileLogHandler();
    fh.setPeriod(1);
    fh.setLogFile("adapter.log");
    fh.setLogDirectory(LOG_DIRECTORY.getCanonicalPath());
    fh.clean();
    assertEquals(1, LOG_DIRECTORY.listFiles().length);
  }

  @Test
  public void testFileLogHandlerCleanNonExistent() throws Exception {
    FileLogHandler fh = new FileLogHandler();
    fh.setPeriod(1);
    fh.setLogFile("adapter.log");
    File tmpDir = File.createTempFile("TMP", "");
    fh.setLogDirectory(tmpDir.getCanonicalPath());
    fh.clean();
    FileUtils.deleteQuietly(tmpDir);
  }


  public static List<Long> createLogFiles(File dir, String prefix, int count) throws Exception {
    ensureDirectory(dir);
    ArrayList<Long> ages = new ArrayList<Long>();
    // Shoudld give us files ranging from 9 days in the past to 1hour in the
    // past.
    for (int i = count - 1; i >= 0; i--) {
      Calendar c = Calendar.getInstance();
      c.add(Calendar.DAY_OF_YEAR, 0 - i);
      c.add(Calendar.MINUTE, -1);
      ages.add(c.getTimeInMillis());
    }
    for (Long age : ages) {
      File f = File.createTempFile(prefix, null, dir);
      f.setLastModified(age);
    }
    return ages;
  }

  public static void ensureDirectory(File dirOrFile) {
    if (dirOrFile.isFile()) {
      dirOrFile.delete();
    }
    dirOrFile.mkdirs();
  }

  public static File createLogFile(File directory, String name) throws Exception {
    File logFile = new File(directory, name);
    try (PrintStream out = new PrintStream(new FileOutputStream(logFile))) {
      out.print(LOG_ENTRY);
    }
    return logFile;
  }

  private void write(long size, File f) throws IOException {
    RandomAccessFile rf = new RandomAccessFile(f, "rw");
    rf.setLength(size);
    rf.close();
  }
}
