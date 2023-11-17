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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.io.File;

import javax.mail.internet.MimeBodyPart;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class TestMultipartFileInput extends PartIteratorCase {

  @BeforeEach
  public void setUp() throws Exception {
  }

  @AfterEach
  public void tearDown() {
  }

  @Test
  public void testIterator() throws Exception {
    File input = generateFileInput();
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
        default: {
        }
        }
      }
      assertEquals(3, count);
    }
  }

  @Test
  public void testGetById() throws Exception {
    File input = generateFileInput();
    try (MultiPartFileInput mimeInput = new MultiPartFileInput(input)) {
      MimeBodyPart part = mimeInput.getBodyPart("payload2");
      assertEquals(PAYLOAD_2, toString(part));
      assertNull(mimeInput.getBodyPart("hello"));
    }

  }

  @Test
  public void testGetByPosition() throws Exception {
    File input = generateFileInput();
    try (MultiPartFileInput mimeInput = new MultiPartFileInput(input)) {
      MimeBodyPart part = mimeInput.getBodyPart(1);
      assertEquals(PAYLOAD_2, toString(part));
      assertNull(mimeInput.getBodyPart(6));
    }
  }

  @Test
  public void testRemove() throws Exception {
    Assertions.assertThrows(UnsupportedOperationException.class, () -> {
      File input = generateFileInput();
      try (MultiPartFileInput mimeInput = new MultiPartFileInput(input)) {
        mimeInput.next();
        mimeInput.remove();
      }
    });
  }
}
