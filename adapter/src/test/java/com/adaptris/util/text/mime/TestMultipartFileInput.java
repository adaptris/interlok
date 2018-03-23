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

package com.adaptris.util.text.mime;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.Properties;

import javax.mail.internet.MimeBodyPart;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.adaptris.core.stubs.TempFileUtils;
import com.adaptris.util.GuidGenerator;

public class TestMultipartFileInput implements MimeConstants {

  private static final String PAYLOAD_1 = "The quick brown fox jumps over the lazy dog";
  private static final String PAYLOAD_2 = "Sixty zippers were quickly picked from the woven jute bag";
  private static final String PAYLOAD_3 = "Quick zephyrs blow, vexing daft Jim";

  private GuidGenerator guid;
  static final Log logR = LogFactory.getLog(TestMultipartFileInput.class);

  private Properties txtProps;


  @Before
  public void setUp() throws Exception {
    guid = new GuidGenerator();
  }

  @After
  public void tearDown() {
  }

  @Test
  public void testIterator() throws Exception {
    File input = generateInput();
    try (MultiPartFileInput mimeInput = new MultiPartFileInput(input)) {
      assertEquals(3, mimeInput.size());
      int count = 0;
      while (mimeInput.hasNext()) {
        MimeBodyPart part = mimeInput.next();
        count++;
        switch (count) {
        case 1: {
          assertEquals(PAYLOAD_1, toString(part));
          break;
        }
        case 2: {
          assertEquals(PAYLOAD_2, toString(part));
          break;
        }
        case 3: {
          assertEquals(PAYLOAD_3, toString(part));
          break;
        }
        default : {}  
        }
      }
      assertEquals(3, count);
    }
  }

  @Test
  public void testGetById() throws Exception {
    File input = generateInput();
    try (MultiPartFileInput mimeInput = new MultiPartFileInput(input)) {
      MimeBodyPart part = mimeInput.getBodyPart("payload2");
      assertEquals(PAYLOAD_2, toString(part));
      assertNull(mimeInput.getBodyPart("hello"));
    }

  }

  @Test
  public void testGetByPosition() throws Exception {
    File input = generateInput();
    try (MultiPartFileInput mimeInput = new MultiPartFileInput(input)) {
      MimeBodyPart part = mimeInput.getBodyPart(1);
      assertEquals(PAYLOAD_2, toString(part));
      assertNull(mimeInput.getBodyPart(6));
    }
  }

  @Test(expected = UnsupportedOperationException.class)
  public void testRemove() throws Exception {
    File input = generateInput();
    try (MultiPartFileInput mimeInput = new MultiPartFileInput(input)) {
      mimeInput.next();
      mimeInput.remove();
    }
  }

  private File generateInput() throws Exception {
    File file = TempFileUtils.createTrackedFile(this);
    MultiPartOutput output = new MultiPartOutput(guid.getUUID());
    output.addPart(PAYLOAD_1, "payload1");
    output.addPart(PAYLOAD_2, "payload2");
    output.addPart(PAYLOAD_3, "payload3");
    try (FileOutputStream out = new FileOutputStream(file)) {
      output.writeTo(out);
    }
    return file;
  }

  private String toString(MimeBodyPart p) throws Exception {
    StringWriter out = new StringWriter();
    try (InputStream in = p.getInputStream()) {
      IOUtils.copy(in, out);
    }
    return out.toString();
  }
}
