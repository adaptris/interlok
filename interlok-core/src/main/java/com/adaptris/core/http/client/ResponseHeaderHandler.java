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

package com.adaptris.core.http.client;

import com.adaptris.core.AdaptrisMessage;

/**
 * Interface to handle the headers from the HTTP response.
 * 
 * 
 */
public interface ResponseHeaderHandler<T> {

  /**
   * Do something with the response headers
   * 
   * @param src the object containing the headers
   * @param msg the AdaptrisMessage.
   * @return the modified message.
   */
  AdaptrisMessage handle(T src, AdaptrisMessage msg);

}
