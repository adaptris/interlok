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
 * Simply convert to and from base58.
 * 
 * @config base58-byte-translator
 * 
 */
@XStreamAlias("base58-byte-translator")
public class Base58ByteTranslator extends ByteTranslator {

  public Base58ByteTranslator() {
    super();
  }
  /**
   *
   * @see ByteTranslator#translate(java.lang.String)
   */
  @Override
  public byte[] translate(String s) {
    return Base58.decode(s);
  }

  /**
   *
   * @see ByteTranslator#translate(byte[])
   */
  @Override
  public String translate(byte[] bytes) {
    return Base58.encode(bytes);
  }
}
