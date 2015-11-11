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
 * Interface to generate http response headers.
 * 
 * 
 */
public interface ResponseHeaderProvider<T> {

  /**
   * Apply any additional headers required.
   * 
   * @param msg the {@link com.adaptris.core.AdaptrisMessage} to source the headers from
   * @param target the target object to configure
   * @return the modified target object
   */
  T addHeaders(AdaptrisMessage msg, T target);
}
