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

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.core.ConfiguredProduceDestination;
import com.adaptris.core.DefaultMessageFactory;
import com.adaptris.core.stubs.TempFileUtils;
import org.apache.commons.io.FileUtils;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

import java.io.File;

import static org.junit.Assert.*;

public class FileDataInputParameterTest {

  private static final String TEXT = "Hello World";

  @Rule
  public TestName testName = new TestName();

  @Test
  public void testUrl() throws Exception {
    AdaptrisMessage m = new DefaultMessageFactory().newMessage();
    FileDataInputParameter p = new FileDataInputParameter();
    assertNull(p.url(m));
    p.setUrl("file:////tmp/abc");
    assertEquals("file:////tmp/abc", p.url(m));
    try {
      p.setUrl("");
      fail();
    } catch (IllegalArgumentException e) {

    }
    assertEquals("file:////tmp/abc", p.url(m));
  }

  @Test
  public void testDestination() throws Exception {
    AdaptrisMessage m = new DefaultMessageFactory().newMessage();
    FileDataInputParameter p = new FileDataInputParameter();
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
  public void testExtract() throws Exception {
    FileDataInputParameter p = new FileDataInputParameter();
    File f = TempFileUtils.createTrackedFile(testName.getMethodName(), "", p);
    p.setUrl("file:///" + f.getCanonicalPath());
    FileUtils.write(f, TEXT, false);
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage();
    assertNotSame(TEXT, msg.getContent());
    assertEquals(TEXT, p.extract(msg));
  }

  @Test
  public void testExtractDestination() throws Exception {
    FileDataInputParameter p = new FileDataInputParameter();
    File f = TempFileUtils.createTrackedFile(testName.getMethodName(), "", p);
    p.setDestination(new ConfiguredProduceDestination("file:///" + f.getCanonicalPath()));
    FileUtils.write(f, TEXT, false);
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage();
    assertNotSame(TEXT, msg.getContent());
    assertEquals(TEXT, p.extract(msg));
  }

}
