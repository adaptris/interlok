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

package com.adaptris.mail;

/** This is the container class for any exceptions that occur
 * 
 * @author lchan
 * @author $Author: lchan $
 */
public class MailException extends Exception {
  
  /**
   * <code>serialVersionUID</code>
   */
  private static final long serialVersionUID = 2006072101L;

  /** @see Exception#Exception() */
  public MailException() {
    super();
  }

  /** @see Exception#Exception(String) */
  public MailException(String s) {
    super(s);
  }

  /** @see Exception#Exception(Throwable) */
  public MailException(Throwable t) {
    super(t);
  }

  /** @see Exception#Exception(String, Throwable) */
  public MailException(String s, Throwable t) {
    super(s, t);
  }
}
