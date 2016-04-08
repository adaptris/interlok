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

import java.io.File;

import org.apache.commons.io.FileUtils;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.core.stubs.TempFileUtils;

public class FileDataOutputParameterTest {

  private static final String TEXT = "Hello World";


  @Rule
  public TestName testName = new TestName();

  @Test
  public void testUrl() throws Exception {
    FileDataOutputParameter p = new FileDataOutputParameter();
    assertNull(p.getUrl());
    p.setUrl("file:////tmp/abc");
    assertEquals("file:////tmp/abc", p.getUrl());
    try {
      p.setUrl("");
      fail();
    } catch (IllegalArgumentException e) {

    }
    assertEquals("file:////tmp/abc", p.getUrl());
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

}
