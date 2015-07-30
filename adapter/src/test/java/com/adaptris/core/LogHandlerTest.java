package com.adaptris.core;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.RandomAccessFile;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.zip.GZIPInputStream;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.adaptris.core.LogHandler.LogFileType;

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
    assertFalse(lh.isCompressed());
    lh.clean();
    assertNotNull(lh.retrieveLog(LogFileType.Standard));
    InputStream in = lh.retrieveLog(LogFileType.Standard);
    assertMatches(in, "(?s)^No implementation of the Logging Handler configured.*");
  }

  @Test
  public void testFileLogHandlerSetUseCompression() throws Exception {
    FileLogHandler fh = new FileLogHandler();
    assertEquals(true, fh.isCompressed());
    fh.setUseCompression(false);
    assertEquals(false, fh.isCompressed());
    assertEquals(false, fh.getUseCompression());
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
    assertEquals(1, fh.getPeriod());
    try {
      fh.setPeriod(-1);
      fail();
    }
    catch (IllegalArgumentException expected) {

    }
    assertEquals(1, fh.getPeriod());
  }

  @Test
  public void testFileLogHandlerSetStatisticsLogFile() throws Exception {
    FileLogHandler fh = new FileLogHandler();
    fh.setStatisticsLogFile("stats.log");
    assertEquals("stats.log", fh.getStatisticsLogFile());
    try {
      fh.setStatisticsLogFile("");
      fail();
    }
    catch (IllegalArgumentException expected) {

    }
    assertEquals("stats.log", fh.getStatisticsLogFile());
    try {
      fh.setStatisticsLogFile(null);
      fail();
    }
    catch (IllegalArgumentException expected) {
    }
    assertEquals("stats.log", fh.getStatisticsLogFile());
  }

  @Test
  public void testFileLogHandlerSetGraphingLogFile() throws Exception {
    FileLogHandler fh = new FileLogHandler();
    fh.setStatisticsGraphLogFile("graph.log");
    assertEquals("graph.log", fh.getStatisticsGraphLogFile());
    try {
      fh.setStatisticsGraphLogFile("");
      fail();
    }
    catch (IllegalArgumentException expected) {

    }
    assertEquals("graph.log", fh.getStatisticsGraphLogFile());
    try {
      fh.setStatisticsGraphLogFile(null);
      fail();
    }
    catch (IllegalArgumentException expected) {
    }
    assertEquals("graph.log", fh.getStatisticsGraphLogFile());
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

  private void assertMatches(InputStream in, String expected) throws IOException {
    String s = toString(in);
    assertTrue("[" + s + "] matches [" + expected + "]", s.matches(expected));
  }

  private String toString(InputStream in) throws IOException {
    InputStreamReader reader = new InputStreamReader(in, "UTF-8");
    StringWriter writer = new StringWriter();
    try {
      IOUtils.copy(reader, writer);
    }
    finally {
      IOUtils.closeQuietly(reader);
      IOUtils.closeQuietly(writer);
    }
    return writer.toString();
  }

  @Test
  public void testFileLogHandlerGetInputStream() throws Exception {
    FileLogHandler fh = new FileLogHandler();
    fh.setUseCompression(false);
    assertMatches(fh.retrieveLog(LogFileType.Standard), "(?s)^Log file type .* not found, try requesting manually.*");
    fh.setLogDirectory("ABCDE");
    assertMatches(fh.retrieveLog(LogFileType.Standard), "(?s)^Log file type .* not found, try requesting manually.*");
    fh.setLogDirectory(null);
    fh.setLogFile("ABCDE");
    assertMatches(fh.retrieveLog(LogFileType.Standard), "(?s)^Log file type .* not found, try requesting manually.*");
    fh.setLogDirectory(LOG_DIRECTORY.getCanonicalPath());
    fh.setLogFile("adapter.log");
  }

  @Test
  public void testFileLogHandlerGetInputStreamNoCompression() throws Exception {
    File logFile = createLogFile(LOG_DIRECTORY, "adapter.log");
    FileLogHandler fh = new FileLogHandler();
    fh.setUseCompression(false);
    fh.setLogFile(logFile.getName());
    fh.setLogDirectory(logFile.getParentFile().getCanonicalPath());
    assertEquals(LOG_ENTRY, toString(fh.retrieveLog(LogFileType.Standard)));
    FileUtils.deleteQuietly(logFile.getParentFile());
  }

  @Test
  public void testFileLogHandlerGetInputStreamWithCompression() throws Exception {
    File logFile = createLogFile(LOG_DIRECTORY, "adapter.log");
    FileLogHandler fh = new FileLogHandler();
    fh.setUseCompression(true);
    fh.setLogFile(logFile.getName());
    fh.setLogDirectory(logFile.getParentFile().getCanonicalPath());
    GZIPInputStream input = null;
    String output = null;
    try {
      input = new GZIPInputStream(fh.retrieveLog(LogFileType.Standard));
      output = toString(input);
    }
    finally {
      IOUtils.closeQuietly(input);
    }
    assertEquals(LOG_ENTRY, output);
  }

  @Test
  public void testFileLogHandlerGetStatisticsLog() throws Exception {
    File logFile = createLogFile(LOG_DIRECTORY, "stats.log");
    FileLogHandler fh = new FileLogHandler();
    fh.setUseCompression(true);
    fh.setStatisticsLogFile(logFile.getName());
    fh.setLogDirectory(logFile.getParentFile().getCanonicalPath());
    GZIPInputStream input = null;
    String output = null;
    try {
      input = new GZIPInputStream(fh.retrieveLog(LogFileType.Statistics));
      output = toString(input);
    }
    finally {
      IOUtils.closeQuietly(input);
    }
    assertEquals(LOG_ENTRY, output);
  }

  @Test
  public void testFileLogHandlerGetStatisticsGraphingLog() throws Exception {
    File logFile = createLogFile(LOG_DIRECTORY, "graph.log");
    FileLogHandler fh = new FileLogHandler();
    fh.setUseCompression(true);
    fh.setStatisticsGraphLogFile(logFile.getName());
    fh.setLogDirectory(logFile.getParentFile().getCanonicalPath());
    GZIPInputStream input = null;
    String output = null;
    try {
      input = new GZIPInputStream(fh.retrieveLog(LogFileType.Graphing));
      output = toString(input);
    }
    finally {
      IOUtils.closeQuietly(input);
    }
    assertEquals(LOG_ENTRY, output);
  }

  @Test
  public void testFileLogHandlerGetInputStreamNonExistentFile() throws Exception {
    FileLogHandler fh = new FileLogHandler();
    fh.setUseCompression(false);
    fh.setLogFile("adapter.log");
    fh.setLogDirectory(LOG_DIRECTORY.getCanonicalPath());
    assertMatches(fh.retrieveLog(LogFileType.Standard), "(?s)^Log file type .* not found, try requesting manually.*");
  }

  @Test
  public void testFileLogHandlerGetInputStreamGreaterThan5Mb() throws Exception {
    RandomAccessFile rf = new RandomAccessFile(new File(LOG_DIRECTORY, "adapter.log"), "rw");
    rf.setLength(1024L * 7 * 1024L);
    rf.close();
    FileLogHandler fh = new FileLogHandler();
    fh.setUseCompression(false);
    fh.setLogFile("adapter.log");
    fh.setLogDirectory(LOG_DIRECTORY.getCanonicalPath());
    InputStream in = null;
    try {
      in = fh.retrieveLog(LogFileType.Standard);
      assertNotNull(in);
      assertEquals(5 * 1024 * 1024, in.available());
    }
    finally {
      IOUtils.closeQuietly(in);
    }
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
    PrintStream out = new PrintStream(new FileOutputStream(logFile));
    try {
      out.print(LOG_ENTRY);
      out.flush();
    }
    finally {
      IOUtils.closeQuietly(out);
    }
    return logFile;
  }

  private void write(long size, File f) throws IOException {
    RandomAccessFile rf = new RandomAccessFile(f, "rw");
    rf.setLength(size);
    rf.close();
  }
}
