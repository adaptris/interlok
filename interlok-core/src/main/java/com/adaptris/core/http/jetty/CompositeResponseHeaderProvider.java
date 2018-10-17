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

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import com.adaptris.annotation.AutoPopulated;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.http.server.ResponseHeaderProvider;
import com.adaptris.core.util.Args;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * {@link ResponseHeaderProvider} implementation that uses a nested set of providers to private HTTP response headers.
 * 
 * @config jetty-composite-response-headers
 * 
 */
@XStreamAlias("jetty-composite-response-headers")
public class CompositeResponseHeaderProvider implements ResponseHeaderProvider<HttpServletResponse> {

  @AutoPopulated
  @NotNull
  @Valid
  private List<ResponseHeaderProvider<HttpServletResponse>> providers;


  public CompositeResponseHeaderProvider() {
    setProviders(new ArrayList<ResponseHeaderProvider<HttpServletResponse>>());
  }

  public CompositeResponseHeaderProvider(ResponseHeaderProvider<HttpServletResponse>... providers) {
    this();
    for (ResponseHeaderProvider<HttpServletResponse> p : providers) {
      addProvider(p);
    }
  }


  @Override
  public HttpServletResponse addHeaders(AdaptrisMessage msg, HttpServletResponse target) {
    HttpServletResponse response = target;
    for (ResponseHeaderProvider<HttpServletResponse> p : getProviders()) {
      response = p.addHeaders(msg, response);
    }
    return response;
  }


  public List<ResponseHeaderProvider<HttpServletResponse>> getProviders() {
    return providers;
  }


  public void setProviders(List<ResponseHeaderProvider<HttpServletResponse>> p) {
    this.providers = Args.notNull(p, "ResponseHeaderProviders");
  }

  public void addProvider(ResponseHeaderProvider<HttpServletResponse> p) {
    providers.add(Args.notNull(p, "ResponseHeaderProvider"));
  }

}
