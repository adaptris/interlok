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

import javax.validation.constraints.NotNull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.adaptris.annotation.AutoPopulated;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.http.client.RequestHeaderProvider;
import com.adaptris.core.util.Args;
import com.adaptris.util.KeyValuePair;
import com.adaptris.util.KeyValuePairSet;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Implementation of {@link RequestHeaderProvider} that applies configured values as headers to a {@link
 * HttpURLConnection}.
 * 
 * @config http-configured-request-headers
 * 
 */
@XStreamAlias("http-configured-request-headers")
public class ConfiguredRequestHeaders implements RequestHeaderProvider<HttpURLConnection> {
  protected transient Logger log = LoggerFactory.getLogger(this.getClass());
  @NotNull
  @AutoPopulated
  private KeyValuePairSet headers;

  public ConfiguredRequestHeaders() {
    headers = new KeyValuePairSet();
  }


  @Override
  public HttpURLConnection addHeaders(AdaptrisMessage msg, HttpURLConnection target) {
    for (KeyValuePair k : getHeaders()) {
      log.trace("Adding Request Property [{}: {}]", k.getKey(), k.getValue());
      target.addRequestProperty(k.getKey(), k.getValue());
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
