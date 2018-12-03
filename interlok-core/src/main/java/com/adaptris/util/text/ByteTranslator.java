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

/**
 * Abstract Base class for converting strings into bytes and vice versa.
 *
 
 * @author lchan
 *
 */
public abstract class ByteTranslator {
  
  public ByteTranslator() {
  }
  
  /**
   * Translate a string into a byte array.
   *
   * @param s the string.
   * @return the byte array
   * @throws IOException wrapping any underlying exception
   */
  public abstract byte[] translate(String s) throws IOException;

  /**
   * Translate a byte array into a String.
   *
   * @param bytes the byte array.
   * @return the string
   * @throws IOException wrapping any underlying exception
   */
  public abstract String translate(byte[] bytes) throws IOException;
}
