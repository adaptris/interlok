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
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;

import java.io.File;

import org.apache.commons.io.FileUtils;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import org.mockito.Mockito;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.core.ConfiguredProduceDestination;
import com.adaptris.core.CoreException;
import com.adaptris.core.DefaultMessageFactory;
import com.adaptris.core.stubs.TempFileUtils;
import com.adaptris.interlok.types.InterlokMessage;

public class FileDataOutputParameterTest {

  private static final String TEXT = "Hello World";


  @Rule
  public TestName testName = new TestName();

  @Test
  public void testUrl() throws Exception {
    AdaptrisMessage m = new DefaultMessageFactory().newMessage();
    FileDataOutputParameter p = new FileDataOutputParameter();
    assertNull(p.url(m));
    p.setUrl("file:////tmp/abc");
    assertEquals("file:////tmp/abc", p.url(m));
    p.setDestination(new ConfiguredProduceDestination("file:////tmp/destination"));
    assertEquals("file:////tmp/destination", p.url(m));
    InterlokMessage msg = Mockito.mock(InterlokMessage.class);
    try {
      p.url(msg);
      fail();
    }
    catch (RuntimeException expected) {

    }
  }

  @Test
  public void testDestination() throws Exception {
    AdaptrisMessage m = new DefaultMessageFactory().newMessage();
    FileDataOutputParameter p = new FileDataOutputParameter();
    assertNull(p.url(m));
    p.setDestination(new ConfiguredProduceDestination("file:////tmp/abc"));
    assertEquals("file:////tmp/abc", p.url(m));
    try {
      p.setDestination(null);
      fail();
    } catch (IllegalArgumentException e) {

    }
    assertEquals("file:////tmp/abc", p.url(m));
  }
  
  @Test
  public void testInsert() throws Exception {
    FileDataOutputParameter p = new FileDataOutputParameter();
    File f = TempFileUtils.createTrackedFile(testName.getMethodName(), "", p);
    p.setUrl("file:///" + f.getCanonicalPath());
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
    p.setDestination(new ConfiguredProduceDestination("file:///" + f.getCanonicalPath()));
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
    p.setDestination(new ConfiguredProduceDestination("file:///" + f.getCanonicalPath()));
    AdaptrisMessage msg = mock(AdaptrisMessage.class);
    doThrow(new RuntimeException()).when(msg).getContentEncoding();
    try {
      p.insert(TEXT, msg);
      fail();
    }
    catch (CoreException expected) {

    }
  }

}
