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

package com.adaptris.security;

import java.io.UnsupportedEncodingException;

import com.adaptris.security.exc.AdaptrisSecurityException;

/**
 * Implement of <code>Output</code> that simply wraps a byte array
 * 
 * @author lchan
 * @author $Author: lchan $
 */
public final class SimpleOutput implements Output {

  private int type;
  private byte[] output;

  private SimpleOutput() {
  }

  public SimpleOutput(int i, byte[] b) {
    this();
    type = i;
    output = b;
  }

  /**
   * 
   * @see com.adaptris.security.Output#getBytes()
   */
  public byte[] getBytes() throws AdaptrisSecurityException {
    return output;
  }

  /**
   * 
   * @see com.adaptris.security.Output#getType()
   */
  public int getType() {
    return type;
  }

  /**
   * @see Output#isEncrypted()
   */
  public boolean isEncrypted() {
    return ((this.getType() & Output.ENCRYPTED) > 0);
  }

  /**
   * @see Output#isPlain()
   */
  public boolean isPlain() {
    return ((this.getType() & Output.PLAIN) > 0);
  }

  /**
   * @see Output#isSigned()
   */
  public boolean isSigned() {
    return ((this.getType() & Output.SIGNED) > 0);
  }

  /**
   * @see Output#getAsString()
   */
  public String getAsString() throws AdaptrisSecurityException {
    return new String(this.getBytes());
  }

  /**
   * 
   * @see com.adaptris.security.Output#getAsString(java.lang.String)
   */
  public String getAsString(String charset)
      throws AdaptrisSecurityException, UnsupportedEncodingException {
    return new String(this.getBytes(), charset);
  }

}
