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

import java.io.IOException;

import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Simply converts to and from a Hex String
 * 
 * @config hex-string-byte-translator
 * @author stuellidge
 */
@XStreamAlias("hex-string-byte-translator")
public class HexStringByteTranslator extends ByteTranslator {

  @Override
  public byte[] translate(String s) throws IOException {
    return Conversion.hexStringToByteArray(s);
  }

  @Override
  public String translate(byte[] bytes) throws IOException {
    return Conversion.byteArrayToHexString(bytes);
  }

}
