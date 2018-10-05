/*
 * Copyright 2015 Adaptris Ltd.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.adaptris.core.lms;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;

import org.apache.commons.io.FileCleaningTracker;
import org.apache.commons.io.FileDeleteStrategy;
import org.junit.Test;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageFactoryImplCase;

public class FileBackedMessageFactoryTest extends AdaptrisMessageFactoryImplCase {
  private static FileCleaningTracker cleaner = new FileCleaningTracker();
  private transient Object marker = new Object();

  /**
   * @see com.adaptris.core.AdaptrisMessageFactoryImplCase#getMessageFactory()
   */
  @Override
  protected FileBackedMessageFactory getMessageFactory() {
    return new FileBackedMessageFactory();
  }

  @Test
  public void testSetTempDirectory() throws Exception {
    FileBackedMessageFactory factory = getMessageFactory();
    String tmpDir = tempDir();
    assertNull(factory.getTempDirectory());
    assertNotNull(factory.tempDirectory());
    assertEquals(new File(System.getProperty("java.io.tmpdir")), factory.tempDirectory());
    factory.setTempDirectory(tmpDir);
    assertEquals(tmpDir, factory.getTempDirectory());
    assertEquals(tmpDir, factory.tempDirectory().getCanonicalPath());
  }

  @Test
  public void testSetCreateTempDir() throws Exception {
    FileBackedMessageFactory factory = getMessageFactory();
    assertNull(factory.getCreateTempDir());
    assertFalse(factory.createTempDir());

    factory.setCreateTempDir(false);
    assertNotNull(factory.getCreateTempDir());
    assertFalse(factory.createTempDir());
  }

  @Test
  public void testCreateMessageWithoutTempDirCreate() throws Exception {
    FileBackedMessageFactory factory = getMessageFactory();
    String tmpDir = tempDir();
    factory.setTempDirectory(tmpDir);
    try {
      AdaptrisMessage msg = factory.newMessage();
      try (Writer out = new OutputStreamWriter(msg.getOutputStream())) {
        out.write("hello");
      }
      fail();
    } catch (Exception expected) {

    }
  }

  @Test
  public void testCreateMessageWithTempDirCreate() throws Exception {
    FileBackedMessageFactory factory = getMessageFactory();
    String tmpDir = tempDir();
    factory.setTempDirectory(tmpDir);
    factory.setCreateTempDir(true);
    factory.setExtendedLogging(true);
    AdaptrisMessage msg = factory.newMessage();
    try (Writer out = new OutputStreamWriter(msg.getOutputStream())) {
      out.write("hello");
    }
    File dir = new File(tmpDir);
    assertTrue(dir.listFiles().length > 0);
  }


  private String tempDir() throws IOException {
    File f = File.createTempFile(FileBackedMessageFactoryTest.class.getSimpleName(), "", null);
    f.delete();
    cleaner.track(f, marker, FileDeleteStrategy.FORCE);
    return f.getCanonicalPath();
  }
}
