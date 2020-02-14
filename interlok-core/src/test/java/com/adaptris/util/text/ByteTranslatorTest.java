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
import static org.junit.Assert.assertTrue;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import org.junit.Test;
import com.adaptris.util.GuidGenerator;

@SuppressWarnings("deprecation")
public class ByteTranslatorTest {

  @Test
  public void testBase64Translator() throws Exception {
    ByteTranslator b = new Base64ByteTranslator();
    String uniq = new GuidGenerator().getUUID();
    String base64String = new String(Conversion.byteArrayToBase64String(uniq
        .getBytes()));
    byte[] bytes = b.translate(base64String);
    String result = b.translate(bytes);
    assertEquals(base64String, result);
  }

  @Test
  public void testCharsetByteTranslatorUsingIso8859() throws Exception {
    CharsetByteTranslator b = new CharsetByteTranslator("ISO-8859-1");
    String uniq = new GuidGenerator().getUUID();
    byte[] bytes = b.translate(uniq);
    String result = b.translate(bytes);
    assertEquals(uniq, result);
    assertEquals("ISO-8859-1", b.getCharsetEncoding());
  }

  @Test
  public void testCharsetByteTranslator() throws Exception {
    CharsetByteTranslator b = new CharsetByteTranslator();
    String uniq = new GuidGenerator().getUUID();
    byte[] bytes = b.translate(uniq);
    String result = b.translate(bytes);
    assertEquals("UTF-8", b.getCharsetEncoding());
    assertEquals(uniq, result);
  }

  @Test
  public void testSimpleByteTranslator() throws Exception {
    ByteTranslator b = new SimpleByteTranslator();
    String uniq = new GuidGenerator().getUUID();
    byte[] bytes = b.translate(uniq);
    String result = b.translate(bytes);
    assertEquals(uniq, result);
  }

  @Test
  public void testHexstringByteTranslator() throws Exception {
    ByteTranslator b = new HexStringByteTranslator();
    byte[] uniq = new GuidGenerator().getUUID().getBytes(StandardCharsets.UTF_8);
    assertTrue(MessageDigest.isEqual(uniq, b.translate(b.translate(uniq))));
  }
}
