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
import org.junit.Test;
import com.adaptris.interlok.junit.scaffolding.BaseCase;
public class EmptyFilenameCreatorTest {

  @Test
  public void testCreateName() {
    EmptyFileNameCreator creator = new EmptyFileNameCreator();
    assertEquals("", creator.createName(new DefaultMessageFactory().newMessage("")));
    assertEquals("", creator.createName(null));
  }

  @Test
  public void testXmlRoundTrip() throws Exception {
    EmptyFileNameCreator input = new EmptyFileNameCreator();
    AdaptrisMarshaller m = DefaultMarshaller.getDefaultMarshaller();
    String xml = m.marshal(input);
    EmptyFileNameCreator output = (EmptyFileNameCreator) m.unmarshal(xml);
    BaseCase.assertRoundtripEquality(input, output);
  }
}
