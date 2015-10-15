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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * {@link ParameterHandler} implementation that ignores HTTP parameters.
 * 
 * @config http-ignore-parameters
 * @deprecated since 3.0.6 use {@link com.adaptris.core.http.jetty.NoOpParameterHandler} instead.
 */
@XStreamAlias("http-ignore-parameters")
@Deprecated
public class NoOpParameterHandler extends com.adaptris.core.http.jetty.NoOpParameterHandler {


  private static transient boolean warningLogged;
  private transient Logger log = LoggerFactory.getLogger(this.getClass());

  public NoOpParameterHandler() {
    super();
    if (!warningLogged) {
      log.warn("[{}] is deprecated, use [{}] instead", this.getClass().getSimpleName(),
          com.adaptris.core.http.jetty.NoOpParameterHandler.class.getName());
      warningLogged = true;
    }
  }
}
