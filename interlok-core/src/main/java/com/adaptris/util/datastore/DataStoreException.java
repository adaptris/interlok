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

package com.adaptris.util.datastore;


/** DataStoreException.
 *  <p>Container class for all exceptions related to the datastore
 * </p>
 
 * @author $Author: lchan $
 */
public class DataStoreException extends Exception {
  /**
   * 
   */
  private static final long serialVersionUID = 1L;

  /** @see Exception#Exception(String)
   */
  public DataStoreException(String msg) {
    super(msg);
  }

  /** @see Exception#Exception(String, Throwable)
   */
  public DataStoreException(String s, Throwable t) {
    super(s, t);
  }

  /** @see Exception#Exception(Throwable)
   */
  public DataStoreException(Throwable t) {
    super(t);
  }
}
