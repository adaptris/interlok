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
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.adaptris.annotation.AutoPopulated;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.http.server.ResponseHeaderProvider;
import com.adaptris.core.util.Args;
import com.adaptris.util.KeyValuePair;
import com.adaptris.util.KeyValuePairSet;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * {@link ResponseHeaderProvider} implementation that provides a static configured set of headers.
 * 
 * @config jetty-configured-response-headers
 * 
 */
@XStreamAlias("jetty-configured-response-headers")
public class ConfiguredResponseHeaderProvider implements ResponseHeaderProvider<HttpServletResponse> {
  protected transient Logger log = LoggerFactory.getLogger(this.getClass());

  @NotNull
  @Valid
  @AutoPopulated
  private KeyValuePairSet headers;

  public ConfiguredResponseHeaderProvider() {
    setHeaders(new KeyValuePairSet());
  }

  public ConfiguredResponseHeaderProvider(KeyValuePair... pairs) {
    this();
    for (KeyValuePair p : pairs) {
      getHeaders().add(p);
    }
  }

  @Override
  public HttpServletResponse addHeaders(AdaptrisMessage msg, HttpServletResponse target) {
    for (KeyValuePair k : getHeaders()) {
      log.trace("Adding Response Header [{}: {}]", k.getKey(), k.getValue());
      target.addHeader(k.getKey(), k.getValue());
    }
    return target;
  }

  public KeyValuePairSet getHeaders() {
    return headers;
  }

  public void setHeaders(KeyValuePairSet headers) {
    this.headers = Args.notNull(headers, "headers");
  }
}
