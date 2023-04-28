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

package com.adaptris.core.services.jdbc;

import org.junit.jupiter.api.Test;

public class PayloadParameterTest extends com.adaptris.interlok.junit.scaffolding.BaseCase {


  @Test
  public void testBinaryStream() throws Exception {
    BinaryStreamStatementParameter sp = new BinaryStreamStatementParameter(null);
    BinaryStreamStatementParameter copy = sp.makeCopy();
    assertRoundtripEquality(sp, copy);
  }

  @Test
  public void testBytePayload() throws Exception {
    BytePayloadStatementParameter sp = new BytePayloadStatementParameter(null);
    BytePayloadStatementParameter copy = sp.makeCopy();
    assertRoundtripEquality(sp, copy);
  }

  @Test
  public void testCharacterStream() throws Exception {
    CharacterStreamStatementParameter sp = new CharacterStreamStatementParameter(null);
    CharacterStreamStatementParameter copy = sp.makeCopy();
    assertRoundtripEquality(sp, copy);
  }
}
