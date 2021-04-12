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
 * Interface for providing a HTTP method.
 * 
 */
public interface RequestMethodProvider {

  /**
   * Valid methods as defined by RFC2616 &amp; RFC5789 (PATCH method).
   * <p>Note that this is simply a list of methods, and there may be limited/no support for those methods within configured
   * components that make use of the {@link RequestMethodProvider} interface.</p>
   */
  public static enum RequestMethod {
    CONNECT,
    DELETE,
    GET,
    HEAD,
    OPTIONS,
    PATCH,
    PUT,
    POST,
    TRACE
  }

  /**
   * Get the method that should be used with the HTTP request.
   * 
   * @param msg the {@link com.adaptris.core.AdaptrisMessage} if required to derive the method.
   * @return the method.
   */
  RequestMethod getMethod(AdaptrisMessage msg);
}
