package com.adaptris.core.runtime;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.io.File;
import java.io.IOException;
import java.util.Date;

import org.apache.commons.io.FileCleaningTracker;
import org.apache.commons.io.FileDeleteStrategy;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.adaptris.core.MessageLifecycleEvent;

public class MessageDigestErrorEntryTest {

  private static FileCleaningTracker cleaner = new FileCleaningTracker();

  @Before
  public void setUp() throws Exception {
  }

  @After
  public void tearDown() throws Exception {
  }

  @Test
  public void testConstructors() throws Exception {
    MessageDigestErrorEntry entry = null;
    entry = new MessageDigestErrorEntry();
    assertNotNull(entry.getDate());
    assertNull(entry.getUniqueId());
    assertNull(entry.getWorkflowId());

    entry = new MessageDigestErrorEntry("123", "234");
    assertNotNull(entry.getDate());
    assertEquals("123", entry.getUniqueId());
    assertEquals("234", entry.getWorkflowId());
    assertNull(entry.getFileSystemPath());
    assertNull(entry.getStackTrace());
    Date d = new Date();
    entry = new MessageDigestErrorEntry("123", "234", d);
    assertNotNull(entry.getDate());
    assertEquals(d, entry.getDate());
    assertEquals("123", entry.getUniqueId());
    assertEquals("234", entry.getWorkflowId());
    assertNull(entry.getFileSystemPath());
    assertNull(entry.getStackTrace());
  }

  @Test
  public void testSetStackTrace() throws Exception {
    MessageDigestErrorEntry entry = null;
    entry = new MessageDigestErrorEntry("123", "234");
    assertNull(entry.getFileSystemPath());
    assertNull(entry.getStackTrace());
    Exception e = new Exception(this.getClass().getSimpleName());
    entry.setStackTrace(e);
    assertNotNull(entry.getStackTrace());
    entry.setStackTrace(e.getMessage());
    assertEquals(e.getMessage(), entry.getStackTrace());
    entry.setStackTrace((Exception) null);
    assertEquals("", entry.getStackTrace());
  }

  @Test
  public void testSetFilesystemLocation() throws Exception {
    MessageDigestErrorEntry entry = null;
    entry = new MessageDigestErrorEntry("123", "234");
    assertNull(entry.getFileSystemPath());
    assertNull(entry.getStackTrace());
    File fsLocation = deleteLater(entry);
    File fsLocation2 = deleteLater(entry);
    entry.setFileSystemFile(fsLocation);
    assertEquals(fsLocation.getCanonicalPath(), entry.getFileSystemPath());
    entry.setFileSystemPath(fsLocation2.getCanonicalPath());
    assertEquals(fsLocation2.getCanonicalPath(), entry.getFileSystemPath());
  }

  @Test
  public void testSetLifecycleEvent() throws Exception {
    MessageDigestErrorEntry entry = null;
    entry = new MessageDigestErrorEntry("123", "234");
    MessageLifecycleEvent event = new MessageLifecycleEvent();
    entry.setLifecycleEvent(event);
    assertNotNull(entry.getLifecycleEvent());
    assertEquals(event, entry.getLifecycleEvent());
    entry.setLifecycleEvent(null);
    assertNull(entry.getLifecycleEvent());
  }

  @Test
  public void testToString() throws Exception {
    Exception e = new Exception();
    MessageDigestErrorEntry entry = new MessageDigestErrorEntry("123", "234");
    assertNotNull(entry.toString());

    entry = new MessageDigestErrorEntry("123", "234", new Date());
    assertNotNull(entry.toString());
  }

  private File deleteLater(Object marker) throws IOException {
    File f = File.createTempFile(this.getClass().getSimpleName(), "");
    f.deleteOnExit();
    cleaner.track(f, marker, FileDeleteStrategy.FORCE);
    return f;
  }
}