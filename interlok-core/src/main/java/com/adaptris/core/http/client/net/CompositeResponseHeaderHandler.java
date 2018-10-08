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

package com.adaptris.core.http.client.net;

import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.List;

import javax.validation.constraints.NotNull;

import com.adaptris.annotation.AutoPopulated;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.http.client.ResponseHeaderHandler;
import com.adaptris.core.util.Args;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamImplicit;

/**
 * Implementation of {@link ResponseHeaderHandler} that uses nested handlers to extract headers from a {@link
 * HttpURLConnection}.
 * 
 * <p>This implementation is primarily so that you can mix and matchhow you capture response headers; If you wanted to use both
 * {@link ResponseHeadersAsMetadata} and {@link ResponseHeadersAsObjectMetadata} then you can.
 * </p>
 * @config http-composite-request-headers
 * 
 */
@XStreamAlias("http-composite-response-header-handler")
public class CompositeResponseHeaderHandler implements ResponseHeaderHandler<HttpURLConnection> {
  @XStreamImplicit
  @NotNull
  @AutoPopulated
  private List<ResponseHeaderHandler<HttpURLConnection>> handlers;

  public CompositeResponseHeaderHandler() {
    setHandlers(new ArrayList<ResponseHeaderHandler<HttpURLConnection>>());
  }

  public CompositeResponseHeaderHandler(ResponseHeaderHandler<HttpURLConnection>... handlers) {
    this();
    for (ResponseHeaderHandler<HttpURLConnection> h : handlers) {
      addHandler(h);
    }
  }



  public List<ResponseHeaderHandler<HttpURLConnection>> getHandlers() {
    return handlers;
  }

  public void setHandlers(List<ResponseHeaderHandler<HttpURLConnection>> handlers) {
    this.handlers = Args.notNull(handlers, "Response Header Handlers");
  }

  public void addHandler(ResponseHeaderHandler<HttpURLConnection> handler) {
    getHandlers().add(Args.notNull(handler, "Response Handler"));
  }


  @Override
  public AdaptrisMessage handle(HttpURLConnection src, AdaptrisMessage msg) {
    AdaptrisMessage target = msg;
    for (ResponseHeaderHandler<HttpURLConnection> h : getHandlers()) {
      target = h.handle(src, target);
    }
    return target;
  }
}
