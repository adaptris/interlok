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
import static org.junit.Assert.assertNotNull;

import org.junit.Test;

public class HexDumpTest {

  private static final byte[] BYTES =
  {
      (byte) 0xc7, (byte) 0x73, (byte) 0x21, (byte) 0x8c, (byte) 0x7e, (byte) 0xc8, (byte) 0xee, (byte) 0x99, (byte) 0xc7,
      (byte) 0x73, (byte) 0x21, (byte) 0x8c, (byte) 0x1a, (byte) 0xc8, (byte) 0xee, (byte) 0x99, (byte) 0xc7, (byte) 0x73,
      (byte) 0x21, (byte) 0x8c, (byte) 0x05
  };

  @Test
  public void testHexDump() {
    assertNotNull(HexDump.parse("Hello There".getBytes()));
    assertNotNull(HexDump.parse(BYTES));
    assertEquals("Request offset > byte array length", HexDump.parse(BYTES, 99, BYTES.length));
    assertEquals("Requested Length > byte array length", HexDump.parse(BYTES, 3, BYTES.length));
  }
}
