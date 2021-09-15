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

package com.adaptris.core.runtime;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.io.File;
import java.io.IOException;
import java.util.Date;

import org.apache.commons.io.FileCleaningTracker;
import org.apache.commons.io.FileDeleteStrategy;
import org.junit.Test;

import com.adaptris.core.MessageLifecycleEvent;

public class MessageDigestErrorEntryTest {

  private static FileCleaningTracker cleaner = new FileCleaningTracker();

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
    assertNull(entry.getExceptionStackTrace());
    Date d = new Date();
    entry = new MessageDigestErrorEntry("123", "234", d);
    assertNotNull(entry.getDate());
    assertEquals(d, entry.getDate());
    assertEquals("123", entry.getUniqueId());
    assertEquals("234", entry.getWorkflowId());
    assertNull(entry.getFileSystemPath());
    assertNull(entry.getStackTrace());
    assertNull(entry.getExceptionStackTrace());
  }

  @Test
  public void testSetStackTrace() throws Exception {
    MessageDigestErrorEntry entry = null;
    entry = new MessageDigestErrorEntry("123", "234");
    assertNull(entry.getFileSystemPath());
    assertNull(entry.getStackTrace());
    assertNull(entry.getExceptionStackTrace());
    Exception e = new Exception(this.getClass().getSimpleName());
    entry.setStackTrace(e);
    assertNotNull(entry.getStackTrace());
    assertNotNull(entry.getExceptionStackTrace());
    entry.setStackTrace(e.getMessage());
    assertEquals(e.getMessage(), entry.getStackTrace());
    assertEquals(e.getMessage(), entry.getExceptionStackTrace());
    entry.setStackTrace((Exception) null);
    assertEquals("", entry.getStackTrace());
    assertEquals("", entry.getExceptionStackTrace());
  }

  @Test
  public void testSetFilesystemLocation() throws Exception {
    MessageDigestErrorEntry entry = null;
    entry = new MessageDigestErrorEntry("123", "234");
    assertNull(entry.getFileSystemPath());
    assertNull(entry.getStackTrace());
    assertNull(entry.getExceptionStackTrace());
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
    MessageDigestErrorEntry entry = new MessageDigestErrorEntry("123", "234");
    assertNotNull(entry.toString());

    entry = new MessageDigestErrorEntry("123", "234", new Date());
    assertNotNull(entry.toString());
  }

  private File deleteLater(Object marker) throws IOException {
    File file = File.createTempFile(this.getClass().getSimpleName(), "");
    cleaner.track(file, marker, FileDeleteStrategy.FORCE);
    return file;
  }

}
