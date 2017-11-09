/*
 * Copyright 2017 Adaptris Ltd.
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
package com.adaptris.core.common;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;

import java.io.File;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.core.CoreException;
import com.adaptris.core.DefaultMessageFactory;
import com.adaptris.core.stubs.TempFileUtils;

public class MetadataFileInputParameterTest {
  public static final String TEXT = "Hello World";

  @Rule
  public TestName testName = new TestName();

  @Before
  public void setUp() throws Exception {
  }

  @After
  public void tearDown() throws Exception {
  }

  @Test
  public void testExtract() throws Exception {
    MetadataFileInputParameter p = new MetadataFileInputParameter();
    File f = TempFileUtils.createTrackedFile(testName.getMethodName(), "", p);
    p.setMetadataKey("metadataKey");
    assertEquals("metadataKey", p.getMetadataKey());
    FileUtils.write(f, TEXT, false);
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage();
    msg.addMetadata("metadataKey", "file:///" + f.getCanonicalPath());
    assertNotSame(TEXT, msg.getContent());
    assertEquals(TEXT, p.extract(msg));
  }


  @Test(expected = CoreException.class)
  public void testNonExistingFile() throws Exception {
    AdaptrisMessage m = new DefaultMessageFactory().newMessage();
    MetadataFileInputParameter p = new MetadataFileInputParameter();
    p.setMetadataKey("metadataKey");
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage();
    msg.addMetadata("metadataKey", "file:///path/does/not/exist/lets/hope/thats/true");
    String result = p.extract(msg);
  }

}
