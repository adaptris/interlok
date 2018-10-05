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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.List;

import javax.mail.MessagingException;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMultipart;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class SelectByHeaderTest {

  @Before
  public void setUp() throws Exception {
  }

  @After
  public void tearDown() throws Exception {
  }

  @Test
  @SuppressWarnings("deprecation")
  public void testSelectMultiPartInput() throws Exception {
    MultiPartInput input = new MultiPartInput(generateByteArrayInput(false), false);
    SelectByHeader selector = new SelectByHeader("Content-Id", "payload1");
    MimeBodyPart part = selector.select(input);
    assertNull(part);
  }

  @Test(expected = MessagingException.class)
  public void testSelectMimeMultipart() throws Exception {
    MimeMultipart mmp = createMultipart();
    SelectByHeader selector = new SelectByHeader("Content-Id", "payload1");
    List<MimeBodyPart> parts = selector.select(mmp);
  }

  @Test(expected = MessagingException.class)
  public void testSelectNoHeader() throws Exception {
    try (BodyPartIterator input = new BodyPartIterator(generateByteArrayInput(false))) {
      SelectByHeader selector = new SelectByHeader();
      selector.select(input);
    }
  }

  @Test
  public void testSelectBodyPartIterator() throws Exception {
    try (BodyPartIterator input = new BodyPartIterator(generateByteArrayInput(false))) {
      SelectByHeader selector = new SelectByHeader("Content-Id", "payload1");
      MimeBodyPart part = selector.select(input);
      assertNotNull(part);
      selector = new SelectByHeader("Content-Id", "payload99");
      assertNull(selector.select(input));
      selector = new SelectByHeader("Content-Blah", "payload99");
      assertNull(selector.select(input));
    }
  }
}
