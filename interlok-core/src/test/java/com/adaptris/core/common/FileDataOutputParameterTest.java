/*
 * Copyright 2016 Adaptris Ltd.
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
package com.adaptris.core.common;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;

import java.io.File;
import java.io.OutputStream;

import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.core.CoreException;
import com.adaptris.core.DefaultMessageFactory;
import com.adaptris.core.stubs.TempFileUtils;

@SuppressWarnings("deprecation")
public class FileDataOutputParameterTest {

  private static final String TEXT = "Hello World";


  
  


  @Test
  public void testDestination() throws Exception {
    AdaptrisMessage m = new DefaultMessageFactory().newMessage();
    FileDataOutputParameter p = new FileDataOutputParameter();
    try {
      p.url(m);
      fail();
    } catch (IllegalArgumentException e) {
      // ok
    }
    p.setUrl("file:////tmp/abc");
    assertEquals("file:////tmp/abc", p.url(m));
    try {
      p.setUrl(null);
      fail();
    } catch (IllegalArgumentException e) {

    }
    assertEquals("file:////tmp/abc", p.url(m));
  }

  @Test
  public void testInsert(TestInfo info) throws Exception {
    FileDataOutputParameter p = new FileDataOutputParameter();
    File f = TempFileUtils.createTrackedFile(info.getDisplayName(), "", p);
    p.setUrl("file:///" + f.getCanonicalPath());
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage();
    p.insert(TEXT, msg);
    // It doesn't insert into the msg; so message should still be blank
    assertNotSame(TEXT, msg.getContent());
    assertEquals(TEXT, FileUtils.readFileToString(f));
  }

  @Test
  public void testInsertDestination(TestInfo info) throws Exception {
    FileDataOutputParameter p = new FileDataOutputParameter();
    File f = TempFileUtils.createTrackedFile(info.getDisplayName(), "", p);
    p.withUrl("file:///" + f.getCanonicalPath());
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage();
    p.insert(TEXT, msg);
    // It doesn't insert into the msg; so message should still be blank
    assertNotSame(TEXT, msg.getContent());
    assertEquals(TEXT, FileUtils.readFileToString(f));
  }

  @Test
  public void testInsertDestination_Exception(TestInfo info) throws Exception {
    FileDataOutputParameter p = new FileDataOutputParameter();
    File f = TempFileUtils.createTrackedFile(info.getDisplayName(), "", p);
    p.setUrl("file:///" + f.getCanonicalPath());
    AdaptrisMessage msg = mock(AdaptrisMessage.class);
    doThrow(new RuntimeException()).when(msg).getContentEncoding();
    try {
      p.insert(TEXT, msg);
      fail();
    }
    catch (CoreException expected) {

    }
  }

  @Test
  public void testWrap(TestInfo info) throws Exception {
    FileOutputMessageWrapper p = new FileOutputMessageWrapper();
    File f = TempFileUtils.createTrackedFile(info.getDisplayName(), "", p);
    assertFalse(f.exists());
    p.setUrl("file:///" + f.getCanonicalPath());
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage();
    try (OutputStream out = p.wrap(msg)) {
      assertNotNull(out);
    }
    assertTrue(f.exists());
  }
}
