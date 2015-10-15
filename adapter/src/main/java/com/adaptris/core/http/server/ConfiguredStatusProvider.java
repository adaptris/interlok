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

import javax.validation.constraints.NotNull;

import com.adaptris.annotation.AdvancedConfig;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.util.Args;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Static implementation of {@link HttpStatusProvider} that uses {@link HttpStatusProvider.HttpStatus} to derive the correct code.
 * 
 * <p>If you are configuring a static HTTP status, then you could use {@link RawStatusProvider} and the actual numeric
 * code if that makes more sense to you. This class is provided so that a typesafe enum can be used to derive the correct status
 * code.</p>
 * 
 *
 * @config http-configured-status
 * @author lchan
 *
 */
@XStreamAlias("http-configured-status")
public class ConfiguredStatusProvider implements HttpStatusProvider {

  @NotNull
  private HttpStatus status;
  @AdvancedConfig
  private String text;

  public ConfiguredStatusProvider() {
    setStatus(HttpStatus.INTERNAL_ERROR_500);
  }

  public ConfiguredStatusProvider(HttpStatus status) {
    this();
    setStatus(status);
  }

  @Override
  public Status getStatus(AdaptrisMessage msg) {
    return new HttpStatusBuilder().withCode(getStatus().getStatusCode()).withText(getText()).build();
  }


  public HttpStatus getStatus() {
    return status;
  }


  /**
   * Set the HTTP Status to use.
   * 
   * @param status the status, which defaults to {@link HttpStatusProvider.HttpStatus#INTERNAL_ERROR_500}
   */
  public void setStatus(HttpStatus status) {
    this.status = Args.notNull(status, "Status Code");
  }


  public String getText() {
    return text;
  }


  /**
   * Set the optional response text that will be sent with the response code.
   * 
   * <p>Note that for {@link com.adaptris.core.http.jetty.ResponseProducer} any values configured here will be ignored as
   * that will use {@link javax.servlet.http.HttpServletResponse#setStatus(int)} method only. This field is included only for
   * completeness, a sensible default will be made available based on the {@link HttpStatusProvider.HttpStatus} selected.
   * </p>
   * 
   * @param txt the text to be sent (e.g. {@code OK}, if the status code is {@link
   * com.adaptris.core.http.server.HttpStatusProvider.HttpStatus#OK_200}).
   */
  public void setText(String txt) {
    this.text = txt;
  }

}
