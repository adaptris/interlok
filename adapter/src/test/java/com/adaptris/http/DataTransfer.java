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

package com.adaptris.http;

import java.io.InputStream;
import java.io.OutputStream;

/** A Generic interface for http read and writes.
 */
public interface DataTransfer {
  /** Write to the supplied outputstream.
   *  @param out the outputstream
   *  @throws HttpException on exception
   */
  void writeTo(OutputStream out) throws HttpException;
 
  /** Load from the supplied inputstream.
   *  @param in the input stream
   *  @throws HttpException on exception
   */
  void load(InputStream in) throws HttpException;
}
