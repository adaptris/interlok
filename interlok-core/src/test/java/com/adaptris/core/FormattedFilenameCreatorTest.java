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

package com.adaptris.core;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import java.util.Date;
import org.junit.Test;
import com.adaptris.interlok.junit.scaffolding.BaseCase;

public class FormattedFilenameCreatorTest {

  @Test
  public void testSetFormat() {
    FormattedFilenameCreator creator = new FormattedFilenameCreator();
    try {
      creator.setFilenameFormat(null);
      fail("null allowed");
    }
    catch (IllegalArgumentException e) {

    }
    assertEquals(creator.getFilenameFormat(), "%1$s");
    try {
      creator.setFilenameFormat("");
      fail("'' allowed");
    }
    catch (IllegalArgumentException e) {

    }
    assertEquals(creator.getFilenameFormat(), "%1$s");
    creator.setFilenameFormat("message");
    assertEquals("message", creator.getFilenameFormat());
  }

  @Test
  public void testPlain() throws Exception {
    FormattedFilenameCreator creator = new FormattedFilenameCreator();
    AdaptrisMessage msg = new DefaultMessageFactory().newMessage("");
    String expectedName = msg.getUniqueId();
    String fileName = creator.createName(msg);
    assertEquals(fileName, expectedName);
  }

  @Test
  public void testWithTimestamp() throws Exception {
    FormattedFilenameCreator creator = new FormattedFilenameCreator();
    creator.setFilenameFormat("%1$s-%2$tF");
    AdaptrisMessage msg = new DefaultMessageFactory().newMessage("");
    String expectedName = String.format("%1$s-%2$tF", msg.getUniqueId(), new Date());
    String fname = creator.createName(msg);
    assertEquals(expectedName, fname);
  }

  @Test
  public void testWithConstants() throws Exception {
    FormattedFilenameCreator creator = new FormattedFilenameCreator();
    creator.setFilenameFormat("message-%1$s-%2$tF");
    AdaptrisMessage msg = new DefaultMessageFactory().newMessage("");
    String expectedName = String.format("message-%1$s-%2$tF", msg.getUniqueId(), new Date());
    String fname = creator.createName(msg);
    assertEquals(expectedName, fname);
  }

  @Test
  public void testWithoutUniqueid() throws Exception {
    FormattedFilenameCreator creator = new FormattedFilenameCreator();
    creator.setFilenameFormat("message-%2$tF");
    AdaptrisMessage msg = new DefaultMessageFactory().newMessage("");
    String expectedName = String.format("message-%2$tF", msg.getUniqueId(), new Date());
    String fname = creator.createName(msg);
    assertEquals(expectedName, fname);
  }

  @Test
  public void testReversedOrder() throws Exception {
    FormattedFilenameCreator creator = new FormattedFilenameCreator();
    creator.setFilenameFormat("%2$tF-%1$s");
    AdaptrisMessage msg = new DefaultMessageFactory().newMessage("");
    String expectedName = String.format("%2$tF-%1$s", msg.getUniqueId(), new Date());
    String fname = creator.createName(msg);
    assertEquals(expectedName, fname);
  }

  @Test
  public void testXmlRoundTrip() throws Exception {
    FormattedFilenameCreator input = new FormattedFilenameCreator();
    AdaptrisMarshaller m = DefaultMarshaller.getDefaultMarshaller();
    String xml = m.marshal(input);
    FormattedFilenameCreator output = (FormattedFilenameCreator) m.unmarshal(xml);
    BaseCase.assertRoundtripEquality(input, output);
  }

}
