/*
 * Copyright 2018 Adaptris Ltd.
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

import static com.adaptris.util.text.mime.PartIteratorCase.createMultipart;
import static com.adaptris.util.text.mime.PartIteratorCase.generateByteArrayInput;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.List;

import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMultipart;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class NullPartSelectorTest {

  @BeforeEach
  public void setUp() throws Exception {
  }

  @AfterEach
  public void tearDown() throws Exception {
  }

  @Test
  public void testSelectMimeMultipart() throws Exception {
    MimeMultipart mmp = createMultipart();
    NullPartSelector selector = new NullPartSelector();
    List<MimeBodyPart> parts = selector.select(mmp);
    assertEquals(3, parts.size());
  }

  @Test
  public void testSelectBodyPartIterator() throws Exception {
    try (BodyPartIterator input = new BodyPartIterator(generateByteArrayInput(false))) {
      NullPartSelector selector = new NullPartSelector();
      MimeBodyPart part = selector.select(input);
      assertNotNull(part);
    }
  }
}
