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

import java.io.UnsupportedEncodingException;

import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Simply convert bytes into strings using the configured encoding.
 * 
 * @config charset-byte-translator
 * 
 * @author lchan
 * 
 */
@XStreamAlias("charset-byte-translator")
public class CharsetByteTranslator extends ByteTranslator {

  private String charsetEncoding;

  /**
   * Default charset encoding is UTF-8.
   *
   */
  public CharsetByteTranslator() {
    this("UTF-8");
  }

  public CharsetByteTranslator(String charset) {
    super();
    setCharsetEncoding(charset);
  }
  
  /**
   *
   * @see ByteTranslator#translate(java.lang.String)
   */
  @Override
  public byte[] translate(String s) throws UnsupportedEncodingException {
    return s.getBytes(charsetEncoding);
  }

  /**
   *
   * @see ByteTranslator#translate(byte[])
   */
  @Override
  public String translate(byte[] bytes) throws UnsupportedEncodingException {
    return new String(bytes, charsetEncoding);
  }

  /**
   * @return the charsetEncoding
   */
  public String getCharsetEncoding() {
    return charsetEncoding;
  }

  /**
   * @param c the charsetEncoding to set
   */
  public void setCharsetEncoding(String c) {
    this.charsetEncoding = c;
  }

}
