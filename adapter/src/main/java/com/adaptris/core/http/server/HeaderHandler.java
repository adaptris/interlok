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

package com.adaptris.core.http.server;

import com.adaptris.core.AdaptrisMessage;

/**
 * Interface for handling behaviour for HTTP headers.
 * 
 * 
 */
public interface HeaderHandler<T> {
  
  /**
   * Handle the headers from the request.
   * 
   * @param message the target {@link AdaptrisMessage}
   * @param request the request
   * @param itemPrefix Any prefix that needs to be applied
   * @deprecated since 3.0.6, concrete implementations of this interface can decide what to do with prefixes.
   */
  @Deprecated
  public void handleHeaders(AdaptrisMessage message, T request, String itemPrefix);

  /**
   * Handle the headers from the request..
   * 
   * @param message the target {@link AdaptrisMessage}
   * @param request the request
   */
  void handleHeaders(AdaptrisMessage message, T request);

}
