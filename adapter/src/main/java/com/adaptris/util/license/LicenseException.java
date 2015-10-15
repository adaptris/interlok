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

package com.adaptris.util.license;


/** Container for exceptions thrown by a license object.
 */
public class LicenseException extends java.lang.Exception {
  /**
   * 
   */
  private static final long serialVersionUID = 1L;

  /** @see Exception#Exception() 
   */
  public LicenseException() {
    super();
  }

  /** @see Exception#Exception(String) 
   */
  public LicenseException(String msg) {
    super(msg);
  }

  /** @see Exception#Exception(String, Throwable) 
   */
  public LicenseException(String msg, Throwable t) {
    super(msg, t);
  }

  /** @see Exception#Exception(Throwable) 
   */
  public LicenseException(Throwable t) {
    super(t);
  }
}
