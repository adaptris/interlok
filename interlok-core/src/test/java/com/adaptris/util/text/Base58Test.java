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

package com.adaptris.util.text;

import static org.junit.Assert.assertEquals;
import java.nio.charset.StandardCharsets;
import org.junit.Test;
import com.adaptris.util.GuidGenerator;

public class Base58Test extends Base58 {

  private static final byte[] BYTES_WITH_NULLS =
      {(byte) 0x00, (byte) 0x00, (byte) 0x21, (byte) 0xcd, (byte) 0x00, (byte) 0x00, (byte) 0x00};
  
  @Test
  public void testTranslate() throws Exception {
    Base58ByteTranslator b = new Base58ByteTranslator();
    String b58 = Base58.encode(new GuidGenerator().getUUID().getBytes(StandardCharsets.UTF_8));
    assertEquals(b58, b.translate(b.translate(b58)));
  }

  @Test
  public void testTranslate_EmptyString() throws Exception {
    Base58ByteTranslator b = new Base58ByteTranslator();
    assertEquals("", b.translate(b.translate("")));
  }

  @Test
  public void testTranslate_leading_nulls() throws Exception {
    Base58ByteTranslator b = new Base58ByteTranslator();
    String b58 = Base58.encode(BYTES_WITH_NULLS);
    assertEquals(b58, b.translate(b.translate(b58)));
  }

  @Test(expected = IllegalArgumentException.class)
  public void testTranslate_Invalid() throws Exception {
    Base58ByteTranslator b = new Base58ByteTranslator();
    b.translate("blahblah");
  }
}
