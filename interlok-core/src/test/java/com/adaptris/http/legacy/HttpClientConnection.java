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

package com.adaptris.http.legacy;

import com.adaptris.http.HttpClientTransport;
import com.adaptris.http.HttpException;

/** Interface for handling client connections.
 * @author lchan
 * @author $Author: hfraser $
 */
public interface HttpClientConnection {

  /** Make a connection to the specified URL.
   * 
   * @param url the url.
   * @return an instance of HttpClientTransport
   * @throws HttpException if there was an error making the connection.
   */
  HttpClientTransport initialiseClient(String url) throws HttpException;
}
