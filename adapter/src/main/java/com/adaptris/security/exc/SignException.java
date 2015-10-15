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

package com.adaptris.security.exc;


/** Wraps any exception during signing.
 * @author lchan
 * @author $Author: lchan $
 */
public class SignException extends AdaptrisSecurityException {

  
  /**
   * <code>serialVersionUID</code>
   */
  private static final long serialVersionUID = 2006092701L;

  /** @see Exception#Exception()
   * 
   *
   */
  public SignException() {
    super();
  }

  /** @see Exception#Exception(String)
   * 
   *
   */
  public SignException(String s) {
    super(s);
  }

  /** @see Exception#Exception(String, Throwable)
   * 
   *
   */
  public SignException(String s, Throwable t) {
    super(s, t);
  }

  /** @see Exception#Exception(Throwable)
   * 
   *
   */
  public SignException(Throwable t) {
    super(t);
  }

}
