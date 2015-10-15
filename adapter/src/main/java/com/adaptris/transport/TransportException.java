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

package com.adaptris.transport;

/** The Transport Exception container.
 *  @see Exception
 */
public class TransportException extends Exception {

  /**
   * <code>serialVersionUID</code>
   */
  private static final long serialVersionUID = 2006100601L;

  /** @see Exception#Exception(Throwable)
   * 
   */
  public TransportException(Throwable e) {
    super(e);
  }

  /** @see Exception#Exception(String)
   * 
   */
  public TransportException(String e) {
    super(e);
  }

  /** @see Exception#Exception(String, Throwable)
   * 
   */
  public TransportException(String e, Throwable t) {
    super(e, t);
  }

}
