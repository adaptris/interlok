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

package com.adaptris.core.http.jetty;

import javax.servlet.http.HttpServletResponse;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.http.server.ResponseHeaderProvider;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * {@link ResponseHeaderProvider} implementation that does not add any HTTP response headers.
 * 
 * @config jetty-no-response-headers
 * 
 */
@XStreamAlias("jetty-no-response-headers")
public class NoOpResponseHeaderProvider implements ResponseHeaderProvider<HttpServletResponse> {

  @Override
  public HttpServletResponse addHeaders(AdaptrisMessage msg, HttpServletResponse target) {
    return target;
  }

}
