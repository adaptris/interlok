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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import java.io.File;
import java.io.OutputStream;
import org.apache.commons.io.FileUtils;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.core.CoreException;
import com.adaptris.core.DefaultMessageFactory;
import com.adaptris.core.stubs.TempFileUtils;

@SuppressWarnings("deprecation")
public class FileDataOutputParameterTest {

  private static final String TEXT = "Hello World";


  @Rule
  public TestName testName = new TestName();


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
    p.setEndpoint("file:////tmp/abc");
    assertEquals("file:////tmp/abc", p.url(m));
    try {
      p.setEndpoint(null);
      fail();
    } catch (IllegalArgumentException e) {

    }
    assertEquals("file:////tmp/abc", p.url(m));
  }

  @Test
  public void testInsert() throws Exception {
    FileDataOutputParameter p = new FileDataOutputParameter();
    File f = TempFileUtils.createTrackedFile(testName.getMethodName(), "", p);
    p.setEndpoint("file:///" + f.getCanonicalPath());
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage();
    p.insert(TEXT, msg);
    // It doesn't insert into the msg; so message should still be blank
    assertNotSame(TEXT, msg.getContent());
    assertEquals(TEXT, FileUtils.readFileToString(f));
  }

  @Test
  public void testInsertDestination() throws Exception {
    FileDataOutputParameter p = new FileDataOutputParameter();
    File f = TempFileUtils.createTrackedFile(testName.getMethodName(), "", p);
    p.setEndpoint("file:///" + f.getCanonicalPath());
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage();
    p.insert(TEXT, msg);
    // It doesn't insert into the msg; so message should still be blank
    assertNotSame(TEXT, msg.getContent());
    assertEquals(TEXT, FileUtils.readFileToString(f));
  }

  @Test
  public void testInsertDestination_Exception() throws Exception {
    FileDataOutputParameter p = new FileDataOutputParameter();
    File f = TempFileUtils.createTrackedFile(testName.getMethodName(), "", p);
    p.setEndpoint("file:///" + f.getCanonicalPath());
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
  public void testWrap() throws Exception {
    FileOutputMessageWrapper p = new FileOutputMessageWrapper();
    File f = TempFileUtils.createTrackedFile(testName.getMethodName(), "", p);
    assertFalse(f.exists());
    p.setEndpoint("file:///" + f.getCanonicalPath());
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage();
    try (OutputStream out = p.wrap(msg)) {
      assertNotNull(out);
    }
    assertTrue(f.exists());
  }
}
