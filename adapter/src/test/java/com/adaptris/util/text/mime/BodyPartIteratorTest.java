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

import javax.mail.internet.MimeBodyPart;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class BodyPartIteratorTest extends PartIteratorCase {


  @Before
  public void setUp() throws Exception {
  }

  @After
  public void tearDown() {
  }

  @Test
  public void testIterator() throws Exception {
    try (BodyPartIterator mimeInput = new BodyPartIterator(generateByteArrayInput(false))) {
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
    try (BodyPartIterator mimeInput = new BodyPartIterator(generateByteArrayInput(true))) {
      MimeBodyPart part = mimeInput.getBodyPart("payload2");
      assertEquals(PAYLOAD_2, toString(part));
      assertNull(mimeInput.getBodyPart("hello"));
    }

  }

  @Test
  public void testGetByPosition() throws Exception {
    try (BodyPartIterator mimeInput = new BodyPartIterator(generateByteArrayInput(false))) {
      MimeBodyPart part = mimeInput.getBodyPart(1);
      assertEquals(PAYLOAD_2, toString(part));
      assertNull(mimeInput.getBodyPart(6));
    }
  }

  @Test(expected = UnsupportedOperationException.class)
  public void testRemove() throws Exception {
    try (BodyPartIterator mimeInput = new BodyPartIterator(generateByteArrayInput(false))) {
      mimeInput.next();
      mimeInput.remove();
    }
  }
}
