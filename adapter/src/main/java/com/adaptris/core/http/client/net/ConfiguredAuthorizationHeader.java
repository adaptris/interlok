/*
 * Copyright 2015 Adaptris Ltd.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.adaptris.core.http.client.net;

import java.net.HttpURLConnection;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.CoreException;
import com.adaptris.core.http.HttpConstants;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Build an {@link HttpConstants#AUTHORIZATION} header from static data.
 * 
 * @author gdries
 * @config http-configured-authorization-header
 */
@XStreamAlias("http-configured-authorization-header")
public class ConfiguredAuthorizationHeader implements HttpURLConnectionAuthenticator {

  private String headerValue;
  
  public ConfiguredAuthorizationHeader() {

  }

  public String getHeaderValue() {
    return headerValue;
  }

  /**
   * The value for the authorization header
   * @param headerValue
   */
  public void setHeaderValue(String headerValue) {
    this.headerValue = headerValue;
  }

  @Override
  public void setup(String target, AdaptrisMessage msg) throws CoreException {
  }

  @Override
  public void configureConnection(HttpURLConnection conn) {
    conn.addRequestProperty(HttpConstants.AUTHORIZATION, headerValue);
  }

  @Override
  public void close() {
  }

}
