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

import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Simply convert to and from base64.
 * 
 * @config base64-byte-translator
 * 
 * @author lchan
 * 
 */
@XStreamAlias("base64-byte-translator")
public class Base64ByteTranslator extends ByteTranslator {

  public Base64ByteTranslator() {
    super();
  }
  /**
   *
   * @see ByteTranslator#translate(java.lang.String)
   */
  @Override
  public byte[] translate(String s) {
    return Conversion.base64StringToByteArray(s);
  }

  /**
   *
   * @see ByteTranslator#translate(byte[])
   */
  @Override
  public String translate(byte[] bytes) {
    return Conversion.byteArrayToBase64String(bytes);
  }
}
