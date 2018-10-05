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

import javax.validation.constraints.NotNull;

import com.adaptris.annotation.AutoPopulated;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.util.Args;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Static implementation of {@link RequestMethodProvider}.
 *
 * @config http-configured-request-method
 * @author lchan
 *
 */
@XStreamAlias("http-configured-request-method")
public class ConfiguredRequestMethodProvider implements RequestMethodProvider {

  @NotNull
  @AutoPopulated
  private RequestMethod method;

  public ConfiguredRequestMethodProvider() {
    setMethod(RequestMethod.POST);
  }

  public ConfiguredRequestMethodProvider(RequestMethod p) {
    this();
    setMethod(p);
  }

  @Override
  public RequestMethod getMethod(AdaptrisMessage msg) {
    return getMethod();
  }

  public RequestMethod getMethod() {
    return method;
  }

  public void setMethod(RequestMethod method) {
    this.method = Args.notNull(method, "Method");
  }

}
