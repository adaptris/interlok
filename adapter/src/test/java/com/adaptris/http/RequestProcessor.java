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

import java.io.IOException;

/** The basic interface for handling an incoming request.
 */
public interface RequestProcessor {

  /** Process in the request in some fashion
   *  @param session the HttpSession.
   *  @throws IOException when a communications error occurs.
   *  @throws IllegalStateException if the state is not correct.
   *  @throws HttpException on other errors.
   */
  void processRequest(HttpSession session)
    throws IOException, IllegalStateException, HttpException;

  /** Get the uri that this request processor is associated with
   *  @return the uri
   */
  String getUri();
}
